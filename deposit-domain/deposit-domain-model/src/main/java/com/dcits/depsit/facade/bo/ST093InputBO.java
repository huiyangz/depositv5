package com.dcits.depsit.facade.bo;

/**
 * ST093 检查存入账户账户属性 输入BO。
 *
 * 字段定义来自正式 Spec ST093「输入」表：baseAcctNo（账号，必填）。
 * 需求未定义账号缺失或为空时的处理（「失败处理」声明无业务失败场景），
 * 本 BO 不做校验，必填性由上送方保证。
 */
public class ST093InputBO {

    /** 账号，与 RB_BUS_ACCT.BASE_ACCT_NO 等值匹配 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
