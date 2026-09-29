package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST046 检查币种 输出BO。
 *
 * 正式 Spec ST046「输出」：无业务输出字段，步骤结果完全由 StepResult 状态字段表达：
 * 检查通过时 succeed=true、errorCode 与 errorMessage 均为空；币种不在产品币种集合内时
 * succeed=false、errorCode="ER0023"（errorMessage 内容需求未约定）。
 */
public class ST046OutputBO extends StepResult {

}
