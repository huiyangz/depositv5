package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

/**
 * ST090 更新存入后账户余额 输入BO。
 *
 * 字段定义来自正式 Spec ST090「输入」表：baseAcctNo（账号）、tranAmt（交易金额）均必填。
 * 必填性由上游交易输入保证；需求未定义缺失或为空时的校验行为，本 BO 不做校验。
 */
public class ST090InputBO {

    /** 账号，与 RB_BUS_ACCT.BASE_ACCT_NO 等值匹配，定位资金入账账户 */
    private String baseAcctNo;

    /** 交易金额，子步骤 3 两个余额字段的加计值 */
    private BigDecimal tranAmt;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }
}
