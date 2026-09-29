package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.AcctNatureNo;

/**
 * ST093 检查存入账户账户属性 输出BO。
 *
 * 字段定义来自正式 Spec ST093「输出」表，仅 acctNatureNo 一个字段且非必填：
 * 仅当查得的[账户属性]为"验资户"或"临时存款账户"时携带该枚举值，其余情形为 null。
 * 检查结果"通过"由"acctNatureNo 为 null 且成功返回"表达，无独立检查结果字段。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST093OutputBO extends StepResult {

    /** 账户属性，来源：对公存款账户主表（RB_BUS_ACCT.ACCT_NATURE_NO） */
    private AcctNatureNo acctNatureNo;

    public AcctNatureNo getAcctNatureNo() {
        return acctNatureNo;
    }

    public void setAcctNatureNo(AcctNatureNo acctNatureNo) {
        this.acctNatureNo = acctNatureNo;
    }
}
