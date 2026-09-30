package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;

/**
 * ST083 登记现金交易明细 输入BO。
 *
 * 字段定义来自正式 Spec ST083「输入（步骤入参）」表：tranType（交易类型）、ccy（币种）、
 * crDrInd（借贷标志）、tranAmt（交易金额），4 个输入均为必填、不允许空。
 * 本步骤无业务失败场景，需求未定义输入为空时的失败处理，本 BO 不做校验。
 */
public class ST083InputBO {

    /** 交易类型 */
    private OthTranType tranType;
    /** 币种 */
    private Ccy ccy;
    /** 借贷标志 */
    private CrDrInd crDrInd;
    /** 交易金额 */
    private BigDecimal tranAmt;

    public OthTranType getTranType() {
        return tranType;
    }

    public void setTranType(OthTranType tranType) {
        this.tranType = tranType;
    }

    public Ccy getCcy() {
        return ccy;
    }

    public void setCcy(Ccy ccy) {
        this.ccy = ccy;
    }

    public CrDrInd getCrDrInd() {
        return crDrInd;
    }

    public void setCrDrInd(CrDrInd crDrInd) {
        this.crDrInd = crDrInd;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }
}
