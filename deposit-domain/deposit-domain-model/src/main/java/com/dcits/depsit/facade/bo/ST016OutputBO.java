package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST016 检查是否存在现金止付限制 输出BO。
 *
 * 字段定义来自正式 Spec ST016「输出」表。空值语义：cashStopFlag 为非必填，
 * 但"是"/"否"两个分支覆盖全部正常执行结果，步骤正常完成时该字段恒有值且
 * 取值仅为"是"或"否"；无其他输出字段。本步骤无业务失败场景，成功时错误码
 * 与错误信息为 null。
 */
public class ST016OutputBO extends StepResult {

    /** 现金止付标志，取值"是"/"否" */
    private String cashStopFlag;

    public String getCashStopFlag() {
        return cashStopFlag;
    }

    public void setCashStopFlag(String cashStopFlag) {
        this.cashStopFlag = cashStopFlag;
    }
}
