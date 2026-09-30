package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;

/**
 * ST104 更新累计限额 输出BO。
 *
 * 字段定义来自正式 Spec ST104「输出」表。空值语义：limitSumAmt 非必填，
 * 更新执行时等于更新写入的值（即输入 [限额累计金额]）；未执行更新时为 null，
 * 不填充默认值。本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST104OutputBO extends StepResult {

    /** 限额累计金额，来源：限额累计信息表（RB_LIMIT_SUM_INFO），更新执行时等于写入值，未执行更新时为 null */
    private BigDecimal limitSumAmt;

    public BigDecimal getLimitSumAmt() {
        return limitSumAmt;
    }

    public void setLimitSumAmt(BigDecimal limitSumAmt) {
        this.limitSumAmt = limitSumAmt;
    }
}
