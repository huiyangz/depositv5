package com.dcits.depsit.facade.bo;

/**
 * ST008 检查是否存在现金止收限制 输入BO。
 *
 * 字段定义来自正式 Spec ST008「输入」表：baseAcctNo（账号，必填）。
 * 必填性由调用方保证；需求未定义账号缺失或空值时的校验行为（验收范围第 1 条），本 BO 不做校验。
 */
public class ST008InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
