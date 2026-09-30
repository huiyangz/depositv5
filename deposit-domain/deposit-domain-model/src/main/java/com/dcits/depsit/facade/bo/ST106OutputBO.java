package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST106 检查限制类型 输出BO。
 *
 * 字段定义来自正式 Spec ST106「输出」表：checkResult 必有值，取值域恰为 {"通过", "不通过"}。
 * "限制类型不存在"与"存在但状态非 A-有效"两条路径的最终输出同为"不通过"，输出层面不可区分。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST106OutputBO extends StepResult {

    /** 检查结果，取值"通过"/"不通过" */
    private String checkResult;

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }
}
