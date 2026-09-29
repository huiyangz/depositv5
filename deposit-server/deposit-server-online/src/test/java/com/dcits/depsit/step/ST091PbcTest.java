package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;
import com.dcits.depsit.facade.bo.ST091InputBO;
import com.dcits.depsit.facade.bo.ST091OutputBO;

/**
 * ST091 检查客户限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST091-TC001 ~ TC004），
 * 预期结果来自正式 Spec docs/specs/ST091.md。
 * 仅组件内步骤 IST001 为实际触达依赖并设桩，桩内以 thenAnswer 记录收到的
 * ST001InputBO 以断言 clientNo 原样传递（只记录字段，不统计调用次数或顺序）；
 * 无 BCC、规则、跨组件客户端或业务错误码，不访问真实数据库、网络或组件。
 */
@ExtendWith(MockitoExtension.class)
public class ST091PbcTest {

	@Mock
	private IST001 st001;

	@InjectMocks
	private ST091Pbc st091Pbc;

	/** 桩记录的 ST001 入参，用于断言 clientNo 原样传递 */
	private final ST001InputBO[] capturedSt001Input = new ST001InputBO[1];

	/** 构造步骤输入 */
	private static ST091InputBO input(String clientNo) {
		ST091InputBO input = new ST091InputBO();
		input.setClientNo(clientNo);
		return input;
	}

	/** 构造 ST001 正常返回的[客户限制信息] */
	private static ST001OutputBO st001Result(String resSeqNo, RestraintType restraintType,
			RestraintsStatus restraintsStatus) {
		ST001OutputBO result = new ST001OutputBO();
		result.setSucceed(true);
		result.setResSeqNo(resSeqNo);
		result.setRestraintType(restraintType);
		result.setRestraintsStatus(restraintsStatus);
		return result;
	}

	/** 设桩：ST001 正常返回给定[客户限制信息]，并记录收到的入参 */
	private void stubSt001Return(ST001OutputBO restraintInfo) {
		lenient().when(st001.execute(any(ST001InputBO.class))).thenAnswer(invocation -> {
			capturedSt001Input[0] = invocation.getArgument(0);
			return restraintInfo;
		});
	}

	// REQ-002-S01、REQ-001-S01 客户无生效限制：ST001 正常返回三字段均为 null，检查通过、成功返回、输出为空，clientNo 原样传递
	@Test
	public void testST091T01() {
		stubSt001Return(st001Result(null, null, null));

		ST091OutputBO output = st091Pbc.execute(input("C004"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertNull(output.getResSeqNo());
		assertNull(output.getRestraintType());
		assertNull(output.getRestraintsStatus());
		assertEquals("C004", capturedSt001Input[0].getClientNo());
	}

	// REQ-003-S01 命中生效限制（挂失止付）：三输出字段赋值后正常返回，枚举常量原样透传，clientNo 原样传递
	@Test
	public void testST091T02() {
		stubSt001Return(st001Result("R20260928000001", RestraintType.VALUE_13, RestraintsStatus.A));

		ST091OutputBO output = st091Pbc.execute(input("C001"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("R20260928000001", output.getResSeqNo());
		assertSame(RestraintType.VALUE_13, output.getRestraintType());
		assertSame(RestraintsStatus.A, output.getRestraintsStatus());
		assertEquals("C001", capturedSt001Input[0].getClientNo());
	}

	// REQ-001-S01、REQ-003-S01 命中其他生效限制类型（黑名单控制专用-停止所有业务）：枚举常量按原样透传到输出，不做取值转换
	@Test
	public void testST091T03() {
		stubSt001Return(st001Result("R20260928000002", RestraintType.VALUE_70, RestraintsStatus.A));

		ST091OutputBO output = st091Pbc.execute(input("C002"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("R20260928000002", output.getResSeqNo());
		assertSame(RestraintType.VALUE_70, output.getRestraintType());
		assertSame(RestraintsStatus.A, output.getRestraintsStatus());
		assertEquals("C002", capturedSt001Input[0].getClientNo());
	}

	// REQ-004-S01 ST001 技术异常传播：客户限制表数据访问失败异常原样向上抛出，不捕获、不转换、不生成业务失败应答
	@Test
	public void testST091T04() {
		RuntimeException dataAccessFailure = new RuntimeException("客户限制表数据访问失败");
		lenient().when(st001.execute(any(ST001InputBO.class))).thenThrow(dataAccessFailure);

		RuntimeException thrown = assertThrows(RuntimeException.class,
				() -> st091Pbc.execute(input("C003")));

		assertSame(dataAccessFailure, thrown);
		assertEquals("客户限制表数据访问失败", thrown.getMessage());
	}
}
