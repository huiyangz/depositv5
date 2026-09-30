package com.dcits.depsit.facade.bo;

/**
 * ST086 检查客户类型 输入BO。
 *
 * 字段定义来自正式 Spec ST086「输入」表：clientNo（客户号，必填）。
 * 需求未定义客户号缺失或为空时的处理（必填性由上游上送方保证），本 BO 不做校验。
 */
public class ST086InputBO {

    /** 客户号 */
    private String clientNo;

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }
}
