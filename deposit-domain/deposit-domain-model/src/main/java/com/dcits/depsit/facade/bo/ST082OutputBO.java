package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.enums.OthTranType;

/**
 * ST082 登记交易流水 输出BO。
 *
 * 字段定义来自正式 Spec ST082「输出」表，五个字段来源均为对公存款账户金融
 * 交易流水表（RB_BUS_TRAN_JNL）。空值语义：crDrInd、ccy、tranType、tranAmt
 * 为本次登记字段，登记成功路径下恒有值且与对应输入一致；channelSeqNo 不属于
 * 步骤描述声明的登记内容，取值来源需求未定义（已接受结论放行），本步骤不赋值。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST082OutputBO extends StepResult {

    /** 借贷标志，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private CrDrInd crDrInd;
    /** 币种，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private Ccy ccy;
    /** 交易类型，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private OthTranType tranType;
    /** 交易金额，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL） */
    private BigDecimal tranAmt;
    /** 渠道流水号，来源：对公存款账户金融交易流水表（RB_BUS_TRAN_JNL）；取值来源需求未定义，本步骤不赋值 */
    private String channelSeqNo;

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

    public String getChannelSeqNo() {
        return channelSeqNo;
    }

    public void setChannelSeqNo(String channelSeqNo) {
        this.channelSeqNo = channelSeqNo;
    }
}
