//
// Created by czm233 on 12/11/24.
//

#ifndef PARSER_H
#define PARSER_H
#include <nlohmann/json.hpp>

#include "circuit.h"

namespace hxon {
    struct parse_context {
        id_t members_total;
        hxon::circuit circuit;
        id_set circuit_input, circuit_output, circuit_input_clocks;
        std::map<id_t, size_t> size_map;
    };

    parse_context parse_circuit(const nlohmann::json &json);
}

#endif //PARSER_H
