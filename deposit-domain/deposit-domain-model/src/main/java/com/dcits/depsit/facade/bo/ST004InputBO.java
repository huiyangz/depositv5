package com.dcits.depsit.facade.bo;

/**
 * ST004 检查转账止收限制 输入BO。
 *
 * 字段定义来自正式 Spec ST004「输入」表：baseAcctNo（账号，必填）。
 * 需求未定义账号缺失或为空时的处理（无业务失败场景），本 BO 不做校验。
 */
public class ST004InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
