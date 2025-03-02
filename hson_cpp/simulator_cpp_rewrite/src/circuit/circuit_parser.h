//
// Created by czm233 on 12/11/24.
//

#ifndef PARSER_H
#define PARSER_H
#include <nlohmann/json.hpp>

#include "circuit.h"
namespace hson {
    struct parse_context {
        id_t counter = 0;
        hson::circuit circuit;
        std::map<std::string, id_t> id_map;
        std::map<id_t, size_t> size_map;
    };

    parse_context parse_circuit(const nlohmann::json& json);
}

#endif //PARSER_H
