package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;

/**
 * ST098 检查限额 输出BO。
 *
 * 字段定义来自正式 Spec ST098「输出」表。空值语义：limitCtrlAmt、limitCtrlNum 在无配置记录
 * 或记录对应字段为空时为 null；limitSumAmt、limitSumNum 原样透传同名输入（不改写数值、不重算
 * 金额标度）；limitCheckResult 恒有值（"超限"/"未超限"，需求文本常量，无枚举绑定）。
 * 本步骤无业务失败场景，"超限"是正常业务结果，成功时错误码与错误信息为 null。
 */
public class ST098OutputBO extends StepResult {

    /** 限额控制金额，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private BigDecimal limitCtrlAmt;
    /** 限额控制笔数，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private Integer limitCtrlNum;
    /** 限额累计金额，同名输入透传（值源头：限额累计信息表 RB_LIMIT_SUM_INFO，由上游提供） */
    private BigDecimal limitSumAmt;
    /** 限额累计笔数，同名输入透传（值源头：限额累计信息表 RB_LIMIT_SUM_INFO，由上游提供） */
    private Integer limitSumNum;
    /** 限额检查结果，取值"超限"/"未超限" */
    private String limitCheckResult;

    public BigDecimal getLimitCtrlAmt() {
        return limitCtrlAmt;
    }

    public void setLimitCtrlAmt(BigDecimal limitCtrlAmt) {
        this.limitCtrlAmt = limitCtrlAmt;
    }

    public Integer getLimitCtrlNum() {
        return limitCtrlNum;
    }

    public void setLimitCtrlNum(Integer limitCtrlNum) {
        this.limitCtrlNum = limitCtrlNum;
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

    public String getLimitCheckResult() {
        return limitCheckResult;
    }

    public void setLimitCheckResult(String limitCheckResult) {
        this.limitCheckResult = limitCheckResult;
    }
}
