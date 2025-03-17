//
// Created by czm233 on 12/11/24.
//

#ifndef RULE_H
#define RULE_H

#include "circuit_def.h"
#include <memory>
#include <utility>

namespace hxon {
    namespace impl {
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

            virtual id_set perform(value_storage &values) const = 0;
        };

        struct clock_impl {
            const id_set dependencies;

            explicit clock_impl(id_set dependencies) : dependencies(std::move(dependencies)) {
            }

            virtual ~clock_impl() = default;

            virtual bool triggered(value_storage &values) const = 0;
        };

        struct source_impl {
            const id_set dependencies;
            const size_t len;

            explicit source_impl(id_set dependencies, const size_t len) : dependencies(std::move(dependencies)), len(len) {
            }

            virtual ~source_impl() = default;

            virtual value_t get(value_storage &values) const = 0;
        };

        struct sink_impl {
            const id_set outcomes;
            const size_t len;

            explicit sink_impl(id_set outcomes, const size_t len) : outcomes(std::move(outcomes)), len(len) {
            }

            virtual ~sink_impl() = default;

            [[nodiscard]] virtual bool check(const value_storage &values, const value_t& v) const = 0;

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

        value_t get(value_storage &values) const {
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

        [[nodiscard]] bool check(const value_storage &values, const value_t& v) const {
            return get()->check(values, v);
        }

        void put(value_storage &values, value_t v) const {
            get()->put(values, std::move(v));
        }
    };

    namespace data_factory {
        [[nodiscard]] clock_t id_clock(id_t id, bool posEdge = true);

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

        [[nodiscard]] rule_t lshr_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t ashr_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t add_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t sub_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t mul_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t div_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t mod_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t concat_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t eq_rule(source_t a, source_t b, sink_t out);

        [[nodiscard]] rule_t register_rule(clock_t clk, source_t in, sink_t out);
    }

    class rule_set {
        std::multimap<id_t, rule_t> triggerRules;

    public:
        void add(const rule_t &newRule) {
            for (id_t trigger: newRule.triggers())
                triggerRules.emplace(trigger, newRule);
        }

        id_set update(value_storage &values, const id_set &hint, const bool toFixed = true) const {
            id_set changed;

            id_set toConsider = hint, toConsiderSwap;
            std::set<rule_t> toPerform;
            do {
                for (id_t id: toConsider) {
                    auto [beg, end] = triggerRules.equal_range(id);
                    for (auto iter = beg; iter != end; ++iter)
                        toPerform.emplace(iter->second);
                }

                for (const auto &rule: toPerform) {
                    auto ruleChanged = rule.perform(values);
                    toConsiderSwap.insert(ruleChanged.begin(), ruleChanged.end());
                    changed.insert(ruleChanged.begin(), ruleChanged.end());
                }
                values.perform_delayed();

                toConsider.clear();
                toConsider.swap(toConsiderSwap);
                toPerform.clear();
            } while (toFixed && !toConsider.empty());

            return changed;
        }
    };
}

#endif //RULE_H
