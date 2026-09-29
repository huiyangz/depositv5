package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.AllDepInd;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST018InputBO;
import com.dcits.depsit.facade.bo.ST018OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST018 检查交易机构 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST018-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST018（fileInputs.spec 绑定的 ST018.md）。
 * 本步骤无业务失败场景，一致与跳转两分支均为 succeed=true（跳转非业务失败）；
 * 失败仅由技术异常传播表达（TC004）。
 */
@ExtendWith(MockitoExtension.class)
public class ST018PbcTest {

    /** 示例账号一（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    /** 示例账号二（TC003 区分值） */
    private static final String BASE_ACCT_NO_ALT = "1002003004005007";

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @InjectMocks
    private ST018Pbc st018;

    /** 构造输入：账号与交易机构 */
    private ST018InputBO input(String baseAcctNo, TranBranch tranBranch) {
        ST018InputBO input = new ST018InputBO();
        input.setBaseAcctNo(baseAcctNo);
        input.setTranBranch(tranBranch);
        return input;
    }

    /** 构造账户信息记录：账号、账户开立行行号、通存标志 */
    private RbBusAcctEO account(String baseAcctNo, TranBranch acctBranch, AllDepInd allDepInd) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setAcctBranch(acctBranch);
        eo.setAllDepInd(allDepInd);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：argThat 可能以占位 null 真实调用 mock，不防护会 NPE；
    // 生产调用恒为构造好的 EO，断言语义不变（参照 ST004PbcTest 先例）
    /** 步骤1 桩：按 baseAcctNo 等值条件核对请求并返回账户记录 */
    private void stubAccount(String baseAcctNo, RbBusAcctEO record) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && baseAcctNo.equals(eo.getBaseAcctNo()))))
                .thenReturn(Collections.singletonList(record));
    }

    // REQ-002-S01 交易机构与账户开立行行号一致（同为 351155）：返回检查结果"通过"，不跳转，acctBranch=查询值
    // 桩额外核对查询 EO 仅以 baseAcctNo 为等值条件（acctBranch、allDepInd 均未作为查询条件）
    @Test
    public void testST018T01() {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo())
                        && eo.getAcctBranch() == null && eo.getAllDepInd() == null)))
                .thenReturn(Collections.singletonList(
                        account(BASE_ACCT_NO, TranBranch.VALUE_351155, AllDepInd.N001)));

        ST018OutputBO output = st018.execute(input(BASE_ACCT_NO, TranBranch.VALUE_351155));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(TranBranch.VALUE_351155, output.getAcctBranch());
        assertNull(output.getGotoStepName());
    }

    // REQ-001-S01 + REQ-002-S02 交易机构 351155 与开立行 351156 不一致：跳转《检查存入账户通存标志》，
    // 跳转非业务失败，acctBranch 输出取查询值不受分支影响；通存标志不进入输出（OutputBO 无该字段）
    @Test
    public void testST018T02() {
        stubAccount(BASE_ACCT_NO, account(BASE_ACCT_NO, TranBranch.VALUE_351156, AllDepInd.N001));

        ST018OutputBO output = st018.execute(input(BASE_ACCT_NO, TranBranch.VALUE_351155));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(TranBranch.VALUE_351156, output.getAcctBranch());
        assertEquals(ST018Pbc.GOTO_STEP_CHECK_ALL_DEP_IND, output.getGotoStepName());
    }

    // REQ-001 $通存标志$ 仅随步骤1读取、不判定、不进输出：allDepInd=N004（对照 TC001 的 N001）、账号与机构取区分值，
    // 交易机构与开立行一致（同为 351157），结论仍"通过"，输出不受通存标志取值影响
    @Test
    public void testST018T03() {
        stubAccount(BASE_ACCT_NO_ALT, account(BASE_ACCT_NO_ALT, TranBranch.VALUE_351157, AllDepInd.N004));

        ST018OutputBO output = st018.execute(input(BASE_ACCT_NO_ALT, TranBranch.VALUE_351157));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(TranBranch.VALUE_351157, output.getAcctBranch());
        assertNull(output.getGotoStepName());
    }

    // REQ-003-S01 查询【账户信息】底层抛技术异常（数据源不可用类）：异常原样向上传播（同一实例），
    // 不捕获、不转换、不生成业务失败应答、不返回检查结果"通过"
    @Test
    public void testST018T04() {
        RuntimeException dbFailure = new RuntimeException("模拟数据源不可用技术异常");
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()))))
                .thenThrow(dbFailure);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> st018.execute(input(BASE_ACCT_NO, TranBranch.VALUE_351155)));
        assertSame(dbFailure, ex);
    }
}
