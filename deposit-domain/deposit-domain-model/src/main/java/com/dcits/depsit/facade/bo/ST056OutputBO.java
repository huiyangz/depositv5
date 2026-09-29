package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST056 检查利率浮动类型 输出BO。
 *
 * 需求未定义独立输出字段，可观察结果经 StepResult 契约表达：
 * 三者恰有一个不为空（检查"通过"）时 succeed=true、errorCode=null、errorMessage=null；
 * 不为空字段数量为 0、2 或 3 时 succeed=false、errorCode="ER0032"、
 * errorMessage 为工程错误码资源 errorcodes.properties 中 ER0032 既有文案。
 */
public class ST056OutputBO extends StepResult {
}
