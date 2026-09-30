package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.CheckObjType;
import com.dcits.depsit.enums.TermType;
import com.dcits.depsit.facade.bo.ST102InputBO;
import com.dcits.depsit.facade.bo.ST102OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.components.IRbLimitSceneDefBcc;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;
import com.dcits.depsit.facade.eo.RbLimitSceneDefEO;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;

/**
 * ST102 登记累计限额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST102-TC001 ~ TC016），
 * 预期结果来自正式 Spec ST102（fileInputs.spec 绑定的 ST102.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；登记条件不满足（TC004/TC005）
 * 是正常返回且六输出均为 null，不是失败；TC016 为技术异常原样传播路径。
 * 日期断言按 yyyy-MM-dd 口径（时间部分不断言）；金额断言用 assertEquals 精确相等
 * （{交易金额}原样赋值、不计算不舍入）；写入 EO 经 create 桩捕获后逐字段断言，
 * 不使用 verify 类交互验证（工程既有约定）。
 */
@ExtendWith(MockitoExtension.class)
public class ST102PbcTest {

	/** 限额场景编码（Spec 示例数据） */
	private static final String LIMIT_SCENE_NO = "DEP0001";

	/** 账号（Spec 示例数据） */
	private static final String BASE_ACCT_NO = "1002003004005006";

	/** 客户号（Spec 示例数据） */
	private static final String CLIENT_NO = "C0001";

	/** 交易参考号（Spec 示例数据） */
	private static final String REFERENCE = "REF20260615000001";

	/** 核心运行日期（Spec 示例数据，日期口径） */
	private static final Date RUN_DATE = java.sql.Date.valueOf("2026-06-15");

	@Mock
	private IRbLimitSceneDefBcc rbLimitSceneDefBcc;

	@Mock
	private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

	@Mock
	private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

	@InjectMocks
	private ST102Pbc st102;

	/** create 桩捕获的写入 EO 实例 */
	private RbLimitSumInfoEO createdEo;

	/** 构造输入：九字段全量赋值，条件字段与交易金额按用例变化 */
	private ST102InputBO input(String limitCheckResult, String tranAmt, String limitSumAmt, Integer limitSumCnt) {
		ST102InputBO input = new ST102InputBO();
		input.setLimitCheckResult(limitCheckResult);
		input.setBaseAcctNo(BASE_ACCT_NO);
		input.setClientNo(CLIENT_NO);
		input.setTranAmt(new BigDecimal(tranAmt));
		input.setLimitSceneNo(LIMIT_SCENE_NO);
		input.setRunDate(RUN_DATE);
		input.setLimitSumAmt(new BigDecimal(limitSumAmt));
		input.setLimitSumCnt(limitSumCnt);
		input.setReference(REFERENCE);
		return input;
	}

	/** 构造限额场景定义记录：主键 limitSceneNo，检查对象类型按用例变化 */
	private RbLimitSceneDefEO sceneDef(CheckObjType checkObjType) {
		RbLimitSceneDefEO eo = new RbLimitSceneDefEO();
		eo.setLimitSceneNo(LIMIT_SCENE_NO);
		eo.setCheckObjType(checkObjType);
		return eo;
	}

	/** 构造限额控制配置记录：周期类型与周期值按用例变化 */
	private RbLimitCtrlConfEO ctrlConf(TermType periodType, String periodValue) {
		RbLimitCtrlConfEO eo = new RbLimitCtrlConfEO();
		eo.setLimitSceneNo(LIMIT_SCENE_NO);
		eo.setPeriodType(periodType);
		eo.setPeriodValue(periodValue);
		return eo;
	}

	// 匹配器 lambda 带 null 防护：argThat 匹配以占位 null 真实调用 mock 时不产生 NPE；
	// 生产调用恒为构造好的 EO，断言语义不变（惯例同 ST105PbcTest）

	/** 桩1 REQ-002：按主键[限额场景编码]返回限额场景定义记录 */
	private void stubSceneDef(CheckObjType checkObjType) {
		lenient().when(rbLimitSceneDefBcc.findByPrimaryKey(LIMIT_SCENE_NO))
				.thenReturn(sceneDef(checkObjType));
	}

	/** 桩2 REQ-004：按[限额场景编码]等值条件核对请求，返回限额控制配置记录（单条） */
	private void stubCtrlConf(TermType periodType, String periodValue) {
		lenient().when(rbLimitCtrlConfBcc.findByEo(argThat(eo ->
				eo != null && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()))))
				.thenReturn(Collections.singletonList(ctrlConf(periodType, periodValue)));
	}

	/** 桩3 REQ-003：捕获 create 写入的 EO 实例并返回 1（写入一条） */
	private void stubCreate() {
		when(rbLimitSumInfoBcc.create(any(RbLimitSumInfoEO.class))).thenAnswer(invocation -> {
			createdEo = invocation.getArgument(0);
			return 1;
		});
	}

	/** 日期按 yyyy-MM-dd 口径格式化（时间部分不断言） */
	private String formatDate(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	/** 不登记路径公共断言：succeed=true、错误字段 null、六输出均为 null（不填充默认值） */
	private void assertNotRegistered(ST102OutputBO output) {
		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertNull(output.getLimitSceneNo());
		assertNull(output.getCheckObjVal());
		assertNull(output.getLimitSumAmt());
		assertNull(output.getLimitSumCnt());
		assertNull(output.getEffectDate());
		assertNull(output.getExpireDate());
	}

	/**
	 * 登记路径公共断言：succeed=true、错误字段 null；输出六字段与捕获的写入 EO
	 * 六字段逐一回显核对（限额场景编码、检查对象值、金额精确相等、笔数恒 1、
	 * 两日期按 yyyy-MM-dd 口径）
	 */
	private void assertRegistered(ST102OutputBO output, String checkObjVal, String tranAmt,
			String effectDate, String expireDate) {
		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals(LIMIT_SCENE_NO, output.getLimitSceneNo());
		assertEquals(checkObjVal, output.getCheckObjVal());
		assertEquals(new BigDecimal(tranAmt), output.getLimitSumAmt());
		assertEquals(Integer.valueOf(1), output.getLimitSumCnt());
		assertEquals(effectDate, formatDate(output.getEffectDate()));
		assertEquals(expireDate, formatDate(output.getExpireDate()));

		assertNotNull(createdEo);
		assertEquals(LIMIT_SCENE_NO, createdEo.getLimitSceneNo());
		assertEquals(checkObjVal, createdEo.getCheckObjVal());
		assertEquals(new BigDecimal(tranAmt), createdEo.getLimitSumAmt());
		assertEquals(Integer.valueOf(1), createdEo.get否());
		assertEquals(effectDate, formatDate(createdEo.getEffectDate()));
		assertEquals(expireDate, formatDate(createdEo.getExpireDate()));
	}

	// ST102-TC001 REQ-001-S01 正常路径：limitCheckResult="未超限"、limitSumAmt=0.00（数值上等于 0，标度不影响判定）、
	// limitSumCnt=2，登记条件满足：ACCT 取账号、M×1 失效日期 2026-07-15，登记并回显六字段输出
	@Test
	public void testST102T01() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "500.00", "0.00", 2));

		assertRegistered(output, BASE_ACCT_NO, "500.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC002 REQ-001-S02 正常路径：limitCheckResult="未超限"、limitSumAmt=15000.00、limitSumCnt=0，
	// 累计笔数等于 0 使登记条件满足：登记并回显六字段输出
	@Test
	public void testST102T02() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "500.00", "15000.00", 0));

		assertRegistered(output, BASE_ACCT_NO, "500.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC003 REQ-001-S03 正常路径：limitCheckResult="未超限"、limitSumAmt=0（new BigDecimal("0")）、
	// limitSumCnt=0，金额与笔数均等于 0（两条件同时成立）：登记并回显六字段输出
	@Test
	public void testST102T03() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "500.00", "0", 0));

		assertRegistered(output, BASE_ACCT_NO, "500.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC004 REQ-001-S04 边界否定路径：limitCheckResult="未超限" 但 limitSumAmt=15000.00、
	// limitSumCnt=3（均不等于 0），登记条件不满足：不写入任何记录，正常返回、六输出均为 null
	@Test
	public void testST102T04() {
		ST102OutputBO output = st102.execute(input("未超限", "500.00", "15000.00", 3));

		assertNotRegistered(output);
	}

	// ST102-TC005 REQ-001-S05 边界否定路径：limitCheckResult="超限"（任一非"未超限"取值同理），
	// 即使 limitSumAmt=0、limitSumCnt=0，登记条件仍不满足：不写入任何记录，正常返回、六输出均为 null
	@Test
	public void testST102T05() {
		ST102OutputBO output = st102.execute(input("超限", "500.00", "0", 0));

		assertNotRegistered(output);
	}

	// ST102-TC006 REQ-002-S01 正常路径：限额场景定义 CHECK_OBJ_TYPE=ACCT（账户级别），
	// 登记的限额检查对象值取输入{账号} baseAcctNo（而非客户号），输出与写入 EO 同值
	@Test
	public void testST102T06() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertEquals(BASE_ACCT_NO, output.getCheckObjVal());
		assertEquals(BASE_ACCT_NO, createdEo.getCheckObjVal());
		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC007 REQ-002-S02 正常路径：限额场景定义 CHECK_OBJ_TYPE=CUST（客户级别），
	// 登记的限额检查对象值取输入{客户号} clientNo（而非账号），输出与写入 EO 同值
	@Test
	public void testST102T07() {
		stubSceneDef(CheckObjType.CUST);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertEquals(CLIENT_NO, output.getCheckObjVal());
		assertEquals(CLIENT_NO, createdEo.getCheckObjVal());
		assertRegistered(output, CLIENT_NO, "1000.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC008 REQ-003-S01 正常路径：登记一条限额累计记录并回显输出，六项显式赋值全字段断言：
	// 限额场景编码=[限额场景编码]、检查对象值（ACCT→账号）、累计金额={交易金额}原样赋值、
	// 累计笔数=1、生效日期={核心运行日期}、失效日期={核心运行日期}+M×1=2026-07-15
	@Test
	public void testST102T08() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC009 REQ-004-S01 正常路径：周期类型 D（日）、周期值 1，
	// 失效日期=2026-06-15 加 1 日=2026-06-16，登记记录 EXPIRE_DATE 与输出 expireDate 均取该值
	@Test
	public void testST102T09() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.D, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-06-16");
	}

	// ST102-TC010 REQ-004-S02 正常路径：周期类型 W（周）、周期值 1，
	// 失效日期=2026-06-15 加 1 周（7 日）=2026-06-22
	@Test
	public void testST102T10() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.W, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-06-22");
	}

	// ST102-TC011 REQ-004-S03 正常路径：周期类型 M（月）、周期值 1，
	// 失效日期=2026-06-15 加 1 个月=2026-07-15（与 REQ-003-S01 数据一致，断言聚焦失效日期）
	@Test
	public void testST102T11() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-07-15");
	}

	// ST102-TC012 REQ-004-S04 正常路径：周期类型 Q（季）、周期值 1，
	// 失效日期=2026-06-15 加 1 季（3 个月）=2026-09-15
	@Test
	public void testST102T12() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.Q, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-09-15");
	}

	// ST102-TC013 REQ-004-S05 正常路径：周期类型 H（半年）、周期值 1，
	// 失效日期=2026-06-15 加半年（6 个月）=2026-12-15
	@Test
	public void testST102T13() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.H, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-12-15");
	}

	// ST102-TC014 REQ-004-S06 正常路径：周期类型 Y（年）、周期值 1，
	// 失效日期=2026-06-15 加 1 年=2027-06-15
	@Test
	public void testST102T14() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.Y, "1");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2027-06-15");
	}

	// ST102-TC015 REQ-004-S07 正常路径：周期类型 M（月）、周期值 3（大于 1，周期值按数量放大），
	// 失效日期=2026-06-15 加 3 个月=2026-09-15
	@Test
	public void testST102T15() {
		stubSceneDef(CheckObjType.ACCT);
		stubCtrlConf(TermType.M, "3");
		stubCreate();

		ST102OutputBO output = st102.execute(input("未超限", "1000.00", "0.00", 1));

		assertRegistered(output, BASE_ACCT_NO, "1000.00", "2026-06-15", "2026-09-15");
	}

	// ST102-TC016 REQ-005-S01 边界否定路径（技术异常传播）：登记条件满足、步骤真实触达限额场景定义查询时，
	// findByPrimaryKey 底层抛技术异常（模拟数据源不可用），异常原样向上传播：同一实例、不捕获、不转换、
	// 不生成业务失败应答；限额控制配置/限额累计信息两依赖未触达、不设桩
	@Test
	public void testST102T16() {
		RuntimeException dataSourceFailure = new RuntimeException("模拟数据源不可用");
		lenient().when(rbLimitSceneDefBcc.findByPrimaryKey(LIMIT_SCENE_NO)).thenThrow(dataSourceFailure);

		RuntimeException thrown = assertThrows(RuntimeException.class,
				() -> st102.execute(input("未超限", "1000.00", "0.00", 1)));

		assertSame(dataSourceFailure, thrown);
		assertEquals("模拟数据源不可用", thrown.getMessage());
	}
}
