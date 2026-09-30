package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.SettleAcctClass;

/**
 * 结算账户元素DTO（ST078 输入 SettleAcctList 的元素类型）。
 *
 * 字段定义来自正式 Spec ST078「元素DTO契约（SettleAcctDTO）」：为实现本步骤新建的输入DTO，
 * 仅含{结算账户类型}字段（命名与工程既有 RbBusAcctSettleEO.settleAcctClass 一致）。
 * 其余字段需求未要求，本步骤不访问、不约束；元素字段缺失时的处理需求未定义，本 DTO 不做校验。
 */
public class SettleAcctDTO {

    /** 结算账户类型，取值来源于代码[结算账户分类]（SettleAcctClass） */
    private SettleAcctClass settleAcctClass;

    public SettleAcctClass getSettleAcctClass() {
        return settleAcctClass;
    }

    public void setSettleAcctClass(SettleAcctClass settleAcctClass) {
        this.settleAcctClass = settleAcctClass;
    }
}
