//
// Created by czm233 on 12/12/24.
//
#include "rule.h"
#include <boost/lambda2.hpp>
#include <utility>

using namespace boost::lambda2;

namespace hson {
    namespace impl {
        struct binary_rule final : rule_impl {
            const source_t a;
            const source_t b;
            const sink_t out;
            const std::function<value_t(const value_t &, const value_t &)> op;

            binary_rule(source_t a, source_t b, sink_t out,
                        std::function<value_t(const value_t &, const value_t &)> op)
                : rule_impl(a.dependencies() + b.dependencies()), a(std::move(a)), b(std::move(b)),
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
                : rule_impl(x.dependencies()), in(std::move(x)), out(std::move(out)), op(std::move(op)) {
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
                : rule_impl(clk.dependencies()), clk(std::move(clk)),
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
        rule_t make_not_rule(source_t in, sink_t out) {
            return rule_t{new impl::unary_rule(std::move(in), std::move(out), ~_1)};
        }

        rule_t make_and_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 & _2)};
        }

        rule_t make_or_rule(source_t a, source_t b, sink_t out) {
            return rule_t{new impl::binary_rule(std::move(a), std::move(b), std::move(out), _1 | _2)};
        }

        rule_t make_register_rule(clock_t clk, source_t in, sink_t out) {
            return rule_t{new impl::register_rule(std::move(clk), std::move(in), std::move(out))};
        }
    }
}
