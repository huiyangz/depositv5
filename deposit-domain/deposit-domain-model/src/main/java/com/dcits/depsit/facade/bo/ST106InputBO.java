package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.RestraintType;

/**
 * ST106 检查限制类型 输入BO。
 *
 * 字段定义来自正式 Spec ST106「输入」表：restraintType（账户限制类型，必填）。
 * 必填性由上游交易输入保证；需求未定义 restraintType 为空时的处理，本 BO 不做校验。
 */
public class ST106InputBO {

    /** 账户限制类型，作为查询【存款限制类型表】的主键条件 */
    private RestraintType restraintType;

    public RestraintType getRestraintType() {
        return restraintType;
    }

    public void setRestraintType(RestraintType restraintType) {
        this.restraintType = restraintType;
    }
}
