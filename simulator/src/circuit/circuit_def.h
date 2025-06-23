//
// Created by czm233 on 12/11/24.
//

#ifndef COMMON_H
#define COMMON_H

#include <unordered_set>
#include <unordered_map>
#include "sized_mpint.h"

namespace nxon {
    using id_t = unsigned;

    using value_t = impl::sized_mpint;

    namespace impl {
        class id_set_impl : public std::unordered_set<id_t> {
            using base_t = unordered_set;

        public:
            using base_t::base_t;

            auto& operator+=(const id_set_impl &other) {
                insert(other.begin(), other.end());
                return *this;
            }

            auto operator+(const id_set_impl &other) const {
                id_set_impl result = *this;
                result += other;
                return result;
            }
        };
    }

    using id_set = impl::id_set_impl;

    class value_storage : public std::unordered_map<id_t, value_t> {
        using base_t = unordered_map;
        std::vector<std::function<void(value_storage&)>> delayed;
    public:
        using base_t::base_t;

        void delay(std::function<void(value_storage&)> func) {
            delayed.emplace_back(std::move(func));
        }

        void perform_delayed() {
            for (auto& func : delayed) func(*this);
            delayed.clear();
        }
    };
}

#endif //COMMON_H
