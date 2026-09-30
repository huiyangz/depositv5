package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST107InputBO;
import com.dcits.depsit.facade.bo.ST107OutputBO;
import com.dcits.depsit.facade.components.IFmBranchBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.FmBranchEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST107 检查是否跨法人 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST107-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST107（inputs 绑定的 ST107.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"不通过"是业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST107PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @Mock
    private IFmBranchBcc fmBranchBcc;

    @InjectMocks
    private ST107Pbc st107;

    /** 构造公共输入：账号 + 输入归属机构号 */
    private ST107InputBO input(TranBranch branch) {
        ST107InputBO input = new ST107InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        input.setBranch(branch);
        return input;
    }

    /** 步骤1 桩：按账号组合条件核对请求并返回单条账户记录；acctBranch 非空时作为背景干扰字段 */
    private void stubAcct(TranBranch homeBranch, TranBranch acctBranch) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setHomeBranch(homeBranch);
        if (acctBranch != null) {
            eo.setAcctBranch(acctBranch);
        }
        // 匹配器 lambda 带 null 防护：argThat 注册时会以占位 null 真实调用 mock，不防护会 NPE
        lenient().when(rbBusAcctBcc.findByEo(argThat(e ->
                e != null && BASE_ACCT_NO.equals(e.getBaseAcctNo()))))
                .thenReturn(Collections.singletonList(eo));
    }

    /** 机构信息桩：按归属机构号（主键）返回记录，法人取 company */
    private void stubBranch(TranBranch branch, String company) {
        FmBranchEO eo = new FmBranchEO();
        eo.setBranch(branch);
        eo.setCompany(company);
        lenient().when(fmBranchBcc.findByBranch(branch)).thenReturn(eo);
    }

    /** 公共断言：succeed=true、错误字段 null，checkResult 等于期望值 */
    private void assertResult(ST107OutputBO output, String expectedCheckResult) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(expectedCheckResult, output.getCheckResult());
    }

    // REQ-003-S01 账户法人"LEGAL01"（经管理机构号351155取得）与交易机构法人"LEGAL02"（输入351888）不一致：checkResult="不通过"
    @Test
    public void testST107T01() {
        stubAcct(TranBranch.VALUE_351155, null);
        stubBranch(TranBranch.VALUE_351155, "LEGAL01");
        stubBranch(TranBranch.VALUE_351888, "LEGAL02");

        ST107OutputBO output = st107.execute(input(TranBranch.VALUE_351888));

        assertResult(output, "不通过");
    }

    // REQ-003-S02 机构不同（351155/351156）但两机构法人均为"LEGAL01"：checkResult="通过"
    @Test
    public void testST107T02() {
        stubAcct(TranBranch.VALUE_351155, null);
        stubBranch(TranBranch.VALUE_351155, "LEGAL01");
        stubBranch(TranBranch.VALUE_351156, "LEGAL01");

        ST107OutputBO output = st107.execute(input(TranBranch.VALUE_351156));

        assertResult(output, "通过");
    }

    // REQ-003-S03 账户管理机构号与输入归属机构号同为351155：步骤1、步骤2 两次机构查询命中同一桩记录，法人均为"LEGAL01"：checkResult="通过"
    @Test
    public void testST107T03() {
        stubAcct(TranBranch.VALUE_351155, null);
        // 仅设一个机构桩，同一桩同时服务步骤1、步骤2 两次查询（不 stub 其他机构号）
        stubBranch(TranBranch.VALUE_351155, "LEGAL01");

        ST107OutputBO output = st107.execute(input(TranBranch.VALUE_351155));

        assertResult(output, "通过");
    }

    // REQ-001-S01 账户法人取管理机构号链路：homeBranch=351155→"LEGAL01"，acctBranch=351156 作背景干扰；输入351156→"LEGAL02"，两者不一致：checkResult="不通过"（误用 acctBranch/输入机构号取账户法人则两法人同为"LEGAL02"会得"通过"，本例可区分）
    @Test
    public void testST107T04() {
        stubAcct(TranBranch.VALUE_351155, TranBranch.VALUE_351156);
        stubBranch(TranBranch.VALUE_351155, "LEGAL01");
        stubBranch(TranBranch.VALUE_351156, "LEGAL02");

        ST107OutputBO output = st107.execute(input(TranBranch.VALUE_351156));

        assertResult(output, "不通过");
    }

    // REQ-002-S01 交易机构法人取输入机构号链路：账户经 homeBranch=351156→"LEGAL01"，输入351888→"LEGAL02"，两者不一致：checkResult="不通过"（步骤2 误用账户归属机构号则两法人同为"LEGAL01"会得"通过"，本例可区分）
    @Test
    public void testST107T05() {
        stubAcct(TranBranch.VALUE_351156, null);
        stubBranch(TranBranch.VALUE_351156, "LEGAL01");
        stubBranch(TranBranch.VALUE_351888, "LEGAL02");

        ST107OutputBO output = st107.execute(input(TranBranch.VALUE_351888));

        assertResult(output, "不通过");
    }
}
