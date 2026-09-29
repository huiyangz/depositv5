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

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST003InputBO;
import com.dcits.depsit.facade.bo.ST003OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST003 检查有权机关冻结限制 单元测试
 *
 * <p>依据正式 Spec ST003 与测试用例 ST003-TC001～TC008：第 1 步按账号与限制状态"A-生效"
 * 查询账户限制信息，第 2 步逐条按账户限制类型查询限制类型表，仅状态"A-生效"记录的有权机关
 * 冻结标志参与输出；无业务失败场景，技术异常原样传播。仅对两个 BCC 设桩，被测步骤真实执行。</p>
 */
@ExtendWith(MockitoExtension.class)
class ST003PbcTest {

    /** Spec 示例账号，全部用例固定取值 */
    private static final String BASE_ACCT_NO = "6100230010001";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST003Pbc st003Pbc;

    private ST003InputBO buildInput() {
        ST003InputBO input = new ST003InputBO();
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

    /** 构造限制类型表记录（账户限制类型主键、状态、有权机关冻结标志） */
    private RbRestraintTypeEO restraintType(RestraintType restraintType, Status status, String ahBuFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(status);
        eo.setAhBuFlag(ahBuFlag);
        return eo;
    }

    /** 第 1 步查询桩：argThat 强制查询条件同时携带账号与限制状态"A-生效"，实现条件缺失则桩不匹配 */
    private void stubFindByEo(List<RbBusRestraintsEO> result) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo -> eo != null
                && BASE_ACCT_NO.equals(eo.getBaseAcctNo())
                && RestraintsStatus.A == eo.getRestraintsStatus()))).thenReturn(result);
    }

    // REQ-002-S01：单条生效限制记录（类型"5"统一查控平台冻结），限制类型表该类型状态"A-生效"且标志"1"：输出 ahBuFlag="1"，成功结束
    @Test
    void testST003T01() {
        stubFindByEo(List.of(restraint(RestraintType.VALUE_5, "RES20260928000001")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.A, "1"));

        ST003OutputBO output = st003Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("1", output.getAhBuFlag());
    }

    // REQ-001-S01：2 条"A-生效"记录（类型"5"/类型"13"挂失止付）逐条查限制类型表，"F-未生效"记录被查询条件排除；仅类型"5"取得非空标志"1"
    @Test
    void testST003T02() {
        stubFindByEo(List.of(
                restraint(RestraintType.VALUE_5, "RES20260928000001"),
                restraint(RestraintType.VALUE_13, "RES20260928000002")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.A, "1"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, null));

        ST003OutputBO output = st003Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("1", output.getAhBuFlag());
    }

    // REQ-002-S02：3 条生效记录（类型"4"统一查控平台全额止付/"5"/"13"）；"4"状态生效但标志空、"5"状态"C"非生效（标志"2"不取用）、"13"状态生效标志"1"，输出 ahBuFlag="1"
    @Test
    void testST003T03() {
        stubFindByEo(List.of(
                restraint(RestraintType.VALUE_4, "RES20260928000001"),
                restraint(RestraintType.VALUE_5, "RES20260928000002"),
                restraint(RestraintType.VALUE_13, "RES20260928000003")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_4))
                .thenReturn(restraintType(RestraintType.VALUE_4, Status.A, null));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.C, "2"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, "1"));

        ST003OutputBO output = st003Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("1", output.getAhBuFlag());
    }

    // 相同账户限制类型重复出现（Spec「依赖与副作用」）：2 条生效记录类型均为"5"（限制编号不同），重复查询结果一致，输出仍为标志"1"
    @Test
    void testST003T04() {
        stubFindByEo(List.of(
                restraint(RestraintType.VALUE_5, "RES20260928000001"),
                restraint(RestraintType.VALUE_5, "RES20260928000002")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.A, "1"));

        ST003OutputBO output = st003Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("1", output.getAhBuFlag());
    }

    // REQ-001-S02 + REQ-002-S04：第 1 步返回空集合（0 条生效记录），不发起任何限制类型表查询，未查得非空标志不赋值，输出 ahBuFlag=null，步骤成功
    @Test
    void testST003T05() {
        stubFindByEo(List.of());

        ST003OutputBO output = st003Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getAhBuFlag());
    }

    // REQ-002-S03：3 条生效记录均未取得非空标志——类型"4"无匹配记录（null）、类型"5"状态"C"非生效、类型"13"状态生效但标志空（null）：不赋值，输出 ahBuFlag=null，步骤成功
    @Test
    void testST003T06() {
        stubFindByEo(List.of(
                restraint(RestraintType.VALUE_4, "RES20260928000001"),
                restraint(RestraintType.VALUE_5, "RES20260928000002"),
                restraint(RestraintType.VALUE_13, "RES20260928000003")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_4))
                .thenReturn(null);
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenReturn(restraintType(RestraintType.VALUE_5, Status.C, "2"));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A, null));

        ST003OutputBO output = st003Pbc.execute(buildInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getAhBuFlag());
    }

    // REQ-003-S02：查询账户限制信息（第 1 步）发生技术异常，异常原样向调用方传播，不转译为业务失败结果
    @Test
    void testST003T07() {
        stubFindByEoThrow(new RuntimeException("数据访问异常-账户限制信息查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st003Pbc.execute(buildInput()));

        assertEquals("数据访问异常-账户限制信息查询", ex.getMessage());
    }

    // REQ-003-S02：查询限制类型表（第 2 步）发生技术异常，异常原样向调用方传播，不转译为业务失败结果
    @Test
    void testST003T08() {
        stubFindByEo(List.of(restraint(RestraintType.VALUE_5, "RES20260928000001")));
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_5))
                .thenThrow(new RuntimeException("数据访问异常-限制类型表查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st003Pbc.execute(buildInput()));

        assertEquals("数据访问异常-限制类型表查询", ex.getMessage());
    }

    private void stubFindByEoThrow(RuntimeException exception) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo -> eo != null
                && BASE_ACCT_NO.equals(eo.getBaseAcctNo())
                && RestraintsStatus.A == eo.getRestraintsStatus()))).thenThrow(exception);
    }
}
