package com.dcits.depsit.facade.bo;

/**
 * ST096 检查账户机构是否可匹配到限额场景配置 输入BO。
 *
 * 字段定义来自正式 Spec ST096「输入」表：baseAcctNo（账号，必填）。
 * 需求未定义账号缺失、为空或在【账户信息】无记录时的处理（无业务失败场景，
 * Spec「明确不覆盖」），本 BO 不做校验。
 */
public class ST096InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
