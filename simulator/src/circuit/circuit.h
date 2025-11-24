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

    public:
        void add_rule(const rule_t &new_rule) {
            rules.add(new_rule);
        }

        void add_clock(const id_t id) {
            clocks.insert(id);
        }

        id_set flip(id_t id) {
            values[id].flip();
            return rules.update(values, {id});
        }

        id_set flip_batch(const id_set &ids) {
            for (const auto &id: ids) values[id].flip();
            return rules.update(values, ids);
        }

        id_set update(id_t id, value_t new_value) {
            values[id] = std::move(new_value);
            return rules.update(values, {id});
        }

        id_set update_batch(const value_batch &to_update) {
            id_set hint;
            for (const auto &[id, value]: to_update) {
                hint.insert(id);
                values[id] = value;
            }
            return rules.update(values, hint);
        }

        id_set refresh_all() {
            return rules.refresh_all_rules(values);
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
