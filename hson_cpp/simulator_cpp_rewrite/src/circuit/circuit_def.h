//
// Created by czm233 on 12/11/24.
//

#ifndef COMMON_H
#define COMMON_H

#include <map>
#include <set>
#include <boost/multiprecision/cpp_int.hpp>

namespace hson {
    using id_t = unsigned;

    namespace impl {
        class sized_mpint final : boost::multiprecision::cpp_int {
            using base_t = boost::multiprecision::cpp_int;
            friend class const_sized_mpint_slice;
            friend class sized_mpint_slice;

            size_t _size;

            sized_mpint(const size_t size, base_t value) : base_t(std::move(value)), _size(size) {
            }

            void bound_check(const size_t from, const size_t to) const {
                if (from > to) throw std::invalid_argument("from > to");
                if (to >= size()) throw std::out_of_range("to >= size");
            }

        public:
            sized_mpint() : sized_mpint(0) {
            }

            explicit sized_mpint(const size_t size) : _size(size) {
            }

            sized_mpint(const size_t size, const unsigned long long value) : base_t(value), _size(size) {
            }

            sized_mpint(const sized_mpint &) = default;

            sized_mpint(sized_mpint &&) = default;

            sized_mpint &operator=(const sized_mpint &) = default;

            sized_mpint &operator=(sized_mpint &&) = default;

            [[nodiscard]] size_t size() const {
                return _size;
            }

            [[nodiscard]] bool empty() const {
                return size() == 0;
            }

            [[nodiscard]] explicit operator bool() const {
                return base_t::operator bool();
            }

            std::strong_ordering operator<=>(const sized_mpint &other) const {
                if (_size != other._size) throw std::runtime_error("Sizes don't match");
                const auto &super = dynamic_cast<const base_t &>(*this);
                if (super < other) return std::strong_ordering::less;
                if (super == other) return std::strong_ordering::equal;
                return std::strong_ordering::greater;
            }

            bool operator==(const sized_mpint &other) const = default;

            [[nodiscard]] auto slice(size_t from, size_t to);

            [[nodiscard]] auto slice(size_t from, size_t to) const;

            sized_mpint operator~() const {
                return {size(), ~dynamic_cast<const base_t &>(*this)};
            }

            sized_mpint &operator|=(const sized_mpint &other) {
                if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
                base_t::operator|=(other);
                return *this;
            }

            sized_mpint &operator&=(const sized_mpint &other) {
                if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
                base_t::operator&=(other);
                return *this;
            }

            sized_mpint &operator^=(const sized_mpint &other) {
                if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
                base_t::operator^=(other);
                return *this;
            }

            sized_mpint operator|(const sized_mpint &other) const {
                sized_mpint result = *this;
                result |= other;
                return result;
            }

            sized_mpint operator&(const sized_mpint &other) const {
                sized_mpint result = *this;
                result &= other;
                return result;
            }

            sized_mpint operator^(const sized_mpint &other) const {
                sized_mpint result = *this;
                result ^= other;
                return result;
            }
        };

        class const_sized_mpint_slice {
            friend class sized_mpint;

        protected:
            const sized_mpint &ref;
            const size_t from, to, len;
            sized_mpint::base_t mask_n{1};

            const_sized_mpint_slice(const sized_mpint &ref, const size_t from, const size_t to) : ref(ref), from(from),
                to(to), len(to + 1 - from) {
                mask_n <<= len;
                mask_n -= 1;
                mask_n <<= from;
                mask_n = ~mask_n;
            }

        public:
            const_sized_mpint_slice(const const_sized_mpint_slice &) = default;

            const_sized_mpint_slice(const_sized_mpint_slice &&) = default;

            const_sized_mpint_slice &operator=(const const_sized_mpint_slice &) = delete;

            const_sized_mpint_slice &operator=(const_sized_mpint_slice &&) = delete;

            [[nodiscard]] sized_mpint from_slice() const {
                auto tmp = dynamic_cast<const sized_mpint::base_t &>(ref);
                tmp >>= from;
                tmp ^= tmp >> len << len;
                return {len, std::move(tmp)};
            }

            [[nodiscard]] size_t length() const {
                return len;
            }
        };

        inline auto sized_mpint::slice(const size_t from, const size_t to) const {
            bound_check(from, to);
            return const_sized_mpint_slice{*this, from, to};
        }

        class sized_mpint_slice final : public const_sized_mpint_slice {
            friend class sized_mpint;

            sized_mpint &ref;

            sized_mpint_slice(sized_mpint &ref, const size_t from, const size_t to) : const_sized_mpint_slice(
                ref, from, to), ref(ref) {
            }

        public:
            sized_mpint_slice(const sized_mpint_slice &) = default;

            sized_mpint_slice(sized_mpint_slice &&) = default;

            sized_mpint_slice &operator=(const sized_mpint_slice &) = delete;

            sized_mpint_slice &operator=(sized_mpint_slice &&) = delete;

            sized_mpint_slice &operator=(sized_mpint other) {
                if (length() != other.size()) throw std::runtime_error("Sizes don't match");
                auto &otherRef = dynamic_cast<sized_mpint::base_t &>(other);
                otherRef <<= from;
                auto &thisRef = dynamic_cast<sized_mpint::base_t &>(ref);
                thisRef &= mask_n;
                thisRef |= otherRef;
                return *this;
            }

            sized_mpint_slice &operator=(const const_sized_mpint_slice &other) {
                if (length() != other.length()) throw std::runtime_error("Sizes don't match");
                operator=(other.from_slice());
                return *this;
            }
        };

        inline auto sized_mpint::slice(const size_t from, const size_t to) {
            bound_check(from, to);
            return sized_mpint_slice{*this, from, to};
        }
    }

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
