//
// Created by czm233 on 12/11/24.
//

#ifndef CIRCUIT_H
#define CIRCUIT_H

#include <utility>

#include "circuit_def.h"
#include "rule.h"

namespace hxon {
    class circuit {
    public:
        rule_set rules;
        value_storage values;
        id_set clocks;

        void add_rule(const rule_t &new_rule) {
            rules.add(new_rule);
        }

        void add_clock(const id_t id) {
            clocks.insert(id);
        }

        id_set flip(const id_set &ids) {
            for (const auto &id: ids) values[id].flip();
            return rules.update(values, ids);
        }

        id_set update(id_t id, value_t new_value) {
            values[id] = std::move(new_value);
            return rules.update(values, {{id}});
        }

        id_set update_batch(const value_storage &to_update) {
            id_set hint;
            for (const auto &[id, value]: to_update) {
                hint.insert(id);
                values[id] = value;
            }
            return rules.update(values, hint);
        }

        [[nodiscard]] value_t get_value(const id_t id) const {
            return values.at(id);
        }
    };
}

#endif //CIRCUIT_H
