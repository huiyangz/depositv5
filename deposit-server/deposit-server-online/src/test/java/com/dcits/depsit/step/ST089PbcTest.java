package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.enums.DocType;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST089InputBO;
import com.dcits.depsit.facade.bo.ST089OutputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;
import com.dcits.depsit.facade.components.ITbVoucherDefBcc;
import com.dcits.depsit.facade.eo.TbVoucherDefEO;

/**
 * ST089 检查存入账户黑名单 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST089-TC001 ~ TC004），
 * 预期结果来自正式 Spec docs/specs/ST089.md。
 * 实际触达依赖仅 ITbVoucherDefBcc（子步骤1 凭证种类查询）与组件内步骤 IST105
 * （子步骤3 调用目标，仅桩接口、不桩其内部 BCC）；IST105 桩以 argThat 核对
 * 8 项传入字段值与 7 项不传入字段为 null，失配时桩返回 null 使用例失败，
 * 以此数据流方式验证参数映射，不使用 verify/次数断言；不访问真实数据库、网络或组件。
 */
@ExtendWith(MockitoExtension.class)
public class ST089PbcTest {

    @Mock
    private ITbVoucherDefBcc tbVoucherDefBcc;

    @Mock
    private IST105 st105;

    @InjectMocks
    private ST089Pbc st089Pbc;

    /** 构造步骤输入（Spec 基准示例数据，9 字段全赋值） */
    private static ST089InputBO baseInput() {
        ST089InputBO input = new ST089InputBO();
        input.setBaseAcctNo("1002003004005006");
        input.setTranBranch(TranBranch.VALUE_351155);
        input.setChannelNo("9001");
        input.setDocType(DocType.VALUE_781);
        input.setTranType(OthTranType.VALUE_38);
        input.setServiceCode("SV0001");
        input.setMessageCode("MC0001");
        input.setMessageType("MT0001");
        input.setSourceType(SourceType.AC);
        return input;
    }

    /** 设桩：子步骤1 按{凭证类型}查询【凭证信息】命中，凭证种类为 CHK（支票） */
    private void stubVoucherFound() {
        TbVoucherDefEO voucherDef = new TbVoucherDefEO();
        voucherDef.setDocType(DocType.VALUE_781);
        voucherDef.setDocClass(DocClass.CHK);
        lenient().when(tbVoucherDefBcc.findByDocType(DocType.VALUE_781)).thenReturn(voucherDef);
    }

    // 匹配器 lambda 需 null 防护：argThat 注册桩时以占位 null 真实调用 mock，不防护会 NPE；
    // 生产调用恒为构造好的 ST105InputBO，断言语义不变
    /** 设桩：子步骤3 调用 IST105，核对 8 项映射字段值与 7 项不传入字段均为 null，返回给定 dealFlow */
    private void stubSt105Return(DealFlow dealFlow) {
        ST105OutputBO st105Result = new ST105OutputBO();
        st105Result.setSucceed(true);
        st105Result.setDealFlow(dealFlow);
        lenient().when(st105.execute(argThat(in -> in != null
                && "1002003004005006".equals(in.getBaseAcctNo())
                && in.getTranType() == OthTranType.VALUE_38
                && "CRET".equals(in.getEventType())
                && "SV0001".equals(in.getServiceCode())
                && "MC0001".equals(in.getMessageCode())
                && "MT0001".equals(in.getMessageType())
                && in.getSourceType() == SourceType.AC
                && in.getDocClass() == DocClass.CHK
                && in.getAcctBranch() == null
                && in.getProgramId() == null
                && in.getBlacklistCheckFlag() == null
                && in.getServiceStatus() == null
                && in.getClientNo() == null
                && in.getDocumentId() == null
                && in.getDocumentType() == null))).thenReturn(st105Result);
    }

    // REQ-001-S01/REQ-002-S01/REQ-003-S01/REQ-004-S01 凭证种类查询命中、事件类型 "CRET"、ST105 八项参数映射正确且七项不传入字段为空；dealFlow=null（通过）→ 继续执行正常完成
    @Test
    public void testST089T01() {
        stubVoucherFound();
        stubSt105Return(null);

        ST089OutputBO output = st089Pbc.execute(baseInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("CRET", output.getEventType());
        assertNull(output.getDealFlow());
        assertNull(output.getAuthFlag());
        assertEquals(DocClass.CHK, output.getDocClass());
    }

    // REQ-004-S02 dealFlow=DealFlow.D（提醒）→ 继续执行正常完成，输出 dealFlow=D，不返回授权标志与错误码
    @Test
    public void testST089T02() {
        stubVoucherFound();
        stubSt105Return(DealFlow.D);

        ST089OutputBO output = st089Pbc.execute(baseInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("CRET", output.getEventType());
        assertEquals(DealFlow.D, output.getDealFlow());
        assertNull(output.getAuthFlag());
        assertEquals(DocClass.CHK, output.getDocClass());
    }

    // REQ-004-S03 dealFlow=DealFlow.A（授权）→ 返回授权标志 authFlag="是" 并等待授权结果，不返回错误码
    @Test
    public void testST089T03() {
        stubVoucherFound();
        stubSt105Return(DealFlow.A);

        ST089OutputBO output = st089Pbc.execute(baseInput());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("CRET", output.getEventType());
        assertEquals(DealFlow.A, output.getDealFlow());
        assertEquals("是", output.getAuthFlag());
        assertEquals(DocClass.CHK, output.getDocClass());
    }

    // REQ-004-S04 dealFlow=DealFlow.B（拒绝）→ 返回错误码 "ER0057" 非成功返回（ST105 成功标志仍为 true，佐证以业务结果 dealFlow 判定）；eventType/docClass 仍按子步骤 1、2 无条件执行结果输出
    @Test
    public void testST089T04() {
        stubVoucherFound();
        stubSt105Return(DealFlow.B);

        ST089OutputBO output = st089Pbc.execute(baseInput());

        assertFalse(output.isSucceed());
        assertEquals("ER0057", output.getErrorCode());
        assertEquals("CRET", output.getEventType());
        assertEquals(DealFlow.B, output.getDealFlow());
        assertNull(output.getAuthFlag());
        assertEquals(DocClass.CHK, output.getDocClass());
    }
}
