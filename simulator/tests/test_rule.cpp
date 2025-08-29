//
// Created by nairen on 29/08/2025.
//

#include <gtest/gtest.h>
#include <print>

#include "rule.h"
#include "circuit_def.h"
#include "sized_mpint.h"

using namespace nxon;

TEST(RuleTest, ConstructionAndAccessors) {
    value_storage values;
    values[1] = value_t(8, 114);
    const auto src = data_factory::constant_source(value_t{8, 42});
    const auto sink = data_factory::id_sink(1, 8);
    const auto rule = rule_factory::id_rule(src, sink);

    EXPECT_EQ(rule.triggers().size(), 0);
    EXPECT_EQ(rule.dependencies().size(), 0);
    EXPECT_EQ(rule.outcomes().size(), 1);
    EXPECT_EQ(*rule.outcomes().begin(), 1);
}

TEST(RuleTest, UnaryRulePerform) {
    value_storage values;
    values[2] = value_t(8, 0xAA);
    const auto src = data_factory::constant_source(value_t{8, 0xAA});
    const auto sink = data_factory::id_sink(2, 8);
    const auto rule = rule_factory::not_rule(src, sink);

    const auto changed = rule.perform(values);
    EXPECT_TRUE(changed->contains(2));
    EXPECT_EQ(static_cast<uint64_t>(values[2]), 0x55);
}

TEST(RuleTest, BinaryRulePerform) {
    value_storage values;
    values[3] = value_t(8, 0xFF);
    const auto src_a = data_factory::constant_source(value_t{8, 0xF0});
    const auto src_b = data_factory::constant_source(value_t{8, 0x0F});
    const auto sink = data_factory::id_sink(3, 8);
    const auto rule = rule_factory::and_rule(src_a, src_b, sink);

    const auto changed = rule.perform(values);
    EXPECT_TRUE(changed->contains(3));
    EXPECT_EQ(static_cast<uint64_t>(values[3]), 0x00);
}

TEST(RuleTest, RegisterRulePerform) {
    value_storage values;
    values[10] = value_t{1, 1}; // clock high
    const auto clk = data_factory::id_clock(10, true);
    const auto src = data_factory::constant_source(value_t{8, 0xAB});
    const auto sink = data_factory::id_sink(4, 8);
    const auto rule = rule_factory::register_rule(clk, src, sink);

    const auto changed = rule.perform(values);
    values.perform_delayed();
    EXPECT_TRUE(changed->contains(4));
    EXPECT_EQ(static_cast<uint64_t>(values[4]), 0xAB);
}

TEST(RuleTest, AsyncRegisterRulePerform) {
    value_storage values;
    values[20] = value_t{1, 1}; // clock high
    values[21] = value_t{1, 1}; // reset high
    const auto clk = data_factory::id_clock(20, true);
    const auto rst = data_factory::id_source(21, 1);
    const auto src = data_factory::constant_source(value_t{8, 0xCD});
    const auto sink = data_factory::id_sink(5, 8);
    const auto rule = rule_factory::async_register_rule(clk, rst, src, sink, true, value_t{8, 0x00});

    const auto changed = rule.perform(values);
    EXPECT_TRUE(changed->contains(5));
    EXPECT_EQ(static_cast<uint64_t>(values[5]), 0x00);
}

TEST(RuleTest, RuleSetUpdateAndRefresh) {
    value_storage values;
    values[6] = value_t{8, 0x00};
    const auto src = data_factory::constant_source(value_t{8, 0x12});
    const auto sink = data_factory::id_sink(6, 8);
    const auto rule = rule_factory::id_rule(src, sink);

    rule_set rules;
    rules.add(rule);

    const id_set hint{6};
    const auto changed = rules.update(values, hint);
    EXPECT_EQ(static_cast<uint64_t>(values[6]), 0x0);

    values[6] = value_t{8, 0x34};
    const auto changed2 = rules.refresh_all_rules(values);
    EXPECT_TRUE(changed2.contains(6));
    EXPECT_EQ(static_cast<uint64_t>(values[6]), 0x12);
}

TEST(RuleTest, ArithmeticRules) {
    value_storage values;
    values[100] = value_t{8, 10};
    values[101] = value_t{8, 5};
    values[102] = value_t{8, 0};
    const auto src_a = data_factory::id_source(100, 8);
    const auto src_b = data_factory::id_source(101, 8);
    const auto sink = data_factory::id_sink(102, 8);

    const auto add_rule = rule_factory::add_rule(src_a, src_b, sink);
    add_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[102]), 15);

    const auto sub_rule = rule_factory::sub_rule(src_a, src_b, sink);
    sub_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[102]), 5);

    const auto mul_rule = rule_factory::mul_rule(src_a, src_b, sink);
    mul_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[102]), 50);

    const auto div_rule = rule_factory::div_rule(src_a, src_b, sink);
    div_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[102]), 2);

    const auto mod_rule = rule_factory::mod_rule(src_a, src_b, sink);
    mod_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[102]), 0);
}

TEST(RuleTest, BitwiseRules) {
    value_storage values;
    values[110] = value_t{8, 0xAA};
    values[111] = value_t{8, 0x55};
    values[112] = value_t{8, 0x00};
    const auto src_a = data_factory::id_source(110, 8);
    const auto src_b = data_factory::id_source(111, 8);
    const auto sink = data_factory::id_sink(112, 8);

    const auto and_rule = rule_factory::and_rule(src_a, src_b, sink);
    and_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[112]), 0x00);

    const auto or_rule = rule_factory::or_rule(src_a, src_b, sink);
    or_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[112]), 0xFF);

    const auto xor_rule = rule_factory::xor_rule(src_a, src_b, sink);
    xor_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[112]), 0xFF);

    const auto not_rule = rule_factory::not_rule(src_a, sink);
    not_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[112]), 0x55);
}

TEST(RuleTest, ShiftRules) {
    value_storage values;
    values[120] = value_t{8, 0x0F};
    values[121] = value_t{8, 2};
    values[122] = value_t{8, 0};
    const auto src_a = data_factory::id_source(120, 8);
    const auto src_b = data_factory::id_source(121, 8);
    const auto sink = data_factory::id_sink(122, 8);

    const auto shl_rule = rule_factory::shl_rule(src_a, src_b, sink);
    shl_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[122]), 0x3C);

    values[120] = value_t{8, 0xF0};
    const auto lshr_rule = rule_factory::logical_shr_rule(src_a, src_b, sink);
    lshr_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[122]), 0x3C);

    values[120] = value_t{8, 0x80};
    const auto ashr_rule = rule_factory::arithmetic_shr_rule(src_a, src_b, sink);
    ashr_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[122]), 0xE0);
}

TEST(RuleTest, ComparisonRules) {
    value_storage values;
    values[130] = value_t{8, 10};
    values[131] = value_t{8, 20};
    values[132] = value_t{8, 0};
    const auto src_a = data_factory::id_source(130, 8);
    const auto src_b = data_factory::id_source(131, 8);
    const auto sink = data_factory::id_sink(132, 1);

    const auto lt_rule = rule_factory::unsigned_lt_rule(src_a, src_b, sink);
    lt_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[132]), 1);

    const auto gt_rule = rule_factory::unsigned_gt_rule(src_a, src_b, sink);
    gt_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[132]), 0);

    const auto le_rule = rule_factory::unsigned_le_rule(src_a, src_b, sink);
    le_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[132]), 1);

    const auto ge_rule = rule_factory::unsigned_ge_rule(src_a, src_b, sink);
    ge_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[132]), 0);

    const auto eq_rule = rule_factory::eq_rule(src_a, src_b, sink);
    eq_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[132]), 0);

    const auto neq_rule = rule_factory::neq_rule(src_a, src_b, sink);
    neq_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[132]), 1);
}

TEST(RuleTest, SignedComparisonRules) {
    value_storage values;
    values[140] = value_t{8, 0x80}; // -128
    values[141] = value_t{8, 0x7F}; // 127
    values[142] = value_t{8, 0};
    const auto src_a = data_factory::id_source(140, 8);
    const auto src_b = data_factory::id_source(141, 8);
    const auto sink = data_factory::id_sink(142, 1);

    const auto slt_rule = rule_factory::signed_lt_rule(src_a, src_b, sink);
    slt_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[142]), 1);

    const auto sgt_rule = rule_factory::signed_gt_rule(src_a, src_b, sink);
    sgt_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[142]), 0);

    const auto sle_rule = rule_factory::signed_le_rule(src_a, src_b, sink);
    sle_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[142]), 1);

    const auto sge_rule = rule_factory::signed_ge_rule(src_a, src_b, sink);
    sge_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[142]), 0);
}

TEST(RuleTest, ConcatRule) {
    value_storage values;
    values[150] = value_t{8, 0xAB};
    values[151] = value_t{8, 0xCD};
    values[152] = value_t{8, 0x00};
    const auto src_a = data_factory::id_source(150, 8);
    const auto src_b = data_factory::id_source(151, 8);
    const auto sink = data_factory::id_sink(152, 16);

    const auto concat_rule = rule_factory::concat_rule(src_a, src_b, sink);
    concat_rule.perform(values);
    EXPECT_EQ(static_cast<uint64_t>(values[152]), 0xABCD);
}

TEST(RuleTest, ResizeSinkAdapters) {
    value_storage values;
    values[160] = value_t{8, 0x80};
    const auto sink = data_factory::id_sink(160, 8);
    const auto signed_adapter = data_factory::signed_resize_sink_adapter(sink);
    const auto unsigned_adapter = data_factory::unsigned_resize_sink_adapter(sink);

    signed_adapter.put(values, value_t{16, 0xFF80});
    EXPECT_EQ(static_cast<uint64_t>(values[160]), 0x80);

    unsigned_adapter.put(values, value_t{16, 0x80});
    EXPECT_EQ(static_cast<uint64_t>(values[160]), 0x80);
}
