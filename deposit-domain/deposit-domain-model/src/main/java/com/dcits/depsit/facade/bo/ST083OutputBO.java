package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;

/**
 * ST083 登记现金交易明细 输出BO。
 *
 * 字段定义来自正式 Spec ST083「输出（步骤出参）」表：tranType、ccy、crDrInd、tranAmt，
 * 来源实体均为对公存款账户金融交易流水表（RB_BUS_TRAN_JNL），取所创建记录的对应字段。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST083OutputBO extends StepResult {

    /** 交易类型，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private OthTranType tranType;
    /** 币种，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private Ccy ccy;
    /** 借贷标志，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private CrDrInd crDrInd;
    /** 交易金额，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
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
