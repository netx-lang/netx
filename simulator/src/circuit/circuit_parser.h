//
// Created by czm233 on 12/11/24.
//

#ifndef PARSER_H
#define PARSER_H
#include <nlohmann/json.hpp>
#include <unordered_map>
#include <string>
#include <format>
#include "circuit.h"

namespace nxon {
    struct parse_context_common {
        void advance_input_clocks(this auto &&self) {
            self.flip_input_clocks();
            self.flip_input_clocks();
        }

        [[nodiscard]] value_t get_by_name(this auto &&self, const std::string &name) {
            return self.get(self.name_map.at(name));
        }

        void flip_by_name(this auto &&self, const std::string &name) {
            self.flip(self.name_map.at(name));
        }

        void update_by_name(this auto &&self, const std::string &name, const value_t &value) {
            self.update(self.name_map.at(name), value);
        }

        void flip_input_clocks(this auto &&self) {
            self.flip_batch(self.circuit_input_clocks);
        }
    };

    struct parse_context : parse_context_common {
        id_t members_total = 0;
        nxon::circuit circuit;
        std::vector<id_t> circuit_input, circuit_output;
        id_set circuit_input_clocks;
        std::unordered_map<id_t, size_t> size_map;
        std::unordered_map<id_t, std::string> name_of_map;
        std::unordered_map<std::string, id_t> name_map;

        void new_wire(const id_t id, const size_t size, std::string org_name) {
            size_map.emplace(id, size);
            if (name_map.contains(org_name)) {
                static constexpr char DELIMITER = '#';
                size_t suffix = 1;
                while (name_map.contains(std::format("{}{}{}", org_name, DELIMITER, suffix)))
                    ++suffix;
                org_name += DELIMITER;
                org_name += std::to_string(suffix);
            }
            name_of_map.emplace(id, org_name);
            name_map.emplace(org_name, id);
        }

        [[nodiscard]] value_t get(const id_t id) const {
            return circuit.get_value(id);
        }

        void flip(const id_t id) {
            circuit.flip(id);
        }

        void flip_batch(const id_set &ids) {
            circuit.flip_batch(ids);
        }

        void update(const id_t id, const value_t &value) {
            circuit.update(id, value);
        }

        void update_batch(const value_batch &batch) {
            circuit.update_batch(batch);
        }
    };

    using native_factory = std::function<rule_t(const parse_context &ctx, const nlohmann::json &json)>;

    using native_map = std::map<std::string, native_factory>;

    clock_t parse_clock(const std::string &source, bool is_pos_trigger);

    source_t parse_source(const std::string &source, const parse_context &context);

    sink_t parse_sink(const std::string &sink, const parse_context &context);

    void parse_circuit(parse_context &context, const nlohmann::json &json, const native_map &native_map = {});

    parse_context parse_circuit(const nlohmann::json &json, const native_map &native_map = {});
}

#endif //PARSER_H
