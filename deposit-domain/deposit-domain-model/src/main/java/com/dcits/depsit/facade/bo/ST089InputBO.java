package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.DocType;
import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.TranBranch;

/**
 * ST089 检查存入账户黑名单 输入BO。
 *
 * 字段定义来自正式 Spec ST089「输入」表（9 个字段）。channelNo 正文子步骤 1–4 未引用、
 * 调用目标 ST105InputBO 亦无对应字段，按输入表声明保留（契约注记 1）；tranBranch 的
 * {交易机构}在 ST105InputBO 无承接字段，不向调用目标传入（契约注记 2）；需求未定义
 * 必填输入缺失或为空时的校验行为，本 BO 不做校验（契约注记 3）。
 */
public class ST089InputBO {

    /** 账号 */
    private String baseAcctNo;
    /** 交易机构号（取值来源于代码[内部机构编号]） */
    private TranBranch tranBranch;
    /** 交易渠道编号 */
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

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
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
}
