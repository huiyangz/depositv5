package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.ThawDocumentType2;
import com.dcits.depsit.enums.TranBranch;

/**
 * ST105 检查黑名单 输入BO。
 *
 * 字段定义来自正式 Spec ST105「输入」表（15 个字段）。需求声明无业务失败场景，
 * 未定义必填输入缺失或为空时的处理，本 BO 不做校验；blacklistCheckFlag、serviceStatus
 * 为输入声明值，子步骤 2 判定以子步骤 1 查询结果为准（契约注记 2）；acctBranch 为输入
 * 声明值，[账户开立行行号]以子步骤 13 查询结果为准（契约注记 3）。
 */
public class ST105InputBO {

    /** 凭证种类 */
    private DocClass docClass;
    /** 账号 */
    private String baseAcctNo;
    /** 账户开立行行号（输入声明值，判定以子步骤13查询结果为准） */
    private TranBranch acctBranch;
    /** 渠道类型 */
    private SourceType sourceType;
    /** 交易代码（子步骤9、10称{交易码}，同一字段） */
    private String programId;
    /** 交易类型 */
    private OthTranType tranType;
    /** 事件类型 */
    private String eventType;
    /** 服务代码 */
    private String serviceCode;
    /** 接口服务类型 */
    private String messageType;
    /** 接口服务代码 */
    private String messageCode;
    /** 黑名单检查标志（输入声明值，判定以子步骤1查询结果为准） */
    private String blacklistCheckFlag;
    /** 服务状态（输入声明值，判定以子步骤1查询结果为准） */
    private String serviceStatus;
    /** 客户号 */
    private String clientNo;
    /** 证件号码 */
    private String documentId;
    /** 证件类型 */
    private ThawDocumentType2 documentType;

    public DocClass getDocClass() {
        return docClass;
    }

    public void setDocClass(DocClass docClass) {
        this.docClass = docClass;
    }

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public TranBranch getAcctBranch() {
        return acctBranch;
    }

    public void setAcctBranch(TranBranch acctBranch) {
        this.acctBranch = acctBranch;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public OthTranType getTranType() {
        return tranType;
    }

    public void setTranType(OthTranType tranType) {
        this.tranType = tranType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getBlacklistCheckFlag() {
        return blacklistCheckFlag;
    }

    public void setBlacklistCheckFlag(String blacklistCheckFlag) {
        this.blacklistCheckFlag = blacklistCheckFlag;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
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
}
