package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.facade.bo.ST082InputBO;
import com.dcits.depsit.facade.bo.ST082OutputBO;
import com.dcits.depsit.facade.components.IRbBusTranJnlBcc;
import com.dcits.depsit.facade.eo.RbBusTranJnlEO;

/**
 * ST082 登记交易流水 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST082-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST082（inputs 绑定的 ST082.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；channelSeqNo 取值来源
 * 需求未定义，所有用例不断言该字段。
 */
@ExtendWith(MockitoExtension.class)
public class ST082PbcTest {

    @Mock
    private IRbBusTranJnlBcc rbBusTranJnlBcc;

    @InjectMocks
    private ST082Pbc st082;

    /** 构造输入：交易类型、币种、借贷标志、交易金额（金额按字符串原精度构造） */
    private ST082InputBO input(OthTranType tranType, Ccy ccy, CrDrInd crDrInd, String tranAmt) {
        ST082InputBO input = new ST082InputBO();
        input.setTranType(tranType);
        input.setCcy(ccy);
        input.setCrDrInd(crDrInd);
        input.setTranAmt(new BigDecimal(tranAmt));
        return input;
    }

    // 匹配器 lambda 需 null 防护（同 ST004PbcTest 约定）：argThat 以占位 null 真实调用 mock，
    // 不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按四个登记字段逐字段核对登记记录（身份映射，金额用 BigDecimal equals 含精度）并返回 1（新增一条） */
    private void stubCreateSelective(OthTranType tranType, Ccy ccy, CrDrInd crDrInd, String tranAmt) {
        BigDecimal expectAmt = new BigDecimal(tranAmt);
        lenient().when(rbBusTranJnlBcc.createSelective(argThat(eo ->
                eo != null && eo.getTranType() == tranType && eo.getCcy() == ccy
                        && eo.getCrDrInd() == crDrInd && expectAmt.equals(eo.getTranAmt()))))
                .thenReturn(1);
    }

    // REQ-001-S01 贷记人民币现金存入登记：1000/CNY/C/1000.00 新增一条，四个登记字段与对应输入完全相同
    @Test
    public void testST082T01() {
        stubCreateSelective(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "1000.00");

        ST082OutputBO output = st082.execute(input(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "1000.00"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(OthTranType.VALUE_1000, output.getTranType());
        assertEquals(Ccy.CNY, output.getCcy());
        assertEquals(CrDrInd.C, output.getCrDrInd());
        assertEquals(new BigDecimal("1000.00"), output.getTranAmt());
    }

    // REQ-001-S02 借记外币现金支取登记：1003/USD/D/500.50 新增一条，覆盖借贷标志另一取值、另一币种与另一交易类型，映射同为身份映射
    @Test
    public void testST082T02() {
        stubCreateSelective(OthTranType.VALUE_1003, Ccy.USD, CrDrInd.D, "500.50");

        ST082OutputBO output = st082.execute(input(OthTranType.VALUE_1003, Ccy.USD, CrDrInd.D, "500.50"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(OthTranType.VALUE_1003, output.getTranType());
        assertEquals(Ccy.USD, output.getCcy());
        assertEquals(CrDrInd.D, output.getCrDrInd());
        assertEquals(new BigDecimal("500.50"), output.getTranAmt());
    }

    // REQ-001-S03 交易金额含分位小数原样登记：12345.67 数值与精度完全一致，无舍入、无变换
    @Test
    public void testST082T03() {
        stubCreateSelective(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "12345.67");

        ST082OutputBO output = st082.execute(input(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "12345.67"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal("12345.67"), output.getTranAmt());
        assertEquals(OthTranType.VALUE_1000, output.getTranType());
        assertEquals(Ccy.CNY, output.getCcy());
        assertEquals(CrDrInd.C, output.getCrDrInd());
    }

    // REQ-002-S01 登记成功后输出与登记记录一致：桩记录收到的登记 EO 并返回 1，输出四字段回自该登记记录（即与本次输入一致）
    @Test
    public void testST082T04() {
        AtomicReference<RbBusTranJnlEO> registered = new AtomicReference<>();
        lenient().when(rbBusTranJnlBcc.createSelective(any(RbBusTranJnlEO.class)))
                .thenAnswer(invocation -> {
                    registered.set(invocation.getArgument(0));
                    return 1;
                });

        ST082OutputBO output = st082.execute(input(OthTranType.VALUE_1000, Ccy.CNY, CrDrInd.C, "1000.00"));

        assertNotNull(registered.get());
        assertEquals(OthTranType.VALUE_1000, registered.get().getTranType());
        assertEquals(Ccy.CNY, registered.get().getCcy());
        assertEquals(CrDrInd.C, registered.get().getCrDrInd());
        assertEquals(new BigDecimal("1000.00"), registered.get().getTranAmt());
        assertEquals(registered.get().getTranType(), output.getTranType());
        assertEquals(registered.get().getCcy(), output.getCcy());
        assertEquals(registered.get().getCrDrInd(), output.getCrDrInd());
        assertEquals(registered.get().getTranAmt(), output.getTranAmt());
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }
}
