package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST078 检查利息资本化标志 输出BO。
 *
 * 字段定义来自正式 Spec ST078「输出（步骤结果）」：可观察结果仅"检查结果'通过'"与
 * 错误码"ER0034"两类，均通过 StepResult 三字段表达，无其他业务输出字段
 * （REQ-003：不得设置两类以外结果，不得输出其他业务字段），故本 BO 不声明业务字段。
 * 通过：succeed=true、errorCode=null、errorMessage=null；
 * ER0034：succeed=false、errorCode="ER0034"，errorMessage 需求未定义、不约束。
 */
public class ST078OutputBO extends StepResult {

}
