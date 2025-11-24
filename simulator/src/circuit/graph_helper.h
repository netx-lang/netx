//
// Created by czm233 on 10/31/25.
//

#ifndef NXSIM_GRAPH_HELPER_H
#define NXSIM_GRAPH_HELPER_H
#include <queue>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/transpose_graph.hpp>
#include "circuit.h"
#include "circuit_parser.h"

namespace nxon::impl {
    using extracted_vertex_t = boost::graph_traits<boost::adjacency_list<> >::vertex_descriptor;
    using extracted_graph_t = boost::adjacency_list<boost::vecS, boost::vecS, boost::directedS, id_t,
        boost::no_property, std::unordered_map<id_t, extracted_vertex_t> >;

    inline extracted_graph_t extract_graph(const circuit &circuit) {
        extracted_graph_t graph;
        auto &to_new = graph[boost::graph_bundle];
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

    inline size_t hierarchy_size(const component_hierarchy &hierarchy) {
        size_t res = hierarchy.wires.size();
        for (const auto &child: hierarchy.children)
            res += hierarchy_size(child);
        return res;
    }

    inline std::vector<extracted_vertex_t> hierarchy_vertices(const extracted_graph_t &graph,
                                                              const component_hierarchy &hierarchy) {
        const auto &to_new = graph[boost::graph_bundle];
        std::vector<extracted_vertex_t> vertices;
        for (const auto &wire: hierarchy.wires) {
            auto iter = to_new.find(wire);
            if (iter == to_new.end()) continue;
            vertices.emplace_back(iter->second);
        }
        for (const auto &child: hierarchy.children) {
#ifdef __cpp_lib_containers_ranges
            vertices.append_range(hierarchy_vertices(graph, child));
#else
            auto child_vertices = hierarchy_vertices(graph, child);
            vertices.insert(vertices.end(), child_vertices.begin(), child_vertices.end());
#endif
        }
        return vertices;
    }

    inline std::unordered_set<extracted_vertex_t> determination_closure(
        const std::vector<extracted_vertex_t> &vertices,
        const extracted_graph_t &graph,
        const extracted_graph_t &graph_T) {
        std::unordered_set<extracted_vertex_t> result;
#ifdef __cpp_lib_containers_ranges
        result.insert_range(vertices);
#else
        result.insert(vertices.begin(), vertices.end());
#endif
        std::queue<extracted_vertex_t> worklist;
        for (const auto &v: vertices) {
            auto [adj_begin, adj_end] = adjacent_vertices(v, graph);
#ifdef __cpp_lib_containers_ranges
            worklist.push_range(std::ranges::subrange(adj_begin, adj_end));
#else
            for (const auto &adj: std::ranges::subrange(adj_begin, adj_end)) {
                worklist.push(adj);
            }
#endif
        }

        while (!worklist.empty()) {
            const extracted_vertex_t v = worklist.front();
            worklist.pop();
            if (result.contains(v)) continue;
            if (auto [adj_begin, adj_end] = adjacent_vertices(v, graph_T);
                !std::all_of(adj_begin, adj_end, [&](const auto &adj) {
                    return result.contains(adj);
                }))
                continue;
            result.insert(v);
            auto [adj_begin, adj_end] = adjacent_vertices(v, graph);
#ifdef __cpp_lib_containers_ranges
            worklist.push_range(std::ranges::subrange(adj_begin, adj_end));
#else
            for (const auto &adj: std::ranges::subrange(adj_begin, adj_end)) {
                worklist.push(adj);
            }
#endif
        }

        return result;
    }

    inline double serial_relation([[maybe_unused]] const extracted_graph_t &graph,
                                  const extracted_graph_t &graph_T,
                                  const std::unordered_set<extracted_vertex_t> &from,
                                  const std::unordered_set<extracted_vertex_t> &to) {
        size_t in_cnt = 0;
        size_t in_mem_cnt = 0;
        for (const auto &v: to) {
            for (auto [adj_begin, adj_end] = boost::adjacent_vertices(v, graph_T);
                 const auto &adj: std::ranges::subrange(adj_begin, adj_end)) {
                if (to.contains(adj)) continue;
                ++in_cnt;
                if (from.contains(adj))
                    ++in_mem_cnt;
            }
        }
        return in_cnt == 0 ? 0 : static_cast<double>(in_mem_cnt) / static_cast<double>(in_cnt);
    }
}

#endif //NXSIM_GRAPH_HELPER_H