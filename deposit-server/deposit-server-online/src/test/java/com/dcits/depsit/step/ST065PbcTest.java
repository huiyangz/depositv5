package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.facade.bo.ST065InputBO;
import com.dcits.depsit.facade.bo.ST065OutputBO;

/**
 * ST065 设置账户开户日期 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST065-TC001、ST065-TC002），
 * 预期结果来自正式 Spec docs/specs/ST065.md。
 * 本步骤为纯赋值步骤，无 BCC、规则、跨组件客户端或组件内步骤调用依赖，
 * 不设任何桩，直接以真实 ST065Pbc 实例驱动，不验证调用次数或顺序。
 */
public class ST065PbcTest {

	/** 构造步骤输入 */
	private static ST065InputBO input(Date runDate) {
		ST065InputBO input = new ST065InputBO();
		input.setRunDate(runDate);
		return input;
	}

	// REQ-001-S01、REQ-002-S01、REQ-003-S01 正常路径：Spec 示例日期，runDate 经[系统日期]值传递至 acctOpenDate 返回
	@Test
	public void testST065T01() {
		ST065InputBO input = input(Date.from(Instant.parse("2026-09-30T00:00:00Z")));

		ST065OutputBO output = new ST065Pbc().execute(input);

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals(input.getRunDate(), output.getAcctOpenDate());
	}

	// REQ-001 MUST NOT 另行取运行环境时钟、REQ-002-S01、REQ-003-S01 正常路径：与环境时钟可区分的固定日期，取值来源为输入 runDate
	@Test
	public void testST065T02() {
		ST065InputBO input = input(Date.from(Instant.parse("2026-01-01T00:00:00Z")));

		ST065OutputBO output = new ST065Pbc().execute(input);

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals(input.getRunDate(), output.getAcctOpenDate());
	}
}
