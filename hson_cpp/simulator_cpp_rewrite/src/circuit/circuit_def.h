//
// Created by czm233 on 12/11/24.
//

#ifndef COMMON_H
#define COMMON_H

#include <map>
#include <set>
#include "sized_mpint.h"

namespace hson {
    using id_t = unsigned;

    using value_t = impl::sized_mpint;

    namespace impl {
        class id_set_impl : public std::set<id_t> {
            using base_t = std::set<id_t>;

        public:
            using base_t::base_t;

            id_set_impl operator+(const id_set_impl &other) const {
                id_set_impl result = *this;
                result.insert(other.begin(), other.end());
                return result;
            }
        };
    }

    using id_set = impl::id_set_impl;

    using value_storage = std::map<id_t, value_t>;
}

#endif //COMMON_H
