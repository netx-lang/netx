//
// Created by czm233 on 12/11/24.
//

#ifndef CIRCUIT_H
#define CIRCUIT_H

#include <utility>

#include "circuit_def.h"
#include "rule.h"

namespace nxon {
    class circuit {
        rule_set rules;
        value_storage values;
        id_set clocks;

        template<bool TrackChanged>
        auto update_ids(const id_set &hint) {
            if constexpr (TrackChanged) {
                return rules.update(values, hint);
            } else {
                rules.update_discard(values, hint);
            }
        }

        template<bool TrackChanged>
        auto update_one(const id_t id, value_t new_value) {
            values[id] = std::move(new_value);
            return update_ids<TrackChanged>({id});
        }

        template<bool TrackChanged>
        auto flip_ids(const id_set &ids) {
            for (const auto &id: ids) values[id].flip();
            return update_ids<TrackChanged>(ids);
        }

        template<bool TrackChanged>
        auto update_batch_impl(const value_batch &to_update) {
            id_set hint;
            for (const auto &[id, value]: to_update) {
                hint.insert(id);
                values[id] = value;
            }
            return update_ids<TrackChanged>(hint);
        }

    public:
        void add_rule(const rule_t &new_rule) {
            rules.add(new_rule);
        }

        void add_clock(const id_t id) {
            clocks.insert(id);
        }

        id_set flip(id_t id) {
            return flip_ids<true>({id});
        }

        void flip_discard(id_t id) {
            flip_ids<false>({id});
        }

        id_set flip_batch(const id_set &ids) {
            return flip_ids<true>(ids);
        }

        void flip_batch_discard(const id_set &ids) {
            flip_ids<false>(ids);
        }

        id_set update(id_t id, value_t new_value) {
            return update_one<true>(id, std::move(new_value));
        }

        void update_discard(id_t id, value_t new_value) {
            update_one<false>(id, std::move(new_value));
        }

        id_set update_batch(const value_batch &to_update) {
            return update_batch_impl<true>(to_update);
        }

        void update_batch_discard(const value_batch &to_update) {
            update_batch_impl<false>(to_update);
        }

        void run_all_discard() {
            rules.run_all_discard(values);
        }

        void put(const id_t id, value_t new_value) {
            values[id] = std::move(new_value);
        }

        void put_batch(const value_batch &to_update) {
            for (const auto &[id, value]: to_update)
                values[id] = value;
        }

        [[nodiscard]] value_t get_value(const id_t id) const {
            return values.at(id);
        }

        [[nodiscard]] std::vector<rule_t> all_rules() const {
            return rules.rules();
        }

        [[nodiscard]] id_set all_clocks() const {
            return clocks;
        }
    };
}

#endif //CIRCUIT_H
