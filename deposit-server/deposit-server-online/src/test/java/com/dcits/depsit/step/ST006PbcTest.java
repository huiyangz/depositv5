package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.dcits.depsit.facade.bo.ST006InputBO;
import com.dcits.depsit.facade.bo.ST006OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST006 检查是否存在转账止付限制 单元测试
 *
 * <p>依据正式 Spec ST006 与测试用例 ST006-TC001～TC009：第 1 步按账号与限制状态"A-生效"
 * 查询账户限制信息，第 2 步逐条按账户限制类型查询限制类型表（仅状态"A-生效"记录取得
 * 借贷方控制标志与转账标志），第 3 步判定"D-禁止借方且N-不允许转账"→转账止付标志"是"
 * 并回显第一条满足记录，否则"否"且回显置空；无业务失败场景，技术异常原样传播。
 * 仅对两个 BCC 设桩，被测步骤真实执行。</p>
 */
@ExtendWith(MockitoExtension.class)
class ST006PbcTest {

    /** Spec 示例账号，全部用例固定取值 */
    private static final String BASE_ACCT_NO = "6100230010001";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST006Pbc st006Pbc;

    private ST006InputBO buildInput() {
        ST006InputBO input = new ST006InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        return input;
    }

    /** 构造账户限制信息记录（账号、限制状态"A-生效"、账户限制类型、限制编号） */
    private RbBusRestraintsEO restraint(RestraintType restraintType, String resSeqNo) {
        RbBusRestraintsEO eo = new RbBusRestraintsEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setRestraintsStatus(RestraintsStatus.A);
        eo.setRestraintType(restraintType);
        eo.setResSeqNo(resSeqNo);
        return eo;
    }

    /** 构造限制类型表记录（账户限制类型主键、状态、借贷方控制标志、转账标志） */
    private RbRestraintTypeEO restraintType(RestraintType restraintType, Status status,
            DrCrCtlFlag drCrCtlFlag, String transferFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(status);
        eo.setDrCrCtlFlag(drCrCtlFlag);
        eo.setTransferFlag(transferFlag);
        return eo;
    }

    /** 第 1 步查询桩：argThat 强制查询条件同时携带账号与限制状态"A-生效"，实现条件缺失则桩不匹配 */
    private void stubFindByEo(List<RbBusRestraintsEO> result) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo -> eo != null
                && BASE_ACCT_NO.equals(eo.getBaseAcctNo())
                && RestraintsStatus.A == eo.getRestraintsStatus()))).thenReturn(result);
    }

    // REQ-003-S01（含 REQ-002-S01）：单条生效限制记录（类型 YC2），限制类型表该类型状态"A-生效"、借贷方控制标志"D"、转账标志"N"：转账止付标志"是"，回显该记录及类型表记录，成功结束
    @Test
    void testST006T01() {
        stubFindByEo(List.of(restraint(RestraintType.YC2, "RES20260928000001")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenReturn(restraintType(RestraintType.YC2, Status.A, DrCrCtlFlag.D, "N"));

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        assertEquals("RES20260928000001", output.getResSeqNo());
        assertEquals(RestraintType.YC2, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
    }

    // REQ-001-S01：2 条"A-生效"记录（类型 YC2/类型 13）逐条查限制类型表，"F-未生效"记录被查询条件排除；YC2 组合不满足（D+"Y"）、13 满足（D+"N"）：转账止付标志"是"，回显满足记录（RES20260928000002）
    @Test
    void testST006T02() {
        stubFindByEo(List.of(
                restraint(RestraintType.YC2, "RES20260928000001"),
                restraint(RestraintType.VALUE_13, "RES20260928000002")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenReturn(restraintType(RestraintType.YC2, Status.A, DrCrCtlFlag.D, "Y"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, DrCrCtlFlag.D, "N"));

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        assertEquals("RES20260928000002", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_13, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
    }

    // REQ-003-S02：3 条"A-生效"记录——R1（类型 13，状态生效但标志"A"+"N"组合不满足）、R2（类型 YC2，状态生效、D+N，满足）、R3（类型 5，类型表状态"C"未生效不取得标志）：转账止付标志"是"，回显 R2（RES20260928000002）
    @Test
    void testST006T03() {
        stubFindByEo(List.of(
                restraint(RestraintType.VALUE_13, "RES20260928000001"),
                restraint(RestraintType.YC2, "RES20260928000002"),
                restraint(RestraintType.VALUE_5, "RES20260928000003")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, DrCrCtlFlag.A, "N"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenReturn(restraintType(RestraintType.YC2, Status.A, DrCrCtlFlag.D, "N"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.C, null, null));

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        assertEquals("RES20260928000002", output.getResSeqNo());
        assertEquals(RestraintType.YC2, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
    }

    // 「依赖与副作用」相同账户限制类型重复出现：2 条"A-生效"记录类型均为 YC2（限制编号不同，重复查询结果一致，D+"Y" 不满足）、第 3 条（类型 13）满足：转账止付标志"是"，回显唯一满足记录（RES20260928000003），重复类型不影响输出
    @Test
    void testST006T04() {
        stubFindByEo(List.of(
                restraint(RestraintType.YC2, "RES20260928000001"),
                restraint(RestraintType.YC2, "RES20260928000002"),
                restraint(RestraintType.VALUE_13, "RES20260928000003")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenReturn(restraintType(RestraintType.YC2, Status.A, DrCrCtlFlag.D, "Y"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, DrCrCtlFlag.D, "N"));

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        assertEquals("RES20260928000003", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_13, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
    }

    // REQ-003-S03：4 条"A-生效"记录的类型表匹配记录均状态"A-生效"但标志组合均不满足——YC2（D+"Y"）、13（"A"+"N"）、5（借贷方控制标志空+"N"）、4（D+转账标志空）：无记录满足，转账止付标志"否"，6 个回显字段全部置空
    @Test
    void testST006T05() {
        stubFindByEo(List.of(
                restraint(RestraintType.YC2, "RES20260928000001"),
                restraint(RestraintType.VALUE_13, "RES20260928000002"),
                restraint(RestraintType.VALUE_5, "RES20260928000003"),
                restraint(RestraintType.VALUE_4, "RES20260928000004")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenReturn(restraintType(RestraintType.YC2, Status.A, DrCrCtlFlag.D, "Y"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, DrCrCtlFlag.A, "N"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.A, null, "N"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_4))
                .thenReturn(restraintType(RestraintType.VALUE_4, Status.A, DrCrCtlFlag.D, null));

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getStopFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getDrCrCtlFlag());
        assertNull(output.getStatus());
        assertNull(output.getTransferFlag());
    }

    // REQ-003-S04（含 REQ-002-S02）：类型 5 在限制类型表无匹配记录（返回 null）、类型 YC2 匹配记录状态"C"非生效（其 D+N 不取用）：无记录取得标志、无记录满足，转账止付标志"否"，回显置空，不构成失败
    @Test
    void testST006T06() {
        stubFindByEo(List.of(
                restraint(RestraintType.VALUE_5, "RES20260928000001"),
                restraint(RestraintType.YC2, "RES20260928000002")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(null);
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenReturn(restraintType(RestraintType.YC2, Status.C, DrCrCtlFlag.D, "N"));

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getStopFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getDrCrCtlFlag());
        assertNull(output.getStatus());
        assertNull(output.getTransferFlag());
    }

    // REQ-001-S02 + REQ-002-S03 + REQ-003-S05：第 1 步查询返回空集合（0 条"A-生效"记录），第 2 步无记录可处理、不发起类型表查询（不设类型表桩），第 3 步无记录满足：转账止付标志"否"，6 个回显字段全部置空，不构成失败
    @Test
    void testST006T07() {
        stubFindByEo(List.of());

        ST006OutputBO output = st006Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getStopFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getDrCrCtlFlag());
        assertNull(output.getStatus());
        assertNull(output.getTransferFlag());
    }

    // REQ-004-S02（第 1 步）：查询账户限制信息发生技术异常，异常原样向调用方传播，不转译为业务失败结果或业务错误码
    @Test
    void testST006T08() {
        stubFindByEoThrow(new RuntimeException("数据访问异常-账户限制信息查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st006Pbc.execute(buildInput()));

        assertEquals("数据访问异常-账户限制信息查询", ex.getMessage());
    }

    // REQ-004-S02（第 2 步）：查询限制类型表发生技术异常，异常原样向调用方传播，不转译为业务失败结果或业务错误码
    @Test
    void testST006T09() {
        stubFindByEo(List.of(restraint(RestraintType.YC2, "RES20260928000001")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.YC2))
                .thenThrow(new RuntimeException("数据访问异常-限制类型表查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st006Pbc.execute(buildInput()));

        assertEquals("数据访问异常-限制类型表查询", ex.getMessage());
    }

    private void stubFindByEoThrow(RuntimeException exception) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo -> eo != null
                && BASE_ACCT_NO.equals(eo.getBaseAcctNo())
                && RestraintsStatus.A == eo.getRestraintsStatus()))).thenThrow(exception);
    }
}
