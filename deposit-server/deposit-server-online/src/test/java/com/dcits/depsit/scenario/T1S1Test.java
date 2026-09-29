package com.dcits.depsit.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.common.task.RespHeader;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;
import com.dcits.depsit.step.IST001;
import com.dcits.depsit.task.dto.T1S1InputDTO;
import com.dcits.depsit.task.dto.T1S1OutputDTO;
import com.dcits.depsit.task.scenario.T1S1;

/**
 * T1S1 检查客户限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（T1S1-TC001 ~ TC003），
 * 预期结果来自正式 Spec docs/specs/T1S1.md（关联步骤 docs/specs/ST001.md）。
 * 仅 IST001 为实际触达依赖并设桩；本交易无对外部业务组件调用，
 * 不设其他桩，不验证调用次数或顺序。技术异常穿透（REQ-003-S01）
 * 由步骤内部实现覆盖，本测试不模拟。
 */
@ExtendWith(MockitoExtension.class)
public class T1S1Test {

	@Mock
	private IST001 st001;

	@InjectMocks
	private T1S1 t1s1;

	/** 构造交易输入 */
	private static T1S1InputDTO input(String clientNo) {
		T1S1InputDTO input = new T1S1InputDTO();
		input.setClientNo(clientNo);
		return input;
	}

	/** 构造步骤成功输出：命中生效记录 */
	private static ST001OutputBO hitOutput(String resSeqNo, RestraintType restraintType,
			RestraintsStatus restraintsStatus) {
		ST001OutputBO stepOutput = new ST001OutputBO();
		stepOutput.setSucceed(true);
		stepOutput.setResSeqNo(resSeqNo);
		stepOutput.setRestraintType(restraintType);
		stepOutput.setRestraintsStatus(restraintsStatus);
		return stepOutput;
	}

	// REQ-001-S01、REQ-002-S01 客户存在生效限制：clientNo 原样传递给 ST001，命中三字段按枚举 value 映射为交易输出
	@Test
	public void testT1S1T01() {
		ST001InputBO[] captured = new ST001InputBO[1];
		lenient().when(st001.execute(any(ST001InputBO.class))).thenAnswer(invocation -> {
			captured[0] = invocation.getArgument(0);
			return hitOutput("R20260928000001", RestraintType.VALUE_13, RestraintsStatus.A);
		});

		RespHeader header = new RespHeader();

		T1S1OutputDTO output = t1s1.execute(header, input("C001"));

		assertEquals("C001", captured[0].getClientNo());
		assertTrue(header.isSucceed());
		assertNull(header.getErrorCode());
		assertNull(header.getErrorMessage());
		assertEquals("R20260928000001", output.getResSeqNo());
		assertEquals("13", output.getRestraintType());
		assertEquals("A", output.getRestraintsStatus());
	}

	// REQ-002-S02 客户无生效限制：ST001 正常返回空输出，交易正常返回且三字段为 null；复用 header 的旧错误被成功清理
	@Test
	public void testT1S1T02() {
		lenient().when(st001.execute(any(ST001InputBO.class))).thenAnswer(invocation -> {
			ST001OutputBO stepOutput = new ST001OutputBO();
			stepOutput.setSucceed(true);
			return stepOutput;
		});

		RespHeader header = new RespHeader();
		header.setSucceed(false);
		header.setErrorCode("OLD");
		header.setErrorMessage("旧错误");

		T1S1OutputDTO output = t1s1.execute(header, input("C004"));

		assertTrue(header.isSucceed());
		assertNull(header.getErrorCode());
		assertNull(header.getErrorMessage());
		assertNull(output.getResSeqNo());
		assertNull(output.getRestraintType());
		assertNull(output.getRestraintsStatus());
	}

	// REQ-001-S01、REQ-002-S01 命中另一枚举取值：VALUE_70 复核输入传递与 value 字符串映射，证明输出映射非固定常量
	@Test
	public void testT1S1T03() {
		ST001InputBO[] captured = new ST001InputBO[1];
		lenient().when(st001.execute(any(ST001InputBO.class))).thenAnswer(invocation -> {
			captured[0] = invocation.getArgument(0);
			return hitOutput("R20260920070150", RestraintType.VALUE_70, RestraintsStatus.A);
		});

		RespHeader header = new RespHeader();

		T1S1OutputDTO output = t1s1.execute(header, input("C002"));

		assertEquals("C002", captured[0].getClientNo());
		assertTrue(header.isSucceed());
		assertNull(header.getErrorCode());
		assertNull(header.getErrorMessage());
		assertEquals("R20260920070150", output.getResSeqNo());
		assertEquals("70", output.getRestraintType());
		assertEquals("A", output.getRestraintsStatus());
	}
}
