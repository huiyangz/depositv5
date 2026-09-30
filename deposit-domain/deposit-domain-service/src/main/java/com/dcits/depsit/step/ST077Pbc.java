package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.AcctCcy;
import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.RbBusAcctPurpose;
import com.dcits.depsit.facade.bo.ST077InputBO;
import com.dcits.depsit.facade.bo.ST077OutputBO;

/**
 * ST077 检查账户用途。
 *
 * 步骤1、步骤2 对{币种}=“人民币元”且{账户用途}=“资本项下”的账户依次检查
 * {核准件编号}非空（为空 ER0012）、{账户属性}非空（为空 ER0013）；步骤3 仅按
 * {账户属性}路由至唯一的用途检查子步骤：“基本存款账户”/“一般存款账户”→子步骤4
 * （用途不为空且不为“无特殊用途”命中 ER0014，空用途不触发）、“验资户”→子步骤5
 * （用途须为“注册验资”“增资验资”“无特殊用途”之一，否则 ER0015，空用途同样命中）、
 * “专用存款账户”→子步骤6（用途须为“预算单位专用”“非预算单位专用”之一，否则
 * ER0016，空用途同样命中）；未列出取值（含空）无子步骤可执行，步骤成功。
 * 任一检查命中即以业务失败短路返回；比较按枚举常量进行，null 不等于任一枚举值。
 * 纯内存判定：无数据访问、无外部调用、无状态变更，无事务要求。
 */
@Service
public class ST077Pbc implements IST077 {

	/** 错误码：步骤1 资本项下人民币账户的核准件编号为空 */
	private static final String ER0012 = "ER0012";

	/** 错误码：步骤2 资本项下人民币账户的账户属性为空 */
	private static final String ER0013 = "ER0013";

	/** 错误码：子步骤4 基本存款账户/一般存款账户的账户用途不为空且不为“无特殊用途” */
	private static final String ER0014 = "ER0014";

	/** 错误码：子步骤5 验资户的账户用途不为“注册验资”“增资验资”“无特殊用途”之一 */
	private static final String ER0015 = "ER0015";

	/** 错误码：子步骤6 专用存款账户的账户用途不为“预算单位专用”“非预算单位专用”之一 */
	private static final String ER0016 = "ER0016";

	@Override
	public ST077OutputBO execute(ST077InputBO input) {
		ST077OutputBO output = new ST077OutputBO();

		// 步骤1、步骤2 前置条件：{币种}=“人民币元”且{账户用途}=“资本项下”两项同时成立
		//（前置不成立时两项检查整体跳过，不产生 ER0012/ER0013）
		if (input.getAcctCcy() == AcctCcy.CNY
				&& input.getRbBusAcctPurpose() == RbBusAcctPurpose.VALUE_501) {
			// 步骤1 检查资本项下人民币账户的核准件编号：为空立即返回业务失败，短路步骤2及后续判定
			if (input.getApprLetterNo() == null) {
				return fail(output, ER0012, "资本项下人民币账户的核准件编号为空");
			}
			// 步骤2 检查资本项下人民币账户的账户属性：为空返回业务失败，短路步骤3及后续判定
			if (input.getAcctNatureNo() == null) {
				return fail(output, ER0013, "资本项下人民币账户的账户属性为空");
			}
		}

		// 步骤3 检查账户属性：仅依据{账户属性}路由，三类路由互斥，至多路由一个子步骤
		AcctNatureNo acctNatureNo = input.getAcctNatureNo();
		if (acctNatureNo == AcctNatureNo.VALUE_11001 || acctNatureNo == AcctNatureNo.VALUE_11002) {
			// 步骤3a “基本存款账户”/“一般存款账户”→子步骤4《检查基本户和验资户的账户用途》
			//（子步骤名称沿用需求原文，判定条件以原文为准，不含“验资户”）
			if (isPurposeHitForBasic(input.getRbBusAcctPurpose())) {
				return fail(output, ER0014, "基本存款账户或一般存款账户的账户用途不为空且不为“无特殊用途”");
			}
		} else if (acctNatureNo == AcctNatureNo.VALUE_17) {
			// 步骤3b “验资户”→子步骤5《检查临时户的账户用途》（名称沿用原文，判定条件为“验资户”）
			if (!isPurposeAllowedForContribution(input.getRbBusAcctPurpose())) {
				return fail(output, ER0015, "验资户的账户用途不为“注册验资”“增资验资”“无特殊用途”之一");
			}
		} else if (acctNatureNo == AcctNatureNo.VALUE_11004) {
			// 步骤3c “专用存款账户”→子步骤6《检查专用户的账户用途》
			if (!isPurposeAllowedForSpecial(input.getRbBusAcctPurpose())) {
				return fail(output, ER0016, "专用存款账户的账户用途不为“预算单位专用”“非预算单位专用”之一");
			}
		}
		// {账户属性}为未列出取值或空：无子步骤可路由，不执行任何用途检查、不产生业务失败

		// 全部判定通过或无检查可执行：步骤成功，错误字段保持 null
		output.setSucceed(true);
		return output;
	}

	/**
	 * 子步骤4《检查基本户和验资户的账户用途》判定（路由条件：基本/一般存款账户）：
	 * {账户用途}不为空（null）且不等于“无特殊用途”时命中；用途为空是明确的不触发条件。
	 */
	private boolean isPurposeHitForBasic(RbBusAcctPurpose purpose) {
		return purpose != null && purpose != RbBusAcctPurpose.VALUE_0;
	}

	/**
	 * 子步骤5《检查临时户的账户用途》判定（路由条件：验资户）：
	 * {账户用途}等于“注册验资”“增资验资”“无特殊用途”之一时通过；
	 * 条件无“不为空”限定，空用途不等于任一允许取值，同样命中。
	 */
	private boolean isPurposeAllowedForContribution(RbBusAcctPurpose purpose) {
		return purpose == RbBusAcctPurpose.VALUE_1
				|| purpose == RbBusAcctPurpose.VALUE_2
				|| purpose == RbBusAcctPurpose.VALUE_0;
	}

	/**
	 * 子步骤6《检查专用户的账户用途》判定（路由条件：专用存款账户）：
	 * {账户用途}等于“预算单位专用”“非预算单位专用”之一时通过；
	 * 条件无“不为空”限定，空用途不等于任一允许取值，同样命中。
	 */
	private boolean isPurposeAllowedForSpecial(RbBusAcctPurpose purpose) {
		return purpose == RbBusAcctPurpose.VALUE_4
				|| purpose == RbBusAcctPurpose.VALUE_3;
	}

	/**
	 * 业务失败统一出口：设置失败状态、错误码与“错误码::业务说明”格式的错误信息后返回。
	 * errorMessage 内容需求未定义，仅为确定的业务描述，不构成契约约束。
	 */
	private ST077OutputBO fail(ST077OutputBO output, String errorCode, String businessMessage) {
		output.setSucceed(false);
		output.setErrorCode(errorCode);
		output.setErrorMessage(errorCode + "::" + businessMessage);
		return output;
	}
}
