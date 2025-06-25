//
// Created by czm233 on 12/11/24.
//

#ifndef PARSER_H
#define PARSER_H
#include <nlohmann/json.hpp>

#include "circuit.h"

namespace nxon {
    struct parse_context {
        id_t members_total;
        nxon::circuit circuit;
        id_set circuit_input, circuit_output, circuit_input_clocks;
        std::map<id_t, size_t> size_map;
        std::map<std::string, id_t> name_map;

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

    using native_factory = std::function<rule_t(const parse_context &ctx, const nlohmann::json &json)>;

    using native_map = std::map<std::string, native_factory>;

    clock_t parse_clock(const std::string &source, bool is_pos_trigger);

    source_t parse_source(const std::string &source, const parse_context &context);

    sink_t parse_sink(const std::string &sink, const parse_context &context);

    parse_context parse_circuit(const nlohmann::json &json, const native_map &native_map = {});
}

#endif //PARSER_H
