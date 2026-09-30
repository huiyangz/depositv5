package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.facade.bo.ST083InputBO;
import com.dcits.depsit.facade.bo.ST083OutputBO;
import com.dcits.depsit.facade.components.IRbBusTranJnlBcc;
import com.dcits.depsit.facade.eo.RbBusTranJnlEO;

/**
 * ST083 登记现金交易明细 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST083-TC001 ~ TC003），
 * 预期结果来自正式 Spec ST083（fileInputs.spec 绑定的 ST083.md）。
 * 本步骤无业务失败场景，正常路径均 succeed=true；失败语义仅由技术异常原样传播（TC003）表达。
 */
@ExtendWith(MockitoExtension.class)
public class ST083PbcTest {

    /** 固定技术异常实例：Spec 对"数据访问异常"仅标注为示例，不引入未核实的具体数据访问异常类型 */
    private static final RuntimeException TECH_FAILURE =
            new RuntimeException("创建 RB_BUS_TRAN_JNL 记录数据访问失败（示例技术异常）");

    @Mock
    private IRbBusTranJnlBcc rbBusTranJnlBcc;

    @InjectMocks
    private ST083Pbc st083;

    /** 构造输入：交易类型、币种、借贷标志、交易金额（均为必填，取值来自 Spec 场景示例） */
    private ST083InputBO input(OthTranType tranType, Ccy ccy, CrDrInd crDrInd, String tranAmt) {
        ST083InputBO input = new ST083InputBO();
        input.setTranType(tranType);
        input.setCcy(ccy);
        input.setCrDrInd(crDrInd);
        input.setTranAmt(new BigDecimal(tranAmt));
        return input;
    }

    /** 成功路径公共断言：succeed=true、错误字段 null、输出 4 字段与所创建记录（传入 createSelective 的 EO）一致 */
    private void assertRegistered(ST083OutputBO output, List<RbBusTranJnlEO> captured,
                                  OthTranType tranType, Ccy ccy, CrDrInd crDrInd, String tranAmt) {
        BigDecimal expectedAmt = new BigDecimal(tranAmt);
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(tranType, output.getTranType());
        assertEquals(ccy, output.getCcy());
        assertEquals(crDrInd, output.getCrDrInd());
        assertEquals(0, output.getTranAmt().compareTo(expectedAmt));
        // 恰好一次创建调用（captured 恰有 1 个 EO），记录 4 个绑定字段与输入原值相等
        assertEquals(1, captured.size());
        assertEquals(tranType, captured.get(0).getTranType());
        assertEquals(ccy, captured.get(0).getCcy());
        assertEquals(crDrInd, captured.get(0).getCrDrInd());
        assertEquals(0, captured.get(0).getTranAmt().compareTo(expectedAmt));
    }

    // 匹配器 lambda 需 null 防护：argThat 注册桩时会以占位 null 真实调用 mock，不防护会 NPE；
    // 匹配器仅核对 Spec 绑定的 4 个字段，EO 其余字段（含 7 个创建契约必填字段）按已接受结论不做断言。
    // "恰好一次创建调用"与"原值写入"以桩内记录的 EO 列表断言表达（不使用 verify/times）。

    // REQ-001-S01 现金存入登记（贷方，CNY）：恰好一次创建调用，4 字段按输入原值写入，输出与所创建记录一致
    @Test
    public void testST083T01() {
        List<RbBusTranJnlEO> captured = new ArrayList<>();
        lenient().when(rbBusTranJnlBcc.createSelective(argThat(eo -> eo != null
                && eo.getTranType() == OthTranType.VALUE_1000
                && eo.getCcy() == Ccy.CNY
                && eo.getCrDrInd() == CrDrInd.C
                && eo.getTranAmt() != null
                && eo.getTranAmt().compareTo(new BigDecimal("1000.00")) == 0)))
                .thenAnswer(inv -> {
                    captured.add(inv.getArgument(0));
                    return 1;
                });

        ST083OutputBO output = st083.execute(input(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "1000.00"));

        assertRegistered(output, captured, OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "1000.00");
    }

    // REQ-001-S02 现金支取登记（借方、USD）：映射规则与借贷方向、币种、交易类型取值无关，同 T01 结构
    @Test
    public void testST083T02() {
        List<RbBusTranJnlEO> captured = new ArrayList<>();
        lenient().when(rbBusTranJnlBcc.createSelective(argThat(eo -> eo != null
                && eo.getTranType() == OthTranType.VALUE_1003
                && eo.getCcy() == Ccy.USD
                && eo.getCrDrInd() == CrDrInd.D
                && eo.getTranAmt() != null
                && eo.getTranAmt().compareTo(new BigDecimal("500.50")) == 0)))
                .thenAnswer(inv -> {
                    captured.add(inv.getArgument(0));
                    return 1;
                });

        ST083OutputBO output = st083.execute(input(OthTranType.VALUE_1003, Ccy.USD, CrDrInd.D, "500.50"));

        assertRegistered(output, captured, OthTranType.VALUE_1003, Ccy.USD, CrDrInd.D, "500.50");
    }

    // REQ-002-S01 创建调用技术异常向上传播：同一异常实例原样抛出（不包装不吞没），仅发起 1 次调用（不重试、无补偿）
    @Test
    public void testST083T03() {
        AtomicInteger invocations = new AtomicInteger();
        lenient().when(rbBusTranJnlBcc.createSelective(argThat(eo -> eo != null
                && eo.getTranType() == OthTranType.VALUE_1000
                && eo.getCcy() == Ccy.CNY
                && eo.getCrDrInd() == CrDrInd.C
                && eo.getTranAmt() != null
                && eo.getTranAmt().compareTo(new BigDecimal("1000.00")) == 0)))
                .thenAnswer(inv -> {
                    invocations.incrementAndGet();
                    throw TECH_FAILURE;
                });

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> st083.execute(input(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "1000.00")));

        assertSame(TECH_FAILURE, thrown);
        assertEquals(1, invocations.get());
    }
}
