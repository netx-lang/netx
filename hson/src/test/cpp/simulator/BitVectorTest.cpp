#include <bitset>
#include <gtest/gtest.h>
#include "model/BitVector.h"

using namespace hson;

TEST(BinaryBitVector, ParseValidLiteral) {
    const auto bv1 = BinaryBitVector::parse("20'd114514");
    const auto bv2 = BinaryBitVector::parse("20'h1BF52");
    const auto bv3 = BinaryBitVector::parse("20'o337522");
    const auto bv4 = BinaryBitVector::parse("20'b00011011111101010010");
    ASSERT_EQ(*bv1, *bv2);
    ASSERT_EQ(*bv2, *bv3);
    ASSERT_EQ(*bv3, *bv4);
    delete bv1;
    delete bv2;
    delete bv3;
    delete bv4;
}

TEST(BinaryBitVector, ParseInvalidLiteral) {
    ASSERT_THROW(auto _ = BinaryBitVector::parse("invalid"), std::invalid_argument);
}

TEST(BinaryBitVector, GetBit) {
    const auto bv = BinaryBitVector::parse("8'b10101010");
    ASSERT_FALSE(bv->get(0));
    ASSERT_TRUE(bv->get(1));
    delete bv;
}

TEST(BinaryBitVector, SetBit) {
    const auto bv = BinaryBitVector::parse("8'b00000000");
    bv->set(0, true);
    ASSERT_TRUE(bv->get(0));
    delete bv;
}

TEST(BinaryBitVector, AndOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b10101010");
    const auto bv2 = BinaryBitVector::parse("8'b11001100");
    const auto result = bv1->AND(*bv2);
    ASSERT_EQ(result->toUL(), 0b10001000);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, OrOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b10101010");
    const auto bv2 = BinaryBitVector::parse("8'b11001100");
    const auto result = bv1->OR(*bv2);
    ASSERT_EQ(result->toUL(), 0b11101110);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, XorOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b10101010");
    const auto bv2 = BinaryBitVector::parse("8'b11001100");
    const auto result = bv1->XOR(*bv2);
    ASSERT_EQ(result->toUL(), 0b01100110);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, NotOperation) {
    const auto bv = BinaryBitVector::parse("8'b10101010");
    const auto result = bv->NOT();
    ASSERT_EQ(result->toUL(), 0b01010101);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, AddOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b00000001");
    const auto bv2 = BinaryBitVector::parse("8'b00000001");
    const auto result = bv1->ADD(*bv2);
    ASSERT_EQ(result->toUL(), 2);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, SubOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b00000010");
    const auto bv2 = BinaryBitVector::parse("8'b00000001");
    const auto result = bv1->SUB(*bv2);
    ASSERT_EQ(result->toUL(), 1);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, MulOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b00000010");
    const auto bv2 = BinaryBitVector::parse("8'b00000010");
    const auto result = bv1->MUL(*bv2);
    ASSERT_EQ(result->toUL(), 4);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, DivOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b00000100");
    const auto bv2 = BinaryBitVector::parse("8'b00000010");
    const auto result = bv1->DIV(*bv2);
    ASSERT_EQ(result->toUL(), 2);
    delete bv1;
    delete bv2;
    delete result;
}

TEST(BinaryBitVector, NegOperation) {
    const auto bv = BinaryBitVector::parse("8'b00000001");
    const auto result = bv->NEG();
    ASSERT_EQ(result->toUL(), 0b11111111);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, AshrOperation) {
    const auto bv = BinaryBitVector::parse("8'b10000000");
    const auto result = bv->ASHR(1);
    ASSERT_EQ(result->toUL(), 0b11000000);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, LshrOperation) {
    const auto bv = BinaryBitVector::parse("8'b10000000");
    const auto result = bv->LSHR(1);
    ASSERT_EQ(result->toUL(), 0b01000000);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, ShlOperation) {
    const auto bv = BinaryBitVector::parse("8'b00000001");
    const auto result = bv->SHL(1);
    ASSERT_EQ(result->toUL(), 0b00000010);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, SelectOperation) {
    const auto bv = BinaryBitVector::parse("8'b10101010");
    const auto result = bv->SELECT(3, 0);
    ASSERT_EQ(result->toUL(), 0b1010);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, Equality) {
    const auto bv1 = BinaryBitVector::parse("8'b10101010");
    const auto bv2 = BinaryBitVector::parse("8'b10101010");
    ASSERT_EQ(*bv1, *bv2);
    delete bv1;
    delete bv2;
}

TEST(BinaryBitVector, Inequality) {
    const auto bv1 = BinaryBitVector::parse("8'b10101010");
    const auto bv2 = BinaryBitVector::parse("8'b10101011");
    ASSERT_NE(*bv1, *bv2);
    delete bv1;
    delete bv2;
}

TEST(BinaryBitVector, CastOperation) {
    const auto bv = BinaryBitVector::parse("8'b10101010");
    const auto result = bv->CAST(16);
    ASSERT_EQ(result->toUL(), 0b1111111110101010);
    delete bv;
    delete result;
}

TEST(BinaryBitVector, SetRangeOperation) {
    const auto bv1 = BinaryBitVector::parse("8'b00000000");
    const auto bv2 = BinaryBitVector::parse("4'b1010");
    bv1->setRange(3, 0, bv2);
    ASSERT_EQ(bv1->toUL(), 0b00001010);
    delete bv1;
    delete bv2;
}
