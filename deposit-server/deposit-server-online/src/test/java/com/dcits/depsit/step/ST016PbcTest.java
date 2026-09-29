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
import com.dcits.depsit.facade.bo.ST016InputBO;
import com.dcits.depsit.facade.bo.ST016OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST016 检查是否存在现金止付限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST016-TC001 ~ TC008），
 * 预期结果来自正式 Spec ST016（inputs 绑定的 ST016.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"否"是业务判定结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST016PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST016Pbc st016;

    /** 构造公共输入：账号 */
    private ST016InputBO input() {
        ST016InputBO input = new ST016InputBO();
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
                                            String cashFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(Status.A);
        eo.setDrCrCtlFlag(drCrCtlFlag);
        eo.setCashFlag(cashFlag);
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

    /** 公共断言：succeed=true、错误字段 null、cashStopFlag 为预期值 */
    private void assertResult(ST016OutputBO output, String expectedCashStopFlag) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(expectedCashStopFlag, output.getCashStopFlag());
    }

    // REQ-001-S01 账号无任何限制记录：步骤1查询为空，不发生对限制类型表的查询，cashStopFlag="否"
    @Test
    public void testST016T01() {
        stubRestraints(Collections.emptyList());

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "否");
    }

    // REQ-001-S02 限制记录存在但限制状态为"E-已终止"（业务背景）：被组合查询条件（限制状态="A"）排除，等同无记录，cashStopFlag="否"
    @Test
    public void testST016T02() {
        stubRestraints(Collections.emptyList());

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "否");
    }

    // REQ-002-S01+REQ-003-S01 单条生效记录（类型92）且类型生效配置 D/N：步骤2取得两标志、步骤3判定满足，cashStopFlag="是"
    @Test
    public void testST016T03() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.D, "N")));

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "是");
    }

    // REQ-002-S02 类型表无 status="A" 记录（业务背景 status="F"，被查询条件排除）：取不到标志值，该条不满足，cashStopFlag="否"
    @Test
    public void testST016T04() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.emptyList());

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "否");
    }

    // REQ-003-S02 借贷方控制标志="D"但现金标志="Y"（非"N"取值，含义需求未定义）：现金标志条件不成立，cashStopFlag="否"
    @Test
    public void testST016T05() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.D, "Y")));

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "否");
    }

    // REQ-003-S03 借贷方控制标志="C"（禁止贷方，"D"以外取值）且现金标志="N"：借贷方控制标志条件不成立，cashStopFlag="否"
    @Test
    public void testST016T06() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.C, "N")));

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "否");
    }

    // REQ-003-S04 两条生效记录（66/92）：66配置 C/N 不满足、92配置 D/N 满足，恰一条满足即"是"（与处理先后顺序无关）
    @Test
    public void testST016T07() {
        stubRestraints(Arrays.asList(
                restraint("RES20260930000001", RestraintType.VALUE_66),
                restraint("RES20260930000002", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_66, Collections.singletonList(
                restraintType(RestraintType.VALUE_66, DrCrCtlFlag.C, "N")));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.D, "N")));

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "是");
    }

    // REQ-003-S05 两条生效记录（66/92）均不同时满足两条件（66配置 C/N、92配置 D/Y），cashStopFlag="否"
    @Test
    public void testST016T08() {
        stubRestraints(Arrays.asList(
                restraint("RES20260930000001", RestraintType.VALUE_66),
                restraint("RES20260930000002", RestraintType.VALUE_92)));
        stubRestraintType(RestraintType.VALUE_66, Collections.singletonList(
                restraintType(RestraintType.VALUE_66, DrCrCtlFlag.C, "N")));
        stubRestraintType(RestraintType.VALUE_92, Collections.singletonList(
                restraintType(RestraintType.VALUE_92, DrCrCtlFlag.D, "Y")));

        ST016OutputBO output = st016.execute(input());

        assertResult(output, "否");
    }
}
