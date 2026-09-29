package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;

/**
 * ST091 检查客户限制 步骤输出。
 *
 * 输出契约来源：docs/specs/ST091.md「输出」。三个业务字段均为非必填，
 * 来源为客户限制表（RB_CLIENT_RESTRAINTS）经组件内步骤 ST001 返回透传：
 * 未命中生效限制时步骤正常返回，三字段均为 null，不填充默认值
 * （"检查结果为通过"由成功返回且输出字段均为空表达）；
 * 成功标志与错误信息沿用基类 StepResult，不在本类重复声明。
 */
public class ST091OutputBO extends StepResult {

	/** 限制状态，经 ST001 返回透传，枚举常量原样传递不做取值转换 */
	private RestraintsStatus restraintsStatus;

	/** 限制编号，经 ST001 返回透传 */
	private String resSeqNo;

	/** 账户限制类型，经 ST001 返回透传，枚举常量原样传递不做取值转换 */
	private RestraintType restraintType;

	public RestraintsStatus getRestraintsStatus() {
		return restraintsStatus;
	}

	public void setRestraintsStatus(RestraintsStatus restraintsStatus) {
		this.restraintsStatus = restraintsStatus;
	}

	public String getResSeqNo() {
		return resSeqNo;
	}

	public void setResSeqNo(String resSeqNo) {
		this.resSeqNo = resSeqNo;
	}

	public RestraintType getRestraintType() {
		return restraintType;
	}

	public void setRestraintType(RestraintType restraintType) {
		this.restraintType = restraintType;
	}
}
