package com.dcits.depsit.facade.bo;

/**
 * ST101 匹配限额场景 输入BO。
 *
 * 字段定义来自正式 Spec ST101「输入」表：factorName（因子名称，必填）。
 * 需求未定义 factorName 缺失或为空时的处理（见「失败处理」：无业务失败场景），本 BO 不做校验。
 */
public class ST101InputBO {

    /** 因子名称，其值作为【限额规则关系表】主键 ruleId 的查询键 */
    private String factorName;

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }
}
