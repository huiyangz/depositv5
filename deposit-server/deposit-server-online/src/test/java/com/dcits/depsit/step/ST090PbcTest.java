package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.facade.bo.ST090InputBO;
import com.dcits.depsit.facade.bo.ST090OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBalanceBcc;
import com.dcits.depsit.facade.eo.RbBusAcctBalanceEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST090 更新存入后账户余额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST090-TC001 ~ TC003），
 * 预期结果来自正式 Spec ST090（inputs 绑定的 ST090.md）。
 * 本步骤为线性三子步骤流程，无业务失败场景，全部用例 succeed=true；
 * 技术异常传播为结构性约束（REQ-004-S02），按用例设计不设技术异常用例。
 */
@ExtendWith(MockitoExtension.class)
public class ST090PbcTest {

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @Mock
    private IRbBusAcctBalanceBcc rbBusAcctBalanceBcc;

    @InjectMocks
    private ST090Pbc st090;

    /** 构造公共输入：账号与交易金额（金额以字符串精确构造） */
    private ST090InputBO input(String baseAcctNo, String tranAmt) {
        ST090InputBO input = new ST090InputBO();
        input.setBaseAcctNo(baseAcctNo);
        input.setTranAmt(new BigDecimal(tranAmt));
        return input;
    }

    // 匹配器 lambda 带 null 防护：argThat 匹配以占位 null 真实调用 mock 时不产生 NPE；
    // 生产调用恒为构造好的 EO，断言语义不变（惯例同 ST004PbcTest）

    /** 子步骤1 桩：按账号等值条件核对请求，返回含账户内部键值的主表记录（单条） */
    private void stubAcct(String baseAcctNo, int internalKey) {
        RbBusAcctEO acct = new RbBusAcctEO();
        acct.setBaseAcctNo(baseAcctNo);
        acct.setInternalKey(internalKey);
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && baseAcctNo.equals(eo.getBaseAcctNo()))))
                .thenReturn(Collections.singletonList(acct));
    }

    /** 子步骤2 桩：按账户内部键值（主键）返回余额基值记录 */
    private void stubBalance(int internalKey, String totalAmount, String acctAvailBal) {
        RbBusAcctBalanceEO balance = new RbBusAcctBalanceEO();
        balance.setInternalKey(internalKey);
        balance.setTotalAmount(new BigDecimal(totalAmount));
        balance.setAcctAvailBal(new BigDecimal(acctAvailBal));
        lenient().when(rbBusAcctBalanceBcc.findByPrimaryKey(internalKey)).thenReturn(balance);
    }

    /** 子步骤3 桩：核对更新 EO 的主键与两个新余额字段（BigDecimal 数值相等）后返回影响 1 行 */
    private void stubModify(int internalKey, String totalAmount, String acctAvailBal) {
        lenient().when(rbBusAcctBalanceBcc.modifyByPrimaryKeySelective(argThat(eo -> eo != null
                && Integer.valueOf(internalKey).equals(eo.getInternalKey())
                && new BigDecimal(totalAmount).compareTo(eo.getTotalAmount()) == 0
                && new BigDecimal(acctAvailBal).compareTo(eo.getAcctAvailBal()) == 0)))
                .thenReturn(1);
    }

    /** 成功路径公共断言：succeed=true、错误字段 null、两输出为更新后数值 */
    private void assertSuccess(ST090OutputBO output, String totalAmount, String acctAvailBal) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal(totalAmount), output.getTotalAmount());
        assertEquals(new BigDecimal(acctAvailBal), output.getAcctAvailBal());
    }

    // ST090-TC001 正常存入全链路（Spec 示例值，REQ-001-S01→REQ-002-S01→REQ-003-S01→REQ-004-S01）：
    // 账号 6100230010001 查得内部键值 20260001，基值 1000.50/800.25 加计 200.25 同步更新，输出 1200.75/1000.50
    @Test
    public void testST090T01() {
        stubAcct("6100230010001", 20260001);
        stubBalance(20260001, "1000.50", "800.25");
        stubModify(20260001, "1200.75", "1000.50");

        ST090OutputBO output = st090.execute(input("6100230010001", "200.25"));

        assertSuccess(output, "1200.75", "1000.50");
    }

    // ST090-TC002 可区分数据复核数据流（同一成功路径复测）：账号 6100230010002 查得内部键值 20260002，
    // 基值 500.00/300.00 加计 150.00，验证 baseAcctNo→内部键值→余额记录→两输出映射及字段不交叉错配
    @Test
    public void testST090T02() {
        stubAcct("6100230010002", 20260002);
        stubBalance(20260002, "500.00", "300.00");
        stubModify(20260002, "650.00", "450.00");

        ST090OutputBO output = st090.execute(input("6100230010002", "150.00"));

        assertSuccess(output, "650.00", "450.00");
    }

    // ST090-TC003 零金额公式边界（REQ-003-S02）：tranAmt=0.00 加计后两余额字段数值不变，
    // 仍执行同一同步更新（更新 EO 携带原值数值）并输出原基值
    @Test
    public void testST090T03() {
        stubAcct("6100230010001", 20260001);
        stubBalance(20260001, "1000.50", "800.25");
        stubModify(20260001, "1000.50", "800.25");

        ST090OutputBO output = st090.execute(input("6100230010001", "0.00"));

        assertSuccess(output, "1000.50", "800.25");
    }
}
