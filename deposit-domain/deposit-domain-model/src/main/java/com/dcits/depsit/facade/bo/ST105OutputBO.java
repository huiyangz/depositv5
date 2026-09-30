package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DealFlow;

/**
 * ST105 检查黑名单 输出BO。
 *
 * 字段定义来自正式 Spec ST105「输出」表，业务字段仅 dealFlow（处理方式，即检查结果）。
 * 空值语义：检查结果为"通过"时 dealFlow 为空（null，不赋值）；检查结果为"拒绝"/"授权"/
 * "提醒"时 dealFlow 分别取 DealFlow.B（"B"）/ DealFlow.A（"A"）/ DealFlow.D（"D"）。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST105OutputBO extends StepResult {

    /** 处理方式，即检查结果，来源：名单限制规则参数表（RC_RULE_TYPE） */
    private DealFlow dealFlow;

    public DealFlow getDealFlow() {
        return dealFlow;
    }

    public void setDealFlow(DealFlow dealFlow) {
        this.dealFlow = dealFlow;
    }
}
