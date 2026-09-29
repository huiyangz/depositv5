package com.dcits.depsit.facade.bo;

/**
 * ST091 检查客户限制 步骤输入。
 *
 * 输入契约来源：docs/specs/ST091.md「输入」。
 */
public class ST091InputBO {

	/** 客户号，必填，作为组件内步骤 ST001 的调用参数（必填性由上送方保证） */
	private String clientNo;

	public String getClientNo() {
		return clientNo;
	}

	public void setClientNo(String clientNo) {
		this.clientNo = clientNo;
	}
}
