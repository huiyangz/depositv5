package com.dcits.depsit.facade.bo;

import java.util.List;

/**
 * ST078 检查利息资本化标志 输入BO。
 *
 * 字段定义来自正式 Spec ST078「输入」表：intCapFlag（利息资本化标志，必填）、
 * settleAcctList（上送的结算账户数组，必填、DTO、集合）。需求输入表中的 settleAcctClass
 * 行按 Spec 落为数组元素DTO SettleAcctDTO 的字段契约，不设数组外的独立比较输入。
 * 必填性由上送方保证，需求未定义缺失或为 null 时的处理，本 BO 不做校验。
 */
public class ST078InputBO {

    /** 利息资本化标志，步骤1判定输入 */
    private String intCapFlag;

    /** 上送的结算账户数组，步骤2判定输入，元素为 SettleAcctDTO */
    private List<SettleAcctDTO> settleAcctList;

    public String getIntCapFlag() {
        return intCapFlag;
    }

    public void setIntCapFlag(String intCapFlag) {
        this.intCapFlag = intCapFlag;
    }

    public List<SettleAcctDTO> getSettleAcctList() {
        return settleAcctList;
    }

    public void setSettleAcctList(List<SettleAcctDTO> settleAcctList) {
        this.settleAcctList = settleAcctList;
    }
}
