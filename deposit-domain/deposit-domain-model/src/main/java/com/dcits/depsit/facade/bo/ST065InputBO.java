package com.dcits.depsit.facade.bo;

import java.util.Date;

/**
 * ST065 设置账户开户日期 步骤输入。
 *
 * 输入契约来源：docs/specs/ST065.md「输入」。
 * runDate 必填性由上游调用方保证，需求未定义其为空时的处理，本 BO 不设校验分支。
 */
public class ST065InputBO {

	/** 系统日期，必填，值来源于系统日期表（FM_DATE）核心运行日期，经上游输入上送 */
	private Date runDate;

	public Date getRunDate() {
		return runDate;
	}

	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}
}
