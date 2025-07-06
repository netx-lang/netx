//
// Created by czm233 on 12/11/24.
//

#ifndef RULE_H
#define RULE_H

#include "circuit_def.h"
#include <version>
#include <memory>
#include <utility>
#include <algorithm>
#include <set>
#include <variant>
#include <vector>

namespace nxon {
    namespace impl {
        template<typename T>
        class potential_holder {
            using const_reference = const T &;

            std::variant<const T *, T, std::monostate> value;

            [[no_unique_address]] struct {
                const_reference operator()(const T *ptr) const {
                    return *ptr;
                }

                const_reference operator()(const T &ref) const {
                    return ref;
                }

                [[noreturn]] const_reference operator()(std::monostate) const {
                    throw std::runtime_error("potential_holder does not have a value");
                }
            } get_ref_visitor;

            [[no_unique_address]] struct {
                T operator()(T &ref) const {
                    return std::move(ref);
                }

                T operator()(const T *ptr) const {
                    return *ptr;
                }

                [[noreturn]] T operator()(std::monostate) const {
                    throw std::runtime_error("potential_holder does not have a value");
                }
            } move_out_visitor;

        public:
            explicit potential_holder(const T &v) : value(&v) {
            }

            explicit potential_holder(T &&v) : value(std::in_place_type<T>, std::move(v)) {
            }

            potential_holder(const potential_holder &) = delete;

            potential_holder &operator=(const potential_holder &) = delete;

            potential_holder(potential_holder &&) = default;

            potential_holder &operator=(potential_holder &&) = default;

            decltype(auto) operator*() const {
                return std::visit(get_ref_visitor, value);
            }

            decltype(auto) operator->() const {
                return &operator*();
            }

            T release() {
                auto result = std::visit(move_out_visitor, value);
                value = std::monostate{};
                return result;
            }
        };

        using indirect_id_set = potential_holder<id_set>;

        struct rule_impl {
            const id_set triggers;
            const id_set dependencies;
            const id_set outcomes;

            explicit rule_impl(id_set triggers, id_set outcomes) : triggers(triggers),
                                                                   dependencies(std::move(triggers)),
                                                                   outcomes(std::move(outcomes)) {
            }

            explicit rule_impl(id_set triggers, id_set dependencies, id_set outcomes) : triggers(std::move(triggers)),
                dependencies(std::move(dependencies)), outcomes(std::move(outcomes)) {
            }

            virtual ~rule_impl() = default;

            virtual indirect_id_set perform(value_storage &values) const = 0;
        };

        struct clock_impl {
            const id_set dependencies;

            explicit clock_impl(id_set dependencies) : dependencies(std::move(dependencies)) {
            }

            virtual ~clock_impl() = default;

            virtual bool triggered(value_storage &values) const = 0;
        };

        using indirect_value_t = potential_holder<value_t>;

        struct source_impl {
            const id_set dependencies;
            const size_t len;

            explicit source_impl(id_set dependencies, const size_t len) : dependencies(std::move(dependencies)),
                                                                          len(len) {
            }

            virtual ~source_impl() = default;

            virtual indirect_value_t get(value_storage &values) const = 0;
        };

        struct sink_impl {
            const id_set outcomes;
            const size_t len;

            explicit sink_impl(id_set outcomes, const size_t len) : outcomes(std::move(outcomes)), len(len) {
            }

            virtual ~sink_impl() = default;

            [[nodiscard]] virtual bool check(const value_storage &values, const value_t &v) const = 0;

            virtual void put(value_storage &values, value_t v) const = 0;
        };
    }

    class rule_t : public std::shared_ptr<impl::rule_impl> {
        using base_t = shared_ptr;

    public:
        using base_t::base_t;

        [[nodiscard]] auto &triggers() const {
            return get()->triggers;
        }

        [[nodiscard]] auto &dependencies() const {
            return get()->dependencies;
        }

        [[nodiscard]] auto &outcomes() const {
            return get()->outcomes;
        }

        auto perform(value_storage &values) const {
            return get()->perform(values);
        }
    };

    class clock_t : public std::shared_ptr<impl::clock_impl> {
        using base_t = shared_ptr;

    public:
        using base_t::base_t;

        [[nodiscard]] const id_set &dependencies() const {
            return get()->dependencies;
        }

        bool triggered(value_storage &values) const {
            return get()->triggered(values);
        }
    };

    class source_t : public std::shared_ptr<impl::source_impl> {
        using base_t = shared_ptr;

    public:
        using base_t::base_t;

        [[nodiscard]] const id_set &dependencies() const {
            return shared_ptr::get()->dependencies;
        }

        [[nodiscard]] size_t len() const {
            return shared_ptr::get()->len;
        }

        auto get(value_storage &values) const {
            return shared_ptr::get()->get(values);
        }
    };

    class sink_t : public std::shared_ptr<impl::sink_impl> {
        using base_t = shared_ptr;

    public:
        using base_t::base_t;

        [[nodiscard]] const id_set &outcomes() const {
            return get()->outcomes;
        }

        [[nodiscard]] size_t len() const {
            return get()->len;
        }

        [[nodiscard]] bool check(const value_storage &values, const value_t &v) const {
            return get()->check(values, v);
        }

        void put(value_storage &values, value_t v) const {
            get()->put(values, std::move(v));
        }
    };

    namespace data_factory {
        [[nodiscard]] clock_t id_clock(id_t id, bool pos_edge = true);

        [[nodiscard]] source_t id_source(id_t id, size_t len);

        [[nodiscard]] sink_t id_sink(id_t id, size_t len);

        [[nodiscard]] source_t select_source(id_t id, size_t from, size_t to);

        [[nodiscard]] sink_t select_sink(id_t id, size_t from, size_t to);

        [[nodiscard]] source_t constant_source(value_t value);

        [[nodiscard]] sink_t signed_resize_sink_adapter(sink_t sink);
    }

    namespace rule_factory {
        [[nodiscard]] rule_t id_rule(source_t in, sink_t out);

        [[nodiscard]] rule_t not_rule(source_t in, sink_t out);

        [[nodiscard]] rule_t and_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t or_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t xor_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t shl_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t logical_shr_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t arithmetic_shr_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t add_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t sub_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t mul_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t div_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t mod_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t concat_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t unsigned_lt_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t unsigned_gt_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t unsigned_le_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t unsigned_ge_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t signed_lt_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t signed_gt_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t signed_le_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t signed_ge_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t eq_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t neq_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t register_rule(clock_t clk, source_t in, sink_t out);
    }

    using rule_storage = impl::id_based_map<std::vector<rule_t> >;

    class rule_set {
        std::set<rule_t> all_rules;
        rule_storage trigger_rules;

        void collect_rules(std::vector<rule_t> &to_perform, const id_t id) const {
            if (!trigger_rules.contains(id)) return;
            for (auto &rule: trigger_rules[id])
                to_perform.emplace_back(rule);
        }

        id_set perform_rules(value_storage &values, std::vector<rule_t> to_perform, const bool to_fixed = true) const {
            std::vector<id_t> changed;

            std::vector<id_t> to_consider, to_consider_swap;
            do {
                std::ranges::sort(to_consider);
                auto [last, end] = std::ranges::unique(to_consider);
                to_consider.erase(last, end);

                for (const id_t id: to_consider) collect_rules(to_perform, id);

                for (const auto &rule: to_perform) {
                    const auto rule_changed = rule.perform(values);
#ifdef __cpp_lib_containers_ranges
                    to_consider_swap.append_range(*rule_changed);
                    changed.append_range(*rule_changed);
#else
                    to_consider_swap.insert(to_consider_swap.end(), rule_changed->begin(), rule_changed->end());
                    changed.insert(changed.end(), rule_changed->begin(), rule_changed->end());
#endif
                }
                values.perform_delayed();

                to_consider.clear();
                to_consider.swap(to_consider_swap);
                to_perform.clear();
            } while (to_fixed && !to_consider.empty());

            return id_set{changed.begin(), changed.end()};
        }

    public:
        void add(const rule_t &new_rule) {
            all_rules.insert(new_rule);
            for (const id_t trigger: new_rule.triggers())
                trigger_rules[trigger].emplace_back(new_rule);
        }

        id_set update(value_storage &values, const id_set &hint, const bool to_fixed = true) const {
            std::vector<rule_t> to_perform;
            for (const id_t id: hint) collect_rules(to_perform, id);
            return perform_rules(values, std::move(to_perform), to_fixed);
        }

        id_set refresh_rules(value_storage &values, const std::set<rule_t> &to_update,
                             const bool to_fixed = true) const {
            return perform_rules(values, std::vector(to_update.begin(), to_update.end()), to_fixed);
        }

        id_set refresh_all_rules(value_storage &values, const bool to_fixed = true) const {
            return refresh_rules(values, all_rules, to_fixed);
        }

        [[nodiscard]] std::set<rule_t> rules() const {
            return all_rules;
        }
    };
}

#endif //RULE_H
