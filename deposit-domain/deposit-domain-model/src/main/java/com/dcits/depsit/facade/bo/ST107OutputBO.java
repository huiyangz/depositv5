package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST107 检查是否跨法人 输出BO。
 *
 * 字段定义来自正式 Spec ST107「输出」表：checkResult 恒有值，
 * 仅有"通过"与"不通过"两个取值（需求文本常量，无枚举绑定），无其他输出字段。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST107OutputBO extends StepResult {

    /** 检查结果，取值"通过"/"不通过" */
    private String checkResult;

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }
}
