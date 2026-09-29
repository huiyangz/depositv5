package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.ClientType;
import com.dcits.depsit.enums.TaxResidentFlag;

/**
 * BR006 设置自贸区种类 单元测试。
 * 用例来源：outputs/测试用例.md（BR006-TC001～TC012）；无平台服务依赖，直接静态调用 BR006.execute(...)，无桩、无 mock。
 */
public class BR006Test {

	/** 个人客户且中国税收居民（a 分支：isIndividual="Y"、VALUE_1）判定为 FTI 3605（REQ-001-S01） */
	@Test
	public void test_01() {
		assertEquals(AcctNatureNo.VALUE_3605,
				BR006.execute("Y", TaxResidentFlag.VALUE_1, ClientType.VALUE_100, "Y"));
	}

	/** 个人客户且双重税收居民（a 分支：isIndividual="Y"、VALUE_3，inlandOffshore="N" 不影响）判定为 FTI 3605（REQ-001-S02） */
	@Test
	public void test_02() {
		assertEquals(AcctNatureNo.VALUE_3605,
				BR006.execute("Y", TaxResidentFlag.VALUE_3, ClientType.VALUE_100, "N"));
	}

	/** 个人客户且非中国税收居民（b 分支：VALUE_100+VALUE_2，a 分支税收居民条件不满足）判定为 FTF 3606（REQ-002-S01） */
	@Test
	public void test_03() {
		assertEquals(AcctNatureNo.VALUE_3606,
				BR006.execute("Y", TaxResidentFlag.VALUE_2, ClientType.VALUE_100, "N"));
	}

	/** 对公境内客户（c 分支：VALUE_200+境内"Y"）判定为 FTE 3603（REQ-003-S01） */
	@Test
	public void test_04() {
		assertEquals(AcctNatureNo.VALUE_3603,
				BR006.execute("N", TaxResidentFlag.VALUE_1, ClientType.VALUE_200, "Y"));
	}

	/** 对公境外客户（d 分支：VALUE_200+境外"N"）判定为 FTN 3604（REQ-004-S01） */
	@Test
	public void test_05() {
		assertEquals(AcctNatureNo.VALUE_3604,
				BR006.execute("N", TaxResidentFlag.VALUE_1, ClientType.VALUE_200, "N"));
	}

	/** 同业境外客户（e 分支：VALUE_300+境外"N"）判定为 FTU 3607（REQ-005-S01） */
	@Test
	public void test_06() {
		assertEquals(AcctNatureNo.VALUE_3607,
				BR006.execute("N", TaxResidentFlag.VALUE_1, ClientType.VALUE_300, "N"));
	}

	/** 同业境内客户（VALUE_300+境内"Y"，e 分支境内境外条件不满足）不设置自贸区种类，返回 null（REQ-006-S01） */
	@Test
	public void test_07() {
		assertNull(BR006.execute("N", TaxResidentFlag.VALUE_1, ClientType.VALUE_300, "Y"));
	}

	/** 个人非中国税收居民且境内标志"Y"（b 分支不评估 inlandOffshore）仍判定为 FTF 3606（REQ-002"仅评估"条款补充） */
	@Test
	public void test_08() {
		assertEquals(AcctNatureNo.VALUE_3606,
				BR006.execute("Y", TaxResidentFlag.VALUE_2, ClientType.VALUE_100, "Y"));
	}

	/** 对公境内且税收居民 VALUE_2（c 分支不评估 taxResidentFlag）仍判定为 FTE 3603（REQ-003"仅评估"条款补充） */
	@Test
	public void test_09() {
		assertEquals(AcctNatureNo.VALUE_3603,
				BR006.execute("N", TaxResidentFlag.VALUE_2, ClientType.VALUE_200, "Y"));
	}

	/** 对公境外且税收居民 VALUE_2（d 分支不评估 taxResidentFlag）仍判定为 FTN 3604（REQ-004"仅评估"条款补充） */
	@Test
	public void test_10() {
		assertEquals(AcctNatureNo.VALUE_3604,
				BR006.execute("N", TaxResidentFlag.VALUE_2, ClientType.VALUE_200, "N"));
	}

	/** 同业境外且税收居民 VALUE_2（e 分支不评估 taxResidentFlag）仍判定为 FTU 3607（REQ-005"仅评估"条款补充） */
	@Test
	public void test_11() {
		assertEquals(AcctNatureNo.VALUE_3607,
				BR006.execute("N", TaxResidentFlag.VALUE_2, ClientType.VALUE_300, "N"));
	}

	/** 客户类型为 a–e 未涉及取值 VALUE_600-内部客户时不满足任何条件分支，返回 null（REQ-006 正文补充） */
	@Test
	public void test_12() {
		assertNull(BR006.execute("N", TaxResidentFlag.VALUE_1, ClientType.VALUE_600, "Y"));
	}
}
