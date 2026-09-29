package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST088 检查账户存在性 输出BO。
 *
 * 字段定义来自正式 Spec ST088「输出」表。空值语义：baseAcctNo 为非必填——
 * 检查通过时取命中记录的 BASE_ACCT_NO（因等值查询恒等于输入{账号}）；
 * 账户不存在（返回 ER0048）时不赋值，为 null。
 * 检查结果"通过"以步骤成功（succeed=true、错误字段 null）表达，
 * 账户不存在时 succeed=false、errorCode="ER0048"。
 */
public class ST088OutputBO extends StepResult {

    /** 账号，来源：对公存款账户主表（RB_BUS_ACCT），检查通过时为命中记录的 BASE_ACCT_NO */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
