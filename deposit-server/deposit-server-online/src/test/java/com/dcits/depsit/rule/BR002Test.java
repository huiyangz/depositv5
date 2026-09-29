package com.dcits.depsit.rule;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * BR002 计算账户利率 单元测试
 *
 * <p>依据正式 Spec BR002 与测试用例 BR002-TC001～TC010：优先级链 a→b→c→d，
 * 空值与任意标度数值零均判为等于 0；断言数值与标度精确相等。</p>
 */
class BR002Test {

    // REQ-001-S01 浮动百分点为正值：a 分支命中，realRate = 0.015 + 0.002 = 0.017（标度 3）
    @Test
    void test_01() {
        BigDecimal realRate = BR002.execute(
                new BigDecimal("0.002"), null, null, new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.017"), realRate);
    }

    // REQ-001-S02 浮动百分点为负值：负值非零 a 条件成立，realRate = 0.015 + (-0.002) = 0.013（标度 3）
    @Test
    void test_02() {
        BigDecimal realRate = BR002.execute(
                new BigDecimal("-0.002"), null, null, new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.013"), realRate);
    }

    // REQ-001-S03 三项调整同时非零：a 优先于 b、c，仅按浮动百分点加点，realRate = 0.017（标度 3）
    @Test
    void test_03() {
        BigDecimal realRate = BR002.execute(
                new BigDecimal("0.002"), new BigDecimal("0.1"), new BigDecimal("0.02"),
                new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.017"), realRate);
    }

    // REQ-002-S01 点数为数值零 0.00、百分比 0.1 非零：b 命中且优先于 c，realRate = 0.015 × (1 + 0.1) = 0.0165（标度 4）
    @Test
    void test_04() {
        BigDecimal realRate = BR002.execute(
                new BigDecimal("0.00"), new BigDecimal("0.1"), new BigDecimal("0.02"),
                new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.0165"), realRate);
    }

    // REQ-002-S02 浮动百分点为空：空值视同 0 a 不成立，b 命中按比例计算，realRate = 0.015 × 1.1 = 0.0165（标度 4）
    @Test
    void test_05() {
        BigDecimal realRate = BR002.execute(
                null, new BigDecimal("0.1"), null, new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.0165"), realRate);
    }

    // REQ-002-S03 浮动百分比为负值：b 条件成立，比例因子 1 + (-0.1) = 0.9，realRate = 0.015 × 0.9 = 0.0135（标度 4）
    @Test
    void test_06() {
        BigDecimal realRate = BR002.execute(
                null, new BigDecimal("-0.1"), null, new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.0135"), realRate);
    }

    // REQ-003-S01 固定利率为正值：a 为空、b 为 0.00 均不成立，c 直取 acctFixedRate，realRate = 0.02（原值原标度）
    @Test
    void test_07() {
        BigDecimal realRate = BR002.execute(
                null, new BigDecimal("0.00"), new BigDecimal("0.02"), new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.02"), realRate);
    }

    // REQ-003-S02 固定利率为负值：a、b 均空不成立，c 直取负值，realRate = -0.005（原值原标度）
    @Test
    void test_08() {
        BigDecimal realRate = BR002.execute(
                null, null, new BigDecimal("-0.005"), new BigDecimal("0.015"));
        assertEquals(new BigDecimal("-0.005"), realRate);
    }

    // REQ-004-S01 三个账户利率字段均为空：均视同 0，a、b、c 均不成立，d 默认返回 prodRate = 0.015（原值原标度）
    @Test
    void test_09() {
        BigDecimal realRate = BR002.execute(
                null, null, null, new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.015"), realRate);
    }

    // REQ-004-S02 空值与数值零混合（0.000、空、0）：任意标度数值零与空值同样判为等于 0，d 返回 prodRate = 0.015
    @Test
    void test_10() {
        BigDecimal realRate = BR002.execute(
                new BigDecimal("0.000"), null, new BigDecimal("0"), new BigDecimal("0.015"));
        assertEquals(new BigDecimal("0.015"), realRate);
    }
}
