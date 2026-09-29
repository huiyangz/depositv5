package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.ResBranchRange;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST002InputBO;
import com.dcits.depsit.facade.bo.ST002OutputBO;
import com.dcits.depsit.facade.components.IFmChannelBcc;
import com.dcits.depsit.facade.components.IRbRestraintControlDetailsBcc;
import com.dcits.depsit.facade.eo.FmChannelEO;
import com.dcits.depsit.facade.eo.RbRestraintControlDetailsEO;

/**
 * ST002 检查限制豁免 单元测试。
 *
 * 依据正式 Spec ST002 与 outputs/测试用例.md（ST002-TC001～TC013），
 * 覆盖 REQ-001～REQ-006 全部 15 个 Scenario：柜面/非柜面路由、三项同时匹配、
 * 无记录按 N、空集不构成失败、明细回填与技术异常原样传播。
 * 仅桩化两个 BCC 接口，被测 execute 真实执行。
 */
@ExtendWith(MockitoExtension.class)
class ST002PbcTest {

	@Mock
	private IFmChannelBcc fmChannelBcc;
	@Mock
	private IRbRestraintControlDetailsBcc rbRestraintControlDetailsBcc;
	@InjectMocks
	private ST002Pbc st002Pbc;

	// 输入基值：sourceType 见各例，其余为 narrativeCode="1001"、prodNo="101001" 等
	private ST002InputBO baseInput(SourceType sourceType) {
		ST002InputBO input = new ST002InputBO();
		input.setSourceType(sourceType);
		input.setRestraintType(RestraintType.VALUE_13);
		input.setTranType(OthTranType.VALUE_38);
		input.setNarrativeCode("1001");
		input.setProdNo("101001");
		return input;
	}

	// MT 渠道记录：柜面标志 "Y"
	private FmChannelEO mtChannel() {
		FmChannelEO channel = new FmChannelEO();
		channel.setChannel(SourceType.MT);
		channel.setCounterFlag("Y");
		channel.setChannelShort("MT");
		channel.setChannelDesc("Teller9综合柜员前端柜面系统");
		channel.setTranTimestamp("20260930000000");
		return channel;
	}

	// M 渠道记录：柜面标志 "N"
	private FmChannelEO mChannel() {
		FmChannelEO channel = new FmChannelEO();
		channel.setChannel(SourceType.M);
		channel.setCounterFlag("N");
		channel.setChannelShort("MB");
		channel.setChannelDesc("手机银行");
		channel.setTranTimestamp("20260930000000");
		return channel;
	}

	// 柜面版匹配明细：明细柜面标志 "Y"
	private RbRestraintControlDetailsEO counterDetail() {
		RbRestraintControlDetailsEO detail = new RbRestraintControlDetailsEO();
		detail.setRestraintType(RestraintType.VALUE_13);
		detail.setStatus(Status.A);
		detail.setProdNo("101001");
		detail.setNarrativeCode("1001");
		detail.setTranTypeLink("36,38,40");
		detail.setChannelMuster("MT,M");
		detail.setResBranchRange(ResBranchRange.A);
		detail.setCounterFlag("Y");
		detail.setExpression("01");
		detail.setBatchFlag("N");
		detail.setCreateTimestamp("20260930000000");
		detail.setLastUpdTimestamp("20260930000000");
		return detail;
	}

	// 非柜面版匹配明细：channelMuster="M"、限制机构范围 C、明细柜面标志 "N"
	private RbRestraintControlDetailsEO nonCounterDetail() {
		RbRestraintControlDetailsEO detail = counterDetail();
		detail.setChannelMuster("M");
		detail.setResBranchRange(ResBranchRange.C);
		detail.setCounterFlag("N");
		return detail;
	}

	// 桩：findByChannel 精确枚举参数设桩
	private void stubChannel(SourceType sourceType, FmChannelEO channel) {
		lenient().when(fmChannelBcc.findByChannel(sourceType)).thenReturn(channel);
	}

	// 桩：findByEo 以 thenAnswer 记录入参 EO（EO 未重写 equals，等值匹配不可用）并返回生效明细集合
	private AtomicReference<RbRestraintControlDetailsEO> stubDetails(List<RbRestraintControlDetailsEO> details) {
		AtomicReference<RbRestraintControlDetailsEO> captured = new AtomicReference<>();
		lenient().when(rbRestraintControlDetailsBcc.findByEo(any(RbRestraintControlDetailsEO.class)))
				.thenAnswer(invocation -> {
					captured.set(invocation.getArgument(0));
					return details;
				});
		return captured;
	}

	// 成功路径公共断言：succeed=true、错误字段为 null
	private void assertSuccess(ST002OutputBO output) {
		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
	}

	// 无匹配时七个明细回填字段均为空
	private void assertDetailFieldsEmpty(ST002OutputBO output) {
		assertNull(output.getStatus());
		assertNull(output.getProdNo());
		assertNull(output.getTranTypeLink());
		assertNull(output.getChannelMuster());
		assertNull(output.getNarrativeCode());
		assertNull(output.getResBranchRange());
		assertNull(output.getDetailCounterFlag());
	}

	// 查询 EO 仅设置 restraintType 与 status=Status.A，其余字段未设置
	private void assertQueryCondition(RbRestraintControlDetailsEO captured) {
		assertEquals(RestraintType.VALUE_13, captured.getRestraintType());
		assertEquals(Status.A, captured.getStatus());
		assertNull(captured.getProdNo());
		assertNull(captured.getNarrativeCode());
		assertNull(captured.getTranTypeLink());
		assertNull(captured.getChannelMuster());
		assertNull(captured.getResBranchRange());
		assertNull(captured.getCounterFlag());
		assertNull(captured.getExpression());
		assertNull(captured.getBatchFlag());
		assertNull(captured.getCreateTimestamp());
		assertNull(captured.getLastUpdTimestamp());
	}

	// ST002-TC001 柜面渠道三项同时匹配：渠道与生效明细柜面标志均 "Y" 路由子步骤 4，返回"不检查限制"并回填七字段（REQ-001-S01、REQ-002-S01、REQ-003-S01、REQ-004-S01）
	@Test
	void testST002T01() {
		stubChannel(SourceType.MT, mtChannel());
		AtomicReference<RbRestraintControlDetailsEO> captured = stubDetails(List.of(counterDetail()));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("不检查限制", output.getCheckResult());
		assertEquals(Status.A, output.getStatus());
		assertEquals("101001", output.getProdNo());
		assertEquals("36,38,40", output.getTranTypeLink());
		assertEquals("MT,M", output.getChannelMuster());
		assertEquals("1001", output.getNarrativeCode());
		assertEquals(ResBranchRange.A, output.getResBranchRange());
		assertEquals("Y", output.getDetailCounterFlag());
		assertQueryCondition(captured.get());
	}

	// ST002-TC002 渠道无记录按 N 处理：findByChannel 返回 null，柜面标志按 "N" 经"否则"进入子步骤 5，三项匹配返回"豁免"（REQ-001-S02；REQ-003-S02 括注情形）
	@Test
	void testST002T02() {
		stubChannel(SourceType.M, null);
		stubDetails(List.of(counterDetail()));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.M));
		assertSuccess(output);
		assertEquals("N", output.getCounterFlag());
		assertEquals("豁免", output.getCheckResult());
		assertEquals(Status.A, output.getStatus());
		assertEquals("101001", output.getProdNo());
		assertEquals("36,38,40", output.getTranTypeLink());
		assertEquals("MT,M", output.getChannelMuster());
		assertEquals("1001", output.getNarrativeCode());
		assertEquals(ResBranchRange.A, output.getResBranchRange());
		assertEquals("Y", output.getDetailCounterFlag());
	}

	// ST002-TC003 渠道记录柜面标志为 N 路由子步骤 5：M 记录 counterFlag="N"、生效明细柜面标志 "Y"，三项匹配返回"豁免"（REQ-003-S02 主体情形）
	@Test
	void testST002T03() {
		stubChannel(SourceType.M, mChannel());
		stubDetails(List.of(counterDetail()));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.M));
		assertSuccess(output);
		assertEquals("N", output.getCounterFlag());
		assertEquals("豁免", output.getCheckResult());
		assertEquals(Status.A, output.getStatus());
		assertEquals("101001", output.getProdNo());
		assertEquals("36,38,40", output.getTranTypeLink());
		assertEquals("MT,M", output.getChannelMuster());
		assertEquals("1001", output.getNarrativeCode());
		assertEquals(ResBranchRange.A, output.getResBranchRange());
		assertEquals("Y", output.getDetailCounterFlag());
	}

	// ST002-TC004 渠道柜面而明细非柜面路由子步骤 5：渠道 "Y"、生效明细柜面标志 "N"（口径说明 1），三项匹配返回"豁免"并回填 detailCounterFlag="N"（REQ-003-S03、REQ-005-S03）
	@Test
	void testST002T04() {
		stubChannel(SourceType.MT, mtChannel());
		stubDetails(List.of(nonCounterDetail()));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("豁免", output.getCheckResult());
		assertEquals(Status.A, output.getStatus());
		assertEquals("101001", output.getProdNo());
		assertEquals("36,38,40", output.getTranTypeLink());
		assertEquals("M", output.getChannelMuster());
		assertEquals("1001", output.getNarrativeCode());
		assertEquals(ResBranchRange.C, output.getResBranchRange());
		assertEquals("N", output.getDetailCounterFlag());
	}

	// ST002-TC005 无生效明细：findByEo 返回空列表，渠道柜面标志 "Y" 时子步骤 3 条件不成立（口径说明 3）进入子步骤 5，无匹配候选返回"不豁免"、七字段为空（REQ-002-S02、REQ-003-S04、REQ-005-S04）
	@Test
	void testST002T05() {
		stubChannel(SourceType.MT, mtChannel());
		stubDetails(Collections.emptyList());
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("不豁免", output.getCheckResult());
		assertDetailFieldsEmpty(output);
	}

	// ST002-TC006 柜面路径摘要码不相等：明细 narrativeCode="1002" ≠ 输入 "1001"，三项不同时匹配返回"需检查限制"、七字段为空（REQ-004-S02）
	@Test
	void testST002T06() {
		stubChannel(SourceType.MT, mtChannel());
		RbRestraintControlDetailsEO detail = counterDetail();
		detail.setNarrativeCode("1002");
		stubDetails(List.of(detail));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("需检查限制", output.getCheckResult());
		assertDetailFieldsEmpty(output);
	}

	// ST002-TC007 柜面路径多交易类型不包含交易代码：明细 tranTypeLink="36,40" 不包含代码值 "38"，返回"需检查限制"、七字段为空（REQ-004-S03）
	@Test
	void testST002T07() {
		stubChannel(SourceType.MT, mtChannel());
		RbRestraintControlDetailsEO detail = counterDetail();
		detail.setTranTypeLink("36,40");
		stubDetails(List.of(detail));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("需检查限制", output.getCheckResult());
		assertDetailFieldsEmpty(output);
	}

	// ST002-TC008 柜面路径产品类型不相等：明细 prodNo="101002" ≠ 输入 "101001"，返回"需检查限制"、七字段为空（REQ-004-S04）
	@Test
	void testST002T08() {
		stubChannel(SourceType.MT, mtChannel());
		RbRestraintControlDetailsEO detail = counterDetail();
		detail.setProdNo("101002");
		stubDetails(List.of(detail));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("需检查限制", output.getCheckResult());
		assertDetailFieldsEmpty(output);
	}

	// ST002-TC009 非柜面渠道三项同时匹配（子步骤 5 正常路径）：渠道记录 counterFlag="N"、生效明细柜面标志 "N"，返回"豁免"并按非柜面明细回填（REQ-005-S01）
	@Test
	void testST002T09() {
		stubChannel(SourceType.M, mChannel());
		stubDetails(List.of(nonCounterDetail()));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.M));
		assertSuccess(output);
		assertEquals("N", output.getCounterFlag());
		assertEquals("豁免", output.getCheckResult());
		assertEquals(Status.A, output.getStatus());
		assertEquals("101001", output.getProdNo());
		assertEquals("36,38,40", output.getTranTypeLink());
		assertEquals("M", output.getChannelMuster());
		assertEquals("1001", output.getNarrativeCode());
		assertEquals(ResBranchRange.C, output.getResBranchRange());
		assertEquals("N", output.getDetailCounterFlag());
	}

	// ST002-TC010 非柜面路径摘要码不相等：路由条件同 TC009，明细 narrativeCode="1002" ≠ 输入 "1001"，返回"不豁免"、七字段为空（REQ-005-S02）
	@Test
	void testST002T10() {
		stubChannel(SourceType.M, mChannel());
		RbRestraintControlDetailsEO detail = nonCounterDetail();
		detail.setNarrativeCode("1002");
		stubDetails(List.of(detail));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.M));
		assertSuccess(output);
		assertEquals("N", output.getCounterFlag());
		assertEquals("不豁免", output.getCheckResult());
		assertDetailFieldsEmpty(output);
	}

	// ST002-TC011 子步骤1数据服务技术异常传播：findByChannel 抛 RuntimeException，原样上抛、不产生业务输出；子步骤 2 未触达不设桩（REQ-006-S01）
	@Test
	void testST002T11() {
		lenient().when(fmChannelBcc.findByChannel(SourceType.MT))
				.thenThrow(new RuntimeException("模拟FM_CHANNEL数据库访问异常"));
		ST002InputBO input = baseInput(SourceType.MT);
		RuntimeException ex = assertThrows(RuntimeException.class, () -> st002Pbc.execute(input));
		assertEquals("模拟FM_CHANNEL数据库访问异常", ex.getMessage());
	}

	// ST002-TC012 子步骤2数据服务技术异常传播：子步骤1正常返回，findByEo 抛 RuntimeException，原样上抛（REQ-006-S01）
	@Test
	void testST002T12() {
		stubChannel(SourceType.MT, mtChannel());
		lenient().when(rbRestraintControlDetailsBcc.findByEo(any(RbRestraintControlDetailsEO.class)))
				.thenThrow(new RuntimeException("模拟RB_RESTRAINT_CONTROL_DETAILS数据库访问异常"));
		ST002InputBO input = baseInput(SourceType.MT);
		RuntimeException ex = assertThrows(RuntimeException.class, () -> st002Pbc.execute(input));
		assertEquals("模拟RB_RESTRAINT_CONTROL_DETAILS数据库访问异常", ex.getMessage());
	}

	// ST002-TC013 多条生效明细的存在性匹配：明细柜面标志一致为 "Y"，第一条仅摘要码不匹配、第二条三项匹配，按"存在匹配"返回"不检查限制"，回填取匹配（第二条）明细字段（REQ-004 存在性判定）
	@Test
	void testST002T13() {
		stubChannel(SourceType.MT, mtChannel());
		RbRestraintControlDetailsEO unmatched = counterDetail();
		unmatched.setNarrativeCode("1002");
		unmatched.setExpression("02");
		unmatched.setChannelMuster("X1");
		unmatched.setResBranchRange(ResBranchRange.B);
		AtomicReference<RbRestraintControlDetailsEO> captured = stubDetails(List.of(unmatched, counterDetail()));
		ST002OutputBO output = st002Pbc.execute(baseInput(SourceType.MT));
		assertSuccess(output);
		assertEquals("Y", output.getCounterFlag());
		assertEquals("不检查限制", output.getCheckResult());
		assertEquals(Status.A, output.getStatus());
		assertEquals("101001", output.getProdNo());
		assertEquals("36,38,40", output.getTranTypeLink());
		assertEquals("MT,M", output.getChannelMuster());
		assertEquals("1001", output.getNarrativeCode());
		assertEquals(ResBranchRange.A, output.getResBranchRange());
		assertEquals("Y", output.getDetailCounterFlag());
		assertEquals(RestraintType.VALUE_13, captured.get().getRestraintType());
		assertEquals(Status.A, captured.get().getStatus());
	}
}
