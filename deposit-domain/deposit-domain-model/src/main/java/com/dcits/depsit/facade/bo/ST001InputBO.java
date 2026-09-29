package com.dcits.depsit.facade.bo;

/**
 * ST001 检查客户是否存在限制 步骤输入。
 *
 * 输入契约来源：docs/specs/ST001.md「输入」。
 */
public class ST001InputBO {

	/** 客户号，必填，与 RB_CLIENT_RESTRAINTS.CLIENT_NO（VARCHAR(22) NOT NULL）等值匹配 */
	private String clientNo;

	public String getClientNo() {
		return clientNo;
	}

	public void setClientNo(String clientNo) {
		this.clientNo = clientNo;
	}
}
