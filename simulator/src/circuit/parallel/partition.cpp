//
// Created by czm233 on 4/1/25.
//
#include <kaHIP_interface.h>
#include <map>
#include <vector>
#include <set>
#include <ranges>
#include <algorithm>
#include <thread>
#include <random>
#include "partition.h"

namespace nxon {
    partitioned_circuit::partitioned_circuit(const circuit &circuit) {
        auto out_rules = circuit.all_rules();

        std::vector<id_t> to_old;
        std::map<id_t, size_t> to_new;
        for (const auto &rule: out_rules)
            for (const auto &old_id: rule.dependencies() + rule.outcomes()) {
                if (to_new.contains(old_id)) continue;
                const auto new_id = to_old.size();
                to_old.push_back(old_id);
                to_new[old_id] = new_id;
            }

        std::map<size_t, std::vector<size_t> > edges;
        for (const auto &rule: out_rules)
            for (const auto &old_id: rule.dependencies()) {
                auto new_id = to_new[old_id];
                for (const auto &old_out_id: rule.outcomes()) {
                    auto new_out_id = to_new[old_out_id];
                    edges[new_id].push_back(new_out_id);
                    edges[new_out_id].push_back(new_id);
                }
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

        int n = static_cast<int>(to_old.size());
        const auto xadj = std::make_unique<int[]>(n + 1);
        const auto adjncy = std::make_unique<int[]>(adj_size);
        int current_ptr = 0;
        for (int id = 0; id < n; ++id) {
            xadj[id] = current_ptr;
            for (const auto &out = edges[id]; const auto &out_id: out) adjncy[current_ptr++] = static_cast<int>(out_id);
        }
        xadj[n] = current_ptr;
        int nparts = static_cast<int>(std::thread::hardware_concurrency());
        double imbalance = 0.02;
        const int seed = static_cast<int>(std::random_device{}());
        int edgecut;
        const auto part = std::make_unique<int[]>(n);
        kaffpa_balance_NE(&n, nullptr, xadj.get(), nullptr, adjncy.get(), &nparts, &imbalance, false, seed, STRONG,
                          &edgecut,
                          part.get());

        impl::partitioned_circuit_context result;
        result.num_parts = nparts;
        for (int id = 0; id < n; ++id) {
            auto old_id = to_old[id];
            auto part_id = part[id];
            result.belongs[old_id] = part_id;
            result.relate[old_id].insert(part_id);
        }
        for (const auto &rule: out_rules)
            for (const auto &old_id: rule.dependencies())
                for (const auto &old_out_id: rule.outcomes())
                    result.relate[old_id].insert(result.belongs[old_out_id]);
        result.clocks = circuit.all_clocks();
        for (size_t i = 0; i < result.num_parts; ++i)
            result.circuit_parts.emplace_back(
                std::make_unique<impl::circuit_part_context>());
        for (const auto &[id, part_id]: result.belongs)
            result.circuit_parts[part_id]->part.put(id, circuit.get_value(id));
        for (const auto &rule: out_rules) {
            std::set<size_t> out_parts;
            for (const auto &outcome: rule.outcomes()) out_parts.insert(result.belongs[outcome]);
            for (const auto &part_id: out_parts) {
                auto &circuit_part = result.circuit_parts[part_id]->part;
                for (const auto &dep: rule.dependencies()) circuit_part.put(dep, circuit.get_value(dep));
                for (const auto &out: rule.outcomes()) circuit_part.put(out, circuit.get_value(out));
                circuit_part.add_rule(rule);
            }
        }

        context = std::move(result);
    }

    void impl::partitioned_circuit_context::worker_part(partitioned_circuit_context &partition, const size_t part_id) {
        auto &part = *partition.circuit_parts[part_id];

        id_set changed = part.part.update_batch(part.updates_old);
        part.updates_old.clear();
        for (auto id: changed) {
            if (partition.belongs[id] != part_id) continue;
            partition.broadcast_impl(id, part.part.get_value(id), true);
        }
    }
}
