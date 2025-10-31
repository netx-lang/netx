//
// Created by czm233 on 10/14/25.
//

#ifndef NXSIM_PARTITIONER_H
#define NXSIM_PARTITIONER_H
#include <type_traits>
#include <unordered_map>
#include <vector>
#include <cmath>
#include <random>
#include <unordered_set>
#include <ranges>
#include <algorithm>

#include "kaHIP_interface.h"
#include "../circuit_def.h"
#include "../graph_helper.h"

namespace nxon {
    namespace impl {
        constexpr double SERIAL_THRESHOLD = 0.6;

        inline void semantic_partitioner_impl(std::vector<std::vector<id_t> > &parts,
                                              std::vector<id_t> &cur_part,
                                              const extracted_graph_t &graph,
                                              const extracted_graph_t &graph_T,
                                              const component_hierarchy &hierarchy,
                                              const size_t min_size) {
            cur_part.append_range(hierarchy.wires);
            if (hierarchy.children.empty()) return;

            const size_t child_cnt = hierarchy.children.size();
            std::vector<std::unordered_set<extracted_vertex_t> > vertices;
            vertices.reserve(child_cnt);
            for (const auto &child: hierarchy.children) {
                auto vert = hierarchy_vertices(graph, child);
                vertices.emplace_back(determination_closure(vert, graph, graph_T));
            }
            // Build directed pseudoforest by serial relationship and threshold
            std::vector<size_t> father;
            std::vector<size_t> in_deg;
            father.resize(child_cnt);
            in_deg.resize(child_cnt);
            for (size_t i = 0; i < child_cnt; ++i) {
                size_t fa = child_cnt;
                double fa_rel = 0;
                for (size_t j = 0; j < child_cnt; ++j) {
                    if (i == j) continue;
                    if (const double rel = serial_relation(graph, graph_T, vertices[i], vertices[j]);
                        fa == child_cnt || rel > fa_rel) {
                        fa = j;
                        fa_rel = rel;
                    }
                }

                if (fa == child_cnt || fa_rel < SERIAL_THRESHOLD)
                    father[i] = child_cnt;
                else {
                    ++in_deg[fa];
                    father[i] = fa;
                }
            }

            // Path decomposition
            std::vector<bool> visited;
            visited.resize(child_cnt, false);
            std::vector<std::vector<size_t> > paths;
            auto collect_path = [&](const size_t begin) {
                std::vector<size_t> path;
                size_t cur = begin;
                while (cur != child_cnt && !visited[cur]) {
                    visited[cur] = true;
                    path.emplace_back(cur);
                    cur = father[cur];
                }

                paths.emplace_back(std::move(path));
            };
            for (size_t i = 0; i < child_cnt; ++i) {
                if (visited[i]) continue;
                if (in_deg[i] > 0) continue;
                collect_path(i);
            }
            for (size_t i = 0; i < child_cnt; ++i) {
                if (visited[i]) continue;
                collect_path(i);
            }

            if (paths.size() <= 1)
                for (const auto &child: hierarchy.children)
                    semantic_partitioner_impl(parts, cur_part, graph, graph_T, child, min_size);
            else {
                for (const auto &path: paths) {
                    std::vector<id_t> new_part;
                    for (const auto i: path)
                        semantic_partitioner_impl(parts, new_part, graph, graph_T, hierarchy.children[i], min_size);
                    if (new_part.size() >= min_size)
                        parts.emplace_back(std::move(new_part));
                    else
                        cur_part.append_range(new_part);
                }
            }
        }
    }

    struct partition_result {
        size_t num_parts = 0;
        std::unordered_map<id_t, id_t> belongs;
    };

    template<typename T>
    concept partitioner_t = std::is_invocable_r_v<partition_result, T, const parse_context &>;

    inline partition_result kahip_partitioner(const parse_context &context) {
        const auto graph = impl::extract_graph(context.circuit);

        auto [edges_begin, edges_end] = boost::edges(graph);
        std::unordered_map<impl::extracted_vertex_t, std::vector<impl::extracted_vertex_t> > edges;
        for (const auto &edge: std::ranges::subrange(edges_begin, edges_end)) {
            edges[edge.m_source].emplace_back(edge.m_target);
            edges[edge.m_target].emplace_back(edge.m_source);
        }

        size_t adj_size = 0;
        for (auto &[id, out]: edges) {
            // Remove parallel edges and self-loops, if any
            std::ranges::sort(out);
            auto [l, r] = std::ranges::unique(out);
            out.erase(l, r);
            erase(out, id);

            adj_size += out.size();
        }

        int n = static_cast<int>(num_vertices(graph));
        const auto xadj = std::make_unique<int[]>(n + 1);
        const auto adjncy = std::make_unique<int[]>(adj_size);
        int current_ptr = 0;
        for (int id = 0; id < n; ++id) {
            xadj[id] = current_ptr;
            for (const auto &out = edges[id]; const auto &out_id: out)
                adjncy[current_ptr++] = static_cast<int>(out_id);
        }
        xadj[n] = current_ptr;
        int nparts = 2 + static_cast<int>(std::thread::hardware_concurrency());
        double imbalance = 10;
        const int seed = static_cast<int>(std::random_device{}());
        int edgecut;
        const auto part = std::make_unique<int[]>(n);
        kaffpa(&n, nullptr, xadj.get(), nullptr, adjncy.get(), &nparts, &imbalance, false, seed, STRONG, &edgecut,
               part.get());

        partition_result res;
        res.num_parts = nparts + 1;
        const auto clocks = context.circuit.all_clocks();
        for (int id = 0; id < n; ++id) {
            auto old_id = graph[id];
            auto part_id = part[id];
            if (clocks.contains(old_id)) part_id = nparts;
            res.belongs[old_id] = part_id;
        }

        return res;
    }

    template<double SizeThreshold = 0.05>
    partition_result semantic_partitioner(const parse_context &context) {
        const auto graph = impl::extract_graph(context.circuit);
        impl::extracted_graph_t graph_T;
        boost::transpose_graph(graph, graph_T);

        std::vector<id_t> root_part;
        std::vector<std::vector<id_t> > parts;
        const auto min_size = static_cast<size_t>(std::round(
            static_cast<double>(impl::hierarchy_size(context.root_hierarchy)) *
            SizeThreshold));
        impl::semantic_partitioner_impl(parts, root_part, graph, graph_T, context.root_hierarchy, min_size);
        parts.emplace_back(std::move(root_part));

        partition_result res;
        res.num_parts = parts.size() + 1;
        const auto clocks = context.circuit.all_clocks();
        for (size_t i = 0; i < parts.size(); ++i)
            for (const auto &id: parts[i])
                if (clocks.contains(id)) res.belongs[id] = parts.size();
                else res.belongs[id] = i;
        return res;
    }
}

#endif //NXSIM_PARTITIONER_H
