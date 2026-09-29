package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

/**
 * ST075 设置账户执行利率 输入BO。
 *
 * 字段定义来自正式 Spec ST075「输入」表：realRate（执行利率，必填）。
 * 必填性为调用方契约，本步骤不做服务端必填或值域校验（Spec 明确不在范围内）。
 */
public class ST075InputBO {

    /** 执行利率（步骤描述中的 [执行利率] 即本字段） */
    private BigDecimal realRate;

    public BigDecimal getRealRate() {
        return realRate;
    }

    public void setRealRate(BigDecimal realRate) {
        this.realRate = realRate;
    }
}
