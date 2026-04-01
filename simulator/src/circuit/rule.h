//
// Created by czm233 on 12/11/24.
//

#ifndef RULE_H
#define RULE_H

#include "circuit_def.h"
#include <atomic>
#include <ranges>
#include <cassert>
#include <memory>
#include <utility>
#include <algorithm>
#include <compare>
#include <variant>
#include <vector>

namespace nxon {
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
    }

    template<typename T>
    class registry {
        std::vector<std::unique_ptr<T>> storage;

    public:
        using handle_t = size_t;

        handle_t add(std::unique_ptr<T> item) {
            storage.push_back(std::move(item));
            return storage.size() - 1;
        }

        T* get(const handle_t h) { return storage[h].get(); }
        [[nodiscard]] const T* get(const handle_t h) const { return storage[h].get(); }

        [[nodiscard]] size_t size() const { return storage.size(); }
    };

    inline registry<impl::rule_impl> &rule_registry() {
        static registry<impl::rule_impl> instance;
        return instance;
    }

    inline registry<impl::clock_impl> &clock_registry() {
        static registry<impl::clock_impl> instance;
        return instance;
    }

    inline size_t next_rule_key() {
        static std::atomic_size_t counter = 0;
        return counter++;
    }

    class rule_t;

    class clock_t {
        registry<impl::clock_impl>::handle_t handle{};
        const impl::clock_impl *impl_ptr = nullptr;
        using triggered_fn_t = bool (*)(const impl::clock_impl *, value_storage &);
        triggered_fn_t triggered_fn = nullptr;
        
    public:
        clock_t() = delete;

        template<typename T>
            requires std::derived_from<T, impl::clock_impl>
        explicit clock_t(T* impl) {
            handle = clock_registry().add(std::unique_ptr<impl::clock_impl>(impl));
            impl_ptr = clock_registry().get(handle);
            triggered_fn = [](const impl::clock_impl *ptr, value_storage &values) {
                return static_cast<const T *>(ptr)->triggered(values);
            };
        }

        [[nodiscard]] const id_set& dependencies() const {
            assert(impl_ptr != nullptr);
            return impl_ptr->dependencies;
        }

        bool triggered(value_storage& values) const {
            assert(impl_ptr != nullptr);
            assert(triggered_fn != nullptr);
            return triggered_fn(impl_ptr, values);
        }

        auto operator<=>(const clock_t& other) const {
            return handle <=> other.handle;
        }
    };

    class source_t {
        enum class kind_t : uint8_t {
            id,
            constant,
            select
        };

        kind_t kind;
        id_set dependencies_;
        size_t len_;
        id_t id_{};
        size_t from_{};
        size_t to_{};
        std::shared_ptr<const value_t> constant_;
        
    public:
        source_t(const id_t id, const size_t len) : kind(kind_t::id), dependencies_{id}, len_(len), id_(id) {
        }

        explicit source_t(value_t value)
            : kind(kind_t::constant), len_(value.size()),
              constant_(std::make_shared<value_t>(std::move(value))) {
        }

        source_t(const id_t id, const size_t from, const size_t to)
            : kind(kind_t::select), dependencies_{id}, len_(to - from + 1), id_(id), from_(from), to_(to) {
        }

        [[nodiscard]] const id_set& dependencies() const {
            return dependencies_;
        }

        [[nodiscard]] size_t len() const {
            return len_;
        }

        [[nodiscard]] bool is_plain_id() const {
            return kind == kind_t::id;
        }

        [[nodiscard]] id_t id() const {
            assert(kind == kind_t::id);
            return id_;
        }

        value_t get(value_storage& values) const {
            switch (kind) {
                case kind_t::id:
                    return values.at(id_);
                case kind_t::constant:
                    return *constant_;
                case kind_t::select:
                    return values.at(id_).extract_unchecked(from_, to_);
            }
            std::unreachable();
        }
        
        auto operator<=>(const source_t &other) const {
            if (const auto cmp = static_cast<uint8_t>(kind) <=> static_cast<uint8_t>(other.kind); cmp != 0) return cmp;
            if (const auto cmp = len_ <=> other.len_; cmp != 0) return cmp;
            if (const auto cmp = id_ <=> other.id_; cmp != 0) return cmp;
            if (const auto cmp = from_ <=> other.from_; cmp != 0) return cmp;
            if (const auto cmp = to_ <=> other.to_; cmp != 0) return cmp;
            return std::compare_three_way{}(constant_.get(), other.constant_.get());
        }
    };

    class sink_t {
    public:
        enum class kind_t : uint8_t {
            id,
            select,
            signed_resize_adapter,
            unsigned_resize_adapter
        };

    private:
        kind_t kind;
        id_set outcomes_;
        size_t len_;
        id_t id_{};
        size_t from_{};
        size_t to_{};
        std::shared_ptr<const sink_t> underlying_;
        
    public:
        sink_t(const id_t id, const size_t len) : kind(kind_t::id), outcomes_{id}, len_(len), id_(id) {
        }

        sink_t(const id_t id, const size_t from, const size_t to)
            : kind(kind_t::select), outcomes_{id}, len_(to - from + 1), id_(id), from_(from), to_(to) {
        }

        sink_t(const kind_t kind, sink_t underlying)
            : kind(kind), outcomes_(underlying.outcomes()), len_(underlying.len()),
              underlying_(std::make_shared<sink_t>(std::move(underlying))) {
        }

        [[nodiscard]] const id_set& outcomes() const {
            return outcomes_;
        }

        [[nodiscard]] size_t len() const {
            return len_;
        }

        [[nodiscard]] bool is_plain_id() const {
            return kind == kind_t::id;
        }

        [[nodiscard]] id_t id() const {
            assert(kind == kind_t::id);
            return id_;
        }

        [[nodiscard]] bool check(const value_storage& values, const value_t& v) const {
            switch (kind) {
                case kind_t::id:
                    return values.at(id_) != v;
                case kind_t::select: {
                    return !values.at(id_).slice_equals_unchecked(from_, to_, v);
                }
                case kind_t::signed_resize_adapter:
                    return underlying_->check(values, v.signed_resize(len_));
                case kind_t::unsigned_resize_adapter:
                    return underlying_->check(values, v.unsigned_resize(len_));
            }
            std::unreachable();
        }

        bool update_if_changed(value_storage &values, value_t v) const {
            switch (kind) {
                case kind_t::id: {
                    auto &slot = values.at(id_);
                    if (slot == v) return false;
                    slot = std::move(v);
                    return true;
                }
                case kind_t::select: {
                    auto &slot = values.at(id_);
                    if (slot.slice_equals_unchecked(from_, to_, v)) return false;
                    slot.assign_slice_unchecked(from_, to_, std::move(v));
                    return true;
                }
                case kind_t::signed_resize_adapter:
                    return underlying_->update_if_changed(values, v.signed_resize(len_));
                case kind_t::unsigned_resize_adapter:
                    return underlying_->update_if_changed(values, v.unsigned_resize(len_));
            }
            std::unreachable();
        }

        void put(value_storage& values, value_t v) const {
            switch (kind) {
                case kind_t::id:
                    values.at(id_) = std::move(v);
                    return;
                case kind_t::select:
                    values.at(id_).assign_slice_unchecked(from_, to_, std::move(v));
                    return;
                case kind_t::signed_resize_adapter:
                    underlying_->put(values, v.signed_resize(len_));
                    return;
                case kind_t::unsigned_resize_adapter:
                    underlying_->put(values, v.unsigned_resize(len_));
                    return;
            }
            std::unreachable();
        }

        void delay_put(value_storage& values, value_t v) const {
            switch (kind) {
                case kind_t::id:
                    values.delay(id_, std::move(v));
                    return;
                case kind_t::select:
                    values.delay(id_, from_, to_, std::move(v));
                    return;
                case kind_t::signed_resize_adapter:
                    underlying_->delay_put(values, v.signed_resize(len_));
                    return;
                case kind_t::unsigned_resize_adapter:
                    underlying_->delay_put(values, v.unsigned_resize(len_));
                    return;
            }
            std::unreachable();
        }
        
        auto operator<=>(const sink_t &other) const {
            if (const auto cmp = static_cast<uint8_t>(kind) <=> static_cast<uint8_t>(other.kind); cmp != 0) return cmp;
            if (const auto cmp = len_ <=> other.len_; cmp != 0) return cmp;
            if (const auto cmp = id_ <=> other.id_; cmp != 0) return cmp;
            if (const auto cmp = from_ <=> other.from_; cmp != 0) return cmp;
            if (const auto cmp = to_ <=> other.to_; cmp != 0) return cmp;
            return std::compare_three_way{}(underlying_.get(), other.underlying_.get());
        }
    };

    namespace impl {
        struct unary_id_rule_data {
            id_t in;
            id_t out;
        };

        struct unary_rule_data {
            source_t in;
            sink_t out;
        };

        struct binary_id_rule_data {
            id_t a;
            id_t b;
            id_t out;
        };

        struct binary_rule_data {
            source_t a;
            source_t b;
            sink_t out;
        };

        struct register_rule_data {
            clock_t clk;
            source_t in;
            sink_t out;
        };

        struct async_register_rule_data {
            clock_t clk;
            source_t rst;
            source_t in;
            sink_t out;
            bool high_rst;
            value_t rst_value;
        };

        inline value_t op_id(const value_t &x) { return x; }
        inline value_t op_not(const value_t &x) { return ~x; }
        inline value_t op_and(const value_t &x, const value_t &y) { return x & y; }
        inline value_t op_or(const value_t &x, const value_t &y) { return x | y; }
        inline value_t op_xor(const value_t &x, const value_t &y) { return x ^ y; }
        inline value_t op_shl(const value_t &x, const value_t &y) { return x << static_cast<size_t>(y); }
        inline value_t op_lshr(const value_t &x, const value_t &y) { return x >> static_cast<size_t>(y); }
        inline value_t op_ashr(const value_t &x, const value_t &y) { return x.arithmetic_shr(static_cast<size_t>(y)); }
        inline value_t op_add(const value_t &x, const value_t &y) { return x + y; }
        inline value_t op_sub(const value_t &x, const value_t &y) { return x - y; }
        inline value_t op_mul(const value_t &x, const value_t &y) { return x * y; }
        inline value_t op_div(const value_t &x, const value_t &y) { return x / y; }
        inline value_t op_mod(const value_t &x, const value_t &y) { return x % y; }
        inline value_t op_concat(const value_t &x, const value_t &y) { return x.concat(y); }
        inline value_t op_ult(const value_t &x, const value_t &y) { return value_t{1, x < y}; }
        inline value_t op_ugt(const value_t &x, const value_t &y) { return value_t{1, x > y}; }
        inline value_t op_ule(const value_t &x, const value_t &y) { return value_t{1, x <= y}; }
        inline value_t op_uge(const value_t &x, const value_t &y) { return value_t{1, x >= y}; }
        inline value_t op_slt(const value_t &x, const value_t &y) {
            return value_t{1, x.signed_compare(y) == std::strong_ordering::less};
        }
        inline value_t op_sgt(const value_t &x, const value_t &y) {
            return value_t{1, x.signed_compare(y) == std::strong_ordering::greater};
        }
        inline value_t op_sle(const value_t &x, const value_t &y) {
            const auto compared = x.signed_compare(y);
            return value_t{1, compared == std::strong_ordering::less || compared == std::strong_ordering::equal};
        }
        inline value_t op_sge(const value_t &x, const value_t &y) {
            const auto compared = x.signed_compare(y);
            return value_t{1, compared == std::strong_ordering::greater || compared == std::strong_ordering::equal};
        }
        inline value_t op_eq(const value_t &x, const value_t &y) { return value_t{1, x == y}; }
        inline value_t op_neq(const value_t &x, const value_t &y) { return value_t{1, x != y}; }
    }

    inline registry<impl::unary_rule_data> &unary_rule_registry() {
        static registry<impl::unary_rule_data> instance;
        return instance;
    }

    inline registry<impl::unary_id_rule_data> &unary_id_rule_registry() {
        static registry<impl::unary_id_rule_data> instance;
        return instance;
    }

    inline registry<impl::binary_id_rule_data> &binary_id_rule_registry() {
        static registry<impl::binary_id_rule_data> instance;
        return instance;
    }

    inline registry<impl::binary_rule_data> &binary_rule_registry() {
        static registry<impl::binary_rule_data> instance;
        return instance;
    }

    inline registry<impl::register_rule_data> &register_rule_registry() {
        static registry<impl::register_rule_data> instance;
        return instance;
    }

    inline registry<impl::async_register_rule_data> &async_register_rule_registry() {
        static registry<impl::async_register_rule_data> instance;
        return instance;
    }

    class rule_t {
        using payload_t = std::variant<
            std::monostate,
            const impl::rule_impl *,
            const impl::unary_id_rule_data *,
            const impl::unary_rule_data *,
            const impl::binary_id_rule_data *,
            const impl::binary_rule_data *,
            const impl::register_rule_data *,
            const impl::async_register_rule_data *
        >;

        size_t order_key{};
        id_set triggers_;
        id_set dependencies_;
        id_set outcomes_;
        id_t outcome_id{};
        payload_t payload;
        const impl::rule_impl *impl_ptr = nullptr;
        using perform_fn_t = id_set (*)(const payload_t &, value_storage &);
        using fast_perform_fn_t = bool (*)(const payload_t &, value_storage &);
        perform_fn_t perform_fn = nullptr;
        fast_perform_fn_t fast_perform_fn = nullptr;

        rule_t() = default;

        template<typename Payload>
        static const Payload &get_payload(const payload_t &payload) {
            return *std::get<const Payload *>(payload);
        }

        template<typename Payload>
        static rule_t make_fast_rule(
            std::unique_ptr<Payload> data,
            registry<Payload> &storage,
            id_set triggers,
            id_set dependencies,
            id_set outcomes,
            fast_perform_fn_t fast_perform_fn
        ) {
            rule_t result;
            result.order_key = next_rule_key();
            const auto handle = storage.add(std::move(data));
            const auto *stored = storage.get(handle);
            result.payload = stored;
            result.triggers_ = std::move(triggers);
            result.dependencies_ = std::move(dependencies);
            result.outcomes_ = std::move(outcomes);
            result.fast_perform_fn = fast_perform_fn;
            return result;
        }

        static id_set perform_generic(const payload_t &payload, value_storage &values) {
            return get_payload<impl::rule_impl>(payload).perform(values);
        }

        template<auto Op>
        static bool perform_unary_id(const payload_t &payload, value_storage &values) {
            const auto &rule = get_payload<impl::unary_id_rule_data>(payload);
            const auto x = values.at(rule.in);
            auto value = Op(x);
            auto &slot = values.at(rule.out);
            if (slot == value) return false;
            slot = std::move(value);
            return true;
        }

        template<auto Op>
        static bool perform_unary(const payload_t &payload, value_storage &values) {
            const auto &rule = get_payload<impl::unary_rule_data>(payload);
            const auto x = rule.in.get(values);
            return rule.out.update_if_changed(values, Op(x));
        }

        template<auto Op>
        static bool perform_binary_id(const payload_t &payload, value_storage &values) {
            const auto &rule = get_payload<impl::binary_id_rule_data>(payload);
            const auto x = values.at(rule.a);
            const auto y = values.at(rule.b);
            auto value = Op(x, y);
            auto &slot = values.at(rule.out);
            if (slot == value) return false;
            slot = std::move(value);
            return true;
        }

        template<auto Op>
        static bool perform_binary(const payload_t &payload, value_storage &values) {
            const auto &rule = get_payload<impl::binary_rule_data>(payload);
            const auto x = rule.a.get(values);
            const auto y = rule.b.get(values);
            return rule.out.update_if_changed(values, Op(x, y));
        }

        static bool perform_register(const payload_t &payload, value_storage &values) {
            const auto &rule = get_payload<impl::register_rule_data>(payload);
            if (!rule.clk.triggered(values)) return false;
            auto value = rule.in.get(values);
            if (!rule.out.check(values, value)) return false;
            rule.out.delay_put(values, std::move(value));
            return true;
        }

        static bool perform_async_register(const payload_t &payload, value_storage &values) {
            const auto &rule = get_payload<impl::async_register_rule_data>(payload);
            if (const auto rst_v = rule.rst.get(values); static_cast<bool>(rst_v) == rule.high_rst &&
                                                     rule.out.update_if_changed(values, rule.rst_value)) {
                return true;
            }
            if (!rule.clk.triggered(values)) return false;
            auto value = rule.in.get(values);
            if (!rule.out.check(values, value)) return false;
            rule.out.delay_put(values, std::move(value));
            return true;
        }

    public:
        template<typename T>
            requires std::derived_from<T, impl::rule_impl>
        explicit rule_t(T* impl) {
            order_key = next_rule_key();
            const auto handle = rule_registry().add(std::unique_ptr<impl::rule_impl>(impl));
            impl_ptr = rule_registry().get(handle);
            payload = impl_ptr;
            triggers_ = impl_ptr->triggers;
            dependencies_ = impl_ptr->dependencies;
            outcomes_ = impl_ptr->outcomes;
            perform_fn = &perform_generic;
            fast_perform_fn = nullptr;
        }

        template<auto Op>
        static rule_t make_unary(const source_t &in, const sink_t &out) {
            if (in.is_plain_id() && out.is_plain_id()) {
                auto data = std::make_unique<impl::unary_id_rule_data>(impl::unary_id_rule_data{
                    .in = in.id(),
                    .out = out.id()
                });
                auto result = make_fast_rule(std::move(data), unary_id_rule_registry(),
                                             in.dependencies(), in.dependencies(), out.outcomes(),
                                             &perform_unary_id<Op>);
                result.outcome_id = *out.outcomes().begin();
                return result;
            }
            auto data = std::make_unique<impl::unary_rule_data>(impl::unary_rule_data{
                .in = in,
                .out = out
            });
            auto result = make_fast_rule(std::move(data), unary_rule_registry(),
                                         in.dependencies(), in.dependencies(), out.outcomes(),
                                         &perform_unary<Op>);
            result.outcome_id = *out.outcomes().begin();
            return result;
        }

        template<auto Op>
        static rule_t make_binary(const source_t &a, const source_t &b, const sink_t &out) {
            if (a.is_plain_id() && b.is_plain_id() && out.is_plain_id()) {
                auto data = std::make_unique<impl::binary_id_rule_data>(impl::binary_id_rule_data{
                    .a = a.id(),
                    .b = b.id(),
                    .out = out.id()
                });
                auto triggers = a.dependencies() + b.dependencies();
                auto result = make_fast_rule(std::move(data), binary_id_rule_registry(),
                                             triggers, triggers, out.outcomes(),
                                             &perform_binary_id<Op>);
                result.outcome_id = *out.outcomes().begin();
                return result;
            }
            auto data = std::make_unique<impl::binary_rule_data>(impl::binary_rule_data{
                .a = a,
                .b = b,
                .out = out
            });
            auto triggers = a.dependencies() + b.dependencies();
            auto result = make_fast_rule(std::move(data), binary_rule_registry(),
                                         triggers, triggers, out.outcomes(),
                                         &perform_binary<Op>);
            result.outcome_id = *out.outcomes().begin();
            return result;
        }

        static rule_t make_register(const clock_t &clk, const source_t &in, const sink_t &out) {
            auto data = std::make_unique<impl::register_rule_data>(impl::register_rule_data{
                .clk = clk,
                .in = in,
                .out = out
            });
            auto result = make_fast_rule(std::move(data), register_rule_registry(),
                                         clk.dependencies(), clk.dependencies() + in.dependencies(), out.outcomes(),
                                         &perform_register);
            result.outcome_id = *out.outcomes().begin();
            return result;
        }

        static rule_t make_async_register(
            const clock_t &clk, const source_t &rst, const source_t &in, const sink_t &out,
            const bool high_rst, value_t rst_value
        ) {
            auto data = std::make_unique<impl::async_register_rule_data>(impl::async_register_rule_data{
                .clk = clk,
                .rst = rst,
                .in = in,
                .out = out,
                .high_rst = high_rst,
                .rst_value = std::move(rst_value)
            });
            auto result = make_fast_rule(std::move(data), async_register_rule_registry(),
                                         clk.dependencies(), clk.dependencies() + rst.dependencies() + in.dependencies(),
                                         out.outcomes(), &perform_async_register);
            result.outcome_id = *out.outcomes().begin();
            return result;
        }

        [[nodiscard]] auto& triggers() const {
            return triggers_;
        }

        [[nodiscard]] auto& dependencies() const {
            return dependencies_;
        }

        [[nodiscard]] const id_set &outcomes() const {
            return outcomes_;
        }

        auto perform(value_storage& values) const {
            if (fast_perform_fn != nullptr) {
                if (fast_perform_fn(payload, values)) return outcomes_;
                return id_set{};
            }
            return perform_fn(payload, values);
        }

        [[nodiscard]] bool is_fast() const {
            return fast_perform_fn != nullptr;
        }

        [[nodiscard]] id_t fast_outcome() const {
            return outcome_id;
        }

        bool perform_fast(value_storage &values) const {
            return fast_perform_fn(payload, values);
        }

        auto operator<=>(const rule_t& other) const {
            return order_key <=> other.order_key;
        }

        [[nodiscard]] const impl::rule_impl* get() const { return impl_ptr; }
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

    }

    namespace data_factory {
        [[nodiscard]] inline clock_t id_clock(const id_t id, const bool pos_edge) {
            return clock_t{new impl::id_clock(id, pos_edge)};
        }

        [[nodiscard]] inline source_t id_source(const id_t id, const size_t len) {
            return source_t{id, len};
        }

        [[nodiscard]] inline sink_t id_sink(const id_t id, const size_t len) {
            return sink_t{id, len};
        }

        [[nodiscard]] inline source_t select_source(const id_t id, const size_t from, const size_t to) {
            return source_t{id, from, to};
        }

        [[nodiscard]] inline sink_t select_sink(const id_t id, const size_t from, const size_t to) {
            return sink_t{id, from, to};
        }

        [[nodiscard]] inline source_t constant_source(value_t value) {
            return source_t{std::move(value)};
        }

        [[nodiscard]] inline sink_t signed_resize_sink_adapter(const sink_t &sink) {
            return sink_t{sink_t::kind_t::signed_resize_adapter, sink};
        }

        [[nodiscard]] inline sink_t unsigned_resize_sink_adapter(const sink_t &sink) {
            return sink_t{sink_t::kind_t::unsigned_resize_adapter, sink};
        }
    }

    using rule_index_storage = impl::id_based_map<std::vector<size_t> >;

#ifdef NX_COUNT_PERFORM
    inline std::atomic_uint32_t perform_cnt = 0;
#endif

    class rule_set {
        std::vector<rule_t> all_rules;
        rule_index_storage trigger_rules;

        template<bool TrackChanged>
        static void record_changes(std::vector<id_t> &to_consider_swap, std::vector<id_t> &changed, const id_set &ids) {
            if (!ids.empty()) {
                if (ids.size() == 1) {
                    to_consider_swap.push_back(*ids.begin());
                } else {
                    impl::append_range_compat(to_consider_swap, ids);
                }
            }
            if constexpr (TrackChanged) {
                if (!ids.empty()) {
                    if (ids.size() == 1) {
                        changed.push_back(*ids.begin());
                    } else {
                        impl::append_range_compat(changed, ids);
                    }
                }
            }
        }

        template<bool TrackChanged>
        static void record_single_change(std::vector<id_t> &to_consider_swap, std::vector<id_t> &changed,
                                         const id_t id) {
            to_consider_swap.push_back(id);
            if constexpr (TrackChanged) {
                changed.push_back(id);
            }
        }

        void collect_rules(std::vector<size_t> &to_perform, const id_t id) const {
            if (!trigger_rules.contains(id)) return;
            const auto &rules = trigger_rules[id];
            if (rules.empty()) return;
            if (rules.size() == 1) {
                to_perform.push_back(rules.front());
                return;
            }
            impl::append_range_compat(to_perform, rules);
        }

        template<bool TrackChanged>
        void perform_rule(const rule_t &rule, value_storage &values,
                          std::vector<id_t> &to_consider_swap, std::vector<id_t> &changed) const {
#ifdef NX_COUNT_PERFORM
            ++perform_cnt;
#endif
            if (rule.is_fast()) {
                if (!rule.perform_fast(values)) return;
                record_single_change<TrackChanged>(to_consider_swap, changed, rule.fast_outcome());
            } else {
                const auto rule_changed = rule.perform(values);
                record_changes<TrackChanged>(to_consider_swap, changed, rule_changed);
            }
        }

        [[nodiscard]] std::vector<size_t> collect_triggered_rules(const id_set &hint) const {
            std::vector<size_t> rule_indices;
            size_t initial_rules = 0;
            for (const id_t id: hint) {
                if (trigger_rules.contains(id)) {
                    initial_rules += trigger_rules[id].size();
                }
            }
            rule_indices.reserve(initial_rules);
            for (const id_t id: hint) collect_rules(rule_indices, id);
            return rule_indices;
        }

        template<bool TrackChanged>
        id_set perform_rules(value_storage &values, const std::vector<size_t> &to_perform,
                                    const bool to_fixed = true) const {
            std::vector<id_t> changed;
            std::vector<id_t> to_consider, to_consider_swap;
            bool first_pass = true;
            do {
                for (const id_t id: to_consider) {
                    if (!trigger_rules.contains(id)) continue;
                    for (const auto &rules = trigger_rules[id]; const auto rule_idx: rules)
                        perform_rule<TrackChanged>(all_rules[rule_idx], values, to_consider_swap, changed);
                }

                if (first_pass) {
                    for (const auto rule_idx: to_perform)
                        perform_rule<TrackChanged>(all_rules[rule_idx], values, to_consider_swap, changed);
                    first_pass = false;
                }
                values.perform_delayed();

                to_consider.clear();
                to_consider.swap(to_consider_swap);
            } while (to_fixed && !to_consider.empty());

            if constexpr (TrackChanged) {
                return id_set{std::move(changed)};
            } else {
                return {};
            }
        }

    public:
        void add(const rule_t &new_rule) {
            all_rules.push_back(new_rule);
            const auto new_index = all_rules.size() - 1;
            for (const id_t trigger: new_rule.triggers())
                trigger_rules[trigger].emplace_back(new_index);
        }

        id_set update(value_storage &values, const id_set &hint, const bool to_fixed = true) const {
            return perform_rules<true>(values, collect_triggered_rules(hint), to_fixed);
        }

        void update_discard(value_storage &values, const id_set &hint, const bool to_fixed = true) const {
            static_cast<void>(perform_rules<false>(values, collect_triggered_rules(hint), to_fixed));
        }

        id_set run_all(value_storage &values, const bool to_fixed = true) const {
            return perform_rules<true>(values,
                std::views::iota(size_t{0}, all_rules.size()) | std::ranges::to<std::vector>(), to_fixed);
        }

        void run_all_discard(value_storage &values, const bool to_fixed = true) const {
            static_cast<void>(perform_rules<false>(values,
                std::views::iota(size_t{0}, all_rules.size()) | std::ranges::to<std::vector>(), to_fixed));
        }

        [[nodiscard]] std::vector<rule_t> rules() const {
            return all_rules;
        }
    };
}

#endif //RULE_H
