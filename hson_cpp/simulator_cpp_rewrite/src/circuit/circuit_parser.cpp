//
// Created by czm233 on 12/11/24.
//

#include "circuit_parser.h"

#include <nlohmann/json.hpp>

namespace hson {
    struct id_info {
        id_t id{};
        std::optional<std::pair<size_t, size_t>> selection;
    };

    static id_info parse_id(const std::string &id) {
        id_info result;
        const auto pos_at = id.find('@');
        if (pos_at == std::string::npos) throw std::invalid_argument("Invalid ID");
        if (const auto pos_l = id.find('['); pos_l != std::string::npos) {
            result.id = std::stoull(id.substr(pos_at + 1, pos_l - pos_at - 1));
            if (const auto pos_c = id.find(':'); pos_c != std::string::npos) {
                const size_t select1 = std::stoull(id.substr(pos_l + 1, pos_c - pos_l - 1));
                const size_t select2 = std::stoull(id.substr(pos_c + 1, id.size() - pos_c - 2));
                result.selection = std::minmax(select1, select2);
            } else {
                const size_t select = std::stoull(id.substr(pos_l + 1, id.size() - pos_l - 2));
                result.selection = std::make_pair(select, select);
            }
        } else result.id = std::stoull(id.substr(pos_at + 1));
        return result;
    }

    static clock_t parse_clock(const nlohmann::json &attribute) {
        auto [id, selection] = parse_id(attribute["clock"]);
        if (selection) throw std::invalid_argument("Clock cannot be selected");
        return data_factory::id_clock(id, attribute["isPosTrigger"] == "true");
    }

    static source_t parse_source(const std::string &source) {
        if (source.find('@') != std::string::npos) {
            auto [id, selection] = parse_id(source);
            if (selection) return data_factory::select_source(id, selection->first, selection->second);
            return data_factory::id_source(id);
        }
        return data_factory::constant_source(value_t::parse(source));
    }

    static sink_t parse_sink(const std::string &sink) {
        auto [id, selection] = parse_id(sink);
        if (selection) return data_factory::select_sink(id, selection->first, selection->second);
        return data_factory::id_sink(id);
    }

    enum class builtin_type {
        NOT, AND, OR, XOR, ADD, SUB, MUL, DIV, MOD, CAST, REGISTER
    };

    static const std::map<std::string, builtin_type> builtin_type_map{
        {"NOT", builtin_type::NOT},
        {"AND", builtin_type::AND},
        {"OR", builtin_type::OR},
        {"XOR", builtin_type::XOR},
        {"ADD", builtin_type::ADD},
        {"SUB", builtin_type::SUB},
        {"MUL", builtin_type::MUL},
        {"DIV", builtin_type::DIV},
        {"MOD", builtin_type::MOD},
        {"CAST", builtin_type::CAST},
        {"REGISTER", builtin_type::REGISTER}
    };

    static void parse_builtin(const nlohmann::json &json, parse_context &context) {
        const auto &input = json["input"];
        const auto &output = json["output"];
        const std::string builtin_id = json["id"];
        if (!builtin_type_map.contains(builtin_id)) throw std::runtime_error("Unknown builtin id " + builtin_id);
        switch (builtin_type_map.at(builtin_id)) {
            case builtin_type::NOT: {
                context.circuit.add_rule(rule_factory::not_rule(parse_source(input[0]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::AND: {
                context.circuit.add_rule(rule_factory::and_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::OR: {
                context.circuit.add_rule(rule_factory::or_rule(parse_source(input[0]),
                                                               parse_source(input[1]),
                                                               parse_sink(output[0])));
                break;
            }
            case builtin_type::XOR: {
                context.circuit.add_rule(rule_factory::xor_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::ADD: {
                context.circuit.add_rule(rule_factory::add_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::SUB: {
                context.circuit.add_rule(rule_factory::sub_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::MUL: {
                context.circuit.add_rule(rule_factory::mul_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::DIV: {
                context.circuit.add_rule(rule_factory::div_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::MOD: {
                context.circuit.add_rule(rule_factory::mod_rule(parse_source(input[0]),
                                                                parse_source(input[1]),
                                                                parse_sink(output[0])));
                break;
            }
            case builtin_type::CAST: {
                context.circuit.add_rule(rule_factory::id_rule(parse_source(input[0]),
                                                               data_factory::sign_extend_sink_adapter(
                                                                   parse_sink(output[0]))));
                break;
            }
            case builtin_type::REGISTER: {
                context.circuit.add_rule(rule_factory::register_rule(parse_clock(json["attribute"]),
                                                                     parse_source(input[0]),
                                                                     parse_sink(output[0])));
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
        const std::string member_type = json["type"];
        if (!member_type_map.contains(member_type)) throw std::runtime_error("Unknown member type " + member_type);
        if (json.contains("members"))
            for (const auto &member: json["members"])
                parse_member(member, context);
        switch (member_type_map.at(member_type)) {
            case member_type::WIRE: {
                auto [id, selection] = parse_id(json["id"]);
                if (selection) throw std::runtime_error("Wire member cannot be selected");
                context.size_map[id] = json["width"];
                break;
            }
            case member_type::CLOCK: {
                auto [id, selection] = parse_id(json["id"]);
                if (selection) throw std::runtime_error("Clock member cannot be selected");
                context.size_map[id] = 1;
                context.circuit.add_clock(id);
                break;
            }
            case member_type::COMPONENT:
                break;
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
        context.members_total = json["totalData"];
        parse_member(json["design"], context);

        value_storage initial_values;
        for (id_t id = 0; id < context.members_total; ++id)
            initial_values[id] = value_t(context.size_map[id], 0ul);
        context.circuit.update_batch(initial_values);

        return context;
    }
}
