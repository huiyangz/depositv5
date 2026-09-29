package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.facade.bo.ST040InputBO;
import com.dcits.depsit.facade.bo.ST040OutputBO;
import com.dcits.depsit.facade.components.IRbTranDefBcc;
import com.dcits.depsit.facade.eo.RbTranDefEO;

/**
 * ST040 检查现金支取交易权限 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST040-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST040（fileInputs.spec 绑定的 ST040.md）。
 * 唯一失败分支为命中现金支取判定（ER0070）；"通过"（含查无记录）均为成功返回。
 */
@ExtendWith(MockitoExtension.class)
public class ST040PbcTest {

    @Mock
    private IRbTranDefBcc rbTranDefBcc;

    @InjectMocks
    private ST040Pbc st040;

    /** 构造交易类型定义记录：三个标志取值由各用例场景给定 */
    private RbTranDefEO tranDef(CrDrInd crDrInd, String cashTranFlag, String reversal) {
        RbTranDefEO eo = new RbTranDefEO();
        eo.setCrDrInd(crDrInd);
        eo.setCashTranFlag(cashTranFlag);
        eo.setReversal(reversal);
        return eo;
    }

    // REQ-001-S01 输出映射 + REQ-002-S01 借方且现金交易且非冲正——命中判定：succeed=false、errorCode="ER0070"，三标志照常输出
    @Test
    public void testST040T01() {
        lenient().when(rbTranDefBcc.findByTranType(OthTranType.VALUE_1003))
                .thenReturn(tranDef(CrDrInd.D, "Y", "N"));

        ST040InputBO input = new ST040InputBO();
        input.setTranType(OthTranType.VALUE_1003);

        ST040OutputBO output = st040.execute(input);

        assertFalse(output.isSucceed());
        assertEquals("ER0070", output.getErrorCode());
        // errorMessage 按“错误码::业务说明”格式拼装，非空且带 ER0070 前缀，不做全文等值断言
        assertNotNull(output.getErrorMessage());
        assertTrue(output.getErrorMessage().startsWith("ER0070::"));
        assertEquals(CrDrInd.D, output.getCrDrInd());
        assertEquals("Y", output.getCashTranFlag());
        assertEquals("N", output.getReversal());
    }

    // REQ-002-S02 借贷标志为贷方——不命中，检查通过：succeed=true、错误字段为 null，输出 C/"Y"/"N"
    @Test
    public void testST040T02() {
        lenient().when(rbTranDefBcc.findByTranType(OthTranType.VALUE_1000))
                .thenReturn(tranDef(CrDrInd.C, "Y", "N"));

        ST040InputBO input = new ST040InputBO();
        input.setTranType(OthTranType.VALUE_1000);

        ST040OutputBO output = st040.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(CrDrInd.C, output.getCrDrInd());
        assertEquals("Y", output.getCashTranFlag());
        assertEquals("N", output.getReversal());
    }

    // REQ-002-S03 非现金交易——不命中，检查通过：succeed=true、错误字段为 null，输出 D/"N"/"N"
    @Test
    public void testST040T03() {
        lenient().when(rbTranDefBcc.findByTranType(OthTranType.VALUE_4186))
                .thenReturn(tranDef(CrDrInd.D, "N", "N"));

        ST040InputBO input = new ST040InputBO();
        input.setTranType(OthTranType.VALUE_4186);

        ST040OutputBO output = st040.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(CrDrInd.D, output.getCrDrInd());
        assertEquals("N", output.getCashTranFlag());
        assertEquals("N", output.getReversal());
    }

    // REQ-002-S04 冲正交易——不命中，检查通过：succeed=true、错误字段为 null，输出 D/"Y"/"Y"
    @Test
    public void testST040T04() {
        lenient().when(rbTranDefBcc.findByTranType(OthTranType.VALUE_1004))
                .thenReturn(tranDef(CrDrInd.D, "Y", "Y"));

        ST040InputBO input = new ST040InputBO();
        input.setTranType(OthTranType.VALUE_1004);

        ST040OutputBO output = st040.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(CrDrInd.D, output.getCrDrInd());
        assertEquals("Y", output.getCashTranFlag());
        assertEquals("Y", output.getReversal());
    }

    // REQ-002-S05 查无交易类型定义——三输出为 null，判定走"否则"：succeed=true、错误字段为 null
    @Test
    public void testST040T05() {
        lenient().when(rbTranDefBcc.findByTranType(OthTranType.VALUE_1006))
                .thenReturn(null);

        ST040InputBO input = new ST040InputBO();
        input.setTranType(OthTranType.VALUE_1006);

        ST040OutputBO output = st040.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getCrDrInd());
        assertNull(output.getCashTranFlag());
        assertNull(output.getReversal());
    }
}
