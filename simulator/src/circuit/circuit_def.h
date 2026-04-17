//
// Created by czm233 on 12/11/24.
//

#ifndef COMMON_H
#define COMMON_H

#include <version>
#include <unordered_map>
#include <vector>
#include <ranges>
#include <algorithm>

#include "sized_mpint.h"

namespace nxon {
    using id_t = unsigned;

#ifndef NX_BACKEND
    #define NX_BACKEND uint64_t
#endif
    using value_t = impl::sized_mpint<NX_BACKEND>;

    namespace impl {
        template<typename Container, std::ranges::input_range Range>
        void append_range_compat(Container &container, Range &&range) {
#ifdef __cpp_lib_containers_ranges
            container.append_range(std::forward<Range>(range));
#else
            container.insert(container.end(), std::ranges::begin(range), std::ranges::end(range));
#endif
        }

        template<typename Container, std::ranges::input_range Range>
        void insert_range_compat(Container &container, Range &&range) {
#ifdef __cpp_lib_containers_ranges
            container.insert_range(std::forward<Range>(range));
#else
            container.insert(std::ranges::begin(range), std::ranges::end(range));
#endif
        }

        class id_set_impl {
            using self_t = id_set_impl;
            std::vector<id_t> content;

        public:
            using value_type = id_t;
            using size_type = std::vector<id_t>::size_type;
            using iterator = std::vector<id_t>::iterator;
            using const_iterator = std::vector<id_t>::const_iterator;

            id_set_impl() = default;
            explicit id_set_impl(std::vector<id_t> init) : content(std::move(init)) {
            }

            id_set_impl(const std::initializer_list<id_t> init) : content(init) {
            }

            template<std::input_iterator It>
            id_set_impl(It first, It last) : content(first, last) {
            }

            [[nodiscard]] iterator begin() {
                return content.begin();
            }

            [[nodiscard]] const_iterator begin() const {
                return content.begin();
            }

            [[nodiscard]] iterator end() {
                return content.end();
            }

            [[nodiscard]] const_iterator end() const {
                return content.end();
            }

            [[nodiscard]] bool empty() const {
                return content.empty();
            }

            [[nodiscard]] size_type size() const {
                return content.size();
            }

            void reserve(const size_type n) {
                content.reserve(n);
            }

            void clear() {
                content.clear();
            }

            void insert(const id_t id) {
                content.emplace_back(id);
            }

            template<std::input_iterator It>
            void insert(It first, It last) {
                content.insert(content.end(), first, last);
            }

            [[nodiscard]] bool contains(const id_t id) const {
                return std::ranges::find(content, id) != content.end();
            }

            size_type erase(const id_t id) {
                const auto old_size = content.size();
                std::erase(content, id);
                return old_size - content.size();
            }

            void dedup() {
                std::ranges::sort(content);
                const auto [first, last] = std::ranges::unique(content);
                content.erase(first, last);
            }

            auto &operator+=(const self_t &other) {
                insert(other.begin(), other.end());
                return *this;
            }

            auto operator+(const self_t &other) const {
                auto result = *this;
                result += other;
                return result;
            }
        };

        template<typename V>
        class id_based_map {
        public:
            using key_type = id_t;
            using mapped_type = V;
            using size_type = size_t;

        private:
            std::vector<mapped_type> content;

        public:
            class iterator {
                friend class id_based_map;

                id_based_map *owner = nullptr;
                size_type index = 0;

                iterator(id_based_map *owner, const size_type index) : owner(owner), index(index) {
                }

            public:
                using value_type = std::pair<key_type, mapped_type &>;
                using difference_type = std::ptrdiff_t;

                iterator() = default;

                value_type operator*() const {
                    return {static_cast<key_type>(index), owner->content[index]};
                }

                iterator &operator++() {
                    ++index;
                    return *this;
                }

                bool operator==(const iterator &other) const = default;
            };

            class const_iterator {
                friend class id_based_map;

                const id_based_map *owner = nullptr;
                size_type index = 0;

                const_iterator(const id_based_map *owner, const size_type index) : owner(owner), index(index) {
                }

            public:
                using value_type = std::pair<key_type, const mapped_type &>;
                using difference_type = std::ptrdiff_t;

                const_iterator() = default;

                value_type operator*() const {
                    return {static_cast<key_type>(index), owner->content[index]};
                }

                const_iterator &operator++() {
                    ++index;
                    return *this;
                }

                bool operator==(const const_iterator &other) const = default;
            };

            [[nodiscard]] size_type size() const {
                return content.size();
            }

            [[nodiscard]] iterator begin() {
                return iterator(this, 0);
            }

            [[nodiscard]] iterator end() {
                return iterator(this, content.size());
            }

            [[nodiscard]] const_iterator begin() const {
                return const_iterator(this, 0);
            }

            [[nodiscard]] const_iterator end() const {
                return const_iterator(this, content.size());
            }

            void prepare(const key_type max_id) {
                if (content.size() <= max_id) content.resize(max_id + 1);
            }

            mapped_type &operator[](const key_type id) {
                if (content.size() <= id) content.resize(id + 1);
                return content[id];
            }

            [[nodiscard]] const mapped_type &operator[](const key_type id) const {
                return content[id];
            }

            [[nodiscard]] mapped_type &at(const key_type id) {
                return content.at(id);
            }

            [[nodiscard]] const mapped_type &at(const key_type id) const {
                return content.at(id);
            }

            [[nodiscard]] bool contains(const key_type id) const {
                return id < content.size();
            }

            [[nodiscard]] iterator find(const key_type id) {
                return contains(id) ? iterator(this, id) : end();
            }

            [[nodiscard]] const_iterator find(const key_type id) const {
                return contains(id) ? const_iterator(this, id) : end();
            }
        };
    }

    using id_set = impl::id_set_impl;

    using value_batch = std::unordered_map<id_t, value_t>;

    class value_storage : public impl::id_based_map<value_t> {
        struct delayed_write {
            enum class kind_t : uint8_t {
                id,
                select
            };

            kind_t kind;
            id_t id;
            size_t from;
            size_t to;
            value_t value;
        };

        std::vector<delayed_write> delayed;

    public:
        void delay(const id_t id, value_t value) {
            delayed.emplace_back(delayed_write{
                .kind = delayed_write::kind_t::id,
                .id = id,
                .from = 0,
                .to = 0,
                .value = std::move(value)
            });
        }

        void delay(const id_t id, const size_t from, const size_t to, value_t value) {
            delayed.emplace_back(delayed_write{
                .kind = delayed_write::kind_t::select,
                .id = id,
                .from = from,
                .to = to,
                .value = std::move(value)
            });
        }

        void perform_delayed() {
            for (auto &[kind, id, from, to, value]: delayed) {
                switch (kind) {
                    case delayed_write::kind_t::id:
                        at(id) = std::move(value);
                        break;
                    case delayed_write::kind_t::select:
                        at(id).assign_slice_unchecked(from, to, std::move(value));
                        break;
                }
            }
            delayed.clear();
        }
    };

    struct sparse_value_batch {
        id_set ids;
        value_storage values;
        impl::id_based_map<uint8_t> present;

        [[nodiscard]] bool empty() const {
            return ids.empty();
        }

        void clear() {
            for (const auto id: ids) present[id] = 0;
            ids.clear();
        }

        void reserve(const size_t n) {
            ids.reserve(n);
        }

        void swap(sparse_value_batch &other) noexcept {
            std::swap(ids, other.ids);
            std::swap(values, other.values);
            std::swap(present, other.present);
        }

        void insert_or_assign(const id_t id, const value_t &value) {
            if (!present.contains(id) || present[id] == 0) {
                present[id] = 1;
                ids.insert(id);
            }
            values[id] = value;
        }

        void emplace(const id_t id, const value_t &value) {
            insert_or_assign(id, value);
        }

        [[nodiscard]] const value_t &at(const id_t id) const {
            return values.at(id);
        }
    };
}

#endif //COMMON_H
