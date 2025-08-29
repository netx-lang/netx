#include <gtest/gtest.h>
#include <print>

#include <boost/multiprecision/cpp_int.hpp>
#include "sized_mpint.h"

using vec_64 = nxon::impl::sized_mpint<uint64_t>;
using vec_unlimited = nxon::impl::sized_mpint<boost::multiprecision::cpp_int>;

TEST(SizedMpint64Test, ConstructionAndMask) {
    const vec_64 a = {8, 0x1FF};
    const vec_64 b = {8, 0xFF};
    EXPECT_TRUE(a == b);

    const vec_64 c = {23, 0x0FFFFF};
    const vec_64 d = {23, 0x0FFFFF};
    EXPECT_TRUE(c == d);
}

TEST(SizedMpint64Test, Arithmetic) {
    vec_64 a{8, 100};
    vec_64 b{8, 50};
    vec_64 result = a + b;
    EXPECT_EQ(static_cast<uint64_t>(result), 150);

    result = a - b;
    EXPECT_EQ(static_cast<uint64_t>(result), 50);

    a = {8, 10};
    b = {8, 5};
    result = a * b;
    EXPECT_EQ(static_cast<uint64_t>(result), 50);

    a = {8, 20};
    b = {8, 4};
    result = a / b;
    EXPECT_EQ(static_cast<uint64_t>(result), 5);

    a = {8, 23};
    b = {8, 5};
    result = a % b;
    EXPECT_EQ(static_cast<uint64_t>(result), 3);

    a = {8, 200};
    b = {8, 100};
    result = a + b;
    EXPECT_EQ(static_cast<uint64_t>(result), 44);
}

TEST(SizedMpint64Test, BitwiseOperations) {
    const vec_64 a{8, 0xAA};
    const vec_64 b{8, 0x55};

    vec_64 result = a | b;
    EXPECT_EQ(static_cast<uint64_t>(result), 0xFF);

    result = a & b;
    EXPECT_EQ(static_cast<uint64_t>(result), 0x00);

    result = a ^ b;
    EXPECT_EQ(static_cast<uint64_t>(result), 0xFF);

    result = ~a;
    EXPECT_EQ(static_cast<uint64_t>(result), 0x55);

    result = vec_64{8, 0x0F} << 2;
    EXPECT_EQ(static_cast<uint64_t>(result), 0x3C);

    result = vec_64{8, 0xF0} >> 2;
    EXPECT_EQ(static_cast<uint64_t>(result), 0x3C);
}

TEST(SizedMpint64Test, ArithmeticShift) {
    vec_64 a{8, 0x7C};
    vec_64 result = a.arithmetic_shr(2);
    EXPECT_EQ(static_cast<uint64_t>(result), 0x1F);

    a = {8, 0x80};
    result = a.arithmetic_shr(2);
    EXPECT_EQ(static_cast<uint64_t>(result), 0xE0);
}

TEST(SizedMpint64Test, Comparisons) {
    vec_64 a{8, 100};
    vec_64 b{8, 50};

    EXPECT_TRUE(a > b);
    EXPECT_TRUE(b < a);
    EXPECT_TRUE(a == a);
    EXPECT_TRUE(a >= a);
    EXPECT_TRUE(b <= a);

    a = {8, 0x80};
    b = {8, 0x7F};
    EXPECT_TRUE(a.signed_compare(b) == std::strong_ordering::less);
    EXPECT_TRUE(b.signed_compare(a) == std::strong_ordering::greater);
}

TEST(SizedMpint64Test, TypeConversions) {
    vec_64 a{32, 12345};

    EXPECT_EQ(static_cast<uint32_t>(a), 12345u);
    EXPECT_EQ(static_cast<uint64_t>(a), 12345ull);

    EXPECT_EQ(static_cast<int32_t>(a), 12345);
    EXPECT_EQ(static_cast<int64_t>(a), 12345ll);

    a = {8, 0xFF};
    EXPECT_EQ(static_cast<int8_t>(a), -1);
    EXPECT_EQ(static_cast<uint8_t>(a), 255u);
}

TEST(SizedMpint64Test, StringParsing) {
    vec_64 a = vec_64::parse("8'b11110000");
    EXPECT_EQ(static_cast<uint64_t>(a), 0xF0);
    EXPECT_EQ(a.size(), 8);

    a = vec_64::parse("16'd1234");
    EXPECT_EQ(static_cast<uint64_t>(a), 1234);
    EXPECT_EQ(a.size(), 16);

    a = vec_64::parse("8'hAB");
    EXPECT_EQ(static_cast<uint64_t>(a), 0xAB);
    EXPECT_EQ(a.size(), 8);

    a = vec_64::parse("8'o77");
    EXPECT_EQ(static_cast<uint64_t>(a), 63);

    a = vec_64::parse("42");
    EXPECT_EQ(static_cast<uint64_t>(a), 42);
    EXPECT_EQ(a.size(), 32);
}

TEST(SizedMpint64Test, BinaryStringOutput) {
    const vec_64 a{8, 0xAA};
    EXPECT_EQ(a.to_binary_string(), "10101010");

    const vec_64 b{4, 0x5};
    EXPECT_EQ(b.to_binary_string(), "0101");
}

TEST(SizedMpint64Test, Slicing) {
    const vec_64 a{16, 0xABCD};

    const auto slice = a.const_slice(4, 7);
    const vec_64 extracted = slice.from_slice();
    EXPECT_EQ(static_cast<uint64_t>(extracted), 0xC);
    EXPECT_EQ(extracted.size(), 4);

    vec_64 b{16, 0x1234};
    const vec_64 replacement{4, 0xF};
    b.slice(4, 7) = replacement;
    EXPECT_EQ(static_cast<uint64_t>(b), 0x12F4);

    const auto bit_slice = a.const_slice(0, 0);
    const vec_64 bit = bit_slice.from_slice();
    EXPECT_EQ(static_cast<uint64_t>(bit), 1);
    EXPECT_EQ(bit.size(), 1);
}

TEST(SizedMpint64Test, ResizeOperations) {
    const vec_64 a{8, 0x80};

    const vec_64 b = a.unsigned_resize(16);
    EXPECT_EQ(static_cast<uint64_t>(b), 0x80);
    EXPECT_EQ(b.size(), 16);

    const vec_64 c = a.signed_resize(16);
    EXPECT_EQ(static_cast<uint64_t>(c), 0xFF80);
    EXPECT_EQ(c.size(), 16);

    const vec_64 d{16, 0x1234};
    const vec_64 e = d.unsigned_resize(8);
    EXPECT_EQ(static_cast<uint64_t>(e), 0x34);
    EXPECT_EQ(e.size(), 8);
}

TEST(SizedMpint64Test, Concatenation) {
    const vec_64 a{8, 0xAB};
    const vec_64 b{8, 0xCD};

    vec_64 result = a.concat(b);
    EXPECT_EQ(static_cast<uint64_t>(result), 0xABCD);
    EXPECT_EQ(result.size(), 16);

    const vec_64 c{4, 0xF};
    const vec_64 d{12, 0x123};
    result = c.concat(d);
    EXPECT_EQ(static_cast<uint64_t>(result), 0xF123);
    EXPECT_EQ(result.size(), 16);
}

TEST(SizedMpint64Test, SignOperations) {
    const vec_64 a{8, 0x7F};
    EXPECT_FALSE(a.sign());

    const vec_64 b{8, 0x80};
    EXPECT_TRUE(b.sign());

    const vec_64 c{8, 5};
    const vec_64 negated = -c;
    EXPECT_EQ(static_cast<uint64_t>(negated), 251);
}

TEST(SizedMpint64Test, BoolConversion) {
    const vec_64 zero{8, 0};
    const vec_64 nonzero{8, 42};

    EXPECT_FALSE(static_cast<bool>(zero));
    EXPECT_TRUE(static_cast<bool>(nonzero));
}

TEST(SizedMpint64Test, CompoundAssignmentOperators) {
    vec_64 a{8, 10};
    vec_64 b{8, 5};

    a += b;
    EXPECT_EQ(static_cast<uint64_t>(a), 15);

    a -= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 10);

    a *= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 50);

    a /= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 10);

    a = {8, 23};
    a %= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 3);

    a = {8, 0xAA};
    b = {8, 0x55};

    a |= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 0xFF);

    a = {8, 0xAA};
    a &= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 0x00);

    a = {8, 0xAA};
    a ^= b;
    EXPECT_EQ(static_cast<uint64_t>(a), 0xFF);

    a = {8, 0x0F};
    a <<= 2;
    EXPECT_EQ(static_cast<uint64_t>(a), 0x3C);

    a = {8, 0xF0};
    a >>= 2;
    EXPECT_EQ(static_cast<uint64_t>(a), 0x3C);
}

TEST(SizedMpint64Test, ErrorHandling) {
    vec_64 a{8, 100};
    vec_64 b{16, 200};

    EXPECT_THROW((void) (a + b), std::invalid_argument);
    EXPECT_THROW((void) (a - b), std::invalid_argument);
    EXPECT_THROW((void) (a * b), std::invalid_argument);
    EXPECT_THROW((void) (a / b), std::invalid_argument);
    EXPECT_THROW((void) (a % b), std::invalid_argument);
    EXPECT_THROW((void) (a | b), std::invalid_argument);
    EXPECT_THROW((void) (a & b), std::invalid_argument);
    EXPECT_THROW((void) (a ^ b), std::invalid_argument);
    EXPECT_THROW((void) (a <=> b), std::invalid_argument);

    EXPECT_THROW((void) a.slice(5, 3), std::invalid_argument);
    EXPECT_THROW((void) a.slice(0, 8), std::out_of_range);

    EXPECT_THROW(vec_64::parse("8'x123"), std::invalid_argument);
}

TEST(SizedMpintUnlimitedTest, StringParsing) {
    const std::string literal_a = "793'h" + std::string(1 + 793 / 4, 'F');
    const std::string literal_b = "793'b" + std::string(793, '1');
    EXPECT_EQ(vec_unlimited::parse(literal_a), vec_unlimited::parse(literal_b));
}

TEST(SizedMpintUnlimitedTest, Arithmetic) {
    const auto a = vec_unlimited::parse("1145'h" + std::string(1 + 1145 / 4, 'A'));
    const auto b = vec_unlimited::parse("1145'h" + std::string(1 + 1145 / 4, '5'));
    const auto c = vec_unlimited::parse("1145'h" + std::string(1 + 1145 / 4, 'F'));
    EXPECT_EQ(a + b, c);
    EXPECT_EQ(c - b, a);
    EXPECT_EQ(c - a, b);

    const auto unit = vec_unlimited::parse("773'd2");
    std::string expected_str = "1";
    auto result = vec_unlimited::parse("773'd1");
    for (int i = 0; i < 773; ++i) {
        result *= unit;
        expected_str.push_back('0');
        EXPECT_EQ(result, vec_unlimited::parse("773'b" + expected_str));
    }

    const auto d = vec_unlimited::parse("2000'h" + std::string(1000 / 4, 'A'));
    const auto e = vec_unlimited::parse("2000'h" + std::string(1000 / 4, '5'));
    const auto f = d * e;
    EXPECT_EQ(f / d, e);
    EXPECT_EQ(f / e, d);
    EXPECT_EQ(f % d, f - f / d * d);
    EXPECT_EQ(f % e, f - f / e * e);
}

TEST(SizedMpintUnlimitedTest, BitwiseOperations) {
    const auto a = vec_unlimited::parse("1500'h" + std::string(1500 / 4, 'A'));
    const auto b = vec_unlimited::parse("1500'h" + std::string(1500 / 4, '5'));
    const auto c = vec_unlimited::parse("1500'h" + std::string(1500 / 4, 'F'));
    const auto d = vec_unlimited::parse("1500'h" + std::string(1500 / 4, '0'));

    EXPECT_EQ(a | b, c);
    EXPECT_EQ(a & b, d);
    EXPECT_EQ(a ^ b, c);
    EXPECT_EQ(~a, b);
}
