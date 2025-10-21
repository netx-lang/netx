//
// Created by czm233 on 10/14/25.
//

#ifndef NXSIM_PARTITIONER_H
#define NXSIM_PARTITIONER_H
#include <unordered_map>
#include <functional>
#include <vector>
#include <boost/graph/adjacency_list.hpp>

#include "kaHIP_interface.h"
#include "../circuit.h"
#include "../circuit_def.h"

namespace nxon {
    namespace impl {
        using extracted_vertex_t = boost::graph_traits<boost::adjacency_list<> >::vertex_descriptor;
        using extracted_graph_t = boost::adjacency_list<boost::vecS, boost::vecS, boost::directedS, id_t,
            boost::no_property, std::unordered_map<id_t, extracted_vertex_t> >;

        inline extracted_graph_t extract_graph(const circuit &circuit) {
            extracted_graph_t graph;
            auto &to_new = graph[boost::graph_bundle_t{}];
            const auto out_rules = circuit.all_rules();
            for (const auto &rule: out_rules)
                for (const auto &old_id: rule.dependencies() + rule.outcomes()) {
                    if (to_new.contains(old_id)) continue;
                    const auto new_id = add_vertex(old_id, graph);
                    to_new[old_id] = new_id;
                }

            for (const auto &rule: out_rules)
                for (const auto &old_id: rule.dependencies()) {
                    const auto new_id = to_new[old_id];
                    for (const auto &old_out_id: rule.outcomes()) {
                        const auto new_out_id = to_new[old_out_id];
                        add_edge(new_id, new_out_id, graph);
                    }
                }

            return graph;
        }
    }

    struct partition_result {
        size_t num_parts = 0;
        std::unordered_map<id_t, id_t> belongs;
    };

    using partitioner_t = std::function<partition_result(const circuit &circuit)>;

    inline partition_result naive_partitioner(const circuit &circuit) {
        const auto graph = impl::extract_graph(circuit);

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
        res.num_parts = nparts;
        for (int id = 0; id < n; ++id) {
            auto old_id = graph[id];
            auto part_id = part[id];
            res.belongs[old_id] = part_id;
        }

        return res;
    }
}

#endif //NXSIM_PARTITIONER_H
