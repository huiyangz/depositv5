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

import com.dcits.depsit.enums.HierarchyCode;
import com.dcits.depsit.enums.ResBranchRange;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST096InputBO;
import com.dcits.depsit.facade.bo.ST096OutputBO;
import com.dcits.depsit.facade.components.IFmBranchBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.FmBranchEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST096 检查账户机构是否可匹配到限额场景配置 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST096-TC001 ~ TC008），
 * 预期结果来自正式 Spec ST096（inputs 绑定的 ST096.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true 且错误字段为 null；
 * 未命中时限额场景配置组为空是业务结果，不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST096PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    /** 启用标志取值：Y-启用 */
    private static final String VALID_FLAG_Y = "Y";

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @Mock
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Mock
    private IFmBranchBcc fmBranchBcc;

    @InjectMocks
    private ST096Pbc st096;

    /** 构造公共输入：账号 */
    private ST096InputBO input() {
        ST096InputBO input = new ST096InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        return input;
    }

    /** 构造账户主表记录：账号与账户开立行行号（351155，支行） */
    private RbBusAcctEO busAcct() {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setAcctBranch(TranBranch.VALUE_351155);
        return eo;
    }

    /** 构造启用限额控制配置记录：validFlag 均为"Y" */
    private RbLimitCtrlConfEO limitConf(TranBranch branchId, String sceneNo, ResBranchRange range) {
        RbLimitCtrlConfEO eo = new RbLimitCtrlConfEO();
        eo.setLimitBranchId(branchId);
        eo.setLimitSceneNo(sceneNo);
        eo.setLimitBranchRange(range);
        eo.setValidFlag(VALID_FLAG_Y);
        return eo;
    }

    /** 构造机构信息表记录：归属机构号、机构层级、归属上级机构号 */
    private FmBranchEO branch(TranBranch branchNo, HierarchyCode hierarchy, TranBranch attachedTo) {
        FmBranchEO eo = new FmBranchEO();
        eo.setBranch(branchNo);
        eo.setHierarchyCode(hierarchy);
        eo.setAttachedTo(attachedTo);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 账号 组合条件核对请求并返回账户主表记录 */
    private void stubBusAcct() {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()))))
                .thenReturn(Collections.singletonList(busAcct()));
    }

    /** 步骤2/5.1 桩：按 限额机构编码+启用标志="Y" 组合条件核对请求并返回配置记录列表 */
    private void stubLimitConf(TranBranch branchId, List<RbLimitCtrlConfEO> records) {
        lenient().when(rbLimitCtrlConfBcc.findByEo(argThat(eo ->
                eo != null && eo.getLimitBranchId() == branchId && VALID_FLAG_Y.equals(eo.getValidFlag()))))
                .thenReturn(records);
    }

    /** 步骤4 桩：示例两级上级链 351155(层级2)→352001(层级1)→351001(层级0,无上级) */
    private void stubStandardBranchChain() {
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_351155))
                .thenReturn(branch(TranBranch.VALUE_351155, HierarchyCode.VALUE_2, TranBranch.VALUE_352001));
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_352001))
                .thenReturn(branch(TranBranch.VALUE_352001, HierarchyCode.VALUE_1, TranBranch.VALUE_351001));
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_351001))
                .thenReturn(branch(TranBranch.VALUE_351001, HierarchyCode.VALUE_0, null));
    }

    /** 成功公共断言：succeed=true、错误字段 null */
    private void assertSuccess(ST096OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    /** 未命中公共断言：限额场景配置组四字段均为 null */
    private void assertNoLimitConf(ST096OutputBO output) {
        assertNull(output.getLimitSceneNo());
        assertNull(output.getLimitBranchId());
        assertNull(output.getLimitBranchRange());
        assertNull(output.getValidFlag());
    }

    // ST096-TC001（REQ-001-S01、REQ-002-S01、REQ-005-S01）开立行直接命中单条启用记录：
    // 配置组取该记录且不进入上级流程，branch/attachedTo 为空
    @Test
    public void testST096T01() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.singletonList(
                limitConf(TranBranch.VALUE_351155, "LS001", ResBranchRange.B)));

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertEquals("LS001", output.getLimitSceneNo());
        assertEquals(TranBranch.VALUE_351155, output.getLimitBranchId());
        assertEquals(ResBranchRange.B, output.getLimitBranchRange());
        assertEquals(VALID_FLAG_Y, output.getValidFlag());
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertNull(output.getBranch());
        assertNull(output.getAttachedTo());
    }

    // ST096-TC002（REQ-002-S02）同一机构两条启用记录取查询结果第一条：四字段同源，
    // 断言不依赖排序口径（LS001→B、LS002→D 逐一对应）
    @Test
    public void testST096T02() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Arrays.asList(
                limitConf(TranBranch.VALUE_351155, "LS001", ResBranchRange.B),
                limitConf(TranBranch.VALUE_351155, "LS002", ResBranchRange.D)));

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        String sceneNo = output.getLimitSceneNo();
        assertTrue("LS001".equals(sceneNo) || "LS002".equals(sceneNo));
        if ("LS001".equals(sceneNo)) {
            assertEquals(ResBranchRange.B, output.getLimitBranchRange());
        } else {
            assertEquals(ResBranchRange.D, output.getLimitBranchRange());
        }
        assertEquals(TranBranch.VALUE_351155, output.getLimitBranchId());
        assertEquals(VALID_FLAG_Y, output.getValidFlag());
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertNull(output.getBranch());
        assertNull(output.getAttachedTo());
    }

    // ST096-TC003（REQ-002-S03、REQ-003-S02、REQ-005-S02）开立行仅停用记录（查询结果为空）
    // 且开立行无上级（attachedTo=null）：上级集合为空，配置组全空，机构组取开立行记录
    @Test
    public void testST096T03() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.emptyList());
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_351155))
                .thenReturn(branch(TranBranch.VALUE_351155, HierarchyCode.VALUE_2, null));

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertNoLimitConf(output);
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertEquals(TranBranch.VALUE_351155, output.getBranch());
        assertNull(output.getAttachedTo());
    }

    // ST096-TC004（REQ-003-S01、REQ-004-S02、REQ-005-S03）两级上级链完整收集：
    // 直接上级与开立行均未命中、总行命中，返回总行配置，机构组取开立行自身记录
    @Test
    public void testST096T04() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.emptyList());
        stubLimitConf(TranBranch.VALUE_352001, Collections.emptyList());
        stubLimitConf(TranBranch.VALUE_351001, Collections.singletonList(
                limitConf(TranBranch.VALUE_351001, "LS003", ResBranchRange.D)));
        stubStandardBranchChain();

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertEquals("LS003", output.getLimitSceneNo());
        assertEquals(TranBranch.VALUE_351001, output.getLimitBranchId());
        assertEquals(ResBranchRange.D, output.getLimitBranchRange());
        assertEquals(VALID_FLAG_Y, output.getValidFlag());
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertEquals(TranBranch.VALUE_351155, output.getBranch());
        assertEquals(TranBranch.VALUE_352001, output.getAttachedTo());
    }

    // ST096-TC005（REQ-004-S01、REQ-004-S03）直接上级与总行均有启用记录：层级从大到小
    // 先查直接上级且命中即中断，输出取层级"1"记录，总行记录不进入输出
    @Test
    public void testST096T05() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.emptyList());
        stubLimitConf(TranBranch.VALUE_352001, Collections.singletonList(
                limitConf(TranBranch.VALUE_352001, "LS002", ResBranchRange.C)));
        // 总行侧有意设桩命中记录：若未在直接上级处中断，输出将变为 LS003，中断缺陷可经输出暴露
        stubLimitConf(TranBranch.VALUE_351001, Collections.singletonList(
                limitConf(TranBranch.VALUE_351001, "LS003", ResBranchRange.D)));
        stubStandardBranchChain();

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertEquals("LS002", output.getLimitSceneNo());
        assertEquals(TranBranch.VALUE_352001, output.getLimitBranchId());
        assertEquals(ResBranchRange.C, output.getLimitBranchRange());
        assertEquals(VALID_FLAG_Y, output.getValidFlag());
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertEquals(TranBranch.VALUE_351155, output.getBranch());
        assertEquals(TranBranch.VALUE_352001, output.getAttachedTo());
    }

    // ST096-TC006（REQ-003-S03）上级编号（VALUE_351156，未落库）在机构信息表无记录：
    // 收集直接上级后停止、保留已收集条目；仅存的上级未命中，配置组为空，
    // attachedTo 仍取开立行自身记录的归属上级机构号
    @Test
    public void testST096T06() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.emptyList());
        stubLimitConf(TranBranch.VALUE_352001, Collections.emptyList());
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_351155))
                .thenReturn(branch(TranBranch.VALUE_351155, HierarchyCode.VALUE_2, TranBranch.VALUE_352001));
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_352001))
                .thenReturn(branch(TranBranch.VALUE_352001, HierarchyCode.VALUE_1, TranBranch.VALUE_351156));
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_351156))
                .thenReturn(null);

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertNoLimitConf(output);
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertEquals(TranBranch.VALUE_351155, output.getBranch());
        assertEquals(TranBranch.VALUE_352001, output.getAttachedTo());
    }

    // ST096-TC007（REQ-003-S04）账户开立行自身在机构信息表无记录：上级集合为空、
    // 机构组输出为空、配置组为空
    @Test
    public void testST096T07() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.emptyList());
        lenient().when(fmBranchBcc.findByBranch(TranBranch.VALUE_351155))
                .thenReturn(null);

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertNoLimitConf(output);
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertNull(output.getBranch());
        assertNull(output.getAttachedTo());
    }

    // ST096-TC008（REQ-004-S04）上级机构集合非空（两级）但均无启用记录：
    // 依层级从大到小全部查询后遍历结束，配置组输出为空，机构组取开立行记录
    @Test
    public void testST096T08() {
        stubBusAcct();
        stubLimitConf(TranBranch.VALUE_351155, Collections.emptyList());
        stubLimitConf(TranBranch.VALUE_352001, Collections.emptyList());
        stubLimitConf(TranBranch.VALUE_351001, Collections.emptyList());
        stubStandardBranchChain();

        ST096OutputBO output = st096.execute(input());

        assertSuccess(output);
        assertNoLimitConf(output);
        assertEquals(BASE_ACCT_NO, output.getBaseAcctNo());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertEquals(TranBranch.VALUE_351155, output.getBranch());
        assertEquals(TranBranch.VALUE_352001, output.getAttachedTo());
    }
}
