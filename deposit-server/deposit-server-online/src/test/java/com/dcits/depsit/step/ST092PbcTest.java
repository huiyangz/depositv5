package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.CommissionRelation;
import com.dcits.depsit.enums.IssCountry;
import com.dcits.depsit.enums.ThawDocumentType2;
import com.dcits.depsit.facade.bo.ST092InputBO;
import com.dcits.depsit.facade.bo.ST092OutputBO;
import com.dcits.depsit.facade.components.IRbCommissionRegisterBcc;

/**
 * ST092 登记代办人信息 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST092-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST092（fileInputs.spec 绑定的 ST092.md）。
 * 本步骤无业务失败场景，登记与不登记分支均 succeed=true；
 * 技术异常按 REQ-003-S03 原样向调用方传播。
 */
@ExtendWith(MockitoExtension.class)
public class ST092PbcTest {

	@Mock
	private IRbCommissionRegisterBcc rbCommissionRegisterBcc;

	@InjectMocks
	private ST092Pbc st092;

	/** 构造基准输入：19 项全赋值（TC001 场景示例数据），后续用例在基准上覆盖差异字段 */
	private ST092InputBO baseInput() {
		ST092InputBO input = new ST092InputBO();
		input.setReference("REF20260930000001");
		input.setCommissionClientName("李代");
		input.setCommissionClientNo("0000000002");
		input.setCommissionDocumentId("11010119920307123X");
		input.setCommissionDocumentType(ThawDocumentType2.VALUE_110001);
		input.setCountry(IssCountry.CHN);
		input.setCommissionStartDate(new GregorianCalendar(2026, Calendar.JANUARY, 1).getTime());
		input.setCommissionExpireDate(new GregorianCalendar(2046, Calendar.JANUARY, 1).getTime());
		input.setCommissionClientTel("13900000000");
		input.setCommissionReason("客户行动不便");
		input.setCommissionRelation(CommissionRelation.VALUE_1);
		input.setCommissionConfirmUserIdKey1("E0001");
		input.setCommissionConfirmUserIdKey2("E0002");
		input.setCommissionConfirmTel("13900000000");
		input.setCommissionConfirmTime("20260930093000");
		input.setCommissionConfirmResult("核实一致");
		input.setChannelSeqNo("CS2026093001");
		input.setClientNo("0000000001");
		input.setInternalKey(1);
		return input;
	}

	// 匹配器 lambda 需 null 防护（与既有 ST004PbcTest 约定一致）；生产调用恒为构造好的 EO，断言语义不变
	// 请求字段映射核对在 create 桩 argThat 匹配器内完成（17 项写入字段 + reference/commissionReason 不写入），
	// 不检查 createTimestamp/lastUpdTimestamp（技术字段，Spec 明确不覆盖）

	// REQ-001-S01（含 REQ-002-S01、REQ-003-S01）：证件号码不为空，新增登记记录写入主键、账户内部键值及全部 14 项业务字段，输出返回登记值
	@Test
	public void testST092T01() {
		ST092InputBO input = baseInput();
		lenient().when(rbCommissionRegisterBcc.create(argThat(eo -> eo != null
				&& "CS2026093001".equals(eo.getChannelSeqNo())
				&& "0000000001".equals(eo.getClientNo())
				&& Integer.valueOf(1).equals(eo.getInternalKey())
				&& "0000000002".equals(eo.getCommissionClientNo())
				&& "李代".equals(eo.getCommissionClientName())
				&& eo.getCommissionDocumentType() == ThawDocumentType2.VALUE_110001
				&& "11010119920307123X".equals(eo.getCommissionDocumentId())
				&& input.getCommissionStartDate().equals(eo.getCommissionStartDate())
				&& input.getCommissionExpireDate().equals(eo.getCommissionExpireDate())
				&& "13900000000".equals(eo.getCommissionClientTel())
				&& eo.getCountry() == IssCountry.CHN
				&& eo.getCommissionRelation() == CommissionRelation.VALUE_1
				&& "核实一致".equals(eo.getCommissionConfirmResult())
				&& "13900000000".equals(eo.getCommissionConfirmTel())
				&& "20260930093000".equals(eo.getCommissionConfirmTime())
				&& "E0001".equals(eo.getCommissionConfirmUserIdKey1())
				&& "E0002".equals(eo.getCommissionConfirmUserIdKey2())
				&& eo.getReference() == null
				&& eo.getCommissionReason() == null))).thenReturn(1);

		ST092OutputBO output = st092.execute(input);

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("0000000002", output.getCommissionClientNo());
		assertEquals("李代", output.getCommissionClientName());
		assertEquals(ThawDocumentType2.VALUE_110001, output.getCommissionDocumentType());
		assertEquals("11010119920307123X", output.getCommissionDocumentId());
		assertEquals(input.getCommissionStartDate(), output.getCommissionStartDate());
		assertEquals(input.getCommissionExpireDate(), output.getCommissionExpireDate());
		assertEquals("13900000000", output.getCommissionClientTel());
		assertEquals(IssCountry.CHN, output.getCountry());
		assertEquals(CommissionRelation.VALUE_1, output.getCommissionRelation());
		assertEquals("核实一致", output.getCommissionConfirmResult());
		assertEquals("13900000000", output.getCommissionConfirmTel());
		assertEquals("20260930093000", output.getCommissionConfirmTime());
		assertEquals("E0001", output.getCommissionConfirmUserIdKey1());
		assertEquals("E0002", output.getCommissionConfirmUserIdKey2());
		assertNull(output.getReference());
		assertNull(output.getCommissionReason());
	}

	// REQ-001-S02：证件号码不为空、7 个非必填登记输入均为 null，仍新增登记记录，空值按原样登记（对应输出为空）
	@Test
	public void testST092T02() {
		ST092InputBO input = baseInput();
		input.setReference("REF20260930000002");
		input.setCommissionDocumentId("110101199506153028");
		input.setCommissionClientName("张代");
		input.setCommissionStartDate(new GregorianCalendar(2026, Calendar.FEBRUARY, 1).getTime());
		input.setCommissionExpireDate(new GregorianCalendar(2036, Calendar.FEBRUARY, 1).getTime());
		input.setCommissionClientTel("13800000000");
		input.setChannelSeqNo("CS2026093002");
		input.setClientNo("0000000011");
		input.setInternalKey(2);
		input.setCommissionClientNo(null);
		input.setCommissionRelation(null);
		input.setCommissionConfirmResult(null);
		input.setCommissionConfirmTel(null);
		input.setCommissionConfirmTime(null);
		input.setCommissionConfirmUserIdKey1(null);
		input.setCommissionConfirmUserIdKey2(null);
		input.setCommissionReason(null);
		lenient().when(rbCommissionRegisterBcc.create(argThat(eo -> eo != null
				&& "CS2026093002".equals(eo.getChannelSeqNo())
				&& "0000000011".equals(eo.getClientNo())
				&& Integer.valueOf(2).equals(eo.getInternalKey())
				&& "110101199506153028".equals(eo.getCommissionDocumentId())
				&& "张代".equals(eo.getCommissionClientName())
				&& eo.getCommissionDocumentType() == ThawDocumentType2.VALUE_110001
				&& input.getCommissionStartDate().equals(eo.getCommissionStartDate())
				&& input.getCommissionExpireDate().equals(eo.getCommissionExpireDate())
				&& "13800000000".equals(eo.getCommissionClientTel())
				&& eo.getCountry() == IssCountry.CHN
				&& eo.getCommissionClientNo() == null
				&& eo.getCommissionRelation() == null
				&& eo.getCommissionConfirmResult() == null
				&& eo.getCommissionConfirmTel() == null
				&& eo.getCommissionConfirmTime() == null
				&& eo.getCommissionConfirmUserIdKey1() == null
				&& eo.getCommissionConfirmUserIdKey2() == null
				&& eo.getReference() == null
				&& eo.getCommissionReason() == null))).thenReturn(1);

		ST092OutputBO output = st092.execute(input);

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertEquals("张代", output.getCommissionClientName());
		assertEquals(ThawDocumentType2.VALUE_110001, output.getCommissionDocumentType());
		assertEquals("110101199506153028", output.getCommissionDocumentId());
		assertEquals(input.getCommissionStartDate(), output.getCommissionStartDate());
		assertEquals(input.getCommissionExpireDate(), output.getCommissionExpireDate());
		assertEquals("13800000000", output.getCommissionClientTel());
		assertEquals(IssCountry.CHN, output.getCountry());
		assertNull(output.getCommissionClientNo());
		assertNull(output.getCommissionRelation());
		assertNull(output.getCommissionConfirmResult());
		assertNull(output.getCommissionConfirmTel());
		assertNull(output.getCommissionConfirmTime());
		assertNull(output.getCommissionConfirmUserIdKey1());
		assertNull(output.getCommissionConfirmUserIdKey2());
		assertNull(output.getReference());
		assertNull(output.getCommissionReason());
	}

	// REQ-001-S03（含 REQ-002-S02、REQ-003-S02）：证件号码为 null，不执行登记写入，正常成功返回，全部 16 个输出字段为空
	@Test
	public void testST092T03() {
		ST092InputBO input = baseInput();
		input.setCommissionDocumentId(null);
		input.setReference("REF20260930000003");
		input.setChannelSeqNo("CS2026093003");

		ST092OutputBO output = st092.execute(input);

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertNull(output.getReference());
		assertNull(output.getCommissionClientName());
		assertNull(output.getCommissionClientNo());
		assertNull(output.getCommissionDocumentId());
		assertNull(output.getCommissionDocumentType());
		assertNull(output.getCountry());
		assertNull(output.getCommissionStartDate());
		assertNull(output.getCommissionExpireDate());
		assertNull(output.getCommissionClientTel());
		assertNull(output.getCommissionReason());
		assertNull(output.getCommissionRelation());
		assertNull(output.getCommissionConfirmUserIdKey1());
		assertNull(output.getCommissionConfirmUserIdKey2());
		assertNull(output.getCommissionConfirmTel());
		assertNull(output.getCommissionConfirmTime());
		assertNull(output.getCommissionConfirmResult());
	}

	// REQ-001-S04（含 REQ-002-S02、REQ-003-S02）：证件号码为空字符串（与 null 同为"为空"），不执行登记写入，正常成功返回，输出全空
	@Test
	public void testST092T04() {
		ST092InputBO input = baseInput();
		input.setCommissionDocumentId("");
		input.setReference("REF20260930000004");
		input.setChannelSeqNo("CS2026093004");

		ST092OutputBO output = st092.execute(input);

		assertTrue(output.isSucceed());
		assertNull(output.getErrorCode());
		assertNull(output.getErrorMessage());
		assertNull(output.getReference());
		assertNull(output.getCommissionClientName());
		assertNull(output.getCommissionClientNo());
		assertNull(output.getCommissionDocumentId());
		assertNull(output.getCommissionDocumentType());
		assertNull(output.getCountry());
		assertNull(output.getCommissionStartDate());
		assertNull(output.getCommissionExpireDate());
		assertNull(output.getCommissionClientTel());
		assertNull(output.getCommissionReason());
		assertNull(output.getCommissionRelation());
		assertNull(output.getCommissionConfirmUserIdKey1());
		assertNull(output.getCommissionConfirmUserIdKey2());
		assertNull(output.getCommissionConfirmTel());
		assertNull(output.getCommissionConfirmTime());
		assertNull(output.getCommissionConfirmResult());
	}

	// REQ-003-S03：登记写入依赖的数据服务调用抛技术异常，异常原样向调用方传播，不转译为业务失败返回、不吞没
	@Test
	public void testST092T05() {
		ST092InputBO input = baseInput();
		lenient().when(rbCommissionRegisterBcc.create(argThat(eo -> eo != null
				&& "11010119920307123X".equals(eo.getCommissionDocumentId()))))
				.thenThrow(new RuntimeException("数据访问异常-代办人登记写入"));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> st092.execute(input));

		assertEquals("数据访问异常-代办人登记写入", ex.getMessage());
	}
}
