package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;
import com.dcits.depsit.facade.components.IRbClientRestraintsBcc;
import com.dcits.depsit.facade.eo.RbClientRestraintsEO;

/**
 * ST001 检查客户是否存在限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST001-TC001 ~ TC005），
 * 预期结果来自正式 Spec docs/specs/ST001.md。
 * 仅 IRbClientRestraintsBcc 为实际触达依赖并设桩；本步骤无规则、
 * 跨组件客户端或组件内步骤调用，不设其他桩，不验证调用次数或顺序。
 */
@ExtendWith(MockitoExtension.class)
public class ST001PbcTest {

	@Mock
	private IRbClientRestraintsBcc rbClientRestraintsBcc;

	@InjectMocks
	private ST001Pbc st001Pbc;

	/** 构造【客户限制表】记录 */
	private static RbClientRestraintsEO eo(String clientNo, String resSeqNo, RestraintType restraintType,
			RestraintsStatus restraintsStatus, String createTimestamp) {
		RbClientRestraintsEO record = new RbClientRestraintsEO();
		record.setClientNo(clientNo);
		record.setResSeqNo(resSeqNo);
		record.setRestraintType(restraintType);
		record.setRestraintsStatus(restraintsStatus);
		record.setCreateTimestamp(createTimestamp);
		return record;
	}

	/** 构造步骤输入 */
	private static ST001InputBO input(String clientNo) {
		ST001InputBO input = new ST001InputBO();
		input.setClientNo(clientNo);
		return input;
	}

	// REQ-001-S01、REQ-002-S01 客户仅有一条生效限制：命中并完整赋值输出，succeed=true 且错误字段为 null
	@Test
	public void testST001T01() {
		lenient().when(rbClientRestraintsBcc.findByEo(argThat(eo -> "C001".equals(eo.getClientNo()))))
				.thenReturn(Collections.singletonList(eo("C001", "R20260928000001", RestraintType.VALUE_13,
						RestraintsStatus.A, "2026-09-28 10:00:00.000000")));

		ST001OutputBO output = st001Pbc.execute(input("C001"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("R20260928000001", output.getResSeqNo());
		assertEquals(RestraintType.VALUE_13, output.getRestraintType());
		assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
	}

	// REQ-001-S02 三条生效限制乱序返回：按创建时间戳从新到旧取第一条，选中 R2
	@Test
	public void testST001T02() {
		lenient().when(rbClientRestraintsBcc.findByEo(argThat(eo -> "C002".equals(eo.getClientNo()))))
				.thenReturn(Arrays.asList(
						eo("C002", "R1", RestraintType.VALUE_13, RestraintsStatus.A, "2026-09-01 08:00:00.000000"),
						eo("C002", "R2", RestraintType.VALUE_70, RestraintsStatus.A, "2026-09-28 09:30:00.000000"),
						eo("C002", "R3", RestraintType.VALUE_14, RestraintsStatus.A, "2026-09-15 12:00:00.000000")));

		ST001OutputBO output = st001Pbc.execute(input("C002"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("R2", output.getResSeqNo());
		assertEquals(RestraintType.VALUE_70, output.getRestraintType());
		assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
	}

	// REQ-001-S03 非生效状态（E、F）记录即使创建时间戳更新也不参与选取：选中最早的生效记录
	@Test
	public void testST001T03() {
		lenient().when(rbClientRestraintsBcc.findByEo(argThat(eo -> "C003".equals(eo.getClientNo()))))
				.thenReturn(Arrays.asList(
						eo("C003", "R20260928000003", RestraintType.VALUE_13, RestraintsStatus.E,
								"2026-09-28 10:00:00.000000"),
						eo("C003", "R20260925000002", RestraintType.VALUE_14, RestraintsStatus.F,
								"2026-09-25 10:00:00.000000"),
						eo("C003", "R20260910000001", RestraintType.VALUE_7, RestraintsStatus.A,
								"2026-09-10 09:00:00.000000")));

		ST001OutputBO output = st001Pbc.execute(input("C003"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("R20260910000001", output.getResSeqNo());
		assertEquals(RestraintType.VALUE_7, output.getRestraintType());
		assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
	}

	// REQ-002-S02 无任何记录变体：正常返回空输出，不产生业务失败，不填充默认值
	@Test
	public void testST001T04() {
		lenient().when(rbClientRestraintsBcc.findByEo(argThat(eo -> "C004".equals(eo.getClientNo()))))
				.thenReturn(Collections.emptyList());

		ST001OutputBO output = st001Pbc.execute(input("C004"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertNull(output.getResSeqNo());
		assertNull(output.getRestraintType());
		assertNull(output.getRestraintsStatus());
	}

	// REQ-002-S02 记录均非生效（E、F）变体：筛选后无生效记录，正常返回空输出
	@Test
	public void testST001T05() {
		lenient().when(rbClientRestraintsBcc.findByEo(argThat(eo -> "C005".equals(eo.getClientNo()))))
				.thenReturn(Arrays.asList(
						eo("C005", "R20260920000005", RestraintType.VALUE_13, RestraintsStatus.E,
								"2026-09-20 08:00:00.000000"),
						eo("C005", "R20260922000006", RestraintType.VALUE_14, RestraintsStatus.F,
								"2026-09-22 08:00:00.000000")));

		ST001OutputBO output = st001Pbc.execute(input("C005"));

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertNull(output.getResSeqNo());
		assertNull(output.getRestraintType());
		assertNull(output.getRestraintsStatus());
	}
}
