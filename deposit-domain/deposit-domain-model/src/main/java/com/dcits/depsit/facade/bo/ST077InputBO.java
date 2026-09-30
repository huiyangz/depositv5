package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.AcctCcy;
import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.RbBusAcctPurpose;

/**
 * ST077 检查账户用途 输入BO。
 *
 * 字段定义来自正式 Spec ST077「输入」表，四个字段由上游交易/步骤提供
 * （来源实体 RB_BUS_ACCT 仅为数据血缘说明，本步骤不访问库表）。
 * acctCcy 标记必填，由上游保证，需求未定义其缺失时的校验行为，本 BO 不做校验。
 * “为空”均指 null：apprLetterNo 不做空字符串扩展，枚举字段只有 null 一种空值形态。
 */
public class ST077InputBO {

	/** 对公存款账户用途 */
	private RbBusAcctPurpose rbBusAcctPurpose;
	/** 账户币种 */
	private AcctCcy acctCcy;
	/** 核准件编号 */
	private String apprLetterNo;
	/** 账户属性 */
	private AcctNatureNo acctNatureNo;

	public RbBusAcctPurpose getRbBusAcctPurpose() {
		return rbBusAcctPurpose;
	}

	public void setRbBusAcctPurpose(RbBusAcctPurpose rbBusAcctPurpose) {
		this.rbBusAcctPurpose = rbBusAcctPurpose;
	}

	public AcctCcy getAcctCcy() {
		return acctCcy;
	}

	public void setAcctCcy(AcctCcy acctCcy) {
		this.acctCcy = acctCcy;
	}

	public String getApprLetterNo() {
		return apprLetterNo;
	}

	public void setApprLetterNo(String apprLetterNo) {
		this.apprLetterNo = apprLetterNo;
	}

	public AcctNatureNo getAcctNatureNo() {
		return acctNatureNo;
	}

	public void setAcctNatureNo(AcctNatureNo acctNatureNo) {
		this.acctNatureNo = acctNatureNo;
	}
}
