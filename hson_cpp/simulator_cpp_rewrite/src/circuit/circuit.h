//
// Created by czm233 on 12/11/24.
//

#ifndef CIRCUIT_H
#define CIRCUIT_H

#include <utility>

#include "circuit_def.h"
#include "rule.h"

namespace hson {
    class circuit {
        rule_set rules;
        value_storage values;

    public:
        void add_rule(const rule_t& newRule) {
            rules.add(newRule);
        }

        id_set update(id_t id, value_t newValue) {
            values[id] = std::move(newValue);
            return rules.update(values, {{id}});
        }

        id_set update_batch(const value_storage& toUpdate) {
            id_set hint;
            for (const auto &[id, value] : toUpdate) {
                hint.insert(id);
                values[id] = value;
            }
            return rules.update(values, hint);
        }

        [[nodiscard]] value_t get_value(id_t id) const {
            return values.at(id);
        }
    };
}

#endif //CIRCUIT_H
