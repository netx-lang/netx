//
// Created by czm233 on 12/11/24.
//

#ifndef PARSER_H
#define PARSER_H
#include <nlohmann/json.hpp>
#include <unordered_map>
#include <string>
#include "circuit.h"

namespace nxon {
    struct parse_context {
        id_t members_total = 0;
        nxon::circuit circuit;
        id_set circuit_input, circuit_output, circuit_input_clocks;
        std::unordered_map<id_t, size_t> size_map;
        std::unordered_map<std::string, id_t> name_map;

        void flip_input_clocks() {
            circuit.flip_batch(circuit_input_clocks);
        }

        void advance_input_clocks() {
            flip_input_clocks();
            flip_input_clocks();
        }

        [[nodiscard]] value_t get_by_name(const std::string &name) const {
            return circuit.get_value(name_map.at(name));
        }

        void flip_by_name(const std::string &name) {
            circuit.flip(name_map.at(name));
        }

        void update_by_name(const std::string &name, const value_t &value) {
            circuit.update(name_map.at(name), value);
        }
    };

    parse_context parse_circuit(const nlohmann::json &json);
}

#endif //PARSER_H
