//
// Created by czm233 on 4/1/25.
//

#ifndef PARTITION_H
#define PARTITION_H

#include <condition_variable>
#include <memory>
#include <latch>
#include <mutex>
#include <map>
#include <set>
#include <vector>
#include <queue>
#include <thread>
#include <atomic>
#include "../circuit.h"

namespace nxon {
    namespace impl {
        struct update_info {
            id_t id;
            value_t value;
            std::shared_ptr<std::latch> latch;
        };

        struct update_queue {
            std::queue<update_info> queue;
            std::condition_variable condition;
        };

        struct circuit_part_context {
            update_queue updates;
            circuit part;
            std::mutex mutex;
        };

        struct partitioned_circuit_context {
            std::atomic_ullong wait_cnt;
            size_t num_parts;
            std::map<id_t, size_t> belongs;
            std::map<id_t, std::set<size_t> > relate;
            id_set clocks;
            std::vector<std::unique_ptr<circuit_part_context> > circuit_parts;

            void broadcast(const id_t id, const value_t &value, const bool exclude_belong = false) {
                broadcast_impl(id, value, exclude_belong);
            }

            void broadcast_batch(const value_storage &to_update, const bool exclude_belong = false) {
                std::set<size_t> to_notify;
                for (auto &[id, value]: to_update) {
                    broadcast_impl(id, value, exclude_belong, false);
                    const auto &belong = belongs[id];
                    for (const auto &related: relate[id]) {
                        if (exclude_belong && related == belong) continue;
                        to_notify.insert(related);
                    }
                }
                for (auto &notify: to_notify) circuit_parts[notify]->updates.condition.notify_all();
            }

            [[nodiscard]] auto get_value(const id_t id) const {
                const auto belong = belongs.at(id);
                std::lock_guard lock(circuit_parts[belong]->mutex);
                return circuit_parts[belong]->part.get_value(id);
            }

        private:
            void broadcast_impl(const id_t id, const value_t &value, const bool exclude_belong = false,
                                const bool notify = true) {
                const auto &belong = belongs[id];
                const auto latch_count = relate[id].size() - exclude_belong;
                auto latch = clocks.contains(id) ? std::make_shared<std::latch>(latch_count) : nullptr;
                for (const auto &related: relate[id]) {
                    if (exclude_belong && related == belong) continue;
                    auto &[update, _, mutex] = *circuit_parts[related];
                    auto &[queue, condition] = update;
                    {
                        auto lock = std::lock_guard(mutex);
                        queue.emplace(id, value, latch);
                    }
                    if (notify) condition.notify_all();
                }
            }
        };
    }

    class partitioned_circuit {
        std::unique_ptr<impl::partitioned_circuit_context> context;
        std::vector<std::jthread> workers;
        static void worker_part(const std::stop_token &stop_token, impl::partitioned_circuit_context &partition, size_t part_id);
    public:
        explicit partitioned_circuit(const circuit &circuit);

        void broadcast(const id_t id, const value_t &value, const bool exclude_belong = false) {
            context->broadcast(id, value, exclude_belong);
        }

        void broadcast_batch(const value_storage &to_update, const bool exclude_belong = false) {
            context->broadcast_batch(to_update, exclude_belong);
        }

        [[nodiscard]] auto get_value(const id_t id) const {
            return context->get_value(id);
        }

        void start_workers() {
            auto& partition = *context;
            for (size_t i = 0; i < partition.num_parts; ++i)
                workers.emplace_back(worker_part, std::ref(partition), i);
        }

        void stop_workers() {
            workers.clear();
        }
    };
}

#endif //PARTITION_H
