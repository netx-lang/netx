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

namespace hxon {
    partitioned_circuit partition(const circuit &circuit) {
        std::vector<rule_t> out_rules;
        for (const auto& rule: circuit.rules.trigger_rules | std::views::values) out_rules.emplace_back(rule);
        std::ranges::sort(out_rules);
        auto [rl, rr] = std::ranges::unique(out_rules);
        out_rules.erase(rl, rr);

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
        int currentPtr = 0;
        for (int id = 0; id < n; ++id) {
            xadj[id] = currentPtr;
            for (const auto &out = edges[id]; const auto &out_id: out) adjncy[currentPtr++] = static_cast<int>(out_id);
        }
        xadj[n] = currentPtr;
        int nparts = static_cast<int>(std::thread::hardware_concurrency());
        double imbalance = 0.02;
        const int seed = static_cast<int>(std::random_device{}());
        int edgecut;
        const auto part = std::make_unique<int[]>(n);
        kaffpa_balance_NE(&n, nullptr, xadj.get(), nullptr, adjncy.get(), &nparts, &imbalance, false, seed, STRONG,
                          &edgecut,
                          part.get());

        partitioned_circuit result;
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
        result.clocks = circuit.clocks;
        for (size_t i = 0; i < result.num_parts; ++i)
            result.circuit_parts.emplace_back(
                std::make_unique<circuit_part_context>());
        for (const auto &[id, part_id]: result.belongs)
            result.circuit_parts[part_id]->part.values[id] = circuit.get_value(id);
        for (const auto &rule: out_rules) {
            std::set<size_t> out_parts;
            for (const auto &outcome: rule.outcomes()) out_parts.insert(result.belongs[outcome]);
            for (const auto &part_id: out_parts) {
                auto &circuit_part = result.circuit_parts[part_id]->part;
                for (const auto &dep: rule.dependencies()) circuit_part.values[dep] = circuit.get_value(dep);
                for (const auto &out: rule.outcomes()) circuit_part.values[out] = circuit.get_value(out);
                circuit_part.add_rule(rule);
            }
        }

        return result;
    }

    void worker_part(std::stop_token stop_token, partitioned_circuit &partition, const size_t part_id) {
        auto &[updates, part] = *partition.circuit_parts[part_id];
        auto &[q, mutex, condition] = updates;
        while (!stop_token.stop_requested()) {
            std::unique_lock lock(mutex);
            if (!condition.wait_for(lock, std::chrono::milliseconds(200), [&q] {
                return !q.empty();
            }))
                continue;

            value_storage to_update;
            std::shared_ptr<std::latch> latch_to_wait;
            while (!(q.empty() || latch_to_wait)) {
                auto &[id, value, latch] = q.front();
                if (part.get_value(id) != value) to_update[id] = value;
                if (latch) latch_to_wait = latch;
                q.pop();
            }
            lock.unlock();

            id_set changed = part.update_batch(to_update);
            to_update.clear();
            if (latch_to_wait) latch_to_wait->arrive_and_wait();
            value_storage changed_values;
            for (auto &id: changed) {
                if (partition.belongs[id] != part_id) continue;
                changed_values[id] = part.get_value(id);
            }
            partition.broadcast_batch(changed_values, true);
        }
    }

    std::vector<std::jthread> work_on_partition(partitioned_circuit &partition) {
        std::vector<std::jthread> result;
        for (size_t i = 0; i < partition.num_parts; ++i)
            result.emplace_back(worker_part, std::ref(partition), i);
        return result;
    }
}
