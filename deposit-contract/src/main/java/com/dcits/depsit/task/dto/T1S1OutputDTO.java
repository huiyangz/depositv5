package com.dcits.depsit.task.dto;

/**
 * T1S1 检查客户限制 交易输出。
 *
 * 输出契约来源：docs/specs/T1S1.md「输出」。三个字段均为非必填：
 * ST001 未命中生效记录时交易正常返回，三字段均为 null，不填充默认值。
 * 成功标志与错误信息由传入的 RespHeader 承载，不在本 DTO 重复声明。
 */
public class T1S1OutputDTO {

	/** 限制编号，来源步骤 ST001 输出 resSeqNo（RB_CLIENT_RESTRAINTS.RES_SEQ_NO），字符串原样传递 */
	private String resSeqNo;

	/** 账户限制类型，取枚举 value 字符串（与库表存值一致），如 "13" */
	private String restraintType;

	/** 限制状态，取枚举 value 字符串；因 ST001 筛选条件固定为生效，命中时恒为 "A" */
	private String restraintsStatus;

	public String getResSeqNo() {
		return resSeqNo;
	}

	public void setResSeqNo(String resSeqNo) {
		this.resSeqNo = resSeqNo;
	}

	public String getRestraintType() {
		return restraintType;
	}

	public void setRestraintType(String restraintType) {
		this.restraintType = restraintType;
	}

	public String getRestraintsStatus() {
		return restraintsStatus;
	}

	public void setRestraintsStatus(String restraintsStatus) {
		this.restraintsStatus = restraintsStatus;
	}
}
