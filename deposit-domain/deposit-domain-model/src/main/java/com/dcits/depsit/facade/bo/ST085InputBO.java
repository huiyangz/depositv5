package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.DocType;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.TranBranch;

/**
 * ST085 检查存入客户黑名单 输入BO。
 *
 * 字段定义来自正式 Spec ST085「输入」表（11 个字段）。需求未定义各必填输入缺失或为空时的
 * 校验行为，本 BO 不做校验；tranBranch、channelNo 正文子步骤未消费（契约注记 1、2），
 * docType 为非必填输入（契约注记 3）。
 */
public class ST085InputBO {

    /** 账号 */
    private String baseAcctNo;
    /** 客户号 */
    private String clientNo;
    /** 交易机构号（正文子步骤未消费，契约注记 1） */
    private TranBranch tranBranch;
    /** 交易渠道编号（正文子步骤未消费，契约注记 2） */
    private String channelNo;
    /** 凭证类型 */
    private DocType docType;
    /** 交易类型 */
    private OthTranType tranType;
    /** 服务代码 */
    private String serviceCode;
    /** 接口服务代码 */
    private String messageCode;
    /** 接口服务类型 */
    private String messageType;
    /** 渠道类型 */
    private SourceType sourceType;
    /** 交易代码 */
    private String programId;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public TranBranch getTranBranch() {
        return tranBranch;
    }

    public void setTranBranch(TranBranch tranBranch) {
        this.tranBranch = tranBranch;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public OthTranType getTranType() {
        return tranType;
    }

    public void setTranType(OthTranType tranType) {
        this.tranType = tranType;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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
}
