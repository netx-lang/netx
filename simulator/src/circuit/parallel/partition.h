//
// Created by czm233 on 4/1/25.
//

#ifndef PARTITION_H
#define PARTITION_H

#include <memory>
#include <mutex>
#include <vector>
#include <random>
#include <thread>

#include "../circuit.h"
#include "partitioner.h"

namespace nxon {
    namespace impl {
        struct update_info {
            id_t id;
            value_t value;
        };

        template<typename Part>
        struct partitioned_circuit_context_common {
            using part_t = Part;
            size_t num_parts = 0;
            std::unordered_map<id_t, id_t> belongs;
            std::unordered_map<id_t, id_set> relate;
            id_set clocks;
            std::vector<std::unique_ptr<part_t> > circuit_parts;

            void broadcast(this auto &&self, const id_t id, const value_t &value, const bool exclude_belong = false) {
                self.broadcast_impl(id, value, exclude_belong);
            }

            void broadcast_batch(this auto &&self, const value_batch &to_update, const bool exclude_belong = false) {
                for (auto &[id, value]: to_update)
                    self.broadcast_impl(id, value, exclude_belong);
            }

            [[nodiscard]] auto get_value(const id_t id) const {
                return circuit_parts[belongs.at(id)]->part.get_value(id);
            }

            void run_to_fixed(this auto &&self) {
                while (true) {
                    bool has_update = false;
                    for (auto &part_ptr: self.circuit_parts) {
                        auto &part = *part_ptr;
                        if (part.updates.empty()) continue;
                        has_update = true;
                        part.updates_old.swap(part.updates);
                    }
                    if (!has_update) break;

                    self.process_parts();
                }
            }

            void process_part(this auto &&self, const id_t part_id) {
                auto &part = *self.circuit_parts[part_id];

                const id_set changed = part.part.update_batch(part.updates_old);
                part.updates_old.clear();
                for (auto id: changed) {
                    if (self.belongs[id] != part_id) continue;
                    self.broadcast_impl(id, part.part.get_value(id), true);
                }
            }
        };

        struct circuit_part {
            value_batch updates_old;
            value_batch updates;
            circuit part;
        };

        struct partitioned_circuit_context : partitioned_circuit_context_common<circuit_part> {
            void process_parts() {
                for (id_t i = 0; i < num_parts; ++i) {
                    if (auto &part = *circuit_parts[i]; part.updates_old.empty()) continue;
                    process_part(i);
                }
            }

            void broadcast_impl(const id_t id, const value_t &value, const bool exclude_belong = false) {
                const auto &belong = belongs[id];
                for (const auto &related: relate[id]) {
                    if (exclude_belong && related == belong) continue;
                    auto &part = *circuit_parts[related];
                    part.updates.emplace(id, value);
                }
            }
        };

        struct parallel_circuit_part : circuit_part {
            std::mutex updates_mutex;
        };

        struct parallel_partitioned_circuit_context : partitioned_circuit_context_common<parallel_circuit_part> {
            void process_parts() {
#pragma omp parallel for default(none) schedule(dynamic)
                for (id_t i = 0; i < num_parts; ++i) {
                    if (auto &part = *circuit_parts[i]; part.updates_old.empty()) continue;
                    process_part(i);
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

    template<typename Context = impl::partitioned_circuit_context>
    class partitioned_circuit {
        Context context;

    public:
        explicit partitioned_circuit(const circuit &circuit, const partitioner_t &partitioner = naive_partitioner) {
            const auto [num_parts, belongs] = partitioner(circuit);

            Context result;
            result.num_parts = num_parts;
            for (auto [id, part]: belongs) {
                result.belongs[id] = part;
                result.relate[id].insert(part);
            }
            const auto out_rules = circuit.all_rules();
            for (const auto &rule: out_rules)
                for (const auto &old_id: rule.dependencies())
                    for (const auto &old_out_id: rule.outcomes())
                        result.relate[old_id].insert(result.belongs[old_out_id]);
            result.clocks = circuit.all_clocks();
            for (size_t i = 0; i < result.num_parts; ++i)
                result.circuit_parts.emplace_back(
                    std::make_unique<typename Context::part_t>());
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
