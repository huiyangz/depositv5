package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.CrDrInd;

/**
 * ST032 设置借记交易的借贷标志 输出BO。
 *
 * 字段定义来自正式 Spec ST032「输出」表。空值语义：crDrInd 标记为非必填
 * （字段级契约标记），本步骤为无条件赋值，执行成功后 crDrInd 恒为 CrDrInd.D，
 * 不存在输出为空的业务路径。本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST032OutputBO extends StepResult {

    /** 借贷标志（步骤1赋值常量"D-借方"，无来源实体） */
    private CrDrInd crDrInd;

    public CrDrInd getCrDrInd() {
        return crDrInd;
    }

    public void setCrDrInd(CrDrInd crDrInd) {
        this.crDrInd = crDrInd;
    }
}
