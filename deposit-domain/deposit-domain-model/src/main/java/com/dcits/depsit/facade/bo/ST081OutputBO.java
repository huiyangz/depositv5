package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.RbAcctType;

/**
 * ST081 检查账户类型 输出BO。
 *
 * 字段定义来自正式 Spec ST081「输出」表。空值语义：rbAcctType 在查得账户记录时
 * 输出该记录的存款账户类型（无论步骤2判定通过与否），查无记录时为 null。
 * 通过即 succeed=true 且 errorCode=null；判定不通过即 succeed=false 且
 * errorCode="ER0052"；查无记录即 succeed=false 且 errorCode 留空待补（不设置，保持 null）。
 */
public class ST081OutputBO extends StepResult {

    /** 存款账户类型，来源：对公存款账户主表（RB_BUS_ACCT） */
    private RbAcctType rbAcctType;

    public RbAcctType getRbAcctType() {
        return rbAcctType;
    }

    public void setRbAcctType(RbAcctType rbAcctType) {
        this.rbAcctType = rbAcctType;
    }
}
