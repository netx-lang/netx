//
// Created by czm233 on 12/11/24.
//

#include "circuit_parser.h"

#include <nlohmann/json.hpp>

namespace hson {
    static clock_t parse_clock(const nlohmann::json &attribute, parse_context &context) {
        return data_factory::id_clock(context.id_map.at(attribute["clock"]), attribute["isPosTrigger"]);
    }

    static source_t parse_source(const std::string &source, parse_context &context) {
        if (const auto pos_l = source.find('['); pos_l != std::string::npos) {
            const auto id = context.id_map.at(source.substr(0, pos_l));
            if (const auto pos_c = source.find(':'); pos_c != std::string::npos) {
                const size_t select1 = std::stoull(source.substr(pos_l + 1, pos_c - pos_l - 1));
                const size_t select2 = std::stoull(source.substr(pos_c + 1, source.size() - pos_c - 2));
                const auto [select_l, select_r] = std::minmax(select1, select2);
                return data_factory::select_source(id, select_l, select_r);
            }
            const size_t select = std::stoull(source.substr(pos_l + 1, source.size() - pos_l - 2));
            return data_factory::select_source(id, select, select);
        }
        return data_factory::id_source(context.id_map.at(source));
    }

    static sink_t parse_sink(const std::string &sink, parse_context &context) {
        if (const auto pos_l = sink.find('['); pos_l != std::string::npos) {
            const auto id = context.id_map.at(sink.substr(0, pos_l));
            if (const auto pos_c = sink.find(':'); pos_c != std::string::npos) {
                const size_t select1 = std::stoull(sink.substr(pos_l + 1, pos_c - pos_l - 1));
                const size_t select2 = std::stoull(sink.substr(pos_c + 1, sink.size() - pos_c - 2));
                const auto [select_l, select_r] = std::minmax(select1, select2);
                return data_factory::select_sink(id, select_l, select_r);
            }
            const size_t select = std::stoull(sink.substr(pos_l + 1, sink.size() - pos_l - 2));
            return data_factory::select_sink(id, select, select);
        }
        return data_factory::id_sink(context.id_map.at(sink));
    }

    enum class builtin_type {
        NOT, AND, OR, XOR, REGISTER
    };

    static const std::map<std::string, builtin_type> builtin_type_map{
        {"NOT", builtin_type::NOT},
        {"AND", builtin_type::AND},
        {"OR", builtin_type::OR},
        {"XOR", builtin_type::XOR},
        {"REGISTER", builtin_type::REGISTER}
    };

    static void parse_builtin(const nlohmann::json &json, parse_context &context) {
        const auto &input = json["input"];
        const auto &output = json["output"];
        switch (builtin_type_map.at(json["id"])) {
            case builtin_type::NOT: {
                context.circuit.add_rule(rule_factory::not_rule(parse_source(input[0], context),
                                                                parse_sink(output[0], context)));
                break;
            }
            case builtin_type::AND: {
                context.circuit.add_rule(rule_factory::and_rule(parse_source(input[0], context),
                                                                parse_source(input[1], context),
                                                                parse_sink(output[0], context)));
                break;
            }
            case builtin_type::OR: {
                context.circuit.add_rule(rule_factory::or_rule(parse_source(input[0], context),
                                                               parse_source(input[1], context),
                                                               parse_sink(output[0], context)));
                break;
            }
            case builtin_type::XOR: {
                context.circuit.add_rule(rule_factory::xor_rule(parse_source(input[0], context),
                                                                parse_source(input[1], context),
                                                                parse_sink(output[0], context)));
                break;
            }
            case builtin_type::REGISTER: {
                context.circuit.add_rule(rule_factory::register_rule(parse_clock(json["attribute"], context),
                                                                     parse_source(input[0], context),
                                                                     parse_sink(output[0], context)));
                break;
            }
            default:
                throw std::runtime_error("Unknown builtin id");
        }
    }

    enum class member_type {
        WIRE, CLOCK, COMPONENT, BUILTIN
    };

    static const std::map<std::string, member_type> member_type_map{
        {"wire", member_type::WIRE},
        {"clock", member_type::CLOCK},
        {"component", member_type::COMPONENT},
        {"builtin", member_type::BUILTIN}
    };

    static void parse_member(const nlohmann::json &json, parse_context &context) {
        switch (member_type_map.at(json["type"])) {
            case member_type::WIRE: {
                context.id_map[json["id"]] = ++context.counter;
                context.size_map[context.counter] = json["width"];
                break;
            }
            case member_type::CLOCK: {
                context.id_map[json["id"]] = ++context.counter;
                context.size_map[context.counter] = 1;
                context.circuit.add_clock(context.counter);
                break;
            }
            case member_type::COMPONENT: {
                for (const auto &member: json["members"])
                    parse_member(member, context);
                break;
            }
            case member_type::BUILTIN: {
                parse_builtin(json, context);
                break;
            }
            default:
                throw std::runtime_error("Unknown member type");
        }
    }

    parse_context parse_circuit(const nlohmann::json &json) {
        parse_context context;
        parse_member(json["design"], context);

        value_storage initial_values;
        for (const auto &id: context.id_map | std::views::values)
            initial_values[id] = boost::dynamic_bitset(context.size_map[id], 0ul);
        context.circuit.update_batch(initial_values);

        return context;
    }
}
