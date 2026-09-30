package com.dcits.depsit.facade.bo;

/**
 * ST081 检查账户类型 输入BO。
 *
 * 字段定义来自正式 Spec ST081「输入」表：baseAcctNo（账号，必填）。
 * 必填为调用方契约：需求未定义账号缺失或为空时的处理，本 BO 不做校验。
 */
public class ST081InputBO {

    /** 账号，步骤1查询【账户信息】的唯一匹配条件（等值匹配，无其他筛选条件） */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
