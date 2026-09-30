package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;
import java.util.Date;

import com.dcits.common.step.StepResult;

/**
 * ST102 登记累计限额 输出BO。
 *
 * 字段定义来自正式 Spec ST102「输出」表（6 个字段，均非必填，来源实体均为
 * 限额累计信息表 RB_LIMIT_SUM_INFO）。空值语义：发生登记时六字段回显本次登记
 * 写入记录的对应取值；登记条件不满足、未发生登记时步骤正常返回，六字段均为
 * null，不填充默认值。本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST102OutputBO extends StepResult {

	/** 限额场景编码，登记时等于输入[限额场景编码] */
	private String limitSceneNo;
	/** 限额检查对象值，登记时按检查对象类型取{账号}或{客户号} */
	private String checkObjVal;
	/** 限额累计金额，登记时等于{交易金额} */
	private BigDecimal limitSumAmt;
	/** 限额累计笔数，登记时恒为 1（对应 RB_LIMIT_SUM_INFO 累计笔数列，该列在 DDL 与实体中名为 `否`，既有契约不改名） */
	private Integer limitSumCnt;
	/** 生效日期，登记时等于{核心运行日期} */
	private Date effectDate;
	/** 失效日期，登记时等于{核心运行日期}＋限额控制配置周期（周期类型×周期值，按日历日期计算） */
	private Date expireDate;

	public String getLimitSceneNo() {
		return limitSceneNo;
	}

	public void setLimitSceneNo(String limitSceneNo) {
		this.limitSceneNo = limitSceneNo;
	}

	public String getCheckObjVal() {
		return checkObjVal;
	}

	public void setCheckObjVal(String checkObjVal) {
		this.checkObjVal = checkObjVal;
	}

	public BigDecimal getLimitSumAmt() {
		return limitSumAmt;
	}

	public void setLimitSumAmt(BigDecimal limitSumAmt) {
		this.limitSumAmt = limitSumAmt;
	}

	public Integer getLimitSumCnt() {
		return limitSumCnt;
	}

	public void setLimitSumCnt(Integer limitSumCnt) {
		this.limitSumCnt = limitSumCnt;
	}

	public Date getEffectDate() {
		return effectDate;
	}

	public void setEffectDate(Date effectDate) {
		this.effectDate = effectDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
}
