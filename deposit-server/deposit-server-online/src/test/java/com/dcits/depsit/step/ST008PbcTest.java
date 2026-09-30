package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST008InputBO;
import com.dcits.depsit.facade.bo.ST008OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST008 检查是否存在现金止收限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST008-TC001 ~ TC008），
 * 预期结果来自正式 Spec ST008（inputs 绑定的 ST008.md）。
 * 本步骤无业务失败场景，全部正常路径用例 succeed=true；"否"是业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST008PbcTest {

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST008Pbc st008;

    /** 构造公共输入：账号 */
    private ST008InputBO input(String baseAcctNo) {
        ST008InputBO input = new ST008InputBO();
        input.setBaseAcctNo(baseAcctNo);
        return input;
    }

    /** 构造账户限制记录：限制状态均为"生效"（RestraintsStatus.A） */
    private RbBusRestraintsEO restraint(String baseAcctNo, String resSeqNo, RestraintType restraintType) {
        RbBusRestraintsEO eo = new RbBusRestraintsEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setResSeqNo(resSeqNo);
        eo.setRestraintType(restraintType);
        eo.setRestraintsStatus(RestraintsStatus.A);
        return eo;
    }

    /** 构造限制类型生效记录：状态均为 Status.A */
    private RbRestraintTypeEO restraintType(RestraintType restraintType, DrCrCtlFlag drCrCtlFlag,
                                            String cashFlag, String stopFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(Status.A);
        eo.setDrCrCtlFlag(drCrCtlFlag);
        eo.setCashFlag(cashFlag);
        eo.setStopFlag(stopFlag);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 账号+限制状态=RestraintsStatus.A 组合条件核对请求并返回生效限制记录列表 */
    private void stubRestraints(String baseAcctNo, List<RbBusRestraintsEO> records) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && baseAcctNo.equals(eo.getBaseAcctNo())
                        && RestraintsStatus.A.equals(eo.getRestraintsStatus()))))
                .thenReturn(records);
    }

    /** 步骤2 桩：按 账户限制类型+状态=Status.A 组合条件核对请求并返回生效类型记录列表 */
    private void stubRestraintType(RestraintType restraintType, List<RbRestraintTypeEO> records) {
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && restraintType.equals(eo.getRestraintType()) && Status.A.equals(eo.getStatus()))))
                .thenReturn(records);
    }

    /** "否"路径公共断言：succeed=true、错误字段 null、cashStopFlag="否"、其余 7 字段 null */
    private void assertNegative(ST008OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getCashStopFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getStatus());
        assertNull(output.getDrCrCtlFlag());
        assertNull(output.getCashFlag());
        assertNull(output.getStopFlag());
    }

    // ST008-TC001 / REQ-003-S01（含 REQ-001 查询条件、REQ-002-S01 取得三标志）：
    // 唯一生效限制记录的生效类型记录 drCrCtlFlag=C、cashFlag=N，首条即命中，输出 8 字段完整
    @Test
    public void testST008T01() {
        stubRestraints("2000010001", List.of(
                restraint("2000010001", "RES0001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, List.of(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N", "1")));

        ST008OutputBO output = st008.execute(input("2000010001"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getCashStopFlag());
        assertEquals("RES0001", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_92, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(Status.A, output.getStatus());
        assertEquals(DrCrCtlFlag.C, output.getDrCrCtlFlag());
        assertEquals("N", output.getCashFlag());
        assertEquals("1", output.getStopFlag());
    }

    // ST008-TC002 / REQ-003-S02（含 REQ-001-S01 两条记录均参与遍历）：
    // 第 1 条（RES0001/类型67，标志 A/Y）不命中、第 2 条（RES0002/类型92，标志 C/N）命中，输出取首条命中记录
    @Test
    public void testST008T02() {
        stubRestraints("2000010002", List.of(
                restraint("2000010002", "RES0001", RestraintType.VALUE_67),
                restraint("2000010002", "RES0002", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_67, List.of(
                restraintType(RestraintType.VALUE_67, DrCrCtlFlag.A, "Y", "0")));
        stubRestraintType(RestraintType.VALUE_92, List.of(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N", "1")));

        ST008OutputBO output = st008.execute(input("2000010002"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getCashStopFlag());
        assertEquals("RES0002", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_92, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(Status.A, output.getStatus());
        assertEquals(DrCrCtlFlag.C, output.getDrCrCtlFlag());
        assertEquals("N", output.getCashFlag());
        assertEquals("1", output.getStopFlag());
    }

    // ST008-TC003 / REQ-002-S02 + REQ-003 首条命中聚合：第 1 条（RES0001/类型91）无生效类型记录，
    // 不命中且遍历继续、不产生业务报错；第 2 条（RES0002/类型92，C/N）命中，输出取第 2 条
    @Test
    public void testST008T03() {
        stubRestraints("2000010006", List.of(
                restraint("2000010006", "RES0001", RestraintType.VALUE_91),
                restraint("2000010006", "RES0002", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_91, List.of());
        stubRestraintType(RestraintType.VALUE_92, List.of(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N", "1")));

        ST008OutputBO output = st008.execute(input("2000010006"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getCashStopFlag());
        assertEquals("RES0002", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_92, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(Status.A, output.getStatus());
        assertEquals(DrCrCtlFlag.C, output.getDrCrCtlFlag());
        assertEquals("N", output.getCashFlag());
        assertEquals("1", output.getStopFlag());
    }

    // ST008-TC004 / REQ-003-S03："C 且 N"两条件仅满足其一时不命中——
    // 第 1 条类型 C+Y（cashFlag 非 N）、第 2 条类型 D+N（drCrCtlFlag 非 C），"否"且其余 7 字段 null
    @Test
    public void testST008T04() {
        stubRestraints("2000010003", List.of(
                restraint("2000010003", "RES0001", RestraintType.VALUE_92),
                restraint("2000010003", "RES0002", RestraintType.VALUE_91)));
        stubRestraintType(RestraintType.VALUE_92, List.of(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "Y", "0")));
        stubRestraintType(RestraintType.VALUE_91, List.of(
                restraintType(RestraintType.VALUE_91, DrCrCtlFlag.D, "N", "1")));

        ST008OutputBO output = st008.execute(input("2000010003"));

        assertNegative(output);
    }

    // ST008-TC005 / REQ-003-S04：存在生效记录但均未命中——第 1 条（RES0001/类型91）无生效类型记录
    // （REQ-002-S02 情形），第 2 条（RES0002/类型67）标志 A/Y 不满足，"否"且其余 7 字段 null
    @Test
    public void testST008T05() {
        stubRestraints("2000010004", List.of(
                restraint("2000010004", "RES0001", RestraintType.VALUE_91),
                restraint("2000010004", "RES0002", RestraintType.VALUE_67)));
        stubRestraintType(RestraintType.VALUE_91, List.of());
        stubRestraintType(RestraintType.VALUE_67, List.of(
                restraintType(RestraintType.VALUE_67, DrCrCtlFlag.A, "Y", "0")));

        ST008OutputBO output = st008.execute(input("2000010004"));

        assertNegative(output);
    }

    // ST008-TC006 / REQ-003-S05：账号无任何生效限制记录，结果集为空，不进入类型判定
    // （类型 BCC 不设桩，空结果集无类型查询路径），"否"且其余 7 字段 null
    @Test
    public void testST008T06() {
        stubRestraints("2000010005", List.of());

        ST008OutputBO output = st008.execute(input("2000010005"));

        assertNegative(output);
    }

    // ST008-TC007 / REQ-004-S01（查询点一）：查询【账户限制信息】抛出技术异常，
    // 异常向调用方原样传播（同一实例即不捕获、不转换），不构造业务失败结果
    @Test
    public void testST008T07() {
        RuntimeException expected = new RuntimeException("模拟RB_BUS_RESTRAINTS数据访问异常");
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && "2000010007".equals(eo.getBaseAcctNo())
                        && RestraintsStatus.A.equals(eo.getRestraintsStatus()))))
                .thenThrow(expected);

        ST008InputBO input = input("2000010007");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> st008.execute(input));
        assertSame(expected, actual);
    }

    // ST008-TC008 / REQ-004-S01（查询点二）：账户限制查询正常返回一条记录，
    // 查询【限制类型表】抛出技术异常，异常原样传播，不产生业务输出
    @Test
    public void testST008T08() {
        stubRestraints("2000010008", List.of(
                restraint("2000010008", "RES0001", RestraintType.VALUE_92)));
        RuntimeException expected = new RuntimeException("模拟RB_RESTRAINT_TYPE数据访问异常");
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && RestraintType.VALUE_92.equals(eo.getRestraintType())
                        && Status.A.equals(eo.getStatus()))))
                .thenThrow(expected);

        ST008InputBO input = input("2000010008");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> st008.execute(input));
        assertSame(expected, actual);
    }
}
