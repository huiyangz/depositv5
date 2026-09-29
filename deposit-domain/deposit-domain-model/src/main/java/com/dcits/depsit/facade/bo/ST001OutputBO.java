package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;

/**
 * ST001 检查客户是否存在限制 步骤输出。
 *
 * 输出契约来源：docs/specs/ST001.md「输出」。三个业务字段均为非必填：
 * 未命中生效记录时步骤正常返回，三字段均为 null，不填充默认值；
 * 成功标志与错误信息沿用基类 StepResult，不在本类重复声明。
 */
public class ST001OutputBO extends StepResult {

	/** 限制编号，来源 RB_CLIENT_RESTRAINTS.RES_SEQ_NO（VARCHAR(50) NOT NULL，主键成员） */
	private String resSeqNo;

	/** 账户限制类型，来源 RB_CLIENT_RESTRAINTS.RESTRAINT_TYPE，按枚举 value 与库存值一致映射 */
	private RestraintType restraintType;

	/** 限制状态，来源 RB_CLIENT_RESTRAINTS.RESTRAINTS_STATUS；筛选条件固定为生效，命中时恒为 A */
	private RestraintsStatus restraintsStatus;

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

	public RestraintsStatus getRestraintsStatus() {
		return restraintsStatus;
	}

	public void setRestraintsStatus(RestraintsStatus restraintsStatus) {
		this.restraintsStatus = restraintsStatus;
	}
}
