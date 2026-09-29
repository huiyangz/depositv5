package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.OthTranType;

/**
 * ST084 检查存入交易类型 步骤输入。
 *
 * 输入契约来源：docs/specs/ST084.md「输入」。
 */
public class ST084InputBO {

    /** 交易类型，必填（必填性由上送方保证，本步骤不发明缺失校验分支） */
    private OthTranType tranType;

    public OthTranType getTranType() {
        return tranType;
    }

    public void setTranType(OthTranType tranType) {
        this.tranType = tranType;
    }
}
