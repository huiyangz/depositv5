package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;

/**
 * ST090 更新存入后账户余额 输出BO。
 *
 * 字段定义来自正式 Spec ST090「输出」表，两字段均为更新后数值，来源：
 * 对公存款账户余额表（RB_BUS_ACCT_BALANCE）。正常路径（查得账户与余额记录
 * 并完成更新）下两字段均有值；本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST090OutputBO extends StepResult {

    /** 汇总金额，更新后=[汇总金额]+{交易金额}（RB_BUS_ACCT_BALANCE.TOTAL_AMOUNT） */
    private BigDecimal totalAmount;

    /** 账户可用余额，更新后=[账户可用余额]+{交易金额}（RB_BUS_ACCT_BALANCE.ACCT_AVAIL_BAL） */
    private BigDecimal acctAvailBal;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAcctAvailBal() {
        return acctAvailBal;
    }

    public void setAcctAvailBal(BigDecimal acctAvailBal) {
        this.acctAvailBal = acctAvailBal;
    }
}
