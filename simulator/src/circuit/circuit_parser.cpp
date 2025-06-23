//
// Created by czm233 on 12/11/24.
//

#include "circuit_parser.h"
#include "rule.h"
#include <nlohmann/json.hpp>
#include <map>
#include <utility>
#include <algorithm>
#include <deque>
#include <optional>

namespace nxon {
    struct id_info {
        std::string name;
        id_t id{};
        std::optional<std::pair<size_t, size_t> > selection;
    };

    static id_info parse_id(const std::string &id) {
        id_info result;
        const auto pos_at = id.find('@');
        if (pos_at == std::string::npos) throw std::invalid_argument("Invalid ID");
        result.name = id.substr(0, pos_at);
        if (const auto pos_l = id.find('[', pos_at + 1); pos_l != std::string::npos) {
            result.id = std::stoull(id.substr(pos_at + 1, pos_l - pos_at - 1));
            if (const auto pos_c = id.find(':', pos_l + 1); pos_c != std::string::npos) {
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

    static clock_t parse_clock(const std::string &source, const nlohmann::json &attribute) {
        auto [name, id, selection] = parse_id(source);
        if (selection) throw std::invalid_argument("Clock cannot be selected");
        return data_factory::id_clock(id, attribute["isPosTrigger"] == "true");
    }

    static source_t parse_source(const std::string &source, const parse_context &context) {
         if (source.find('@') != std::string::npos) {
             auto [name, id, selection] = parse_id(source);
             if (selection) return data_factory::select_source(id, selection->first, selection->second);
             return data_factory::id_source(id, context.size_map.at(id));
         }
         return data_factory::constant_source(value_t::parse(source));
     }

    static sink_t parse_sink(const std::string &sink, const parse_context &context) {
        auto [name, id, selection] = parse_id(sink);
        if (selection) return data_factory::select_sink(id, selection->first, selection->second);
        return data_factory::id_sink(id, context.size_map.at(id));
    }

    static void parse_binary(const nlohmann::json &inputs, const nlohmann::json &outputs, parse_context &context,
                             rule_t (*cons)(source_t, source_t, sink_t)) {
        if (inputs.size() != 2) throw std::invalid_argument("Need exactly two inputs");
        if (outputs.size() != 1) throw std::invalid_argument("Need exactly one output");
        context.circuit.add_rule(cons(parse_source(inputs.at(0), context), parse_source(inputs.at(1), context),
                                      parse_sink(outputs.at(0), context)));
    }

    static source_t binary_combine(rule_t (*cons)(source_t, source_t, sink_t), source_t a, source_t b, parse_context &context, const bool len_sum) {
        const auto new_id = context.members_total;
        const auto new_len = len_sum ? a.len() + b.len() : a.len();
        ++context.members_total;
        context.size_map[new_id] = new_len;
        context.circuit.add_rule(cons(std::move(a), std::move(b), data_factory::id_sink(new_id, new_len)));

        return data_factory::id_source(new_id, new_len);
    }

    static void parse_variadic_binary(const nlohmann::json &inputs, const nlohmann::json &outputs,
                                      parse_context &context,
                                      rule_t (*cons)(source_t, source_t, sink_t), const bool assoc = true,
                                      const bool len_sum = false) {
        if (outputs.size() != 1) throw std::invalid_argument("Need exactly one output");
        sink_t output = parse_sink(outputs.at(0), context);

        std::deque<source_t> sources;
        for (auto &input: inputs) sources.push_back(parse_source(input, context));
        if (sources.empty()) throw std::invalid_argument("No sources specified");
        if (sources.size() == 1) {
            context.circuit.add_rule(rule_factory::id_rule(sources.front(), std::move(output)));
            return;
        }

        if (assoc) {
            std::deque<source_t> nextSources;
            for (size_t left = sources.size(); left > 2; --left) {
                if (sources.size() < 2) {
                    while (!sources.empty()) {
                        nextSources.push_back(sources.front());
                        sources.pop_front();
                    }
                    sources.swap(nextSources);
                }

                auto src1 = sources.front();
                sources.pop_front();
                auto src2 = sources.front();
                sources.pop_front();
                nextSources.push_back(binary_combine(cons, std::move(src1), std::move(src2), context, len_sum));
            }

            while (!sources.empty()) {
                nextSources.push_back(sources.front());
                sources.pop_front();
            }
            sources.swap(nextSources);
        } else {
            while (sources.size() > 2) {
                auto src1 = sources.front();
                sources.pop_front();
                auto src2 = sources.front();
                sources.pop_front();
                sources.push_front(binary_combine(cons, std::move(src1), std::move(src2), context, len_sum));
            }
        }
        auto src1 = sources.front();
        sources.pop_front();
        auto src2 = sources.front();
        sources.pop_front();
        context.circuit.add_rule(cons(std::move(src1), std::move(src2), std::move(output)));
    }

    enum class builtin_type {
        NOT, AND, OR, XOR, SHL, LSHR, ASHR, ADD, SUB, MUL, DIV, MOD, CONCAT, ULT, UGT, ULE, UGE, SLT, SGT, SLE, SGE, EQ,
        NEQ, CAST, REGISTER, TO
    };

    static const std::map<std::string, builtin_type> builtin_type_map{
        {"NOT", builtin_type::NOT},
        {"AND", builtin_type::AND},
        {"OR", builtin_type::OR},
        {"XOR", builtin_type::XOR},
        {"SHL", builtin_type::SHL},
        {"LSHR", builtin_type::LSHR},
        {"ASHR", builtin_type::ASHR},
        {"ADD", builtin_type::ADD},
        {"SUB", builtin_type::SUB},
        {"MUL", builtin_type::MUL},
        {"DIV", builtin_type::DIV},
        {"MOD", builtin_type::MOD},
        {"CONCAT", builtin_type::CONCAT},
        {"ULT", builtin_type::ULT},
        {"UGT", builtin_type::UGT},
        {"ULE", builtin_type::ULE},
        {"UGE", builtin_type::UGE},
        {"SLT", builtin_type::SLT},
        {"SGT", builtin_type::SGT},
        {"SLE", builtin_type::SLE},
        {"SGE", builtin_type::SGE},
        {"EQ", builtin_type::EQ},
        {"NEQ", builtin_type::NEQ},
        {"CAST", builtin_type::CAST},
        {"TO", builtin_type::TO},
        {"REGISTER", builtin_type::REGISTER}
    };

    static void parse_builtin(const nlohmann::json &json, parse_context &context) {
        const auto &input = json["input"];
        const auto &output = json["output"];
        const std::string builtin_id = json["id"];
        if (!builtin_type_map.contains(builtin_id)) throw std::runtime_error("Unknown builtin id " + builtin_id);
        switch (builtin_type_map.at(builtin_id)) {
            case builtin_type::NOT: {
                context.circuit.add_rule(rule_factory::not_rule(parse_source(input[0], context),
                                                                parse_sink(output[0], context)));
                break;
            }
            case builtin_type::AND: {
                parse_variadic_binary(input, output, context, rule_factory::and_rule);
                break;
            }
            case builtin_type::OR: {
                parse_variadic_binary(input, output, context, rule_factory::or_rule);
                break;
            }
            case builtin_type::XOR: {
                parse_variadic_binary(input, output, context, rule_factory::xor_rule);
                break;
            }
            case builtin_type::SHL: {
                parse_binary(input, output, context, rule_factory::shl_rule);
                break;
            }
            case builtin_type::LSHR: {
                parse_binary(input, output, context, rule_factory::logical_shr_rule);
                break;
            }
            case builtin_type::ASHR: {
                parse_binary(input, output, context, rule_factory::arithmetic_shr_rule);
                break;
            }
            case builtin_type::ADD: {
                parse_variadic_binary(input, output, context, rule_factory::add_rule);
                break;
            }
            case builtin_type::SUB: {
                parse_variadic_binary(input, output, context, rule_factory::sub_rule, false);
                break;
            }
            case builtin_type::MUL: {
                parse_variadic_binary(input, output, context, rule_factory::mul_rule);
                break;
            }
            case builtin_type::DIV: {
                parse_variadic_binary(input, output, context, rule_factory::div_rule, false);
                break;
            }
            case builtin_type::MOD: {
                parse_variadic_binary(input, output, context, rule_factory::mod_rule, false);
                break;
            }
            case builtin_type::CONCAT: {
                parse_variadic_binary(input, output, context, rule_factory::concat_rule, true, true);
                break;
            }
            case builtin_type::ULT: {
                parse_binary(input, output, context, rule_factory::unsigned_lt_rule);
                break;
            }
            case builtin_type::UGT: {
                parse_binary(input, output, context, rule_factory::unsigned_gt_rule);
                break;
            }
            case builtin_type::ULE: {
                parse_binary(input, output, context, rule_factory::unsigned_le_rule);
                break;
            }
            case builtin_type::UGE: {
                parse_binary(input, output, context, rule_factory::unsigned_ge_rule);
                break;
            }
            case builtin_type::SLT: {
                parse_binary(input, output, context, rule_factory::signed_lt_rule);
                break;
            }
            case builtin_type::SGT: {
                parse_binary(input, output, context, rule_factory::signed_gt_rule);
                break;
            }
            case builtin_type::SLE: {
                parse_binary(input, output, context, rule_factory::signed_le_rule);
                break;
            }
            case builtin_type::SGE: {
                parse_binary(input, output, context, rule_factory::signed_ge_rule);
                break;
            }
            case builtin_type::EQ: {
                parse_binary(input, output, context, rule_factory::eq_rule);
                break;
            }
            case builtin_type::NEQ: {
                parse_binary(input, output, context, rule_factory::neq_rule);
                break;
            }
            case builtin_type::CAST: {
                context.circuit.add_rule(rule_factory::id_rule(parse_source(input[0], context),
                                                               data_factory::signed_resize_sink_adapter(
                                                                   parse_sink(output[0], context))));
                break;
            }
            case builtin_type::TO: {
                context.circuit.add_rule(rule_factory::id_rule(parse_source(input[0], context),
                                                               parse_sink(output[0], context)));
                break;
            }
            case builtin_type::REGISTER: {
                context.circuit.add_rule(rule_factory::register_rule(parse_clock(input[1], json["attribute"]),
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
        // NOLINT(*-no-recursion)
        const std::string member_type = json["type"];
        if (!member_type_map.contains(member_type)) throw std::runtime_error("Unknown member type " + member_type);
        if (json.contains("members"))
            for (const auto &member: json["members"])
                parse_member(member, context);
        switch (member_type_map.at(member_type)) {
            case member_type::WIRE:
            case member_type::CLOCK:
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

    static void parse_name_size(const nlohmann::json &json, parse_context &context) {
        const std::string member_type = json["type"];
        if (!member_type_map.contains(member_type)) throw std::runtime_error("Unknown member type " + member_type);
        if (json.contains("members"))
            for (const auto &member: json["members"])
                parse_name_size(member, context);
        switch (member_type_map.at(member_type)) {
            case member_type::WIRE: {
                auto [name, id, selection] = parse_id(json["id"]);
                if (selection) throw std::runtime_error("Wire member cannot be selected");
                context.name_map[name] = id;
                context.size_map[id] = json["width"];
                break;
            }
            case member_type::CLOCK: {
                auto [name, id, selection] = parse_id(json["id"]);
                if (selection) throw std::runtime_error("Clock member cannot be selected");
                context.name_map[name] = id;
                context.size_map[id] = 1;
                context.circuit.add_clock(id);
                if (context.circuit_input.contains(id)) context.circuit_input_clocks.insert(id);
                break;
            }
            case member_type::COMPONENT:
            case member_type::BUILTIN:
                break;
            default:
                throw std::runtime_error("Unknown member type");
        }
    }

    static void parse_circuit_io(const nlohmann::json &json, parse_context &context) {
        for (auto &input: json.at("input")) {
            auto [name, id, selection] = parse_id(input);
            if (selection) throw std::runtime_error("Design input cannot be selected");
            context.circuit_input.insert(id);
        }

        for (auto &output: json.at("output")) {
            auto [name, id, selection] = parse_id(output);
            if (selection) throw std::runtime_error("Design output cannot be selected");
            context.circuit_output.insert(id);
        }
    }

    parse_context parse_circuit(const nlohmann::json &json) {
        parse_context context;
        context.members_total = json["totalData"];
        auto &design = json["design"];
        parse_circuit_io(design, context);
        parse_name_size(design, context);
        parse_member(design, context);

        value_batch initial_values;
        for (id_t id = 0; id < context.members_total; ++id)
            initial_values[id] = value_t(context.size_map.at(id), 0ul);
        context.circuit.put_batch(initial_values);
        context.circuit.refresh_all();

        return context;
    }
}
