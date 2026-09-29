package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST108 检查增加限制起始日期 输出BO。
 *
 * 字段定义来自正式 Spec ST108「输出」表。空值语义：checkResult 恒有值（必填），
 * 仅取"通过"/"不通过"两值之一（需求文本常量，无枚举绑定）。
 * 本步骤无业务失败场景：判定为"不通过"仍是正常返回（succeed=true，错误字段为 null），
 * checkResult 为业务执行结果，与 StepResult 的调用成功标志相互独立。
 */
public class ST108OutputBO extends StepResult {

    /** 检查结果，取值"通过"/"不通过"，来源：步骤1判定结果 */
    private String checkResult;

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }
}
