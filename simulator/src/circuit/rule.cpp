//
// Created by czm233 on 12/12/24.
//
#include "rule.h"
#include <boost/lambda2.hpp>
#include <utility>
#include <functional>

using namespace boost::lambda2;

namespace nxon {
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

        void noop_deleter(const value_t *) {
        }

        void delete_deleter(const value_t *ptr) {
            delete ptr;
        }

        struct id_source final : source_impl {
            const id_t id;

            explicit id_source(id_t id, const size_t len)
                : source_impl({id}, len), id(id) {
            }

            indirect_const_value_t get(value_storage &values) const override {
                return {&values[id], noop_deleter};
            }
        };

        struct constant_source final : source_impl {
            const value_t value;

            explicit constant_source(value_t value) : source_impl({}, value.size()), value(std::move(value)) {
            }

            indirect_const_value_t get(value_storage &) const override {
                return {&value, noop_deleter};
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

            indirect_const_value_t get(value_storage &values) const override {
                auto ptr = new value_t(values[id].slice(from, to).from_slice());
                return {ptr, delete_deleter};
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
    }

    namespace data_factory {
        clock_t id_clock(const id_t id, const bool pos_edge) {
            return clock_t{new impl::id_clock(id, pos_edge)};
        }

        source_t id_source(const id_t id, const size_t len) {
            return source_t{new impl::id_source(id, len)};
        }

        sink_t id_sink(const id_t id, const size_t len) {
            return sink_t{new impl::id_sink(id, len)};
        }

        source_t select_source(const id_t id, const size_t from, const size_t to) {
            return source_t{new impl::select_source(id, from, to)};
        }

        sink_t select_sink(const id_t id, const size_t from, const size_t to) {
            return sink_t{new impl::select_sink(id, from, to)};
        }

        source_t constant_source(value_t value) {
            return source_t{new impl::constant_source(std::move(value))};
        }

        sink_t signed_resize_sink_adapter(sink_t sink) {
            return sink_t{new impl::signed_resize_sink_adapter(std::move(sink))};
        }
    }

    namespace impl {
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

            id_set perform(value_storage &values) const override {
                const auto x = a.get(values);
                const auto y = b.get(values);
                if (auto value = op(*x, *y); out.check(values, value)) {
                    out.put(values, std::move(value));
                    return out.outcomes();
                }
                return {};
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

            id_set perform(value_storage &values) const override {
                const auto x = in.get(values);
                if (auto value = op(*x); out.check(values, value)) {
                    out.put(values, std::move(value));
                    return out.outcomes();
                }
                return {};
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

            id_set perform(value_storage &values) const override {
                if (auto value = in.get(values); clk.triggered(values) && out.check(values, *value)) {
                    values.delay([value = value_t(*value), out = out](auto &cur_values) {
                        out.put(cur_values, std::move(value));
                    });
                    return out.outcomes();
                }
                return {};
            }
        };
    }

    namespace rule_factory {
        rule_t id_rule(source_t in, sink_t out) {
            return rule_t{new impl::unary_rule(std::move(in), std::move(out), _1)};
        }

        rule_t not_rule(source_t in, sink_t out) {
            return rule_t{new impl::unary_rule(std::move(in), std::move(out), ~_1)};
        }

        rule_t and_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 & _2)};
        }

        rule_t or_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 | _2)};
        }

        rule_t xor_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 ^ _2)};
        }

        rule_t shl_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x << static_cast<size_t>(y);
                })
            };
        }

        rule_t logical_shr_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x >> static_cast<size_t>(y);
                })
            };
        }

        rule_t arithmetic_shr_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x.arithmetic_shr(static_cast<size_t>(y));
                })
            };
        }

        rule_t add_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 + _2)};
        }

        rule_t sub_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 - _2)};
        }

        rule_t mul_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 * _2)};
        }

        rule_t div_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 / _2)};
        }

        rule_t mod_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 % _2)};
        }

        rule_t concat_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return x.concat(y);
                })
            };
        }

        rule_t unsigned_lt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x < y};
                })
            };
        }

        rule_t unsigned_gt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x > y};
                })
            };
        }

        rule_t unsigned_le_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x <= y};
                })
            };
        }

        rule_t unsigned_ge_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x >= y};
                })
            };
        }

        rule_t signed_lt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x.signed_compare(y) == std::strong_ordering::less};
                })
            };
        }

        rule_t signed_gt_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x.signed_compare(y) == std::strong_ordering::greater};
                })
            };
        }

        rule_t signed_le_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    auto compared = x.signed_compare(y);
                    return value_t{
                        1, compared == std::strong_ordering::less || compared == std::strong_ordering::equal
                    };
                })
            };
        }

        rule_t signed_ge_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    auto compared = x.signed_compare(y);
                    return value_t{
                        1, compared == std::strong_ordering::greater || compared == std::strong_ordering::equal
                    };
                })
            };
        }

        rule_t eq_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x == y};
                })
            };
        }

        rule_t neq_rule(source_t a, source_t b, sink_t out) {
            return rule_t{
                new impl::binary_rule(std::move(a), std::move(b), std::move(out), [](auto &x, auto &y) {
                    return value_t{1, x != y};
                })
            };
        }

        rule_t register_rule(clock_t clk, source_t in, sink_t out) {
            return rule_t{new impl::register_rule(std::move(clk), std::move(in), std::move(out))};
        }
    }
}
