package com.dcits.depsit.facade.bo;

import java.util.Date;

/**
 * ST103 检查限额场景配置是否有效 输入BO。
 *
 * 字段定义来自正式 Spec ST103「输入」表：tranDate（交易日期，必填）、
 * tranTimestamp（交易时间戳，格式 HHmmss，必填）、limitBranchId（限额机构编码，必填）、
 * limitSceneNo（限额场景编码，必填）。
 * 需求未定义各输入缺失、为空或 tranTimestamp 格式非法时的处理（「失败处理」声明
 * 无业务失败场景），本 BO 不做校验。
 */
public class ST103InputBO {

    /** 交易日期 */
    private Date tranDate;
    /** 交易时间戳，格式 HHmmss */
    private String tranTimestamp;
    /** 限额机构编码 */
    private String limitBranchId;
    /** 限额场景编码 */
    private String limitSceneNo;

    public Date getTranDate() {
        return tranDate;
    }

    public void setTranDate(Date tranDate) {
        this.tranDate = tranDate;
    }

    public String getTranTimestamp() {
        return tranTimestamp;
    }

    public void setTranTimestamp(String tranTimestamp) {
        this.tranTimestamp = tranTimestamp;
    }

    public String getLimitBranchId() {
        return limitBranchId;
    }

    public void setLimitBranchId(String limitBranchId) {
        this.limitBranchId = limitBranchId;
    }

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }
}
