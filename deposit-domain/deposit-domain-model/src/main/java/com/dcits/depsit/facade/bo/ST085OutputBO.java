package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.enums.IssCountry;
import com.dcits.depsit.enums.ThawDocumentType2;

/**
 * ST085 检查存入客户黑名单 输出BO。
 *
 * 字段定义来自正式 Spec ST085「输出」表（8 个字段）。检查结果不另设输出字段，仅由
 * dealFlow、authFlag 及错误码"ER0003"表达：dealFlow、authFlag 互斥使用，[执行结果]
 * 为"提醒"时仅赋 dealFlow=DealFlow.D，为"授权"时仅赋 authFlag="是"，"通过"、"拒绝"
 * 两分支均不赋值（null）；"拒绝"分支以 StepResult 失败契约表达（succeed=false、
 * errorCode="ER0003"）。成功时错误码与错误信息为 null。
 */
public class ST085OutputBO extends StepResult {

    /** 事件类型（子步骤3无条件赋常量"CRET"） */
    private String eventType;
    /** 处理方式（仅[执行结果]="提醒"时赋 DealFlow.D，其余分支 null） */
    private DealFlow dealFlow;
    /** 授权标志（仅[执行结果]="授权"时赋"是"，其余分支 null） */
    private String authFlag;
    /** 凭证种类，来源：凭证类型定义表（TB_VOUCHER_DEF） */
    private DocClass docClass;
    /** 客户名称，来源：客户副本表（FM_CLIENT_COPY） */
    private String clientName;
    /** 证件号码，来源：客户副本表（FM_CLIENT_COPY） */
    private String documentId;
    /** 证件类型，来源：客户副本表（FM_CLIENT_COPY） */
    private ThawDocumentType2 documentType;
    /** 发证国家，来源：客户副本表（FM_CLIENT_COPY） */
    private IssCountry issCountry;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public DealFlow getDealFlow() {
        return dealFlow;
    }

    public void setDealFlow(DealFlow dealFlow) {
        this.dealFlow = dealFlow;
    }

    public String getAuthFlag() {
        return authFlag;
    }

    public void setAuthFlag(String authFlag) {
        this.authFlag = authFlag;
    }

    public DocClass getDocClass() {
        return docClass;
    }

    public void setDocClass(DocClass docClass) {
        this.docClass = docClass;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public ThawDocumentType2 getDocumentType() {
        return documentType;
    }

    public void setDocumentType(ThawDocumentType2 documentType) {
        this.documentType = documentType;
    }

    public IssCountry getIssCountry() {
        return issCountry;
    }

    public void setIssCountry(IssCountry issCountry) {
        this.issCountry = issCountry;
    }
}
