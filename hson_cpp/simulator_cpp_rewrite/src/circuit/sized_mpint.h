//
// Created by czm233 on 3/6/25.
//

#ifndef SIZED_MPINT_H
#define SIZED_MPINT_H

#include <boost/multiprecision/cpp_int.hpp>

namespace hson::impl {
    class sized_mpint final : boost::multiprecision::cpp_int {
        using base_t = boost::multiprecision::cpp_int;
        friend class const_sized_mpint_slice;
        friend class sized_mpint_slice;

        size_t _size;
        base_t mask;

        void init_mask() {
            mask = 1;
            mask <<= size();
            mask -= 1;
            and_mask();
        }

        sized_mpint(const size_t size, base_t value) : base_t(std::move(value)), _size(size) {
            init_mask();
        }

        void bound_check(const size_t from, const size_t to) const {
            if (from > to) throw std::invalid_argument("from > to");
            if (to >= size()) throw std::out_of_range("to >= size");
        }

        void and_mask() {
            base_t::operator&=(mask);
        }

        bool normalize_sign(sized_mpint& other) {
            bool result_sign = false;
            if (sign()) {
                negate();
                result_sign = !result_sign;
            }
            if (other.sign()) {
                other.negate();
                result_sign = !result_sign;
            }
            return result_sign;
        }
    public:
        friend std::ostream& operator<<(std::ostream& out, const sized_mpint& self) {
            sized_mpint tmp = self;
            const bool flag = tmp.sign();
            if (flag) tmp.negate();
            auto v = tmp.convert_to<long long>();
            if (flag) v = -v;
            out << "sized_mpint(" << self.size() << "," << v << ")";
            return out;
        }

        static sized_mpint parse(const std::string& str) {
            if (const auto pos = str.find('\''); pos != std::string::npos) {
                const size_t size = std::stoull(str.substr(0, pos));
                int base;
                switch (str[pos + 1]) {
                    case 'd':
                        base = 10;
                        break;
                    case 'b':
                        base = 2;
                        break;
                    case 'o':
                        base = 8;
                        break;
                    case 'h':
                        base = 16;
                        break;
                    default:
                        throw std::invalid_argument("Invalid number base");
                }
                return {size, std::stoull(str.substr(pos + 2), nullptr, base)};
            }
            return {32, std::stoull(str)};
        }

        sized_mpint() : sized_mpint(1) {
        }

        explicit sized_mpint(const size_t size) : _size(size) {
            init_mask();
        }

        sized_mpint(const size_t size, const unsigned long long value) : base_t(value), _size(size) {
            init_mask();
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

        [[nodiscard]] auto const_slice(size_t from, size_t to) const;

        [[nodiscard]] auto slice(size_t from, size_t to) const;

        [[nodiscard]] bool sign() const {
            return operator>>(size() - 1).operator bool();
        }

        [[nodiscard]] sized_mpint signed_widen(const size_t new_size) const {
            assert(new_size >= size());
            sized_mpint result(new_size);
            result.base_t::operator|=(*this);
            if (sign()) result.base_t::operator|=(result.mask ^ mask);
            return result;
        }

        sized_mpint &flip() {
            base_t::operator^=(mask);
            return *this;
        }

        sized_mpint operator~() const {
            sized_mpint result = *this;
            result.flip();
            return result;
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

        sized_mpint &operator>>=(const size_t cnt) {
            base_t::operator>>=(cnt);
            return *this;
        }

        sized_mpint &operator<<=(const size_t cnt) {
            base_t::operator<<=(cnt);
            and_mask();
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

        sized_mpint operator>>(const size_t cnt) const {
            sized_mpint result = *this;
            result >>= cnt;
            return result;
        }

        sized_mpint operator<<(const size_t cnt) const {
            sized_mpint result = *this;
            result <<= cnt;
            return result;
        }

        sized_mpint &negate() {
            flip();
            operator+=(1);
            return *this;
        }

        sized_mpint operator-() const {
            sized_mpint result = *this;
            result.negate();
            return result;
        }

        sized_mpint &operator+=(const unsigned long long v) {
            base_t::operator+=(v);
            and_mask();
            return *this;
        }

        sized_mpint &operator+=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            base_t::operator+=(other);
            and_mask();
            return *this;
        }

        sized_mpint &operator-=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            operator+=(-other);
            return *this;
        }

        sized_mpint &operator*=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            base_t::operator*=(other);
            and_mask();
            return *this;
        }

        sized_mpint &operator/=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            sized_mpint b = other;
            const bool result_sign = normalize_sign(b);
            base_t::operator/=(b);
            if (result_sign) negate();
            return *this;
        }

        sized_mpint &operator%=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            sized_mpint b = other;
            const bool result_sign = normalize_sign(b);
            base_t::operator%=(b);
            if (result_sign) negate();
            return *this;
        }

        sized_mpint operator+(const sized_mpint &other) const {
            sized_mpint result = *this;
            result += other;
            return result;
        }

        sized_mpint operator-(const sized_mpint &other) const {
            sized_mpint result = *this;
            result -= other;
            return result;
        }

        sized_mpint operator*(const sized_mpint &other) const {
            sized_mpint result = *this;
            result *= other;
            return result;
        }

        sized_mpint operator/(const sized_mpint &other) const {
            sized_mpint result = *this;
            result /= other;
            return result;
        }

        sized_mpint operator%(const sized_mpint &other) const {
            sized_mpint result = *this;
            result %= other;
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

    inline auto sized_mpint::const_slice(const size_t from, const size_t to) const {
        return slice(from, to);
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

#endif //SIZED_MPINT_H
