package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.AcctCcy;

/**
 * ST080 检查交易币种 输出BO。
 *
 * 字段定义来自正式 Spec ST080「输出」表。空值语义：不存在正常返回且 acctCcy 为 null 的
 * 业务路径——数据异常（含账户币种为空）按技术异常抛出、不生成步骤结果；"通过"与"ER0051"
 * 两条业务路径下 acctCcy 均取已取得的账户币种，赋值不随检查结果变化。
 */
public class ST080OutputBO extends StepResult {

    /** 账户币种，来源：对公存款账户主表（RB_BUS_ACCT）ACCT_CCY */
    private AcctCcy acctCcy;

    public AcctCcy getAcctCcy() {
        return acctCcy;
    }

    public void setAcctCcy(AcctCcy acctCcy) {
        this.acctCcy = acctCcy;
    }
}
