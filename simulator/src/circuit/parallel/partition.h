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
#include "../circuit.h"

namespace hxon {
    struct update_info {
        id_t id;
        value_t value;
        std::shared_ptr<std::latch> latch;
    };

    struct update_queue {
        std::queue<update_info> queue;
        std::mutex mutex;
        std::condition_variable condition;
    };

    struct circuit_part_context {
        update_queue updates;
        circuit part;
    };

    struct partitioned_circuit {
        size_t num_parts;
        std::map<id_t, size_t> belongs;
        std::map<id_t, std::set<size_t> > relate;
        id_set clocks;
        std::vector<std::unique_ptr<circuit_part_context>> circuit_parts;

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
            return circuit_parts[belongs.at(id)]->part.get_value(id);
        }
    private:
        void broadcast_impl(const id_t id, const value_t &value, const bool exclude_belong = false,
                            const bool notify = true) {
            const auto &belong = belongs[id];
            const auto latch_count = relate[id].size() - exclude_belong;
            auto latch = clocks.contains(id) ? std::make_shared<std::latch>(latch_count) : nullptr;
            for (const auto &related: relate[id]) {
                if (exclude_belong && related == belong) continue;
                auto &[queue, mutex, condition] = circuit_parts[related]->updates;
                {
                    auto lock = std::lock_guard(mutex);
                    queue.emplace(id, value, latch);
                }
                if (notify) condition.notify_all();
            }
        }
    };

    partitioned_circuit partition(const circuit &circuit);
    std::vector<std::jthread> work_on_partition(partitioned_circuit &partition);
}

#endif //PARTITION_H
