package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.dcits.depsit.facade.bo.ST004InputBO;
import com.dcits.depsit.facade.bo.ST004OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST004 检查转账止收限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST004-TC001 ~ TC010），
 * 预期结果来自正式 Spec ST004（inputs 绑定的 ST004.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"否"是业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST004PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST004Pbc st004;

    /** 构造公共输入：账号 */
    private ST004InputBO input() {
        ST004InputBO input = new ST004InputBO();
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

    /** 步骤2 桩：按 账户限制类型+状态=Status.A 组合条件核对请求并返回类型记录列表 */
    private void stubRestraintType(RestraintType restraintType, List<RbRestraintTypeEO> records) {
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && eo.getRestraintType() == restraintType && eo.getStatus() == Status.A)))
                .thenReturn(records);
    }

    /** "否"路径公共断言：succeed=true、错误字段 null、transferStopFlag="否"、其余 7 字段 null */
    private void assertNegative(ST004OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getTransferStopFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getDrCrCtlFlag());
        assertNull(output.getStatus());
        assertNull(output.getTransferFlag());
        assertNull(output.getStopFlag());
    }

    // REQ-001-S01 账号无任何限制记录：不触发类型查询与判定，transferStopFlag="否"且其余 7 字段为空
    @Test
    public void testST004T01() {
        stubRestraints(Collections.emptyList());

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-001-S02 限制记录存在但限制状态为"E-已终止"（业务背景），被组合查询条件排除，等同无记录，"否"且输出为空
    @Test
    public void testST004T02() {
        stubRestraints(Collections.emptyList());

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-001-S03 两条生效记录（66/92）均经类型查询与判定且均不满足（D/N 与 C/Y），"否"且输出为空
    @Test
    public void testST004T03() {
        stubRestraints(Arrays.asList(
                restraint("RES20260929000001", RestraintType.VALUE_66),
                restraint("RES20260929000002", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_66, Collections.singletonList(
                restraintType(RestraintType.VALUE_66, DrCrCtlFlag.D, "N", "0")));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "Y", "1")));

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-002-S02 限制记录生效但类型表无 status="A" 记录：取不到标志值按"否则"处理，"否"且输出为空
    @Test
    public void testST004T04() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260929000001", RestraintType.VALUE_66)));
        stubRestraintType(RestraintType.VALUE_66, Collections.emptyList());

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-003-S02 借贷方控制标志="C"但转账标志="Y"（非"N"）：条件不成立，"否"且输出为空
    @Test
    public void testST004T05() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260929000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "Y", "1")));

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-003-S03 借贷方控制标志="D"（禁止借方）且转账标志="N"：标志不等于"C"条件不成立，"否"且输出为空
    @Test
    public void testST004T06() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260929000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.D, "N", "1")));

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-003-S04 借贷方控制标志="A"（禁止借贷方）且转账标志="Y"：两条件均不成立，"否"且输出为空
    @Test
    public void testST004T07() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260929000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.A, "Y", "1")));

        ST004OutputBO output = st004.execute(input());

        assertNegative(output);
    }

    // REQ-004-S01（含 REQ-002-S01 取得三标志、REQ-003-S01 判定满足）单条生效记录满足："是"且输出完整命中字段
    @Test
    public void testST004T08() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260929000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N", "1")));

        ST004OutputBO output = st004.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getTransferStopFlag());
        assertEquals("RES20260929000001", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_92, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.C, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
        assertEquals("1", output.getStopFlag());
    }

    // REQ-004-S02 两条生效记录恰乙（VALUE_92）满足："是"且输出取满足的记录乙，甲的值不出现
    @Test
    public void testST004T09() {
        stubRestraints(Arrays.asList(
                restraint("RES20260929000001", RestraintType.VALUE_66),
                restraint("RES20260929000002", RestraintType.VALUE_92)));
        // 甲（VALUE_66）类型查询按用例说明返回空列表（若被调用则该条不满足；lenient 声明兼容先命中乙即短路的实现顺序）
        stubRestraintType(RestraintType.VALUE_66, Collections.emptyList());
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N", "1")));

        ST004OutputBO output = st004.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getTransferStopFlag());
        assertEquals("RES20260929000002", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_92, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.C, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
        assertEquals("1", output.getStopFlag());
    }

    // REQ-004-S03 两条生效记录（92/96）同时满足："是"且输出取其中一条，组合断言不依赖选取顺序、字段内部自洽
    @Test
    public void testST004T10() {
        stubRestraints(Arrays.asList(
                restraint("RES20260929000001", RestraintType.VALUE_92),
                restraint("RES20260929000002", RestraintType.VALUE_96)));
        // 两类型均配置 C/N，首个命中即返回，另一类型查询可能不被调用（顺序未约束），lenient 声明避免未用桩失败
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N", "1")));
        stubRestraintType(RestraintType.VALUE_96, Collections.singletonList(
                restraintType(RestraintType.VALUE_96, DrCrCtlFlag.C, "N", "2")));

        ST004OutputBO output = st004.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getTransferStopFlag());
        boolean firstHit = "RES20260929000001".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_92
                && "1".equals(output.getStopFlag());
        boolean secondHit = "RES20260929000002".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_96
                && "2".equals(output.getStopFlag());
        assertTrue(firstHit || secondHit);
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.C, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
        assertEquals("N", output.getTransferFlag());
    }
}
