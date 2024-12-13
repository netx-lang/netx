//
// Created by czm233 on 12/11/24.
//

#ifndef RULE_H
#define RULE_H

#include "circuit_def.h"
#include <memory>
#include <utility>

namespace hson {
    namespace impl {
        struct rule_impl {
            const id_set triggers;

            explicit rule_impl(id_set triggers) : triggers(std::move(triggers)) {}

            virtual ~rule_impl() = default;

            virtual id_set perform(value_storage& values) const = 0;
        };

        struct clock_impl {
            const id_set dependencies;

            explicit clock_impl(id_set dependencies) : dependencies(std::move(dependencies)) {}

            virtual ~clock_impl() = default;

            virtual bool triggered(value_storage& values) const = 0;
        };

        struct source_impl {
            const id_set dependencies;

            explicit source_impl(id_set dependencies) : dependencies(std::move(dependencies)) {}

            virtual ~source_impl() = default;

            virtual value_t get(value_storage& values) const = 0;
        };

        struct sink_impl {
            const id_set outcomes;

            explicit sink_impl(id_set outcomes) : outcomes(std::move(outcomes)) {}

            virtual ~sink_impl() = default;

            virtual bool put(value_storage& values, value_t v) const = 0;
        };
    }

    class rule_t : public std::shared_ptr<impl::rule_impl> {
        using base_t = shared_ptr;
    public:
        using base_t::base_t;

        [[nodiscard]] auto& triggers() const {
            return get()->triggers;
        }

        auto perform(value_storage& values) const {
            return get()->perform(values);
        }
    };

    class clock_t : public std::shared_ptr<impl::clock_impl> {
        using base_t = shared_ptr;
    public:
        using base_t::base_t;

        [[nodiscard]] const id_set& dependencies() const {
            return get()->dependencies;
        }

        bool triggered(value_storage& values) const {
            return get()->triggered(values);
        }
    };

    class source_t : public std::shared_ptr<impl::source_impl> {
        using base_t = shared_ptr;
    public:
        using base_t::base_t;

        [[nodiscard]] const id_set& dependencies() const {
            return shared_ptr::get()->dependencies;
        }

        value_t get(value_storage& values) const {
            return shared_ptr::get()->get(values);
        }
    };

    class sink_t : public std::shared_ptr<impl::sink_impl> {
        using base_t = shared_ptr;
    public:
        using base_t::base_t;

        [[nodiscard]] const id_set& outcomes() const {
            return get()->outcomes;
        }

        bool put(value_storage& values, value_t v) const {
            return get()->put(values, std::move(v));
        }
    };

    namespace rule_factory {
        rule_t make_not_rule(source_t in, sink_t out);
        rule_t make_and_rule(source_t a, source_t b, sink_t out);
        rule_t make_or_rule(source_t a, source_t b, sink_t out);
        rule_t make_register_rule(clock_t clk, source_t in, sink_t out);
    }

    class rule_set {
        std::multimap<id_t, rule_t> triggerRules;
    public:
        void add(rule_t newRule) {
            for (id_t trigger : newRule.triggers())
                triggerRules.emplace(trigger, std::move(newRule));
        }

        id_set update(value_storage& values, const id_set& hint, const bool toFixed = true) const {
            id_set changed;

            id_set toConsider = hint, toConsiderSwap;
            std::set<rule_t> toPerform;
            do {
                for (id_t id : toConsider) {
                    auto [beg, end] = triggerRules.equal_range(id);
                    for (auto iter = beg; iter != end; ++iter)
                        toPerform.emplace(iter->second);
                }

                for (const auto& rule : toPerform) {
                    auto ruleChanged = rule.perform(values);
                    toConsiderSwap.insert(ruleChanged.begin(), ruleChanged.end());
                    changed.insert(ruleChanged.begin(), ruleChanged.end());
                }

                toConsider.clear();
                toConsider.swap(toConsiderSwap);
                toPerform.clear();
            } while (toFixed && !toConsider.empty());

            return changed;
        }
    };
}

#endif //RULE_H
