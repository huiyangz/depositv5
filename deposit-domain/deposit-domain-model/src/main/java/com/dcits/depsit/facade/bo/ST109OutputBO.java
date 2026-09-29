package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST109 登记账户限制信息 输出BO。
 *
 * 正式 Spec ST109「输出」：需求未定义输出字段，不适用。本步骤的可观察结果为
 * 登记写入的 RbBusRestraintsEO 记录（副作用），无返回值约定，故本 BO 无业务字段，
 * 仅继承 StepResult 状态字段。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST109OutputBO extends StepResult {
}
