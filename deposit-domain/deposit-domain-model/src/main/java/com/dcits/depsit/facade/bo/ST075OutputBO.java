package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;

/**
 * ST075 设置账户执行利率 输出BO。
 *
 * 字段定义来自正式 Spec ST075「输出」表：realRate（执行利率，源文档标记非必填），
 * 实体绑定为对公存款利息明细表（RB_BUS_ACCT_INT_DETAIL）的执行利率字段（REAL_RATE），
 * 仅作输出绑定说明，本步骤不触发该实体的数据库访问。
 * 正常返回时 realRate 恒等于非空输入（原值传递，数值与标度均不变），不存在置空分支。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST075OutputBO extends StepResult {

    /** 执行利率，原值传递（数值与标度均不变），来源：对公存款利息明细表（RB_BUS_ACCT_INT_DETAIL）执行利率 */
    private BigDecimal realRate;

    public BigDecimal getRealRate() {
        return realRate;
    }

    public void setRealRate(BigDecimal realRate) {
        this.realRate = realRate;
    }
}
