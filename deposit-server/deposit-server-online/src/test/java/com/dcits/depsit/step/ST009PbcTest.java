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
import com.dcits.depsit.facade.bo.ST009InputBO;
import com.dcits.depsit.facade.bo.ST009OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST009 检查是否存在止付限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST009-TC001 ~ TC009），
 * 预期结果来自正式 Spec ST009（inputs 绑定的 ST009.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"否"是业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST009PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST009Pbc st009;

    /** 构造公共输入：账号 */
    private ST009InputBO input() {
        ST009InputBO input = new ST009InputBO();
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
    private RbRestraintTypeEO restraintType(RestraintType restraintType, DrCrCtlFlag drCrCtlFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(Status.A);
        eo.setDrCrCtlFlag(drCrCtlFlag);
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

    /** "否"路径公共断言：succeed=true、错误字段 null、stopFlag="否"、其余 5 字段 null */
    private void assertNegative(ST009OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getStopFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getDrCrCtlFlag());
        assertNull(output.getStatus());
    }

    // REQ-001-S01 账号无任何限制记录：不触发类型查询与判定，stopFlag="否"且其余 5 字段为空
    @Test
    public void testST009T01() {
        stubRestraints(Collections.emptyList());

        ST009OutputBO output = st009.execute(input());

        assertNegative(output);
    }

    // REQ-001-S02 限制记录存在但限制状态为"E-已终止"（业务背景），被组合查询条件排除，等同无记录，"否"且输出为空
    @Test
    public void testST009T02() {
        stubRestraints(Collections.emptyList());

        ST009OutputBO output = st009.execute(input());

        assertNegative(output);
    }

    // REQ-001-S03 两条生效记录（13/66）均经类型查询与判定且均不满足（C 与 A），"否"且输出为空
    @Test
    public void testST009T03() {
        stubRestraints(Arrays.asList(
                restraint("RES20260930000001", RestraintType.VALUE_13),
                restraint("RES20260930000002", RestraintType.VALUE_66)));
        stubRestraintType(RestraintType.VALUE_13, Collections.singletonList(
                restraintType(RestraintType.VALUE_13, DrCrCtlFlag.C)));
        stubRestraintType(RestraintType.VALUE_66, Collections.singletonList(
                restraintType(RestraintType.VALUE_66, DrCrCtlFlag.A)));

        ST009OutputBO output = st009.execute(input());

        assertNegative(output);
    }

    // REQ-002-S02 限制记录生效但类型表无 status="A" 记录：取不到借贷方控制标志按"否则"处理，"否"且输出为空
    @Test
    public void testST009T04() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_13)));
        stubRestraintType(RestraintType.VALUE_13, Collections.emptyList());

        ST009OutputBO output = st009.execute(input());

        assertNegative(output);
    }

    // REQ-003-S02 借贷方控制标志="C"（禁止贷方）：不等于"D"条件不成立，"否"且输出为空
    @Test
    public void testST009T05() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_13)));
        stubRestraintType(RestraintType.VALUE_13, Collections.singletonList(
                restraintType(RestraintType.VALUE_13, DrCrCtlFlag.C)));

        ST009OutputBO output = st009.execute(input());

        assertNegative(output);
    }

    // REQ-003-S03 借贷方控制标志="A"（禁止借贷方）：严格相等判定不等于"D"，"否"且输出为空
    @Test
    public void testST009T06() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_13)));
        stubRestraintType(RestraintType.VALUE_13, Collections.singletonList(
                restraintType(RestraintType.VALUE_13, DrCrCtlFlag.A)));

        ST009OutputBO output = st009.execute(input());

        assertNegative(output);
    }

    // REQ-004-S01（含 REQ-002-S01 取得生效类型标志、REQ-003-S01 标志=D 判定满足）单条生效记录满足："是"且输出完整命中字段
    @Test
    public void testST009T07() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_13)));
        stubRestraintType(RestraintType.VALUE_13, Collections.singletonList(
                restraintType(RestraintType.VALUE_13, DrCrCtlFlag.D)));

        ST009OutputBO output = st009.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        assertEquals("RES20260930000001", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_13, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
    }

    // REQ-004-S02 两条生效记录恰乙（VALUE_13）满足："是"且输出取满足的记录乙，甲的值不出现
    @Test
    public void testST009T08() {
        stubRestraints(Arrays.asList(
                restraint("RES20260930000001", RestraintType.VALUE_66),
                restraint("RES20260930000002", RestraintType.VALUE_13)));
        // 甲（VALUE_66）生效配置为 C 不满足；lenient 声明兼容先命中乙即短路的实现顺序
        stubRestraintType(RestraintType.VALUE_66, Collections.singletonList(
                restraintType(RestraintType.VALUE_66, DrCrCtlFlag.C)));
        stubRestraintType(RestraintType.VALUE_13, Collections.singletonList(
                restraintType(RestraintType.VALUE_13, DrCrCtlFlag.D)));

        ST009OutputBO output = st009.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        assertEquals("RES20260930000002", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_13, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
    }

    // REQ-004-S03 两条生效记录（13/92）同时满足："是"且输出取其中一条，组合断言不依赖选取顺序、字段内部自洽
    @Test
    public void testST009T09() {
        stubRestraints(Arrays.asList(
                restraint("RES20260930000001", RestraintType.VALUE_13),
                restraint("RES20260930000002", RestraintType.VALUE_92)));
        // 两类型均配置 D，首个命中即返回，另一类型查询可能不被调用（顺序未约束），lenient 声明避免未用桩失败
        stubRestraintType(RestraintType.VALUE_13, Collections.singletonList(
                restraintType(RestraintType.VALUE_13, DrCrCtlFlag.D)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.D)));

        ST009OutputBO output = st009.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getStopFlag());
        boolean firstHit = "RES20260930000001".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_13;
        boolean secondHit = "RES20260930000002".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_92;
        assertTrue(firstHit || secondHit);
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(DrCrCtlFlag.D, output.getDrCrCtlFlag());
        assertEquals(Status.A, output.getStatus());
    }
}
