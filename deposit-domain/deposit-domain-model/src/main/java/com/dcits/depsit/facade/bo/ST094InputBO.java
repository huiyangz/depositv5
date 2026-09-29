package com.dcits.depsit.facade.bo;

/**
 * ST094 检查存入账户通存标志 输入BO。
 *
 * 字段定义来自正式 Spec ST094「输入」表：baseAcctNo（账号，必填）。
 * 需求未定义{账号}缺失或为空时的处理，必填性由上送方保证，本 BO 不做校验。
 */
public class ST094InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
