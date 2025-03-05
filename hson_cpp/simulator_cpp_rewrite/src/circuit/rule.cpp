//
// Created by czm233 on 12/12/24.
//
#include "rule.h"
#include <boost/lambda2.hpp>
#include <utility>

using namespace boost::lambda2;

namespace hson {
    namespace impl {
        struct id_clock final : clock_impl {
            const id_t id;
            const bool posEdge;

            explicit id_clock(id_t id, const bool posEdge)
                : clock_impl({id}), id(id), posEdge(posEdge) {
            }

            bool triggered(value_storage &values) const override {
                const auto &clk = values[id];
                assert(!clk.empty());
                if (posEdge) return static_cast<bool>(clk);
                return !static_cast<bool>(clk);
            }
        };

        struct id_source final : source_impl {
            const id_t id;

            explicit id_source(id_t id)
                : source_impl({id}), id(id) {
            }

            value_t get(value_storage &values) const override {
                return values[id];
            }
        };

        struct id_sink final : sink_impl {
            const id_t id;

            explicit id_sink(id_t id)
                : sink_impl({id}), id(id) {
            }

            bool put(value_storage &values, value_t v) const override {
                auto &out = values[id];
                if (out == v) return false;
                out = std::move(v);
                return true;
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
                : source_impl({id}), id(id),
                  from((assert(from <= to), from)), to(to), len(to - from + 1) {
            }

            value_t get(value_storage &values) const override {
                return values[id].slice(from, to).from_slice();
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
                : sink_impl({id}), id(id),
                  from((assert(from <= to), from)), to(to), len(to - from + 1) {
            }

            bool put(value_storage &values, value_t v) const override {
                auto out = values[id].slice(from, to);
                if (out.from_slice() == v) return false;
                out = std::move(v);
                return true;
            }
        };
    }

    namespace data_factory {
        clock_t id_clock(id_t id, const bool posEdge) {
            return clock_t{new impl::id_clock(id, posEdge)};
        }

        source_t id_source(const id_t id) {
            return source_t{new impl::id_source(id)};
        }

        sink_t id_sink(const id_t id) {
            return sink_t{new impl::id_sink(id)};
        }

        source_t select_source(const id_t id, const size_t from, const size_t to) {
            return source_t{new impl::select_source(id, from, to)};
        }

        sink_t select_sink(const id_t id, const size_t from, const size_t to) {
            return sink_t{new impl::select_sink(id, from, to)};
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
                if (out.put(values, op(a.get(values), b.get(values))))
                    return out.outcomes();
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
                if (out.put(values, op(in.get(values))))
                    return out.outcomes();
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
                if (clk.triggered(values) && out.put(values, in.get(values)))
                    return out.outcomes();
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

        rule_t register_rule(clock_t clk, source_t in, sink_t out) {
            return rule_t{new impl::register_rule(std::move(clk), std::move(in), std::move(out))};
        }
    }
}
