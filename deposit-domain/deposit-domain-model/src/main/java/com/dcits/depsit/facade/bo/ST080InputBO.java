package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.Ccy;

/**
 * ST080 检查交易币种 输入BO。
 *
 * 字段定义来自正式 Spec ST080「输入」表：baseAcctNo（账号，必填）、tranCcy（交易币种，必填）。
 * 必填性由上送方保证，需求未定义任一输入缺失或为空时的处理，本 BO 不做校验。
 */
public class ST080InputBO {

    /** 账号 */
    private String baseAcctNo;
    /** 交易币种（代码[货币币种]） */
    private Ccy tranCcy;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public Ccy getTranCcy() {
        return tranCcy;
    }

    public void setTranCcy(Ccy tranCcy) {
        this.tranCcy = tranCcy;
    }
}
