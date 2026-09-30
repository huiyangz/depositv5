package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.facade.bo.ST085InputBO;
import com.dcits.depsit.facade.bo.ST085OutputBO;
import com.dcits.depsit.facade.bo.ST105InputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;
import com.dcits.depsit.facade.components.IFmClientCopyBcc;
import com.dcits.depsit.facade.components.ITbVoucherDefBcc;
import com.dcits.depsit.facade.eo.FmClientCopyEO;
import com.dcits.depsit.facade.eo.TbVoucherDefEO;

/**
 * ST085 检查存入客户黑名单。
 *
 * 子步骤1 按{客户号}主键查询【客户副本表】（FM_CLIENT_COPY）取得客户名称、证件类型、
 * 证件号码、发证国家；子步骤2 按{凭证类型}查询【凭证类型定义表】（TB_VOUCHER_DEF）
 * 取得凭证种类；子步骤3 无条件赋值[事件类型]="CRET"；子步骤4 以组件内步骤调用方式执行
 * 本组件步骤 ST105「检查黑名单」，12 个上送参数按映射取值，不上送 acctBranch、
 * blacklistCheckFlag、serviceStatus（由 ST105 内部查询承载）及 clientName、issCountry、
 * tranBranch、channelNo（目标无对应上送字段）；子步骤5 按返回 dealFlow 映射[执行结果]
 * 并表达检查结果：null="通过"、DealFlow.D="提醒"→dealFlow=DealFlow.D、DealFlow.A=
 * "授权"→authFlag="是"均成功返回，DealFlow.B="拒绝"→失败返回错误码 ER0003。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
@Service
public class ST085Pbc implements IST085 {

    /** 子步骤3 事件类型常量：CRET */
    private static final String EVENT_TYPE = "CRET";

    /** 子步骤5b 授权标志："是"（需求文本常量） */
    private static final String AUTH_FLAG_YES = "是";

    /** 子步骤5c 拒绝错误码 */
    private static final String ERROR_CODE_REJECT = "ER0003";

    /** 错误码 ER0003 配置文案（deposit-application/src/main/resources/errorcodes.properties） */
    private static final String REJECT_CONFIG_MESSAGE = "黑名单客户，拒绝交易";

    /** 失败错误信息：按“错误码::业务说明”格式拼装错误码与配置文案 */
    private static final String ERROR_MESSAGE_REJECT = ERROR_CODE_REJECT + "::" + REJECT_CONFIG_MESSAGE;

    @Autowired
    private IFmClientCopyBcc fmClientCopyBcc;

    @Autowired
    private ITbVoucherDefBcc tbVoucherDefBcc;

    @Autowired
    private IST105 st105;

    @Override
    public ST085OutputBO execute(ST085InputBO input) {
        ST085OutputBO output = new ST085OutputBO();

        // 子步骤1 获取客户证件信息：按{客户号}主键查询【客户副本表】（至多一条，验收按单条命中），
        // 将客户名称、证件类型、证件号码、发证国家赋值到输出；证件类型、证件号码供子步骤4上送
        FmClientCopyEO clientCopy = fmClientCopyBcc.findByPrimaryKey(input.getClientNo());
        output.setClientName(clientCopy.getClientName());
        output.setDocumentType(clientCopy.getDocumentType());
        output.setDocumentId(clientCopy.getDocumentId());
        output.setIssCountry(clientCopy.getIssCountry());

        // 子步骤2 获取凭证种类：按{凭证类型}查询【凭证类型定义表】取得凭证种类，供子步骤4上送
        TbVoucherDefEO voucherDef = tbVoucherDefBcc.findByDocType(input.getDocType());
        output.setDocClass(voucherDef.getDocClass());

        // 子步骤3 赋值事件类型：无条件赋常量"CRET"，不依赖输入、查询结果或[执行结果]分支
        output.setEventType(EVENT_TYPE);

        // 子步骤4 组件内步骤调用 ST105「检查黑名单」（恰好一次）：12 个上送参数按映射取值；
        // acctBranch、blacklistCheckFlag、serviceStatus 及 clientName、issCountry、tranBranch、
        // channelNo 一律不设置（null）
        ST105InputBO st105Input = new ST105InputBO();
        st105Input.setBaseAcctNo(input.getBaseAcctNo());
        st105Input.setClientNo(input.getClientNo());
        st105Input.setDocumentType(clientCopy.getDocumentType());
        st105Input.setDocumentId(clientCopy.getDocumentId());
        st105Input.setProgramId(input.getProgramId());
        st105Input.setTranType(input.getTranType());
        st105Input.setEventType(EVENT_TYPE);
        st105Input.setServiceCode(input.getServiceCode());
        st105Input.setMessageCode(input.getMessageCode());
        st105Input.setMessageType(input.getMessageType());
        st105Input.setSourceType(input.getSourceType());
        st105Input.setDocClass(voucherDef.getDocClass());
        ST105OutputBO st105Output = st105.execute(st105Input);

        // 子步骤5 检查执行结果：ST105 返回 dealFlow 映射[执行结果]（null=通过、D=提醒、A=授权、B=拒绝）
        DealFlow dealFlow = st105Output.getDealFlow();
        if (dealFlow == DealFlow.B) {
            // 5c "拒绝"：失败返回；子步骤1–3 的赋值与 ST105 调用已先行完成
            output.setSucceed(false);
            output.setErrorCode(ERROR_CODE_REJECT);
            output.setErrorMessage(ERROR_MESSAGE_REJECT);
            return output;
        }
        if (dealFlow == DealFlow.A) {
            // 5b "授权"：仅赋授权标志"是"；等待授权结果后再执行后续步骤（编排不在本步骤范围）
            output.setAuthFlag(AUTH_FLAG_YES);
        } else if (dealFlow == DealFlow.D) {
            // 5a "提醒"：检查结果为"通过"，以 dealFlow=DealFlow.D（提醒处理）表达
            output.setDealFlow(DealFlow.D);
        }
        // 5a "通过"（dealFlow 不赋值）/"提醒"/"授权"：步骤成功返回，dealFlow、authFlag 互斥表达
        output.setSucceed(true);
        return output;
    }
}
