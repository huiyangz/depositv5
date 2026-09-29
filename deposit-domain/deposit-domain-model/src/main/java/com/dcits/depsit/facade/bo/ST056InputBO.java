package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

/**
 * ST056 检查利率浮动类型 输入BO。
 *
 * 字段定义来自正式 Spec ST056「输入」表：账户固定利率（acctFixedRate）、
 * 账户利率浮动百分比（acctPercentRate）、账户利率浮动百分点（acctSpreadRate），
 * 均为 java.math.BigDecimal、非必填。
 * 空值语义："为空"指字段值为 null；任意非 null 的 BigDecimal 数值（含 0）均为"不为空"。
 * 三字段均非必填与判定规则一致：任意空/非空组合均可上送，组合是否合法由本步骤判定，
 * 本步骤不对上送方做前置校验，也不访问任何数据实体。
 */
public class ST056InputBO {

    /** 账户固定利率 */
    private BigDecimal acctFixedRate;

    /** 账户利率浮动百分比 */
    private BigDecimal acctPercentRate;

    /** 账户利率浮动百分点 */
    private BigDecimal acctSpreadRate;

    public BigDecimal getAcctFixedRate() {
        return acctFixedRate;
    }

    public void setAcctFixedRate(BigDecimal acctFixedRate) {
        this.acctFixedRate = acctFixedRate;
    }

    public BigDecimal getAcctPercentRate() {
        return acctPercentRate;
    }

    public void setAcctPercentRate(BigDecimal acctPercentRate) {
        this.acctPercentRate = acctPercentRate;
    }

    public BigDecimal getAcctSpreadRate() {
        return acctSpreadRate;
    }

    public void setAcctSpreadRate(BigDecimal acctSpreadRate) {
        this.acctSpreadRate = acctSpreadRate;
    }
}
