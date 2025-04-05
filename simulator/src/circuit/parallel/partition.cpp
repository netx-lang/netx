//
// Created by czm233 on 4/1/25.
//
#include <kaHIP_interface.h>
#include <map>
#include <vector>
#include <ranges>
#include <algorithm>
#include <thread>
#include <random>
#include "partition.h"

namespace hxon {
    std::map<id_t, size_t> partition(const circuit& circuit) {
        const auto& out_rules = circuit.rules.trigger_rules;

        std::vector<id_t> to_old;
        std::map<id_t, size_t> to_new;
        for (const auto& old_id : std::views::keys(out_rules)) {
            const auto new_id = to_old.size();
            to_old.push_back(old_id);
            to_new[old_id] = new_id;
        }

        std::map<size_t, std::vector<size_t>> edges;
        for (const auto&[old_id, rule] : out_rules) {
            auto new_id = to_new[old_id];
            for (const auto& old_out_id : rule.outcomes()) {
                auto new_out_id = to_new[old_out_id];
                edges[new_id].push_back(new_out_id);
                edges[new_out_id].push_back(new_id);
            }
        }

        size_t adj_size = 0;
        for (auto&[id, out] : edges) {
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
        {
            int current = 0;
            for (int id = 0; id < n; ++id) {
                xadj[id] = current;
                for (const auto& out = edges[id]; const auto& out_id : out) adjncy[current++] = static_cast<int>(out_id);
            }
            xadj[n] = current;
        }
        int nparts = static_cast<int>(std::thread::hardware_concurrency() * 2);
        double imbalance = 0.05;
        const int seed = static_cast<int>(std::random_device{}());
        int edgecut;
        const auto part = std::make_unique<int[]>(n);
        kaffpa(&n, nullptr, xadj.get(), nullptr, adjncy.get(), &nparts, &imbalance, false, seed, STRONG, &edgecut, part.get());

        std::map<id_t, size_t> result;
        for (int id = 0; id < n; ++id) result[to_old[id]] = part[id];
        return result;
    }
}
