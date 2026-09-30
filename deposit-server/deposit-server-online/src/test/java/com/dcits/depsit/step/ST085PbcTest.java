package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.enums.DocType;
import com.dcits.depsit.enums.IssCountry;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.ThawDocumentType2;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST105InputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;
import com.dcits.depsit.facade.bo.ST085InputBO;
import com.dcits.depsit.facade.bo.ST085OutputBO;
import com.dcits.depsit.facade.components.IFmClientCopyBcc;
import com.dcits.depsit.facade.components.ITbVoucherDefBcc;
import com.dcits.depsit.facade.eo.FmClientCopyEO;
import com.dcits.depsit.facade.eo.TbVoucherDefEO;

/**
 * ST085 检查存入客户黑名单 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST085-TC001 ~ TC008），
 * 预期结果来自正式 Spec ST085（inputs 绑定的 ST085.md），ST105 按契约边界打桩
 * （给定 dealFlow 返回值驱动四分支）。基准示例数据：FM_CLIENT_COPY 单条命中
 * clientNo="C2009000012345"，TB_VOUCHER_DEF 单条命中 docType="778"。
 */
@ExtendWith(MockitoExtension.class)
public class ST085PbcTest {

    @Mock
    private IFmClientCopyBcc fmClientCopyBcc;

    @Mock
    private ITbVoucherDefBcc tbVoucherDefBcc;

    @Mock
    private IST105 st105;

    @InjectMocks
    private ST085Pbc st085;

    /** 构造公共输入：Spec 基准示例数据，11 个字段全量赋值 */
    private ST085InputBO input() {
        ST085InputBO input = new ST085InputBO();
        input.setBaseAcctNo("1002003004005006");
        input.setClientNo("C2009000012345");
        input.setTranBranch(TranBranch.VALUE_351155);
        input.setChannelNo("01");
        input.setDocType(DocType.VALUE_778);
        input.setTranType(OthTranType.VALUE_38);
        input.setServiceCode("SV0001");
        input.setMessageCode("MC0001");
        input.setMessageType("MT0001");
        input.setSourceType(SourceType.AC);
        input.setProgramId("PRG0001");
        return input;
    }

    /** 构造客户副本表记录：clientNo="C2009000012345" 单条命中 */
    private FmClientCopyEO clientCopy() {
        FmClientCopyEO eo = new FmClientCopyEO();
        eo.setClientNo("C2009000012345");
        eo.setClientName("张三");
        eo.setDocumentType(ThawDocumentType2.VALUE_110001);
        eo.setDocumentId("140105199001011234");
        eo.setIssCountry(IssCountry.CHN);
        return eo;
    }

    /** 构造凭证类型定义表记录：docType="778" 单条命中 */
    private TbVoucherDefEO voucherDef() {
        TbVoucherDefEO eo = new TbVoucherDefEO();
        eo.setDocType(DocType.VALUE_778);
        eo.setDocClass(DocClass.CHK);
        return eo;
    }

    /** 桩：IST105#execute 将收到的 ST105InputBO 记录到持有对象并返回给定 dealFlow 的预制结果（succeed=true） */
    private void stubSt105(ST105InputBO[] captured, DealFlow dealFlow) {
        ST105OutputBO st105Output = new ST105OutputBO();
        st105Output.setSucceed(true);
        st105Output.setDealFlow(dealFlow);
        lenient().when(st105.execute(any(ST105InputBO.class))).thenAnswer(invocation -> {
            captured[0] = invocation.getArgument(0);
            return st105Output;
        });
    }

    /** 公共数据桩：客户副本表与凭证类型定义表均按基准示例数据单条命中 */
    private void stubBaseData() {
        lenient().when(fmClientCopyBcc.findByPrimaryKey("C2009000012345")).thenReturn(clientCopy());
        lenient().when(tbVoucherDefBcc.findByDocType(DocType.VALUE_778)).thenReturn(voucherDef());
    }

    // REQ-001-S01 子步骤1 按客户号命中客户证件信息：四字段输出，ST105 入参证件类型、证件号码取同一记录值
    @Test
    public void testST085T01() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, null);

        ST085OutputBO result = st085.execute(input());

        assertTrue(result.isSucceed());
        assertNull(result.getErrorCode());
        assertEquals("张三", result.getClientName());
        assertEquals(ThawDocumentType2.VALUE_110001, result.getDocumentType());
        assertEquals("140105199001011234", result.getDocumentId());
        assertEquals(IssCountry.CHN, result.getIssCountry());
        assertEquals(ThawDocumentType2.VALUE_110001, captured[0].getDocumentType());
        assertEquals("140105199001011234", captured[0].getDocumentId());
    }

    // REQ-002-S01 子步骤2 按凭证类型命中凭证信息：凭证种类输出，ST105 入参凭证种类取同一记录值
    @Test
    public void testST085T02() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, null);

        ST085OutputBO result = st085.execute(input());

        assertTrue(result.isSucceed());
        assertNull(result.getErrorCode());
        assertEquals(DocClass.CHK, result.getDocClass());
        assertEquals(DocClass.CHK, captured[0].getDocClass());
    }

    // REQ-003-S01 子步骤3 事件类型无条件赋常量"CRET"：输出与 ST105 入参一致
    @Test
    public void testST085T03() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, null);

        ST085OutputBO result = st085.execute(input());

        assertTrue(result.isSucceed());
        assertNull(result.getErrorCode());
        assertEquals("CRET", result.getEventType());
        assertEquals("CRET", captured[0].getEventType());
    }

    // REQ-004-S01 子步骤4 组件内步骤调用 ST105：12 个上送参数按映射取值，不上送字段均为 null
    @Test
    public void testST085T04() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, null);

        ST085OutputBO result = st085.execute(input());

        assertEquals("1002003004005006", captured[0].getBaseAcctNo());
        assertEquals("C2009000012345", captured[0].getClientNo());
        assertEquals(ThawDocumentType2.VALUE_110001, captured[0].getDocumentType());
        assertEquals("140105199001011234", captured[0].getDocumentId());
        assertEquals("PRG0001", captured[0].getProgramId());
        assertEquals(OthTranType.VALUE_38, captured[0].getTranType());
        assertEquals("CRET", captured[0].getEventType());
        assertEquals("SV0001", captured[0].getServiceCode());
        assertEquals("MC0001", captured[0].getMessageCode());
        assertEquals("MT0001", captured[0].getMessageType());
        assertEquals(SourceType.AC, captured[0].getSourceType());
        assertEquals(DocClass.CHK, captured[0].getDocClass());
        assertNull(captured[0].getAcctBranch());
        assertNull(captured[0].getBlacklistCheckFlag());
        assertNull(captured[0].getServiceStatus());
        assertTrue(result.isSucceed());
    }

    // REQ-005-S01 子步骤5a 执行结果"通过"（dealFlow=null）：dealFlow/authFlag 均不赋值，步骤成功返回
    @Test
    public void testST085T05() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, null);

        ST085OutputBO result = st085.execute(input());

        assertTrue(result.isSucceed());
        assertNull(result.getErrorCode());
        assertNull(result.getErrorMessage());
        assertNull(result.getDealFlow());
        assertNull(result.getAuthFlag());
        assertEquals("CRET", result.getEventType());
        assertEquals("张三", result.getClientName());
        assertEquals(ThawDocumentType2.VALUE_110001, result.getDocumentType());
        assertEquals("140105199001011234", result.getDocumentId());
        assertEquals(IssCountry.CHN, result.getIssCountry());
        assertEquals(DocClass.CHK, result.getDocClass());
    }

    // REQ-005-S02 子步骤5a 执行结果"提醒"（dealFlow=DealFlow.D）：dealFlow=提醒处理、authFlag 不赋值，步骤成功返回
    @Test
    public void testST085T06() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, DealFlow.D);

        ST085OutputBO result = st085.execute(input());

        assertTrue(result.isSucceed());
        assertNull(result.getErrorCode());
        assertNull(result.getErrorMessage());
        assertEquals(DealFlow.D, result.getDealFlow());
        assertEquals("D", result.getDealFlow().getValue());
        assertNull(result.getAuthFlag());
    }

    // REQ-005-S03 子步骤5b 执行结果"授权"（dealFlow=DealFlow.A）：authFlag="是"、dealFlow 不赋值，步骤成功返回
    @Test
    public void testST085T07() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, DealFlow.A);

        ST085OutputBO result = st085.execute(input());

        assertTrue(result.isSucceed());
        assertNull(result.getErrorCode());
        assertNull(result.getErrorMessage());
        assertEquals("是", result.getAuthFlag());
        assertNull(result.getDealFlow());
    }

    // REQ-005-S04 子步骤5c 执行结果"拒绝"（dealFlow=DealFlow.B）：失败返回错误码 ER0003，子步骤1–3 赋值已先行完成
    @Test
    public void testST085T08() {
        stubBaseData();
        ST105InputBO[] captured = new ST105InputBO[1];
        stubSt105(captured, DealFlow.B);

        ST085OutputBO result = st085.execute(input());

        assertFalse(result.isSucceed());
        assertEquals("ER0003", result.getErrorCode());
        assertNull(result.getDealFlow());
        assertNull(result.getAuthFlag());
        assertEquals("CRET", result.getEventType());
        assertEquals("张三", result.getClientName());
        assertEquals(DocClass.CHK, result.getDocClass());
    }
}
