//
// Created by czm233 on 12/11/24.
//

#ifndef COMMON_H
#define COMMON_H

#include <version>
#include <unordered_map>
#ifdef __cpp_lib_flat_set
#include <flat_set>
template<typename T>
using compatible_flat_set = std::flat_set<T>;
#else
#include <boost/container/flat_set.hpp>
template<typename T>
using compatible_flat_set = boost::container::flat_set<T>;
#endif

#include "sized_mpint.h"

namespace nxon {
    using id_t = unsigned;

#ifndef SIZED_MPINT_BACKEND
    using value_t = impl::sized_mpint<boost::multiprecision::cpp_int>;
#else
    using value_t = impl::sized_mpint<SIZED_MPINT_BACKEND>;
#endif


    namespace impl {
        class id_set_impl : public compatible_flat_set<id_t> {
            using base_t = compatible_flat_set<id_t>;
            using self_t = id_set_impl;

        public:
            using base_t::base_t;

            auto &operator+=(const self_t &other) {
#ifdef __cpp_lib_flat_set
                insert(std::sorted_unique, other.begin(), other.end());
#else
                insert(boost::container::ordered_unique_range, other.begin(), other.end());
#endif
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
        };
    }

    using id_set = impl::id_set_impl;

    using value_batch = std::unordered_map<id_t, value_t>;

    class value_storage : public impl::id_based_map<value_t> {
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
