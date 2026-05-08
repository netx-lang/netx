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
#include "rule.h"
#include <map>
#include <utility>
#include <algorithm>
#include <deque>
#include <optional>

namespace nxon {
    struct parse_context_common {
        [[nodiscard]] id_t require_name(this auto &&self, const std::string &name) {
            if (const auto iter = self.name_map.find(name); iter != self.name_map.end()) {
                return iter->second;
            }
            throw std::out_of_range("Unknown wire name: " + name);
        }

        void advance_input_clocks(this auto &&self) {
            self.flip_input_clocks();
            self.flip_input_clocks();
        }

        [[nodiscard]] value_t get(this auto &&self, const std::string &name) {
            return self.iget(self.require_name(name));
        }

        void flip(this auto &&self, const std::string &name) {
            self.iflip(self.require_name(name));
        }

        void set(this auto &&self, const std::string &name, const value_t &value) {
            self.iset(self.require_name(name), value);
        }

        void flip_input_clocks(this auto &&self) {
            self.iflip_batch(self.circuit_input_clocks);
        }

        void stashed_flip(this auto &&self, const std::string &name) {
            self.stashed_iflip(self.require_name(name));
        }

        void stashed_set(this auto &&self, const std::string &name, const value_t &value) {
            self.stashed_iset(self.require_name(name), value);
        }

        size_t size(this auto &&self, const std::string &name) {
            return self.isize(self.require_name(name));
        }
    };

    struct trivial_cached_capability {
        void stashed_iflip(const id_t id) {
            if (flip_stash.contains(id)) flip_stash.erase(id);
            else flip_stash.insert(id);
        }

        void stashed_iflip_batch(const id_set &ids) {
            for (auto &id: ids)
                stashed_iflip(id);
        }

        void stashed_iset(const id_t id, const value_t &value) {
            set_stash.emplace(id, value);
        }

        void stashed_iset_batch(const value_batch &batch) {
            impl::insert_range_compat(set_stash, batch);
        }

        void apply_stash(this auto &&self) {
            if (!self.flip_stash.empty()) {
                self.iflip_batch(self.flip_stash);
                self.flip_stash.clear();
            }
            if (!self.set_stash.empty()) {
                self.iset_batch(self.set_stash);
                self.set_stash.clear();
            }
        }

    private:
        id_set flip_stash;
        value_batch set_stash;
    };

    struct component_hierarchy {
        std::vector<id_t> wires;
        std::vector<component_hierarchy> children;
    };

    struct parse_context : parse_context_common, trivial_cached_capability {
        id_t members_total = 0;
        nxon::circuit circuit;
        std::vector<id_t> circuit_input, circuit_output;
        id_set circuit_input_clocks;
        std::unordered_map<id_t, size_t> size_map;
        std::unordered_map<id_t, std::string> name_of_map;
        std::unordered_map<std::string, id_t> name_map;
        std::unordered_map<std::string, size_t> next_name_suffix;
        component_hierarchy root_hierarchy;
        component_hierarchy *cur_hierarchy = &root_hierarchy;

        void new_wire(const id_t id, const size_t size, std::string org_name) {
            size_map.emplace(id, size);
            if (name_map.contains(org_name)) {
                static constexpr char DELIMITER = '#';
                const auto base_name = org_name;
                auto &suffix = next_name_suffix[base_name];
                if (suffix == 0) suffix = 1;
                do {
                    org_name = std::format("{}{}{}", base_name, DELIMITER, suffix++);
                } while (name_map.contains(org_name));
            }
            name_of_map.emplace(id, org_name);
            name_map.emplace(org_name, id);
            cur_hierarchy->wires.emplace_back(id);
        }

        id_t new_tmp_wire(const size_t size, std::string org_name) {
            const auto id = members_total;
            ++members_total;
            new_wire(id, size, std::move(org_name));
            return id;
        }

        [[nodiscard]] value_t iget(const id_t id) const {
            return circuit.get_value(id);
        }

        void validate_value_size(const id_t id, const value_t &value) const {
            if (value.size() != isize(id)) throw std::invalid_argument("Sizes don't match");
        }

        void iflip(const id_t id) {
            circuit.flip_discard(id);
        }

        void iflip_batch(const id_set &ids) {
            circuit.flip_batch_discard(ids);
        }

        void iset(const id_t id, const value_t &value) {
            validate_value_size(id, value);
            circuit.update_discard(id, value);
        }

        void iset_batch(const value_batch &batch) {
            for (const auto &[id, value]: batch) validate_value_size(id, value);
            circuit.update_batch_discard(batch);
        }

        void stashed_iset(const id_t id, const value_t &value) {
            validate_value_size(id, value);
            trivial_cached_capability::stashed_iset(id, value);
        }

        void stashed_iset_batch(const value_batch &batch) {
            for (const auto &[id, value]: batch) validate_value_size(id, value);
            trivial_cached_capability::stashed_iset_batch(batch);
        }

        size_t isize(const id_t id) const {
            return size_map.at(id);
        }
    };

    using native_factory = std::function<rule_t(const parse_context & ctx, const nlohmann::json & json)>;

    using native_map = std::map<std::string, native_factory>;

    clock_t parse_clock(const std::string &source, bool is_pos_trigger);

    clock_t parse_clock(const std::string &source, const nlohmann::json &attribute);

    source_t parse_source(const std::string &source, const parse_context &context);

    sink_t parse_sink(const std::string &sink, const parse_context &context);

    void parse_circuit_json(parse_context &context, const nlohmann::json &json, const native_map &native_map = {});

    template<typename T>
    void parse_circuit(parse_context &context, T &&json_source, const native_map &native_map = {}) {
        parse_circuit_json(context, nlohmann::json::parse(std::forward<T>(json_source)), native_map);
    }

    template<>
    inline void parse_circuit(parse_context &context, nlohmann::json &json, const native_map &native_map) {
        parse_circuit_json(context, json, native_map);
    }

    template<>
    inline void parse_circuit(parse_context &context, const nlohmann::json &json, const native_map &native_map) {
        parse_circuit_json(context, json, native_map);
    }

    template<>
    inline void parse_circuit(parse_context &context, nlohmann::json &&json, const native_map &native_map) {
        parse_circuit_json(context, json, native_map);
    }

    template<typename T>
    parse_context parse_circuit(T &&json_source, const native_map &native_map = {}) {
        parse_context context;
        parse_circuit(context, std::forward<T>(json_source), native_map);
        return context;
    }

    namespace impl {
        struct id_info {
            std::string name;
            id_t id{};
            std::optional<std::pair<size_t, size_t> > selection;
        };

        inline id_info parse_id(const std::string &id) {
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

        inline void parse_binary(const nlohmann::json &inputs, const nlohmann::json &outputs, parse_context &context,
                                 rule_t (*cons)(const source_t &, const source_t &, const sink_t &)) {
            if (inputs.size() != 2) throw std::invalid_argument("Need exactly two inputs");
            if (outputs.size() != 1) throw std::invalid_argument("Need exactly one output");
            context.circuit.add_rule(cons(parse_source(inputs.at(0), context), parse_source(inputs.at(1), context),
                                          parse_sink(outputs.at(0), context)));
        }

        inline size_t binary_output_len(const source_t &a, const source_t &b, const bool len_sum) {
            return len_sum ? a.len() + b.len() : a.len();;
        }

        inline source_t binary_combine(rule_t(*cons)(const source_t &, const source_t &, const sink_t &),
                                       const source_t &a, const source_t &b,
                                       parse_context &context, const bool len_sum) {
            const auto new_len = binary_output_len(a, b, len_sum);
            const auto new_id = context.new_tmp_wire(new_len, "binary_mid");
            context.circuit.add_rule(cons(a, b, data_factory::id_sink(new_id, new_len)));

            return data_factory::id_source(new_id, new_len);
        }

        inline void parse_variadic_binary(const nlohmann::json &inputs, const nlohmann::json &outputs,
                                          parse_context &context,
                                          rule_t (*cons)(const source_t &, const source_t &, const sink_t &),
                                          const bool assoc = true, const bool len_sum = false,
                                          rule_t (*last)(const source_t &, const sink_t &) = nullptr) {
            if (outputs.size() != 1) throw std::invalid_argument("Need exactly one output");
            sink_t output = parse_sink(outputs.at(0), context);

            std::deque<source_t> sources;
            for (auto &input: inputs) sources.push_back(parse_source(input, context));
            if (sources.empty()) throw std::invalid_argument("No sources specified");
            if (sources.size() == 1) {
                context.circuit.add_rule(rule_t::make_unary<&impl::op_id>(sources.front(), output));
                return;
            }

            if (assoc) {
                std::deque<source_t> next_sources;
                for (size_t left = sources.size(); left > 2; --left) {
                    if (sources.size() < 2) {
                        while (!sources.empty()) {
                            next_sources.push_back(sources.front());
                            sources.pop_front();
                        }
                        sources.swap(next_sources);
                    }

                    auto src1 = sources.front();
                    sources.pop_front();
                    auto src2 = sources.front();
                    sources.pop_front();
                    next_sources.push_back(binary_combine(cons, src1, src2, context, len_sum));
                }

                while (!sources.empty()) {
                    next_sources.push_back(sources.front());
                    sources.pop_front();
                }
                sources.swap(next_sources);
            } else {
                while (sources.size() > 2) {
                    auto src1 = sources.front();
                    sources.pop_front();
                    auto src2 = sources.front();
                    sources.pop_front();
                    sources.push_front(binary_combine(cons, src1, src2, context, len_sum));
                }
            }
            const auto src1 = sources.front();
            sources.pop_front();
            const auto src2 = sources.front();
            sources.pop_front();
            if (last == nullptr) {
                context.circuit.add_rule(cons(src1, src2, output));
            } else {
                context.circuit.add_rule(last(binary_combine(cons, src1, src2, context, len_sum), output));
            }
        }

        enum class builtin_type {
            NOT, AND, NAND, OR, NOR, XOR, SHL, LSHR, ASHR, ADD, SUB, MUL, DIV, MOD, CONCAT, ULT, UGT, ULE, UGE, SLT,
            SGT, SLE, SGE,
            EQ,
            NE, UCAST, CAST, REGISTER, ASYNC_REGISTER, TO
        };

        const std::map<std::string, builtin_type> builtin_type_map{
            {"NOT", builtin_type::NOT},
            {"AND", builtin_type::AND},
            {"NAND", builtin_type::NAND},
            {"OR", builtin_type::OR},
            {"NOR", builtin_type::NOR},
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
            {"NE", builtin_type::NE},
            {"CAST", builtin_type::CAST},
            {"UCAST", builtin_type::UCAST},
            {"TO", builtin_type::TO},
            {"REGISTER", builtin_type::REGISTER},
            {"ASYNC_REGISTER", builtin_type::ASYNC_REGISTER}
        };

        inline void parse_builtin(const nlohmann::json &json, parse_context &context) {
            const auto &input = json["input"];
            const auto &output = json["output"];
            const std::string builtin_id = json["id"];
            if (!builtin_type_map.contains(builtin_id)) throw std::runtime_error("Unknown builtin id " + builtin_id);
            switch (builtin_type_map.at(builtin_id)) {
                case builtin_type::NOT: {
                    context.circuit.add_rule(rule_t::make_unary<&impl::op_not>(parse_source(input[0], context),
                                                                               parse_sink(output[0], context)));
                    break;
                }
                case builtin_type::AND: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_and>);
                    break;
                }
                case builtin_type::NAND: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_and>, true, false,
                                          rule_t::make_unary<&impl::op_not>);
                    break;
                }
                case builtin_type::OR: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_or>);
                    break;
                }
                case builtin_type::NOR: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_or>, true, false,
                                          rule_t::make_unary<&impl::op_not>);
                    break;
                }
                case builtin_type::XOR: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_xor>);
                    break;
                }
                case builtin_type::SHL: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_shl>);
                    break;
                }
                case builtin_type::LSHR: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_lshr>);
                    break;
                }
                case builtin_type::ASHR: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_ashr>);
                    break;
                }
                case builtin_type::ADD: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_add>);
                    break;
                }
                case builtin_type::SUB: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_sub>, false);
                    break;
                }
                case builtin_type::MUL: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_mul>);
                    break;
                }
                case builtin_type::DIV: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_div>, false);
                    break;
                }
                case builtin_type::MOD: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_mod>, false);
                    break;
                }
                case builtin_type::CONCAT: {
                    parse_variadic_binary(input, output, context, rule_t::make_binary<&impl::op_concat>, true, true);
                    break;
                }
                case builtin_type::ULT: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_ult>);
                    break;
                }
                case builtin_type::UGT: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_ugt>);
                    break;
                }
                case builtin_type::ULE: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_ule>);
                    break;
                }
                case builtin_type::UGE: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_uge>);
                    break;
                }
                case builtin_type::SLT: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_slt>);
                    break;
                }
                case builtin_type::SGT: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_sgt>);
                    break;
                }
                case builtin_type::SLE: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_sle>);
                    break;
                }
                case builtin_type::SGE: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_sge>);
                    break;
                }
                case builtin_type::EQ: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_eq>);
                    break;
                }
                case builtin_type::NE: {
                    parse_binary(input, output, context, rule_t::make_binary<&impl::op_neq>);
                    break;
                }
                case builtin_type::CAST: {
                    context.circuit.add_rule(rule_t::make_unary<&impl::op_id>(
                            parse_source(input[0], context),
                            data_factory::signed_resize_sink_adapter(parse_sink(output[0], context)))
                    );
                    break;
                }
                case builtin_type::UCAST: {
                    context.circuit.add_rule(rule_t::make_unary<&impl::op_id>(
                            parse_source(input[0], context),
                            data_factory::unsigned_resize_sink_adapter(parse_sink(output[0], context)))
                    );
                    break;
                }
                case builtin_type::TO: {
                    context.circuit.add_rule(rule_t::make_unary<&impl::op_id>(
                            parse_source(input[0], context),
                            parse_sink(output[0], context))
                    );
                    break;
                }
                case builtin_type::REGISTER: {
                    context.circuit.add_rule(rule_t::make_register(
                        parse_clock(input[0], json["attribute"]),
                        parse_source(input[1], context),
                        parse_sink(output[0], context)));
                    break;
                }
                case builtin_type::ASYNC_REGISTER: {
                    context.circuit.add_rule(rule_t::make_async_register(
                        parse_clock(input[0], json["attribute"]),
                        parse_source(input[1], context),
                        parse_source(input[2], context),
                        parse_sink(output[0], context),
                        json["attribute"]["high_rst"] == "true",
                        value_t::parse(json["attribute"]["rst_value"])));
                    break;
                }
                default:
                    throw std::runtime_error("Unknown builtin id");
            }
        }

        enum class member_type {
            WIRE, CLOCK, COMPONENT, BUILTIN
        };

        const std::map<std::string, member_type> member_type_map{
            {"wire", member_type::WIRE},
            {"clock", member_type::CLOCK},
            {"component", member_type::COMPONENT},
            {"builtin", member_type::BUILTIN}
        };

        inline void parse_member(const nlohmann::json &json, parse_context &context, const native_map &native_map) { // NOLINT(*-no-recursion)
            const std::string type = json["type"];
            if (!member_type_map.contains(type)) throw std::runtime_error("Unknown member type " + type);

            switch (member_type_map.at(type)) {
                case member_type::WIRE:
                case member_type::CLOCK: break;
                case member_type::COMPONENT: {
                    if (native_map.contains(json["id"])) {
                        const auto &factory = native_map.at(json["id"]);
                        context.circuit.add_rule(factory(context, json));
                    } else if (json.contains("members")) {
                        for (size_t i = 0; const auto &member: json["members"]) {
                            if (member.contains("members")) {
                                const auto cur_hierarchy = context.cur_hierarchy;
                                context.cur_hierarchy = &cur_hierarchy->children.at(i);
                                parse_member(member, context, native_map);
                                context.cur_hierarchy = cur_hierarchy;
                                ++i;
                            } else
                                parse_member(member, context, native_map);
                        }
                    }
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

        inline void parse_name_size(const nlohmann::json &json, parse_context &context) { // NOLINT(*-no-recursion)
            const std::string member_type = json["type"];
            if (!member_type_map.contains(member_type)) throw std::runtime_error("Unknown member type " + member_type);
            if (json.contains("members")) {
                const auto cur_hierarchy = context.cur_hierarchy;
                component_hierarchy sub_hierarchy;
                context.cur_hierarchy = &sub_hierarchy;
                for (const auto &member: json["members"])
                    parse_name_size(member, context);
                context.cur_hierarchy = cur_hierarchy;
                cur_hierarchy->children.emplace_back(std::move(sub_hierarchy));
            }
            switch (member_type_map.at(member_type)) {
                case member_type::WIRE: {
                    auto [name, id, selection] = parse_id(json["id"]);
                    if (selection) throw std::runtime_error("Wire member cannot be selected");
                    context.new_wire(id, json["width"], name);
                    break;
                }
                case member_type::CLOCK: {
                    auto [name, id, selection] = parse_id(json["id"]);
                    if (selection) throw std::runtime_error("Clock member cannot be selected");
                    context.new_wire(id, 1, name);
                    context.circuit.add_clock(id);
                    if (std::ranges::contains(context.circuit_input, id)) {
                        context.circuit_input_clocks.insert(id);
                    }
                    break;
                }
                case member_type::COMPONENT:
                case member_type::BUILTIN:
                    break;
                default:
                    throw std::runtime_error("Unknown member type");
            }
        }

        inline void parse_circuit_io(const nlohmann::json &json, parse_context &context) {
            for (auto &input: json.at("input")) {
                auto [name, id, selection] = parse_id(input);
                if (selection) throw std::runtime_error("Design input cannot be selected");
                context.circuit_input.push_back(id);
            }

            for (auto &output: json.at("output")) {
                auto [name, id, selection] = parse_id(output);
                if (selection) throw std::runtime_error("Design output cannot be selected");
                context.circuit_output.push_back(id);
            }
        }

        inline void parse_circuit_io_alias(parse_context &context, const nlohmann::json &json) {
            auto input_iter = context.circuit_input.begin();
            for (auto &top_input: json.at("top_input")) {
                context.name_map.emplace(top_input, *input_iter++);
            }
            auto output_iter = context.circuit_output.begin();
            for (auto &top_output: json.at("top_output")) {
                context.name_map.emplace(top_output, *output_iter++);
            }
        }
    }

    inline clock_t parse_clock(const std::string &source, const bool is_pos_trigger) {
        auto [name, id, selection] = impl::parse_id(source);
        if (selection) throw std::invalid_argument("Clock cannot be selected");
        return data_factory::id_clock(id, is_pos_trigger);
    }

    inline clock_t parse_clock(const std::string &source, const nlohmann::json &attribute) {
        return parse_clock(source, attribute["pos_edge"] == "true");
    }

    inline source_t parse_source(const std::string &source, const parse_context &context) {
        if (source.find('@') != std::string::npos) {
            auto [name, id, selection] = impl::parse_id(source);
            if (selection) return data_factory::select_source(id, selection->first, selection->second);
            return data_factory::id_source(id, context.size_map.at(id));
        }
        return data_factory::constant_source(value_t::parse(source));
    }

    inline sink_t parse_sink(const std::string &sink, const parse_context &context) {
        auto [name, id, selection] = impl::parse_id(sink);
        if (selection) return data_factory::select_sink(id, selection->first, selection->second);
        return data_factory::id_sink(id, context.size_map.at(id));
    }

    inline void parse_circuit_json(parse_context &context, const nlohmann::json &json, const native_map &native_map) {
        context.members_total = json["total"];
        auto &design = json["design"];
        impl::parse_circuit_io(design, context);
        impl::parse_circuit_io_alias(context, json);
        impl::parse_name_size(design, context);
        context.cur_hierarchy = &context.root_hierarchy.children.at(0);
        impl::parse_member(design, context, native_map);
        context.cur_hierarchy = &context.root_hierarchy;

        value_batch initial_values;
        for (id_t id = 0; id < context.members_total; ++id)
            initial_values[id] = value_t(context.size_map.at(id), 0ul);
        context.circuit.put_batch(initial_values);
        context.circuit.run_all_discard();
    }

    namespace native_parse {
        struct io_view {
            const parse_context &ctx;
            const nlohmann::json &input;
            const nlohmann::json &output;
            const nlohmann::json *attribute = nullptr;

            [[nodiscard]] const std::string &in(const size_t index) const {
                return input.at(index).get_ref<const std::string &>();
            }

            [[nodiscard]] const std::string &out(const size_t index) const {
                return output.at(index).get_ref<const std::string &>();
            }

            [[nodiscard]] source_t source_in(const size_t index) const {
                return parse_source(in(index), ctx);
            }

            [[nodiscard]] sink_t sink_out(const size_t index) const {
                return parse_sink(out(index), ctx);
            }

            [[nodiscard]] clock_t pos_clock_in(const size_t index) const {
                return parse_clock(in(index), true);
            }

            [[nodiscard]] clock_t neg_clock_in(const size_t index) const {
                return parse_clock(in(index), false);
            }

            [[nodiscard]] clock_t attr_clock_in(const size_t index) const {
                if (attribute == nullptr) throw std::invalid_argument("Missing attribute for clock parsing");
                return parse_clock(in(index), *attribute);
            }
        };

        inline io_view io(const parse_context &ctx, const nlohmann::json &json) {
            return io_view{
                .ctx = ctx,
                .input = json.at("input"),
                .output = json.at("output"),
                .attribute = json.contains("attribute") ? &json.at("attribute") : nullptr
            };
        }

        template<size_t Index>
        struct pos_clock_in_t {
            [[nodiscard]] clock_t operator()(const io_view &view) const {
                return view.pos_clock_in(Index);
            }
        };

        template<size_t Index>
        struct neg_clock_in_t {
            [[nodiscard]] clock_t operator()(const io_view &view) const {
                return view.neg_clock_in(Index);
            }
        };

        template<size_t Index>
        struct attr_clock_in_t {
            [[nodiscard]] clock_t operator()(const io_view &view) const {
                return view.attr_clock_in(Index);
            }
        };

        template<size_t Index>
        struct source_in_t {
            [[nodiscard]] source_t operator()(const io_view &view) const {
                return view.source_in(Index);
            }
        };

        template<size_t Index>
        struct sink_out_t {
            [[nodiscard]] sink_t operator()(const io_view &view) const {
                return view.sink_out(Index);
            }
        };

        template<size_t Index>
        inline constexpr pos_clock_in_t<Index> pos_clock_in{};

        template<size_t Index>
        inline constexpr neg_clock_in_t<Index> neg_clock_in{};

        template<size_t Index>
        inline constexpr attr_clock_in_t<Index> attr_clock_in{};

        template<size_t Index>
        inline constexpr source_in_t<Index> source_in{};

        template<size_t Index>
        inline constexpr sink_out_t<Index> sink_out{};

        template<typename Rule, typename... Specs>
        [[nodiscard]] rule_t make_rule(const parse_context &ctx, const nlohmann::json &json, Specs... specs) {
            const auto view = io(ctx, json);
            return rule_t{new Rule(specs(view)...)};
        }
    }

    using native_parse::make_rule;

    template<size_t Index>
    inline constexpr auto pos_clock_in = native_parse::pos_clock_in<Index>;

    template<size_t Index>
    inline constexpr auto neg_clock_in = native_parse::neg_clock_in<Index>;

    template<size_t Index>
    inline constexpr auto attr_clock_in = native_parse::attr_clock_in<Index>;

    template<size_t Index>
    inline constexpr auto source_in = native_parse::source_in<Index>;

    template<size_t Index>
    inline constexpr auto sink_out = native_parse::sink_out<Index>;
}
#endif //PARSER_H
