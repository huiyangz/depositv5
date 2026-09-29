package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST084 检查存入交易类型 输出BO。
 *
 * 需求未定义输出字段表（docs/specs/ST084.md「结果表达（步骤输出）」），
 * 本步骤无独立业务输出字段，可观察结果经 StepResult 契约表达：
 * 检查通过时 succeed=true、错误字段为 null；不通过时 succeed=false、errorCode="ER0049"。
 */
public class ST084OutputBO extends StepResult {
}
