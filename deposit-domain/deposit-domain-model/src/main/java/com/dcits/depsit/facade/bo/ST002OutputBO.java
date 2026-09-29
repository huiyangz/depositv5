package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.ResBranchRange;
import com.dcits.depsit.enums.Status;

/**
 * ST002 检查限制豁免 输出BO。
 *
 * 依据正式 Spec ST002「输入、输出及依赖契约 → 输出」：counterFlag 与 checkResult 在步骤成功执行时恒有值；
 * 来源为 RB_RESTRAINT_CONTROL_DETAILS 的七个字段取决于是否存在同时匹配的明细（有匹配回填、无匹配为空）。
 */
public class ST002OutputBO extends StepResult {

	/** 渠道柜面标志：FM_CHANNEL 中 sourceType 对应记录的柜面标志；无对应记录时为 "N"；取值 "Y"/"N" 为规范常量 */
	private String counterFlag;
	/** 匹配明细的状态；因查询限状态 = Status.A，有匹配时恒为 Status.A，无匹配时为空 */
	private Status status;
	/** 匹配明细的产品编号（与输入 prodNo 相等）；无匹配时为空 */
	private String prodNo;
	/** 匹配明细的多交易类型；无匹配时为空 */
	private String tranTypeLink;
	/** 匹配明细的渠道集合；无匹配时为空。仅回填，不参与匹配判定 */
	private String channelMuster;
	/** 匹配明细的摘要码（与输入 narrativeCode 相等）；无匹配时为空 */
	private String narrativeCode;
	/** 匹配明细的限制机构范围；无匹配时为空 */
	private ResBranchRange resBranchRange;
	/** 匹配明细的柜面标志（实体字段 counterFlag，输出名 detailCounterFlag 以区别渠道柜面标志）；无匹配时为空 */
	private String detailCounterFlag;
	/** 检查结果，取值 ∈ {不检查限制, 需检查限制, 豁免, 不豁免}，为规范常量；步骤成功执行时恒有值 */
	private String checkResult;

	public String getCounterFlag() {
		return counterFlag;
	}

	public void setCounterFlag(String counterFlag) {
		this.counterFlag = counterFlag;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getProdNo() {
		return prodNo;
	}

	public void setProdNo(String prodNo) {
		this.prodNo = prodNo;
	}

	public String getTranTypeLink() {
		return tranTypeLink;
	}

	public void setTranTypeLink(String tranTypeLink) {
		this.tranTypeLink = tranTypeLink;
	}

	public String getChannelMuster() {
		return channelMuster;
	}

	public void setChannelMuster(String channelMuster) {
		this.channelMuster = channelMuster;
	}

	public String getNarrativeCode() {
		return narrativeCode;
	}

	public void setNarrativeCode(String narrativeCode) {
		this.narrativeCode = narrativeCode;
	}

	public ResBranchRange getResBranchRange() {
		return resBranchRange;
	}

	public void setResBranchRange(ResBranchRange resBranchRange) {
		this.resBranchRange = resBranchRange;
	}

	public String getDetailCounterFlag() {
		return detailCounterFlag;
	}

	public void setDetailCounterFlag(String detailCounterFlag) {
		this.detailCounterFlag = detailCounterFlag;
	}

	public String getCheckResult() {
		return checkResult;
	}

	public void setCheckResult(String checkResult) {
		this.checkResult = checkResult;
	}
}
