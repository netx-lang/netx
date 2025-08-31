//
// Created by czm233 on 12/11/24.
//

#ifndef RULE_H
#define RULE_H

#include "circuit_def.h"
#include <version>
#include <atomic>
#include <memory>
#include <utility>
#include <algorithm>
#include <set>
#include <variant>
#include <vector>
#include <boost/lambda2.hpp>
#include <functional>

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

    namespace impl {
        struct id_clock final : clock_impl {
            const id_t id;
            const bool pos_edge;

            explicit id_clock(id_t id, const bool pos_edge)
                : clock_impl({id}), id(id), pos_edge(pos_edge) {
            }

            bool triggered(value_storage &values) const override {
                const auto &clk = values[id];
                assert(!clk.empty());
                if (pos_edge) return static_cast<bool>(clk);
                return !static_cast<bool>(clk);
            }
        };

        struct id_source final : source_impl {
            const id_t id;

            explicit id_source(id_t id, const size_t len)
                : source_impl({id}, len), id(id) {
            }

            indirect_value_t get(value_storage &values) const override {
                return indirect_value_t(values[id]);
            }
        };

        struct constant_source final : source_impl {
            const value_t value;

            explicit constant_source(value_t value) : source_impl({}, value.size()), value(std::move(value)) {
            }

            indirect_value_t get(value_storage &) const override {
                return indirect_value_t{value};
            }
        };

        struct id_sink final : sink_impl {
            const id_t id;

            explicit id_sink(id_t id, const size_t len)
                : sink_impl({id}, len), id(id) {
            }

            [[nodiscard]] bool check(const value_storage &values, const value_t &v) const override {
                assert(len == v.size());
                return values.at(id) != v;
            }

            void put(value_storage &values, value_t v) const override {
                assert(len == v.size());
                values[id] = std::move(v);
            }
        };

        struct select_source final : source_impl {
            const id_t id;
            const size_t from;
            const size_t to;
            const size_t len;

            /**
             *
             * @param id Input id
             * @param from From index (inclusive)
             * @param to To index (inclusive)
             */
            select_source(id_t id, const size_t from, const size_t to)
                : source_impl({id}, to - from + 1), id(id),
                  from((assert(from <= to), from)), to(to), len(to - from + 1) {
            }

            indirect_value_t get(value_storage &values) const override {
                return indirect_value_t(values[id].slice(from, to).from_slice());
            }
        };

        struct select_sink final : sink_impl {
            const id_t id;
            const size_t from;
            const size_t to;
            const size_t len;

            /**
             *
             * @param id Output id
             * @param from From index (inclusive)
             * @param to To index (inclusive)
             */
            select_sink(id_t id, const size_t from, const size_t to)
                : sink_impl({id}, to - from + 1), id(id),
                  from((assert(from <= to), from)), to(to), len(to - from + 1) {
            }

            [[nodiscard]] bool check(const value_storage &values, const value_t &v) const override {
                assert(len == v.size());
                const auto out = values.at(id).slice(from, to);
                return out.from_slice() != v;
            }

            void put(value_storage &values, value_t v) const override {
                assert(len == v.size());
                values[id].slice(from, to) = std::move(v);
            }
        };

        struct signed_resize_sink_adapter final : sink_impl {
            const sink_t underlying;

            explicit signed_resize_sink_adapter(sink_t underlying)
                : sink_impl(underlying.outcomes(), underlying.len()), underlying(std::move(underlying)) {
            }

            [[nodiscard]] bool check(const value_storage &values, const value_t &v) const override {
                return underlying.check(values, v.signed_resize(len));
            }

            void put(value_storage &values, const value_t v) const override {
                underlying.put(values, v.signed_resize(len));
            }
        };

        struct unsigned_resize_sink_adapter final : sink_impl {
            const sink_t underlying;

            explicit unsigned_resize_sink_adapter(sink_t underlying)
                : sink_impl(underlying.outcomes(), underlying.len()), underlying(std::move(underlying)) {
            }

            [[nodiscard]] bool check(const value_storage &values, const value_t &v) const override {
                return underlying.check(values, v.unsigned_resize(len));
            }

            void put(value_storage &values, const value_t v) const override {
                underlying.put(values, v.unsigned_resize(len));
            }
        };
    }

    namespace data_factory {
        [[nodiscard]] inline clock_t id_clock(const id_t id, const bool pos_edge) {
            return clock_t{new impl::id_clock(id, pos_edge)};
        }

        [[nodiscard]] inline source_t id_source(const id_t id, const size_t len) {
            return source_t{new impl::id_source(id, len)};
        }

        [[nodiscard]] inline sink_t id_sink(const id_t id, const size_t len) {
            return sink_t{new impl::id_sink(id, len)};
        }

        [[nodiscard]] inline source_t select_source(const id_t id, const size_t from, const size_t to) {
            return source_t{new impl::select_source(id, from, to)};
        }

        [[nodiscard]] inline sink_t select_sink(const id_t id, const size_t from, const size_t to) {
            return sink_t{new impl::select_sink(id, from, to)};
        }

        [[nodiscard]] inline source_t constant_source(value_t value) {
            return source_t{new impl::constant_source(std::move(value))};
        }

        [[nodiscard]] inline sink_t signed_resize_sink_adapter(sink_t sink) {
            return sink_t{new impl::signed_resize_sink_adapter(std::move(sink))};
        }

        [[nodiscard]] inline sink_t unsigned_resize_sink_adapter(sink_t sink) {
            return sink_t{new impl::unsigned_resize_sink_adapter(std::move(sink))};
        }
    }

    namespace impl {
        const id_set EMPTY_ID_SET;

        struct binary_rule final : rule_impl {
            const source_t a;
            const source_t b;
            const sink_t out;
            const std::function<value_t(const value_t &, const value_t &)> op;

            binary_rule(source_t a, source_t b, sink_t out,
                        std::function<value_t(const value_t &, const value_t &)> op)
                : rule_impl(a.dependencies() + b.dependencies(), out.outcomes()), a(std::move(a)), b(std::move(b)),
                  out(std::move(out)), op(std::move(op)) {
            }

            indirect_id_set perform(value_storage &values) const override {
                const auto x = a.get(values);
                const auto y = b.get(values);
                if (auto value = op(*x, *y); out.check(values, value)) {
                    out.put(values, std::move(value));
                    return indirect_id_set(out.outcomes());
                }
                return indirect_id_set(EMPTY_ID_SET);
            }
        };

        struct unary_rule final : rule_impl {
            const source_t in;
            const sink_t out;
            const std::function<value_t(const value_t &)> op;

            unary_rule(source_t x, sink_t out,
                       std::function<value_t(const value_t &)> op)
                : rule_impl(x.dependencies(), out.outcomes()), in(std::move(x)), out(std::move(out)),
                  op(std::move(op)) {
            }

            indirect_id_set perform(value_storage &values) const override {
                const auto x = in.get(values);
                if (auto value = op(*x); out.check(values, value)) {
                    out.put(values, std::move(value));
                    return indirect_id_set(out.outcomes());
                }
                return indirect_id_set(EMPTY_ID_SET);
            }
        };

        struct register_rule final : rule_impl {
            const clock_t clk;
            const source_t in;
            const sink_t out;

            register_rule(clock_t clk, source_t in, sink_t out)
                : rule_impl(clk.dependencies(), clk.dependencies() + in.dependencies(), out.outcomes()),
                  clk(std::move(clk)),
                  in(std::move(in)), out(std::move(out)) {
            }

            indirect_id_set perform(value_storage &values) const override {
                if (auto value = in.get(values); clk.triggered(values) && out.check(values, *value)) {
                    values.delay([value = value.release(), out = out](auto &cur_values) {
                        out.put(cur_values, std::move(value));
                    });
                    return indirect_id_set(out.outcomes());
                }
                return indirect_id_set(EMPTY_ID_SET);
            }
        };

        struct async_register_rule final : rule_impl {
            const clock_t clk;
            const source_t rst;
            const source_t in;
            const sink_t out;

            const bool high_rst;
            const value_t rst_value;

            async_register_rule(clock_t clk, source_t rst, source_t in, sink_t out, const bool high_rst,
                                value_t rst_value)
                : rule_impl(clk.dependencies(), clk.dependencies() + rst.dependencies() + in.dependencies(),
                            out.outcomes()),
                  clk(std::move(clk)), rst(std::move(rst)),
                  in(std::move(in)), out(std::move(out)),
                  high_rst(high_rst), rst_value(std::move(rst_value)) {
            }

            indirect_id_set perform(value_storage &values) const override {
                if (const auto rst_v = rst.get(values); static_cast<bool>(*rst_v) == high_rst && out.check(
                                                            values, rst_value)) {
                    out.put(values, rst_value);
                    return indirect_id_set(out.outcomes());
                }

                if (auto value = in.get(values); clk.triggered(values) && out.check(values, *value)) {
                    values.delay([value = value.release(), out = out](auto &cur_values) {
                        out.put(cur_values, std::move(value));
                    });
                    return indirect_id_set(out.outcomes());
                }

                return indirect_id_set(EMPTY_ID_SET);
            }
        };
    }

    namespace rule_factory {
        using boost::lambda2::_1;
        using boost::lambda2::_2;

        [[nodiscard]] inline rule_t id_rule(source_t in, sink_t out) {
            return rule_t{new impl::unary_rule(std::move(in), std::move(out), _1)};
        }

        [[nodiscard]] inline rule_t not_rule(source_t in, sink_t out) {
            return rule_t{new impl::unary_rule(std::move(in), std::move(out), ~_1)};
        }

        [[nodiscard]] inline rule_t and_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 & _2)};
        }

        [[nodiscard]] inline rule_t or_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 | _2)};
        }

        [[nodiscard]] inline rule_t xor_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 ^ _2)};
        }

        [[nodiscard]] inline rule_t shl_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x << static_cast<size_t>(y);
                })
            };
        }

        [[nodiscard]] inline rule_t logical_shr_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x >> static_cast<size_t>(y);
                })
            };
        }

        [[nodiscard]] inline rule_t arithmetic_shr_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x.arithmetic_shr(static_cast<size_t>(y));
                })
            };
        }

        [[nodiscard]] inline rule_t add_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 + _2)};
        }

        [[nodiscard]] inline rule_t sub_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 - _2)};
        }

        [[nodiscard]] inline rule_t mul_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 * _2)};
        }

        [[nodiscard]] inline rule_t div_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 / _2)};
        }

        [[nodiscard]] inline rule_t mod_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 % _2)};
        }

        [[nodiscard]] inline rule_t concat_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x.concat(y);
                })
            };
        }

        [[nodiscard]] inline rule_t unsigned_lt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x < y};
                })
            };
        }

        [[nodiscard]] inline rule_t unsigned_gt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x > y};
                })
            };
        }

        [[nodiscard]] inline rule_t unsigned_le_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x <= y};
                })
            };
        }

        [[nodiscard]] inline rule_t unsigned_ge_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x >= y};
                })
            };
        }

        [[nodiscard]] inline rule_t signed_lt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x.signed_compare(y) == std::strong_ordering::less};
                })
            };
        }

        [[nodiscard]] inline rule_t signed_gt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x.signed_compare(y) == std::strong_ordering::greater};
                })
            };
        }

        [[nodiscard]] inline rule_t signed_le_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    auto compared = x.signed_compare(y);
                    return value_t{
                        1, compared == std::strong_ordering::less || compared == std::strong_ordering::equal
                    };
                })
            };
        }

        [[nodiscard]] inline rule_t signed_ge_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    auto compared = x.signed_compare(y);
                    return value_t{
                        1, compared == std::strong_ordering::greater || compared == std::strong_ordering::equal
                    };
                })
            };
        }

        [[nodiscard]] inline rule_t eq_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x == y};
                })
            };
        }

        [[nodiscard]] inline rule_t neq_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x != y};
                })
            };
        }

        [[nodiscard]] inline rule_t register_rule(clock_t clk, source_t in, sink_t out) {
            return rule_t{new impl::register_rule(std::move(clk), std::move(in), std::move(out))};
        }

        [[nodiscard]] inline rule_t async_register_rule(clock_t clk, source_t rst, source_t in, sink_t out,
                                                        const bool high_rst, value_t rst_value) {
            return rule_t{
                new impl::async_register_rule(
                    std::move(clk), std::move(rst),
                    std::move(in), std::move(out),
                    high_rst, std::move(rst_value))
            };
        }
    }

    using rule_storage = impl::id_based_map<std::vector<rule_t> >;

#ifdef NX_COUNT_PERFORM
    inline std::atomic_uint32_t perform_cnt = 0;
#endif

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
#ifdef NX_COUNT_PERFORM
                    ++perform_cnt;
#endif
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
