package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

/**
 * ST098 检查限额 输入BO。
 *
 * 字段定义来自正式 Spec ST098「输入」表：limitSceneNo（限额场景编码）、
 * limitBranchId（限额机构编码）、limitSumAmt（限额累计金额）、limitSumNum（限额累计笔数），均必填。
 * 需求未定义任一输入缺失或为空时的处理（无业务失败场景），必填性由上送方保证，本 BO 不做校验。
 */
public class ST098InputBO {

    /** 限额场景编码 */
    private String limitSceneNo;
    /** 限额机构编码 */
    private String limitBranchId;
    /** 限额累计金额 */
    private BigDecimal limitSumAmt;
    /** 限额累计笔数 */
    private Integer limitSumNum;

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public String getLimitBranchId() {
        return limitBranchId;
    }

    public void setLimitBranchId(String limitBranchId) {
        this.limitBranchId = limitBranchId;
    }

    public BigDecimal getLimitSumAmt() {
        return limitSumAmt;
    }

    public void setLimitSumAmt(BigDecimal limitSumAmt) {
        this.limitSumAmt = limitSumAmt;
    }

    public Integer getLimitSumNum() {
        return limitSumNum;
    }

    public void setLimitSumNum(Integer limitSumNum) {
        this.limitSumNum = limitSumNum;
    }
}
