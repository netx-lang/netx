//
// Created by czm233 on 12/11/24.
//

#ifndef COMMON_H
#define COMMON_H

#include <flat_set>
#include <unordered_map>

#include "sized_mpint.h"

namespace nxon {
    using id_t = unsigned;

    using value_t = impl::sized_mpint;

    namespace impl {
        class id_set_impl : public std::flat_set<id_t> {
            using base_t = flat_set;
            using self_t = id_set_impl;

        public:
            using base_t::base_t;

            auto &operator+=(const self_t &other) {
                insert(std::sorted_unique, other.begin(), other.end());
                return *this;
            }

            auto operator+(const self_t &other) const {
                auto result = *this;
                result += other;
                return result;
            }
        };
    }

    using id_set = impl::id_set_impl;

    using value_batch = std::unordered_map<id_t, value_t>;

    template<typename V>
    class id_based_map {
    public:
        using key_type = id_t;
        using mapped_type = V;
        using size_type = size_t;

    private:
        std::vector<mapped_type> content;

    public:
        mapped_type &operator[](const key_type id) {
            if (content.size() <= id) content.resize(id + 1, mapped_type{});
            return content[id];
        }

        [[nodiscard]] const mapped_type &operator[](const key_type id) const {
            return content[id];
        }

        [[nodiscard]] mapped_type &at(const key_type id) {
            if (content.size() <= id) throw std::out_of_range("id does not exist");
            return content[id];
        }

        [[nodiscard]] const mapped_type &at(const key_type id) const {
            if (content.size() <= id) throw std::out_of_range("id does not exist");
            return content[id];
        }
    };

    class value_storage : public id_based_map<value_t> {
        std::vector<std::function<void(value_storage &)> > delayed;

    public:
        void delay(std::function<void(value_storage &)> func) {
            delayed.emplace_back(std::move(func));
        }

        void perform_delayed() {
            for (auto &func: delayed) func(*this);
            delayed.clear();
        }
    };
}

#endif //COMMON_H
