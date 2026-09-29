package com.dcits.depsit.facade.bo;

import java.util.Date;

import com.dcits.common.step.StepResult;

/**
 * ST065 设置账户开户日期 步骤输出。
 *
 * 输出契约来源：docs/specs/ST065.md「输出」。本步骤唯一业务路径（成功执行）下
 * acctOpenDate 恒被赋值为[系统日期]（值等于输入 runDate），不存在成功路径下
 * 输出为空的分支；成功标志与错误信息沿用基类 StepResult，不在本类重复声明。
 */
public class ST065OutputBO extends StepResult {

	/** 账户开户日期，赋值自[系统日期]，对应 RB_BUS_ACCT.ACCT_OPEN_DATE（TIMESTAMP），本步骤不写库 */
	private Date acctOpenDate;

	public Date getAcctOpenDate() {
		return acctOpenDate;
	}

	public void setAcctOpenDate(Date acctOpenDate) {
		this.acctOpenDate = acctOpenDate;
	}
}
