package com.dcits.depsit.rule;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * BR002-计算账户利率 单元测试
 *
 * 用例来源：outputs/测试用例.md（BR002-TC001~TC014，依据正式 Spec docs/specs/BR002.md）。
 * 无平台服务依赖，直接静态调用 {@link BR002#execute}；所有 BigDecimal 值一律以字符串构造，
 * 断言采用 compareTo 数值比较口径（0.030 与 0.03 视为相等）。
 */
public class BR002Test {

    // 场景：浮动百分点命中（分支a），acctSpreadRate=0.005、其余为 0；预期 realRate=0.025+0.005=0.030（REQ-001-S01）
    @Test
    public void test_01() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0.005"), new BigDecimal("0"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.030")));
    }

    // 场景：浮动百分比命中（分支b），acctSpreadRate=0、acctPercentRate=0.1；预期 realRate=0.025×1.1=0.0275（REQ-001-S02）
    @Test
    public void test_02() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0"), new BigDecimal("0.1"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.0275")));
    }

    // 场景：账户固定利率命中（分支c），前两项为 0、acctFixedRate=0.031；预期 realRate=0.031（不使用 productRate，REQ-001-S03）
    @Test
    public void test_03() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0.031"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.031")));
    }

    // 场景：三分支条件同时满足时分支a优先；预期仅命中分支a，realRate=0.030（非分支b 的 0.0275 或分支c 的 0.031，REQ-001-S04）
    @Test
    public void test_04() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0.005"), new BigDecimal("0.1"), new BigDecimal("0.031"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.030")));
    }

    // 场景：分支b、c 同时满足时分支b优先；预期仅命中分支b，realRate=0.0275（非分支c 的 0.031，REQ-001-S05）
    @Test
    public void test_05() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0"), new BigDecimal("0.1"), new BigDecimal("0.031"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.0275")));
    }

    // 场景：负值浮动百分点命中分支a，acctSpreadRate=-0.005 非 0；预期 realRate=0.025+(-0.005)=0.020（REQ-001-S06）
    @Test
    public void test_06() {
        BigDecimal result = BR002.execute(
                new BigDecimal("-0.005"), new BigDecimal("0"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.020")));
    }

    // 场景：负值浮动百分比命中分支b，acctPercentRate=-0.1 非 0；预期 realRate=0.025×0.9=0.0225（REQ-001-S07）
    @Test
    public void test_07() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0"), new BigDecimal("-0.1"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.0225")));
    }

    // 场景：数值上等于 0 的不同标度表示（0.00）不命中分支a；预期命中分支b，realRate=0.0275（REQ-001-S08）
    @Test
    public void test_08() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0.00"), new BigDecimal("0.1"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.0275")));
    }

    // 场景：浮动百分点为 null 时对应分支不命中并继续判定；预期命中分支b，realRate=0.0275（REQ-002-S01）
    @Test
    public void test_09() {
        BigDecimal result = BR002.execute(
                null, new BigDecimal("0.1"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.0275")));
    }

    // 场景：前两项浮动为 null 时固定利率命中；预期命中分支c，realRate=0.031（REQ-002-S02）
    @Test
    public void test_10() {
        BigDecimal result = BR002.execute(
                null, null, new BigDecimal("0.031"),
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.031")));
    }

    // 场景：首分支已命中时后续分支字段为 null 不影响结果；预期命中分支a，realRate=0.030（REQ-002-S03）
    @Test
    public void test_11() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0.005"), null, null,
                new BigDecimal("0.025"));
        assertEquals(0, result.compareTo(new BigDecimal("0.030")));
    }

    // 场景：三项浮动均为 0 时无分支命中；预期 realRate=null（不返回任何默认利率，包括 productRate，REQ-003-S01）
    @Test
    public void test_12() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"),
                new BigDecimal("0.025"));
        assertNull(result);
    }

    // 场景：三项浮动均为 null 时无分支命中（null 与 0 分支选择行为一致）；预期 realRate=null（REQ-003-S02）
    @Test
    public void test_13() {
        BigDecimal result = BR002.execute(
                null, null, null,
                new BigDecimal("0.025"));
        assertNull(result);
    }

    // 场景：0 与 null 混合且均不满足条件（acctFixedRate=0.00 数值上等于 0）；预期 realRate=null（REQ-003-S03）
    @Test
    public void test_14() {
        BigDecimal result = BR002.execute(
                new BigDecimal("0"), null, new BigDecimal("0.00"),
                new BigDecimal("0.025"));
        assertNull(result);
    }
}
