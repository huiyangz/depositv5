package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;

/**
 * ST097 计算限额累计金额 输出BO。
 *
 * 字段定义来自正式 Spec ST097「输出」表。空值语义：未取到启用有效配置记录、按来源未取到
 * [限额信息]、或限额控制类型为 O-单笔金额时两字段均为 null；其余场景两字段同时有值
 * （金额为 BigDecimal 加法结果，笔数为整数加法结果）。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST097OutputBO extends StepResult {

    /** 限额累计金额 */
    private BigDecimal limitSumAmt;
    /** 限额累计笔数 */
    private Integer limitSumNum;

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
