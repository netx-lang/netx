//
// Created by czm233 on 4/1/25.
//

#ifndef PARTITION_H
#define PARTITION_H

#include <memory>
#include <mutex>
#include <vector>
#include <omp.h>
#include <random>
#include <thread>

#include "kaHIP_interface.h"

#include "../circuit.h"

namespace nxon {
    namespace impl {
        struct update_info {
            id_t id;
            value_t value;
        };

        struct circuit_part_context {
            value_batch updates_old;
            value_batch updates;
            std::mutex updates_mutex;
            circuit part;
        };

        struct partitioned_circuit_context {
            size_t num_parts = 0;
            std::unordered_map<id_t, size_t> belongs;
            std::unordered_map<id_t, id_set> relate;
            id_set clocks;
            std::vector<std::unique_ptr<circuit_part_context> > circuit_parts;

            void broadcast(const id_t id, const value_t &value, const bool exclude_belong = false) {
                broadcast_impl(id, value, exclude_belong);
            }

            void broadcast_batch(const value_batch &to_update, const bool exclude_belong = false) {
                for (auto &[id, value]: to_update)
                    broadcast_impl(id, value, exclude_belong);
            }

            [[nodiscard]] auto get_value(const id_t id) const {
                return circuit_parts[belongs.at(id)]->part.get_value(id);
            }

            void run_to_fixed() {
                while (true) {
                    size_t cnt = 0;
                    for (size_t i = 0; i < num_parts; ++i) {
                        auto &part = *circuit_parts[i];
                        if (part.updates.empty()) continue;
                        ++cnt;
                        part.updates_old.swap(part.updates);
                    }
                    if (cnt == 0) break;

#pragma omp parallel for default(none) schedule(dynamic)
                    for (size_t i = 0; i < num_parts; ++i) {
                        if (auto &part = *circuit_parts[i]; part.updates_old.empty()) continue;
                        worker_part(*this, i);
                    }
                }
            }

        private:
            static void worker_part(partitioned_circuit_context &partition,
                                    const size_t part_id) {
                auto &part = *partition.circuit_parts[part_id];

                id_set changed = part.part.update_batch(part.updates_old);
                part.updates_old.clear();
                for (auto id: changed) {
                    if (partition.belongs[id] != part_id) continue;
                    partition.broadcast_impl(id, part.part.get_value(id), true);
                }
            }

            void broadcast_impl(const id_t id, const value_t &value, const bool exclude_belong = false) {
                const auto &belong = belongs[id];
                for (const auto &related: relate[id]) {
                    if (exclude_belong && related == belong) continue;
                    auto &part = *circuit_parts[related];
                    auto lock = std::lock_guard(part.updates_mutex);
                    part.updates.emplace(id, value);
                }
            }
        };
    }

    class partitioned_circuit {
        impl::partitioned_circuit_context context;

    public:
        explicit partitioned_circuit(const circuit &circuit) {
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
                for (const auto &out = edges[id]; const auto &out_id: out)
                    adjncy[current_ptr++] = static_cast<int>(out_id);
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

        [[nodiscard]] auto get_value(const id_t id) const {
            return context.get_value(id);
        }

        void broadcast(const id_t id, const value_t &value, const bool exclude_belong = false) {
            context.broadcast(id, value, exclude_belong);
        }

        void broadcast_batch(const value_batch &to_update, const bool exclude_belong = false) {
            context.broadcast_batch(to_update, exclude_belong);
        }

        void run_to_fixed() {
            context.run_to_fixed();
        }

        void update(const id_t id, const value_t &value) {
            broadcast(id, value);
            run_to_fixed();
        }

        void update_batch(const value_batch &to_update) {
            broadcast_batch(to_update);
            run_to_fixed();
        }

        void broadcast_flip(const id_t id) {
            auto v = get_value(id);
            v.flip();
            broadcast(id, v);
        }

        void broadcast_flip_batch(const id_set &ids) {
            for (auto &id: ids) broadcast_flip(id);
        }

        void flip(const id_t id) {
            broadcast_flip(id);
            run_to_fixed();
        }

        void flip_batch(const id_set &ids) {
            broadcast_flip_batch(ids);
            run_to_fixed();
        }
    };
}

#endif //PARTITION_H
