package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ST102 登记累计限额 输入BO。
 *
 * 字段定义来自正式 Spec ST102「输入」表（9 个字段，均必填）。必填性由上送方保证，
 * 需求未定义必填输入缺失或为空（null）时的处理（「失败处理」声明无业务失败场景），
 * 本 BO 不做校验、不发明报错行为。
 */
public class ST102InputBO {

	/** 限额检查结果；与需求正文常量"未超限"做 String 等值比较 */
	private String limitCheckResult;
	/** 账号；限额检查对象类型为"账户级别（ACCT）"时作为 $限额检查对象值$ */
	private String baseAcctNo;
	/** 客户号；限额检查对象类型为"客户级别（CUST）"时作为 $限额检查对象值$ */
	private String clientNo;
	/** 交易金额；登记时 $限额累计金额$ 取该值（原样赋值，不计算、不舍入） */
	private BigDecimal tranAmt;
	/** 限额场景编码；查询【限额场景定义】【限额控制配置】的键，同时是登记的 $限额场景编码$ */
	private String limitSceneNo;
	/** 核心运行日期（来源实体：系统日期表 FM_DATE）；$生效日期$ 取该值，$失效日期$ 以该值为基准加周期 */
	private Date runDate;
	/** 限额累计金额（登记前累计值），仅用于登记条件判定（数值上等于 0 即视为等于 0，标度不影响判定） */
	private BigDecimal limitSumAmt;
	/** 限额累计笔数（登记前累计值），仅用于登记条件判定（Integer 等于 0 判定） */
	private Integer limitSumCnt;
	/** 交易参考号；需求正文未将其绑定到登记字段的显式赋值（其余字段按系统规则自动生成） */
	private String reference;

	public String getLimitCheckResult() {
		return limitCheckResult;
	}

	public void setLimitCheckResult(String limitCheckResult) {
		this.limitCheckResult = limitCheckResult;
	}

	public String getBaseAcctNo() {
		return baseAcctNo;
	}

	public void setBaseAcctNo(String baseAcctNo) {
		this.baseAcctNo = baseAcctNo;
	}

	public String getClientNo() {
		return clientNo;
	}

	public void setClientNo(String clientNo) {
		this.clientNo = clientNo;
	}

	public BigDecimal getTranAmt() {
		return tranAmt;
	}

	public void setTranAmt(BigDecimal tranAmt) {
		this.tranAmt = tranAmt;
	}

	public String getLimitSceneNo() {
		return limitSceneNo;
	}

	public void setLimitSceneNo(String limitSceneNo) {
		this.limitSceneNo = limitSceneNo;
	}

	public Date getRunDate() {
		return runDate;
	}

	public void setRunDate(Date runDate) {
		this.runDate = runDate;
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

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
}
