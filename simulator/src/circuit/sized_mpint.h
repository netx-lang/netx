//
// Created by czm233 on 3/6/25.
//

#ifndef SIZED_MPINT_H
#define SIZED_MPINT_H

#include <concepts>
#include <string>
#include <algorithm>
#include <array>
#include <unordered_map>
#include <mutex>
#include <shared_mutex>
#include <utility>

namespace nxon::impl {
    template<typename Backend>
    class sized_mpint {
        using base_t = Backend;

        template<typename T>
        friend class const_sized_mpint_slice;

        template<typename T>
        friend class sized_mpint_slice;

        base_t content;
        size_t _size;

        [[nodiscard]] const base_t &mask() const {
            return mask(_size);
        }

        [[nodiscard]] static const base_t &mask(const size_t _size) {
            if constexpr (std::unsigned_integral<base_t>) {
                static const base_t zero = 0;
                static const auto masks = [] {
                    std::array<base_t, sizeof(base_t) * 8 + 1> result{};
                    result[0] = 0;
                    for (size_t bits = 1; bits < result.size(); ++bits)
                        result[bits] = (result[bits - 1] << 1) | 1;
                    return result;
                }();
                if (_size == 0) return zero;
                if (_size >= masks.size()) return masks.back();
                return masks[_size];
            }

            static std::unordered_map<size_t, base_t> mask_cache;
            static std::shared_mutex mask_mutex; {
                std::shared_lock lock(mask_mutex);
                if (const auto iter = mask_cache.find(_size); iter != mask_cache.end())
                    return iter->second;
            } {
                std::unique_lock lock(mask_mutex);
                if (const auto iter = mask_cache.find(_size); iter != mask_cache.end())
                    return iter->second;
                base_t mask = 1;
                mask <<= _size;
                mask -= 1;
                return mask_cache.emplace(_size, std::move(mask)).first->second;
            }
        }

        void init_mask() {
            and_mask();
        }

        sized_mpint(const size_t size, base_t value, uint32_t) : content(std::move(value)), _size(size) {
            init_mask();
        }

        void bound_check(const size_t from, const size_t to) const {
            if (from > to) throw std::invalid_argument("from > to");
            if (to >= size()) throw std::out_of_range("to >= size");
        }

        void and_mask() {
            content &= mask();
        }

        bool normalize_sign(sized_mpint &other) {
            bool result_sign = false;
            if (sign()) {
                negate();
                result_sign = true;
            }
            if (other.sign()) {
                other.negate();
                result_sign = !result_sign;
            }
            return result_sign;
        }

        static base_t stomul(const std::string &s, const int base = 10) {
            base_t result = 0;
            for (const auto c: s) {
                result *= base;
                result += std::stoi(std::string{c}, nullptr, base);
            }
            return result;
        }

    public:
        friend std::ostream &operator<<(std::ostream &out, const sized_mpint &self) {
            out << self.size() << "'b";
            out << self.to_binary_string();
            out << " (" << static_cast<long long>(self) << ')';
            return out;
        }

        [[nodiscard]] std::string to_binary_string() const {
            auto num = content;
            std::string res;
            for (size_t i = 0, bound = size(); i < bound; ++i) {
                res += num & 1u ? '1' : '0';
                num >>= 1u;
            }
            std::ranges::reverse(res);
            return res;
        }

        static sized_mpint parse(const std::string &str) {
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
                return {size, stomul(str.substr(pos + 2), base), 0};
            }
            return {32, stomul(str), 0};
        }

        static sized_mpint parse_fixed(const size_t size, std::string str) {
            if (str.empty()) return sized_mpint{size};

            int base;
            switch (str.back()) {
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
                    base = -1;
                    break;
            }
            if (base == -1) base = 10;
            else str.pop_back();

            return {size, stomul(str, base), 0};
        }

        sized_mpint() : sized_mpint(1) {
        }

        explicit sized_mpint(const size_t size) : content(), _size(size) {
            init_mask();
        }

        sized_mpint(const size_t size, const std::unsigned_integral auto value) : content(value), _size(size) {
            init_mask();
        }

        template<std::signed_integral T>
        static auto unsigned_abs(const T value) {
            using target_t = std::make_unsigned_t<T>;
            if (value >= 0) return static_cast<target_t>(value);
            return static_cast<target_t>(-(value + 1)) + 1;
        }

        sized_mpint(const size_t size, const std::signed_integral auto value) : sized_mpint(size, unsigned_abs(value)) {
            if (value < 0) negate();
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
            return static_cast<bool>(content);
        }

        template<std::unsigned_integral T>
        [[nodiscard]] explicit operator T() const {
            return static_cast<T>(content);
        }

        template<std::signed_integral T>
        [[nodiscard]] explicit operator T() const {
            sized_mpint tmp = *this;
            const bool flag = tmp.sign();
            if (flag) tmp.negate();
            auto v = static_cast<T>(tmp.content);
            if (flag) v = -v;
            return v;
        }

        [[nodiscard]] std::strong_ordering operator<=>(const sized_mpint &other) const {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            if (content < other.content) return std::strong_ordering::less;
            if (content == other.content) return std::strong_ordering::equal;
            return std::strong_ordering::greater;
        }

        [[nodiscard]] std::strong_ordering signed_compare(const sized_mpint &other) const {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            const bool this_sign = sign(), other_sign = other.sign();
            if (this_sign && other_sign) return -other <=> -*this;
            if (this_sign) return std::strong_ordering::less;
            if (other_sign) return std::strong_ordering::greater;
            return operator<=>(other);
        }

        [[nodiscard]] bool operator==(const sized_mpint &other) const {
            return _size == other._size && content == other.content;
        }

        [[nodiscard]] sized_mpint extract_unchecked(const size_t from, const size_t to) const {
            return {to + 1 - from, content >> from, 0};
        }

        [[nodiscard]] sized_mpint extract(const size_t from, const size_t to) const {
            bound_check(from, to);
            return extract_unchecked(from, to);
        }

        [[nodiscard]] bool slice_equals_unchecked(const size_t from, const size_t to, const sized_mpint &other) const {
            const auto len = to + 1 - from;
            if (len != other.size()) return false;
            if constexpr (std::unsigned_integral<base_t>) {
                const auto masked = (content >> from) & mask(len);
                return masked == other.content;
            } else {
                base_t masked = content;
                masked >>= from;
                masked &= mask(len);
                return masked == other.content;
            }
        }

        [[nodiscard]] bool slice_equals(const size_t from, const size_t to, const sized_mpint &other) const {
            bound_check(from, to);
            return slice_equals_unchecked(from, to, other);
        }

        void assign_slice_unchecked(const size_t from, const size_t to, sized_mpint other) {
            auto &other_ref = other.content;
            other_ref <<= from;
            content &= ~(mask(to + 1 - from) << from);
            content |= other_ref;
        }

        void assign_slice(const size_t from, const size_t to, sized_mpint other) {
            bound_check(from, to);
            if (to + 1 - from != other.size()) throw std::invalid_argument("Sizes don't match");
            assign_slice_unchecked(from, to, std::move(other));
        }

        [[nodiscard]] auto slice(size_t from, size_t to);

        [[nodiscard]] auto const_slice(size_t from, size_t to) const;

        [[nodiscard]] auto slice(size_t from, size_t to) const;

        [[nodiscard]] bool sign() const {
            return operator>>(size() - 1).operator bool();
        }

        [[nodiscard]] sized_mpint signed_resize(const size_t new_size) const {
            const auto comp = new_size <=> size();
            if (comp == std::strong_ordering::equal) return *this;
            if (comp == std::strong_ordering::greater) {
                sized_mpint result(new_size);
                result.content |= content;
                if (sign()) result.content |= result.mask() ^ mask();
                return result;
            }
            if (comp == std::strong_ordering::less)
                return {new_size, content, 0};
            std::unreachable();
        }

        [[nodiscard]] sized_mpint unsigned_resize(const size_t new_size) const {
            const auto comp = new_size <=> size();
            if (comp == std::strong_ordering::equal) return *this;
            if (comp == std::strong_ordering::greater) {
                sized_mpint result(new_size);
                result.content |= content;
                return result;
            }
            if (comp == std::strong_ordering::less) {
                return {new_size, content, 0};
            }
            std::unreachable();
        }

        sized_mpint &flip() {
            content ^= mask();
            return *this;
        }

        [[nodiscard]] sized_mpint operator~() const {
            sized_mpint result = *this;
            result.flip();
            return result;
        }

        sized_mpint &operator|=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            content |= other.content;
            return *this;
        }

        sized_mpint &operator&=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            content &= other.content;
            return *this;
        }

        sized_mpint &operator^=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            content ^= other.content;
            return *this;
        }

        sized_mpint &operator>>=(const size_t cnt) {
            content >>= cnt;
            return *this;
        }

        sized_mpint &ashr_eq(const size_t cnt) {
            const bool flag = sign();
            operator>>=(cnt);
            if (flag) {
                const auto mask_tail = size() - std::min(cnt, size());
                auto mask_head = mask();
                mask_head >>= mask_tail;
                mask_head <<= mask_tail;
                content |= mask_head;
            }
            return *this;
        }

        sized_mpint &operator<<=(const size_t cnt) {
            content <<= cnt;
            and_mask();
            return *this;
        }

        [[nodiscard]] sized_mpint operator|(const sized_mpint &other) const {
            sized_mpint result = *this;
            result |= other;
            return result;
        }

        [[nodiscard]] sized_mpint operator&(const sized_mpint &other) const {
            sized_mpint result = *this;
            result &= other;
            return result;
        }

        [[nodiscard]] sized_mpint operator^(const sized_mpint &other) const {
            sized_mpint result = *this;
            result ^= other;
            return result;
        }

        [[nodiscard]] sized_mpint operator>>(const size_t cnt) const {
            sized_mpint result = *this;
            result >>= cnt;
            return result;
        }

        [[nodiscard]] sized_mpint arithmetic_shr(const size_t cnt) const {
            sized_mpint result = *this;
            result.ashr_eq(cnt);
            return result;
        }

        [[nodiscard]] sized_mpint operator<<(const size_t cnt) const {
            sized_mpint result = *this;
            result <<= cnt;
            return result;
        }

        sized_mpint &negate() {
            flip();
            operator+=(1);
            return *this;
        }

        [[nodiscard]] sized_mpint operator-() const {
            sized_mpint result = *this;
            result.negate();
            return result;
        }

        sized_mpint &operator+=(const unsigned long long v) {
            content += v;
            and_mask();
            return *this;
        }

        sized_mpint &operator+=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            content += other.content;
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
            content *= other.content;
            and_mask();
            return *this;
        }

        sized_mpint &operator/=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            sized_mpint b = other;
            const bool result_sign = normalize_sign(b);
            content /= b.content;
            if (result_sign) negate();
            return *this;
        }

        sized_mpint &operator%=(const sized_mpint &other) {
            if (size() != other.size()) throw std::invalid_argument("Sizes don't match");
            sized_mpint b = other;
            const bool result_sign = normalize_sign(b);
            content %= b.content;
            if (result_sign) negate();
            return *this;
        }

        [[nodiscard]] sized_mpint operator+(const sized_mpint &other) const {
            sized_mpint result = *this;
            result += other;
            return result;
        }

        [[nodiscard]] sized_mpint operator-(const sized_mpint &other) const {
            sized_mpint result = *this;
            result -= other;
            return result;
        }

        [[nodiscard]] sized_mpint operator*(const sized_mpint &other) const {
            sized_mpint result = *this;
            result *= other;
            return result;
        }

        [[nodiscard]] sized_mpint operator/(const sized_mpint &other) const {
            sized_mpint result = *this;
            result /= other;
            return result;
        }

        [[nodiscard]] sized_mpint operator%(const sized_mpint &other) const {
            sized_mpint result = *this;
            result %= other;
            return result;
        }

        [[nodiscard]] sized_mpint concat(const sized_mpint &other) const {
            base_t tmp = content;
            tmp <<= other.size();
            tmp |= other.content;
            return {size() + other.size(), std::move(tmp), 0};
        }
    };

    template<typename Backend>
    class const_sized_mpint_slice {
        template<typename T>
        friend class sized_mpint;
        sized_mpint<Backend>::base_t mask{1};

    protected:
        const sized_mpint<Backend> &ref;
        const size_t from, len;

        const_sized_mpint_slice(const sized_mpint<Backend> &ref, const size_t from, const size_t to) : ref(ref),
            from(from),
            len(to + 1 - from) {
            mask <<= from;
        }

    public:
        const_sized_mpint_slice(const const_sized_mpint_slice &) = default;

        const_sized_mpint_slice(const_sized_mpint_slice &&) = default;

        const_sized_mpint_slice &operator=(const const_sized_mpint_slice &) = delete;

        const_sized_mpint_slice &operator=(const_sized_mpint_slice &&) = delete;

        [[nodiscard]] sized_mpint<Backend> from_slice() const {
            if (len == 1) {
                return {1, static_cast<unsigned>(static_cast<bool>(ref.content & mask))};
            }
            return {len, ref.content >> from, 0};
        }

        [[nodiscard]] size_t length() const {
            return len;
        }
    };

    template<typename Backend>
    auto sized_mpint<Backend>::slice(const size_t from, const size_t to) const {
        bound_check(from, to);
        return const_sized_mpint_slice<Backend>(*this, from, to);
    }

    template<typename Backend>
    auto sized_mpint<Backend>::const_slice(const size_t from, const size_t to) const {
        return slice(from, to);
    }

    template<typename Backend>
    class sized_mpint_slice final : public const_sized_mpint_slice<Backend> {
        template<typename T>
        friend class sized_mpint;

        sized_mpint<Backend> &ref;

        sized_mpint_slice(sized_mpint<Backend> &ref, const size_t from,
                          const size_t to) : const_sized_mpint_slice<Backend>(ref, from, to), ref(ref) {
        }

    public:
        sized_mpint_slice(const sized_mpint_slice &) = default;

        sized_mpint_slice(sized_mpint_slice &&) = default;

        sized_mpint_slice &operator=(const sized_mpint_slice &) = delete;

        sized_mpint_slice &operator=(sized_mpint_slice &&) = delete;

        sized_mpint_slice &operator=(sized_mpint<Backend> other) {
            ref.assign_slice(this->from, this->from + this->len - 1, std::move(other));
            return *this;
        }

        sized_mpint_slice &operator=(const const_sized_mpint_slice<Backend> &other) {
            if (this->length() != other.length()) throw std::invalid_argument("Sizes don't match");
            operator=(other.from_slice());
            return *this;
        }
    };

    template<typename Backend>
    auto sized_mpint<Backend>::slice(const size_t from, const size_t to) {
        bound_check(from, to);
        return sized_mpint_slice{*this, from, to};
    }
}

template<typename Backend>
struct std::formatter<nxon::impl::sized_mpint<Backend> > {
    static constexpr auto parse(const std::format_parse_context &ctx) {
        return ctx.begin();
    }

    static auto format(const nxon::impl::sized_mpint<Backend> &v, std::format_context &ctx) {
        return std::format_to(ctx.out(), "{}'b{} ({})", v.size(), v.to_binary_string(), static_cast<uint64_t>(v));
    }
};

#endif //SIZED_MPINT_H
