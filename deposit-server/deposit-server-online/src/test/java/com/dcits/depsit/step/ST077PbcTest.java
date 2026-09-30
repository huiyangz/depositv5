package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.AcctCcy;
import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.RbBusAcctPurpose;
import com.dcits.depsit.facade.bo.ST077InputBO;
import com.dcits.depsit.facade.bo.ST077OutputBO;

/**
 * ST077 检查账户用途 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST077-TC001 ~ TC028），
 * 预期结果来自正式 Spec ST077（fileInputs.spec 绑定的 ST077.md）。
 * 本步骤为纯内存判定（无数据访问、无外部调用、无组件内步骤调用），不设依赖桩；
 * 失败用例仅断言 succeed 与 errorCode（errorMessage 内容 Spec 未定义、不断言）。
 */
@ExtendWith(MockitoExtension.class)
public class ST077PbcTest {

	/** 核准件编号非空示例值（Spec 场景 GIVEN） */
	private static final String APPR_LETTER_NO = "ZJ20260930001";

	@InjectMocks
	private ST077Pbc st077;

	/** 构造输入：四个字段按用例 GIVEN 取值，null 表示该字段为空 */
	private ST077InputBO input(AcctCcy acctCcy, RbBusAcctPurpose rbBusAcctPurpose,
			String apprLetterNo, AcctNatureNo acctNatureNo) {
		ST077InputBO input = new ST077InputBO();
		input.setAcctCcy(acctCcy);
		input.setRbBusAcctPurpose(rbBusAcctPurpose);
		input.setApprLetterNo(apprLetterNo);
		input.setAcctNatureNo(acctNatureNo);
		return input;
	}

	/** 断言步骤成功：succeed=true、错误码与错误信息均为 null */
	private void assertSuccess(ST077OutputBO output) {
		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
	}

	/** 断言业务失败：succeed=false、errorCode 等于指定错误码（errorMessage 内容不断言） */
	private void assertFailure(ST077OutputBO output, String errorCode) {
		assertFalse(output.isSucceed());
		assertEquals(errorCode, output.getErrorCode());
	}

	/** 人民币资本项下且核准件编号为空→步骤1命中，立即返回 ER0012，步骤2及后续判定不执行 */
	@Test
	public void testST077T01() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_501, null, AcctNatureNo.VALUE_11001));
		assertFailure(output, "ER0012");
	}

	/** 人民币资本项下且核准件编号非空→步骤1不触发流程继续，路由子步骤6，用途“资本项下”不在专用户允许集合→ER0016 */
	@Test
	public void testST077T02() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_501, APPR_LETTER_NO, AcctNatureNo.VALUE_11004));
		assertFailure(output, "ER0016");
	}

	/** 币种非人民币元（美元）→步骤1前置不成立不检查（核准件编号为空不报错），属性“临时存款账户”未路由→步骤成功 */
	@Test
	public void testST077T03() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.USD, RbBusAcctPurpose.VALUE_501, null, AcctNatureNo.VALUE_11003));
		assertSuccess(output);
	}

	/** 用途非资本项下（“无特殊用途”）→步骤1前置不成立，路由子步骤4，用途等于“无特殊用途”判定通过 */
	@Test
	public void testST077T04() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_0, null, AcctNatureNo.VALUE_11001));
		assertSuccess(output);
	}

	/** 人民币资本项下、核准件编号非空、账户属性为空→步骤1通过、步骤2命中 ER0013，步骤3不执行 */
	@Test
	public void testST077T05() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_501, APPR_LETTER_NO, null));
		assertFailure(output, "ER0013");
	}

	/** 人民币资本项下、核准件编号非空、账户属性非空（基本存款账户）→步骤2不触发，路由子步骤4，用途非空且非“无特殊用途”→ER0014 */
	@Test
	public void testST077T06() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_501, APPR_LETTER_NO, AcctNatureNo.VALUE_11001));
		assertFailure(output, "ER0014");
	}

	/** 币种非人民币元且账户属性为空→步骤2前置不成立不触发（外币不强制账户属性），无子步骤路由→步骤成功 */
	@Test
	public void testST077T07() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.USD, RbBusAcctPurpose.VALUE_501, null, null));
		assertSuccess(output);
	}

	/** 属性“基本存款账户”→路由且仅路由子步骤4，用途“结算性”非空且非“无特殊用途”→ER0014（该码仅子步骤4可产生） */
	@Test
	public void testST077T08() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_6, null, AcctNatureNo.VALUE_11001));
		assertFailure(output, "ER0014");
	}

	/** 属性“一般存款账户”（路由变体）→同样仅路由子步骤4，用途“结算性”→ER0014 */
	@Test
	public void testST077T09() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_6, null, AcctNatureNo.VALUE_11002));
		assertFailure(output, "ER0014");
	}

	/** 属性“验资户”→路由且仅路由子步骤5，用途“结算性”不在允许集合→ER0015（该码仅子步骤5可产生） */
	@Test
	public void testST077T10() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_6, null, AcctNatureNo.VALUE_17));
		assertFailure(output, "ER0015");
	}

	/** 属性“专用存款账户”→路由且仅路由子步骤6，用途“结算性”不在允许集合→ER0016（该码仅子步骤6可产生） */
	@Test
	public void testST077T11() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_6, null, AcctNatureNo.VALUE_11004));
		assertFailure(output, "ER0016");
	}

	/** 属性为未列出取值“临时存款账户”→不路由任何子步骤、无业务失败→步骤成功 */
	@Test
	public void testST077T12() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_901, null, AcctNatureNo.VALUE_11003));
		assertSuccess(output);
	}

	/** 属性为空（null）→不路由任何子步骤（空属性不触发任何检查）→步骤成功 */
	@Test
	public void testST077T13() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_901, null, null));
		assertSuccess(output);
	}

	/** 子步骤4：属性“一般存款账户”、用途“无特殊用途”→判定通过 */
	@Test
	public void testST077T14() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_0, null, AcctNatureNo.VALUE_11002));
		assertSuccess(output);
	}

	/** 子步骤4：属性“一般存款账户”、用途为空→“不为空”必要条件不成立、不触发 ER0014→通过 */
	@Test
	public void testST077T15() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, null, null, AcctNatureNo.VALUE_11002));
		assertSuccess(output);
	}

	/** 子步骤4空用途变体：属性“基本存款账户”、用途为空→通过 */
	@Test
	public void testST077T16() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, null, null, AcctNatureNo.VALUE_11001));
		assertSuccess(output);
	}

	/** 子步骤5：验资户、用途“注册验资”→允许集合内判定通过（三取值之一） */
	@Test
	public void testST077T17() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_1, null, AcctNatureNo.VALUE_17));
		assertSuccess(output);
	}

	/** 子步骤5：验资户、用途“增资验资”→允许集合内判定通过（三取值之二） */
	@Test
	public void testST077T18() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_2, null, AcctNatureNo.VALUE_17));
		assertSuccess(output);
	}

	/** 子步骤5：验资户、用途“无特殊用途”→允许集合内判定通过（三取值之三） */
	@Test
	public void testST077T19() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_0, null, AcctNatureNo.VALUE_17));
		assertSuccess(output);
	}

	/** 子步骤5：验资户、用途为空→不等于任一允许取值（无“不为空”限定）→ER0015 */
	@Test
	public void testST077T20() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, null, null, AcctNatureNo.VALUE_17));
		assertFailure(output, "ER0015");
	}

	/** 子步骤5：验资户、用途“预算单位专用”（允许集合外取值）→ER0015 */
	@Test
	public void testST077T21() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_4, null, AcctNatureNo.VALUE_17));
		assertFailure(output, "ER0015");
	}

	/** 子步骤6：专用存款账户、用途“预算单位专用”→允许集合内判定通过（两取值之一） */
	@Test
	public void testST077T22() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_4, null, AcctNatureNo.VALUE_11004));
		assertSuccess(output);
	}

	/** 子步骤6：专用存款账户、用途“非预算单位专用”→允许集合内判定通过（两取值之二） */
	@Test
	public void testST077T23() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_3, null, AcctNatureNo.VALUE_11004));
		assertSuccess(output);
	}

	/** 子步骤6：专用存款账户、用途为空→不等于任一允许取值→ER0016 */
	@Test
	public void testST077T24() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, null, null, AcctNatureNo.VALUE_11004));
		assertFailure(output, "ER0016");
	}

	/** 子步骤6：专用存款账户、用途“无特殊用途”（允许集合外取值，含“无特殊用途”对照）→ER0016 */
	@Test
	public void testST077T25() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_0, null, AcctNatureNo.VALUE_11004));
		assertFailure(output, "ER0016");
	}

	/** 步骤1、2条件同时命中（核准件编号与账户属性均为空）→步骤1先判定并短路，仅返回 ER0012、非 ER0013 */
	@Test
	public void testST077T26() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_501, null, null));
		assertFailure(output, "ER0012");
		assertNotEquals("ER0013", output.getErrorCode());
	}

	/** 前置检查全部通过（核准件编号非空、账户属性非空但“临时存款账户”未路由）→无子步骤命中→全链路成功 */
	@Test
	public void testST077T27() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.CNY, RbBusAcctPurpose.VALUE_501, APPR_LETTER_NO, AcctNatureNo.VALUE_11003));
		assertSuccess(output);
	}

	/** 前置检查整体跳过（外币）后子步骤仍执行：属性“专用存款账户”路由子步骤6，用途“资本项下”不在允许集合→ER0016（非 ER0012） */
	@Test
	public void testST077T28() {
		ST077OutputBO output = st077.execute(
				input(AcctCcy.USD, RbBusAcctPurpose.VALUE_501, null, AcctNatureNo.VALUE_11004));
		assertFailure(output, "ER0016");
		assertNotEquals("ER0012", output.getErrorCode());
	}
}
