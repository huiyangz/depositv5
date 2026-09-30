package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;

/**
 * ST082 登记交易流水 输入BO。
 *
 * 字段定义来自正式 Spec ST082「输入」表：crDrInd（借贷标志）、ccy（币种）、
 * tranType（交易类型）、tranAmt（交易金额），均必填。需求未定义输入缺失或
 * 非法时的处理（无业务失败场景），本 BO 不做校验。
 */
public class ST082InputBO {

    /** 借贷标志 */
    private CrDrInd crDrInd;
    /** 币种 */
    private Ccy ccy;
    /** 交易类型 */
    private OthTranType tranType;
    /** 交易金额 */
    private BigDecimal tranAmt;

    public CrDrInd getCrDrInd() {
        return crDrInd;
    }

    public void setCrDrInd(CrDrInd crDrInd) {
        this.crDrInd = crDrInd;
    }

    public Ccy getCcy() {
        return ccy;
    }

    public void setCcy(Ccy ccy) {
        this.ccy = ccy;
    }

    public OthTranType getTranType() {
        return tranType;
    }

    public void setTranType(OthTranType tranType) {
        this.tranType = tranType;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }
}
