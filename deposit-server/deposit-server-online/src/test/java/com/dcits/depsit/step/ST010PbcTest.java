package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collections;
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
import com.dcits.depsit.facade.bo.ST010InputBO;
import com.dcits.depsit.facade.bo.ST010OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST010 检查是否存在转账不收不付限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST010-TC001 ~ TC011），
 * 预期结果来自正式 Spec ST010（inputs 绑定的 ST010.md）。
 * 本步骤无业务失败场景，业务用例均 succeed=true；"否"是业务结果不是失败，
 * 未命中时其余 7 个输出字段按 Spec 不作断言依据。
 */
@ExtendWith(MockitoExtension.class)
public class ST010PbcTest {

    /** 示例账号（用例公共输入） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST010Pbc st010;

    /** 构造公共输入：账号 */
    private ST010InputBO input() {
        ST010InputBO input = new ST010InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        return input;
    }

    /** 构造账户限制记录：限制状态均为"生效"（RestraintsStatus.A） */
    private RbBusRestraintsEO restraint(String resSeqNo, RestraintType restraintType) {
        RbBusRestraintsEO eo = new RbBusRestraintsEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setResSeqNo(resSeqNo);
        eo.setRestraintType(restraintType);
        eo.setRestraintsStatus(RestraintsStatus.A);
        return eo;
    }

    /** 构造限制类型生效记录：状态均为 Status.A */
    private RbRestraintTypeEO restraintType(RestraintType restraintType, DrCrCtlFlag drCrCtlFlag,
                                            String transferFlag, String stopFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(Status.A);
        eo.setDrCrCtlFlag(drCrCtlFlag);
        eo.setTransferFlag(transferFlag);
        eo.setStopFlag(stopFlag);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 账号+限制状态=RestraintsStatus.A 组合条件核对请求并返回限制记录列表 */
    private void stubRestraints(List<RbBusRestraintsEO> records) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()) && eo.getRestraintsStatus() == RestraintsStatus.A)))
                .thenReturn(records);
    }

    /** 步骤1 桩（技术异常）：对条件匹配请求抛出给定异常，验证原样传播 */
    private void stubRestraintsThrow(RuntimeException exception) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()) && eo.getRestraintsStatus() == RestraintsStatus.A)))
                .thenThrow(exception);
    }

    /** 步骤2 桩：按 账户限制类型+状态=Status.A 组合条件核对请求并返回类型记录列表 */
    private void stubRestraintType(RestraintType restraintType, List<RbRestraintTypeEO> records) {
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && eo.getRestraintType() == restraintType && eo.getStatus() == Status.A)))
                .thenReturn(records);
    }

    /** 步骤2 桩（技术异常）：对条件匹配请求抛出给定异常，验证原样传播 */
    private void stubRestraintTypeThrow(RestraintType restraintType, RuntimeException exception) {
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && eo.getRestraintType() == restraintType && eo.getStatus() == Status.A)))
                .thenThrow(exception);
    }

    /** 未命中路径公共断言：succeed=true、错误字段 null、transferNoRecvNoPayFlag="否"；其余 7 字段 Spec 不作断言依据 */
    private void assertNegative(ST010OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getTransferNoRecvNoPayFlag());
    }

    /** 命中路径公共断言：转账不收不付标志"是"及完整命中字段映射 */
    private void assertHit(ST010OutputBO output, String resSeqNo, RestraintType restraintType, String stopFlag) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getTransferNoRecvNoPayFlag());
        assertEquals(resSeqNo, output.getResSeqNo());
        assertEquals(restraintType, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.A, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
        assertEquals(stopFlag, output.getStopFlag());
    }

    // REQ-003-S01（含 REQ-001-S01、REQ-002-S01）单条生效限制记录命中（类型 17 为 A/N）："是"及完整命中字段
    @Test
    public void testST010T01() {
        stubRestraints(Collections.singletonList(
                restraint("RSN20260928000001", RestraintType.VALUE_17)));
        stubRestraintType(RestraintType.VALUE_17, Collections.singletonList(
                restraintType(RestraintType.VALUE_17, DrCrCtlFlag.A, "N", "1")));

        ST010OutputBO output = st010.execute(input());

        assertHit(output, "RSN20260928000001", RestraintType.VALUE_17, "1");
    }

    // REQ-003-S02 两条记录均命中（17、20 均 A/N）：取第一条命中记录，后续命中不改变输出
    @Test
    public void testST010T02() {
        stubRestraints(Arrays.asList(
                restraint("RSN20260928000001", RestraintType.VALUE_17),
                restraint("RSN20260928000002", RestraintType.VALUE_20)));
        stubRestraintType(RestraintType.VALUE_17, Collections.singletonList(
                restraintType(RestraintType.VALUE_17, DrCrCtlFlag.A, "N", "1")));
        // 首条命中即返回时 VALUE_20 类型查询可能不被调用，lenient 声明兼容实现顺序
        stubRestraintType(RestraintType.VALUE_20, Collections.singletonList(
                restraintType(RestraintType.VALUE_20, DrCrCtlFlag.A, "N", "2")));

        ST010OutputBO output = st010.execute(input());

        assertHit(output, "RSN20260928000001", RestraintType.VALUE_17, "1");
    }

    // REQ-003-S04 首条不命中（类型 20 为 C/Y）、第二条命中（类型 17 为 A/N）：输出取第二条满足记录
    @Test
    public void testST010T03() {
        stubRestraints(Arrays.asList(
                restraint("RSN20260928000001", RestraintType.VALUE_20),
                restraint("RSN20260928000002", RestraintType.VALUE_17)));
        stubRestraintType(RestraintType.VALUE_20, Collections.singletonList(
                restraintType(RestraintType.VALUE_20, DrCrCtlFlag.C, "Y", "2")));
        stubRestraintType(RestraintType.VALUE_17, Collections.singletonList(
                restraintType(RestraintType.VALUE_17, DrCrCtlFlag.A, "N", "1")));

        ST010OutputBO output = st010.execute(input());

        assertHit(output, "RSN20260928000002", RestraintType.VALUE_17, "1");
    }

    // REQ-001-S02（情形一）账号无任何账户限制记录：查询结果为空，不进入步骤2、3，"否"
    @Test
    public void testST010T04() {
        stubRestraints(Collections.emptyList());

        ST010OutputBO output = st010.execute(input());

        assertNegative(output);
    }

    // REQ-001-S02（情形二）记录状态均非"A-生效"（业务背景 E/F）被组合查询条件排除：等同空结果，"否"
    @Test
    public void testST010T05() {
        stubRestraints(Collections.emptyList());

        ST010OutputBO output = st010.execute(input());

        assertNegative(output);
    }

    // REQ-002-S02 限制记录生效但类型表无状态生效记录（无记录或状态非 A，单元边界均为空列表）：按未命中处理，"否"
    @Test
    public void testST010T06() {
        stubRestraints(Collections.singletonList(
                restraint("RSN20260928000001", RestraintType.VALUE_17)));
        stubRestraintType(RestraintType.VALUE_17, Collections.emptyList());

        ST010OutputBO output = st010.execute(input());

        assertNegative(output);
    }

    // REQ-003-S05（情形一）命中条件仅部分满足：drCrCtlFlag=A 但 transferFlag="Y"（非"N"），"否"
    @Test
    public void testST010T07() {
        stubRestraints(Collections.singletonList(
                restraint("RSN20260928000001", RestraintType.VALUE_17)));
        stubRestraintType(RestraintType.VALUE_17, Collections.singletonList(
                restraintType(RestraintType.VALUE_17, DrCrCtlFlag.A, "Y", "1")));

        ST010OutputBO output = st010.execute(input());

        assertNegative(output);
    }

    // REQ-003-S05（情形二）命中条件仅部分满足：transferFlag=N 但 drCrCtlFlag="D-禁止借方"（非"A"），"否"
    @Test
    public void testST010T08() {
        stubRestraints(Collections.singletonList(
                restraint("RSN20260928000001", RestraintType.VALUE_17)));
        stubRestraintType(RestraintType.VALUE_17, Collections.singletonList(
                restraintType(RestraintType.VALUE_17, DrCrCtlFlag.D, "N", "1")));

        ST010OutputBO output = st010.execute(input());

        assertNegative(output);
    }

    // REQ-003-S03 两条生效记录逐条判定均不满足（首条 A/Y、次条 D/N）：遍历全部记录后"否"
    @Test
    public void testST010T09() {
        stubRestraints(Arrays.asList(
                restraint("RSN20260928000001", RestraintType.VALUE_17),
                restraint("RSN20260928000002", RestraintType.VALUE_20)));
        stubRestraintType(RestraintType.VALUE_17, Collections.singletonList(
                restraintType(RestraintType.VALUE_17, DrCrCtlFlag.A, "Y", "1")));
        stubRestraintType(RestraintType.VALUE_20, Collections.singletonList(
                restraintType(RestraintType.VALUE_20, DrCrCtlFlag.D, "N", "2")));

        ST010OutputBO output = st010.execute(input());

        assertNegative(output);
    }

    // REQ-004-S01（步骤1）底层查询技术异常：异常向调用方原样传播，不吞没、不转换为业务结果
    @Test
    public void testST010T10() {
        stubRestraintsThrow(new RuntimeException("RB_BUS_RESTRAINTS 数据存取异常"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st010.execute(input()));

        assertEquals("RB_BUS_RESTRAINTS 数据存取异常", ex.getMessage());
    }

    // REQ-004-S01（步骤2）限制记录生效后类型查询技术异常：异常原样传播，不吞没、不转换为业务结果
    @Test
    public void testST010T11() {
        stubRestraints(Collections.singletonList(
                restraint("RSN20260928000001", RestraintType.VALUE_17)));
        stubRestraintTypeThrow(RestraintType.VALUE_17, new RuntimeException("RB_RESTRAINT_TYPE 数据存取异常"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st010.execute(input()));

        assertEquals("RB_RESTRAINT_TYPE 数据存取异常", ex.getMessage());
    }
}
