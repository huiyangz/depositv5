package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.OthTranType;

/**
 * ST040 检查现金支取交易权限 输入BO。
 *
 * 字段定义来自正式 Spec ST040「输入」表：tranType（交易类型，必填）。
 * 交易类型缺失、null 或枚举外取值不在本步骤范围内（需求未定义步骤内校验行为），
 * 由调用方契约保证，本 BO 不做校验。
 */
public class ST040InputBO {

    /** 交易类型，作为查询 RB_TRAN_DEF 的条件（该表主键） */
    private OthTranType tranType;

    public OthTranType getTranType() {
        return tranType;
    }

    public void setTranType(OthTranType tranType) {
        this.tranType = tranType;
    }
}
