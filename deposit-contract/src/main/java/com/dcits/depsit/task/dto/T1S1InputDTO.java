package com.dcits.depsit.task.dto;

import jakarta.validation.constraints.NotNull;

/**
 * T1S1 检查客户限制 交易输入。
 *
 * 输入契约来源：docs/specs/T1S1.md「输入」。clientNo 必填，必填性由上送方保证，
 * 缺失时的校验行为需求未定义（见 Spec「明确不覆盖」第 1 条），不另行强化约束。
 */
public class T1S1InputDTO {

	/** 客户号，交易上送；作为步骤 ST001 入参 clientNo 使用 */
	@NotNull
	private String clientNo;

	public String getClientNo() {
		return clientNo;
	}

	public void setClientNo(String clientNo) {
		this.clientNo = clientNo;
	}
}
