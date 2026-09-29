package com.dcits.depsit.facade.bo;

/**
 * ST088 检查账户存在性 输入BO。
 *
 * 字段定义来自正式 Spec ST088「输入」表：baseAcctNo（账号，必填，与
 * RB_BUS_ACCT.BASE_ACCT_NO 等值匹配）。必填性由上送方保证；需求未定义
 * 账号缺失或为空时的处理，本 BO 不做校验。
 */
public class ST088InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
