package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * BR003 检查允许转久悬标志 单元测试。
 *
 * 覆盖 Spec REQ-001 全部场景：条件 a（S01）、条件 b（S02）、条件 c（S03–S06），
 * 以及空值口径边界（null 与空字符串 "" 的区分）。
 */
public class BR003Test {

	// REQ-001-S01 条件 a 命中：账户允许转久悬且显式不允许转久悬，返回否（false）
	@Test
	public void test_01() {
		assertFalse(BR003.execute("Y", "N"));
	}

	// REQ-001-S02 条件 b 命中：账户不允许转久悬且是否允许转久悬未传入（null），返回否（false）
	@Test
	public void test_02() {
		assertFalse(BR003.execute("N", null));
	}

	// REQ-001-S03 条件 c：账户允许转久悬且显式允许转久悬，条件 a、b 均不满足，返回是（true）
	@Test
	public void test_03() {
		assertTrue(BR003.execute("Y", "Y"));
	}

	// REQ-001-S04 条件 c：账户允许转久悬，是否允许转久悬未传入（null），条件 a、b 均不满足，返回是（true）
	@Test
	public void test_04() {
		assertTrue(BR003.execute("Y", null));
	}

	// REQ-001-S05 条件 c：账户不允许转久悬但显式允许转久悬，条件 a、b 均不满足，返回是（true）
	@Test
	public void test_05() {
		assertTrue(BR003.execute("N", "Y"));
	}

	// REQ-001-S06 边界：是否允许转久悬为空字符串 ""（已传入取值，不等于「空」null），落入条件 c，返回是（true）
	@Test
	public void test_06() {
		assertTrue(BR003.execute("N", ""));
	}
}
