package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.CrDrInd;

/**
 * ST040 检查现金支取交易权限 输出BO。
 *
 * 字段定义来自正式 Spec ST040「输出」表。三个输出字段均取自步骤1查询到的
 * RB_TRAN_DEF 记录，与步骤2判定分支无关（命中与通过两分支均输出查询到的值）；
 * 查无记录时保持空（null）。
 * 本步骤唯一业务失败为命中现金支取判定（errorCode="ER0070"）；检查"通过"时
 * succeed=true 且错误码与错误信息为 null。
 */
public class ST040OutputBO extends StepResult {

    /** 借贷标志，来源：交易类型定义表（RB_TRAN_DEF）；值域 D-借、C-贷 */
    private CrDrInd crDrInd;
    /** 现金交易标志，来源：交易类型定义表（RB_TRAN_DEF）；"Y"=是 */
    private String cashTranFlag;
    /** 冲正交易标志，来源：交易类型定义表（RB_TRAN_DEF）；"N"=否 */
    private String reversal;

    public CrDrInd getCrDrInd() {
        return crDrInd;
    }

    public void setCrDrInd(CrDrInd crDrInd) {
        this.crDrInd = crDrInd;
    }

    public String getCashTranFlag() {
        return cashTranFlag;
    }

    public void setCashTranFlag(String cashTranFlag) {
        this.cashTranFlag = cashTranFlag;
    }

    public String getReversal() {
        return reversal;
    }

    public void setReversal(String reversal) {
        this.reversal = reversal;
    }
}
