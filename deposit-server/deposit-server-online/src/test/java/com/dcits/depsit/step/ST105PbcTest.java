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

import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.RcBlackStatus;
import com.dcits.depsit.enums.ResBranchRange;
import com.dcits.depsit.enums.ResOperateFlag;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.ThawDocumentType2;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST105InputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;
import com.dcits.depsit.facade.components.IFmBranchBcc;
import com.dcits.depsit.facade.components.IFmServiceDefineBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRcAllListBcc;
import com.dcits.depsit.facade.components.IRcListCheckRangeBcc;
import com.dcits.depsit.facade.components.IRcListNotCheckRangeBcc;
import com.dcits.depsit.facade.components.IRcListTypeBcc;
import com.dcits.depsit.facade.components.IRcRuleTypeBcc;
import com.dcits.depsit.facade.eo.FmBranchEO;
import com.dcits.depsit.facade.eo.FmServiceDefineEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;
import com.dcits.depsit.facade.eo.RcAllListEO;
import com.dcits.depsit.facade.eo.RcListCheckRangeEO;
import com.dcits.depsit.facade.eo.RcListNotCheckRangeEO;
import com.dcits.depsit.facade.eo.RcListTypeEO;
import com.dcits.depsit.facade.eo.RcRuleTypeEO;

/**
 * ST105 检查黑名单 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST105-TC001 ~ TC017），
 * 预期结果来自正式 Spec ST105（fileInputs.spec 绑定的 ST105.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；检查结果"通过"（dealFlow=null）
 * 是正常提前结束，不是失败。当前{交易机构}以输入 acctBranch 字段值参与判定
 * （数据来源需求未定义、不纳入断言，Spec 契约注记 1、不覆盖事项 6）。
 */
@ExtendWith(MockitoExtension.class)
public class ST105PbcTest {

    /** 接口服务代码（Spec 基准链路示例数据） */
    private static final String MESSAGE_CODE = "MC0001";

    /** 接口服务类型 */
    private static final String MESSAGE_TYPE = "MT0001";

    /** 服务代码 */
    private static final String SERVICE_CODE = "SV0001";

    /** 交易码 */
    private static final String PROGRAM_ID = "PRG0001";

    /** 事件类型 */
    private static final String EVENT_TYPE = "EVT0001";

    /** 客户号 */
    private static final String CLIENT_NO = "C2009000012345";

    /** 账号 */
    private static final String BASE_ACCT_NO = "1002003004005006";

    /** 名单类型代码 */
    private static final String LIST_TYPE = "1010";

    /** 黑名单检查规则编号 */
    private static final String RULE_ID = "R0001";

    @Mock
    private IFmServiceDefineBcc fmServiceDefineBcc;

    @Mock
    private IRcAllListBcc rcAllListBcc;

    @Mock
    private IRcListTypeBcc rcListTypeBcc;

    @Mock
    private IRcRuleTypeBcc rcRuleTypeBcc;

    @Mock
    private IRcListCheckRangeBcc rcListCheckRangeBcc;

    @Mock
    private IRcListNotCheckRangeBcc rcListNotCheckRangeBcc;

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @Mock
    private IFmBranchBcc fmBranchBcc;

    @InjectMocks
    private ST105Pbc st105;

    /** 构造基准输入：Spec「基准链路示例数据」全 15 字段 */
    private ST105InputBO baseInput() {
        ST105InputBO input = new ST105InputBO();
        input.setMessageCode(MESSAGE_CODE);
        input.setMessageType(MESSAGE_TYPE);
        input.setServiceCode(SERVICE_CODE);
        input.setProgramId(PROGRAM_ID);
        input.setEventType(EVENT_TYPE);
        input.setTranType(OthTranType.VALUE_38);
        input.setSourceType(SourceType.AC);
        input.setClientNo(CLIENT_NO);
        input.setDocumentId("140105199001011234");
        input.setDocumentType(ThawDocumentType2.VALUE_110001);
        input.setBaseAcctNo(BASE_ACCT_NO);
        input.setDocClass(DocClass.CHK);
        input.setAcctBranch(TranBranch.VALUE_351155);
        input.setBlacklistCheckFlag("Y");
        input.setServiceStatus("A");
        return input;
    }

    /** 构造核心服务定义记录：请求字段为 messageCode/messageType */
    private FmServiceDefineEO serviceDefine(String serviceStatus, String blacklistCheckFlag) {
        FmServiceDefineEO eo = new FmServiceDefineEO();
        eo.setMessageCode(MESSAGE_CODE);
        eo.setMessageType(MESSAGE_TYPE);
        eo.setServiceStatus(serviceStatus);
        eo.setBlacklistCheckFlag(blacklistCheckFlag);
        return eo;
    }

    /** 构造名单信息记录：请求字段为 clientNo */
    private RcAllListEO rcAllList(RcBlackStatus rcBlackStatus, String listType) {
        RcAllListEO eo = new RcAllListEO();
        eo.setClientNo(CLIENT_NO);
        eo.setRcBlackStatus(rcBlackStatus);
        eo.setListType(listType);
        return eo;
    }

    /** 构造名单类型记录：请求字段为 listType */
    private RcListTypeEO rcListType(String ruleId) {
        RcListTypeEO eo = new RcListTypeEO();
        eo.setListType(LIST_TYPE);
        eo.setRuleId(ruleId);
        return eo;
    }

    /** 构造名单限制规则记录：请求字段为 ruleId */
    private RcRuleTypeEO rcRuleType(ResOperateFlag resOperateFlag, String cardMedium,
                                    ResBranchRange resBranchRange, DealFlow dealFlow) {
        RcRuleTypeEO eo = new RcRuleTypeEO();
        eo.setRuleId(RULE_ID);
        eo.setResOperateFlag(resOperateFlag);
        eo.setCardMedium(cardMedium);
        eo.setResBranchRange(resBranchRange);
        eo.setDealFlow(dealFlow);
        return eo;
    }

    /** 构造名单检查范围记录：请求字段为七条件 */
    private RcListCheckRangeEO checkRange(String eventType) {
        RcListCheckRangeEO eo = new RcListCheckRangeEO();
        eo.setEventType(eventType);
        eo.setTranType(OthTranType.VALUE_38);
        eo.setSourceType(SourceType.AC);
        eo.setProgramId(PROGRAM_ID);
        eo.setServiceCode(SERVICE_CODE);
        eo.setMessageType(MESSAGE_TYPE);
        eo.setMessageCode(MESSAGE_CODE);
        return eo;
    }

    /** 构造名单不检查范围记录：请求字段为六条件，eventType 为约束条件取值 */
    private RcListNotCheckRangeEO notCheckRange(String eventType) {
        RcListNotCheckRangeEO eo = new RcListNotCheckRangeEO();
        eo.setEventType(eventType);
        eo.setTranType(OthTranType.VALUE_38);
        eo.setSourceType(SourceType.AC);
        eo.setProgramId(PROGRAM_ID);
        eo.setServiceCode(SERVICE_CODE);
        eo.setMessageType(MESSAGE_TYPE);
        eo.setMessageCode(MESSAGE_CODE);
        return eo;
    }

    /** 构造账户信息记录：请求字段为 baseAcctNo */
    private RbBusAcctEO rbBusAcct(TranBranch acctBranch) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setAcctBranch(acctBranch);
        return eo;
    }

    /** 构造机构信息记录：branch 为机构号，attachedTo 为上级机构 */
    private FmBranchEO fmBranch(TranBranch branch, TranBranch attachedTo) {
        FmBranchEO eo = new FmBranchEO();
        eo.setBranch(branch);
        eo.setAttachedTo(attachedTo);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册同方法第二个桩时 argThat 会以占位 null 真实调用 mock，
    // 不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变

    /** 桩1 子步骤1：按 messageCode+messageType 组合条件核对请求并返回服务信息列表 */
    private void stubServiceDefines(List<FmServiceDefineEO> records) {
        lenient().when(fmServiceDefineBcc.findByEo(argThat(eo ->
                eo != null && MESSAGE_CODE.equals(eo.getMessageCode())
                        && MESSAGE_TYPE.equals(eo.getMessageType()))))
                .thenReturn(records);
    }

    /** 桩2 子步骤3：按 clientNo 条件核对请求并返回名单信息列表 */
    private void stubRcAllLists(List<RcAllListEO> records) {
        lenient().when(rcAllListBcc.findByEo(argThat(eo ->
                eo != null && CLIENT_NO.equals(eo.getClientNo()))))
                .thenReturn(records);
    }

    /** 桩3 子步骤5：按 listType 条件核对请求并返回名单类型列表 */
    private void stubRcListTypes(List<RcListTypeEO> records) {
        lenient().when(rcListTypeBcc.findByEo(argThat(eo ->
                eo != null && LIST_TYPE.equals(eo.getListType()))))
                .thenReturn(records);
    }

    /** 桩4 子步骤7、15：按 ruleId 条件核对请求并返回名单限制规则列表（两次查询同一桩覆盖） */
    private void stubRcRuleTypes(List<RcRuleTypeEO> records) {
        lenient().when(rcRuleTypeBcc.findByEo(argThat(eo ->
                eo != null && RULE_ID.equals(eo.getRuleId()))))
                .thenReturn(records);
    }

    /** 桩5 子步骤9：按七条件核对请求并返回名单检查范围列表 */
    private void stubCheckRanges(List<RcListCheckRangeEO> records) {
        lenient().when(rcListCheckRangeBcc.findByEo(argThat(eo ->
                eo != null && EVENT_TYPE.equals(eo.getEventType())
                        && eo.getTranType() == OthTranType.VALUE_38
                        && eo.getSourceType() == SourceType.AC
                        && PROGRAM_ID.equals(eo.getProgramId())
                        && SERVICE_CODE.equals(eo.getServiceCode())
                        && MESSAGE_TYPE.equals(eo.getMessageType())
                        && MESSAGE_CODE.equals(eo.getMessageCode()))))
                .thenReturn(records);
    }

    /** 桩6 子步骤10：按六条件核对请求并返回名单不检查范围列表 */
    private void stubNotCheckRanges(List<RcListNotCheckRangeEO> records) {
        lenient().when(rcListNotCheckRangeBcc.findByEo(argThat(eo ->
                eo != null && eo.getTranType() == OthTranType.VALUE_38
                        && eo.getSourceType() == SourceType.AC
                        && PROGRAM_ID.equals(eo.getProgramId())
                        && SERVICE_CODE.equals(eo.getServiceCode())
                        && MESSAGE_TYPE.equals(eo.getMessageType())
                        && MESSAGE_CODE.equals(eo.getMessageCode()))))
                .thenReturn(records);
    }

    /** 桩7 子步骤13：按 baseAcctNo 条件核对请求并返回账户信息 */
    private void stubRbBusAcct(TranBranch acctBranch) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()))))
                .thenReturn(Collections.singletonList(rbBusAcct(acctBranch)));
    }

    /** 层级桩 子步骤14：按机构号返回机构信息（上级机构 attachedTo） */
    private void stubFmBranch(TranBranch branch, TranBranch attachedTo) {
        lenient().when(fmBranchBcc.findByBranch(branch))
                .thenReturn(fmBranch(branch, attachedTo));
    }

    /** 基准名单限制规则记录：标识 E、介质含 CHK、机构范围 B、处理方式可变 */
    private List<RcRuleTypeEO> ruleTypes(DealFlow dealFlow) {
        return Collections.singletonList(
                rcRuleType(ResOperateFlag.E, "CHK", ResBranchRange.B, dealFlow));
    }

    /** "通过"路径公共断言：succeed=true、错误字段 null、dealFlow=null（正常提前结束） */
    private void assertPass(ST105OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getDealFlow());
    }

    /** 检查结果三值断言：succeed=true、错误字段 null、dealFlow 等于预期枚举及取值 */
    private void assertDealFlow(ST105OutputBO output, DealFlow expected, String expectedValue) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(expected, output.getDealFlow());
        assertEquals(expectedValue, output.getDealFlow().getValue());
    }

    /** 设置基准链路桩1–桩6（服务定义→名单→名单类型→规则→检查范围→不检查范围为空） */
    private void stubBaselineThroughNotCheckRange() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(ruleTypes(DealFlow.B));
        stubCheckRanges(Collections.singletonList(checkRange(EVENT_TYPE)));
        stubNotCheckRanges(Collections.emptyList());
    }

    // REQ-001-S01 等 8 个"继续执行"场景合并（Spec 以基准链路终值佐证各层未短路）：
    // 开关开启→生效黑名单→规则 R0001→检查类 E→不检查范围无匹配→介质 CHK 命中→机构 B 且 351155 等于交易机构→拒绝
    @Test
    public void testST105T01() {
        stubBaselineThroughNotCheckRange();
        stubRbBusAcct(TranBranch.VALUE_351155);

        ST105OutputBO output = st105.execute(baseInput());

        assertDealFlow(output, DealFlow.B, "B");
    }

    // REQ-001-S02 服务定义两条记录均不同时满足两条件（甲 A/N、乙 F/Y），判定以查询结果为准→"通过"，子步骤3–16不执行
    @Test
    public void testST105T02() {
        stubServiceDefines(Arrays.asList(
                serviceDefine("A", "N"),
                serviceDefine("F", "Y")));

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-001-S03 服务定义查询无记录，[服务信息列表]为空→"通过"，子步骤3–16不执行
    @Test
    public void testST105T03() {
        stubServiceDefines(Collections.emptyList());

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-002-S02 名单信息表无可命中记录，[黑名单信息]为空→"通过"，子步骤5–16不执行
    @Test
    public void testST105T04() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.emptyList());

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-002-S03 名单记录存在但黑名单状态为"E-失效"，被"生效"条件排除，[黑名单信息]为空→"通过"，子步骤5–16不执行
    @Test
    public void testST105T05() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.E, LIST_TYPE)));

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-003-S02 名单类型表无 listType="1010" 记录，[黑名单检查规则编号]为空→"通过"，子步骤7–16不执行
    @Test
    public void testST105T06() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.emptyList());

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-004-S02 黑名单限制操作标识为"C-控制"（非 E）→"通过"，子步骤9–16不执行
    @Test
    public void testST105T07() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(Collections.singletonList(
                rcRuleType(ResOperateFlag.C, "CHK", ResBranchRange.B, DealFlow.B)));

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-004-S03 未查询到名单限制规则信息，标识无值条件不成立按"否则"→"通过"，子步骤9–16不执行
    @Test
    public void testST105T08() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(Collections.emptyList());

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-005-S01 名单不检查范围存在匹配六条件且 eventType 属于[事件类型]的记录→"通过"，子步骤12–16不执行（账户查询不发生）
    @Test
    public void testST105T09() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(ruleTypes(DealFlow.B));
        stubCheckRanges(Collections.singletonList(checkRange(EVENT_TYPE)));
        stubNotCheckRanges(Collections.singletonList(notCheckRange(EVENT_TYPE)));

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-005-S03 名单检查范围无匹配，[事件类型]为空集合，[名单不检查范围信息]为空→继续执行至子步骤16，dealFlow=B
    @Test
    public void testST105T10() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(ruleTypes(DealFlow.B));
        stubCheckRanges(Collections.emptyList());
        stubNotCheckRanges(Collections.emptyList());
        stubRbBusAcct(TranBranch.VALUE_351155);

        ST105OutputBO output = st105.execute(baseInput());

        assertDealFlow(output, DealFlow.B, "B");
    }

    // REQ-006-S01 凭证种类为空（docClass=null）→"通过"，子步骤13–16不执行（账户查询不发生）
    @Test
    public void testST105T11() {
        stubBaselineThroughNotCheckRange();

        ST105InputBO input = baseInput();
        input.setDocClass(null);

        ST105OutputBO output = st105.execute(input);

        assertPass(output);
    }

    // REQ-006-S02 凭证种类 CHK 不在介质范围（范围仅含 CRD）→"通过"，子步骤13–16不执行（账户查询不发生）
    @Test
    public void testST105T12() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(Collections.singletonList(
                rcRuleType(ResOperateFlag.E, "CRD", ResBranchRange.B, DealFlow.B)));
        stubCheckRanges(Collections.singletonList(checkRange(EVENT_TYPE)));
        stubNotCheckRanges(Collections.emptyList());

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-007-S02 机构范围 B 且账户开立行 351156 属于交易机构 351155 的下级机构（上级机构为 351155）→继续，dealFlow=B
    @Test
    public void testST105T13() {
        stubBaselineThroughNotCheckRange();
        stubRbBusAcct(TranBranch.VALUE_351156);
        stubFmBranch(TranBranch.VALUE_351156, TranBranch.VALUE_351155);

        ST105OutputBO output = st105.execute(baseInput());

        assertDealFlow(output, DealFlow.B, "B");
    }

    // REQ-007-S03 机构范围 B 但账户开立行 351164 既不等于交易机构 351155 也不属其下级
    // （上溯链 351164→351001→无上级，链上无 351155）→"通过"，子步骤15–16不执行
    @Test
    public void testST105T14() {
        stubBaselineThroughNotCheckRange();
        stubRbBusAcct(TranBranch.VALUE_351164);
        stubFmBranch(TranBranch.VALUE_351164, TranBranch.VALUE_351001);
        stubFmBranch(TranBranch.VALUE_351001, null);

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-007-S04 限制机构范围为"A-所有机构"（非 B 取值，A/C/D/F 一律落入"否则"）→"通过"，
    // 子步骤15–16不执行（账户开立行虽等于交易机构仍短路）
    @Test
    public void testST105T15() {
        stubServiceDefines(Collections.singletonList(serviceDefine("A", "Y")));
        stubRcAllLists(Collections.singletonList(rcAllList(RcBlackStatus.A, LIST_TYPE)));
        stubRcListTypes(Collections.singletonList(rcListType(RULE_ID)));
        stubRcRuleTypes(Collections.singletonList(
                rcRuleType(ResOperateFlag.E, "CHK", ResBranchRange.A, DealFlow.B)));
        stubCheckRanges(Collections.singletonList(checkRange(EVENT_TYPE)));
        stubNotCheckRanges(Collections.emptyList());
        stubRbBusAcct(TranBranch.VALUE_351155);

        ST105OutputBO output = st105.execute(baseInput());

        assertPass(output);
    }

    // REQ-008-S02 全链路命中至子步骤16，处理方式为"授权"（DealFlow.A）→检查结果"授权"，输出 dealFlow=A
    @Test
    public void testST105T16() {
        stubBaselineThroughNotCheckRange();
        stubRcRuleTypes(ruleTypes(DealFlow.A));
        stubRbBusAcct(TranBranch.VALUE_351155);

        ST105OutputBO output = st105.execute(baseInput());

        assertDealFlow(output, DealFlow.A, "A");
    }

    // REQ-008-S03 全链路命中至子步骤16，处理方式为"提醒"（DealFlow.D）→检查结果"提醒"，输出 dealFlow=D
    @Test
    public void testST105T17() {
        stubBaselineThroughNotCheckRange();
        stubRcRuleTypes(ruleTypes(DealFlow.D));
        stubRbBusAcct(TranBranch.VALUE_351155);

        ST105OutputBO output = st105.execute(baseInput());

        assertDealFlow(output, DealFlow.D, "D");
    }
}
