package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST077 检查账户用途 输出BO。
 *
 * 正式 Spec ST077「输出」明示本步骤无额外业务输出字段，结果由继承状态字段表达：
 * 检查全部通过或无检查可执行（账户属性未路由到任何子步骤）时 succeed=true、
 * errorCode 与 errorMessage 为 null；任一检查命中错误条件时 succeed=false、
 * errorCode 为 "ER0012"～"ER0016" 之一（errorMessage 内容需求未定义）。
 */
public class ST077OutputBO extends StepResult {
	// 无业务输出字段
}
