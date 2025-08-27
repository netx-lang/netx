//
// Created by czm233 on 4/1/25.
//

#ifndef PARTITION_H
#define PARTITION_H

#include <memory>
#include <mutex>
#include <vector>
#include <omp.h>
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

            void run_to_fixed();

        private:
            static void worker_part(partitioned_circuit_context &partition,
                                    size_t part_id);

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
        explicit partitioned_circuit(const circuit &circuit);

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
