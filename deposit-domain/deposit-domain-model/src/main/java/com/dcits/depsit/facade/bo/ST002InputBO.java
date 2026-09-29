package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.SourceType;

/**
 * ST002 检查限制豁免 输入BO。
 *
 * 依据正式 Spec ST002「输入、输出及依赖契约 → 输入」：五个字段全部必填，取自步骤入参；
 * 空值输入的行为需求未定义，由上游保证，本步骤不增设校验或默认值。
 */
public class ST002InputBO {

	/** 渠道类型 */
	private SourceType sourceType;
	/** 账户限制类型 */
	private RestraintType restraintType;
	/** 交易类型 */
	private OthTranType tranType;
	/** 摘要码 */
	private String narrativeCode;
	/** 产品类型 */
	private String prodNo;

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public RestraintType getRestraintType() {
		return restraintType;
	}

	public void setRestraintType(RestraintType restraintType) {
		this.restraintType = restraintType;
	}

	public OthTranType getTranType() {
		return tranType;
	}

	public void setTranType(OthTranType tranType) {
		this.tranType = tranType;
	}

	public String getNarrativeCode() {
		return narrativeCode;
	}

	public void setNarrativeCode(String narrativeCode) {
		this.narrativeCode = narrativeCode;
	}

	public String getProdNo() {
		return prodNo;
	}

	public void setProdNo(String prodNo) {
		this.prodNo = prodNo;
	}
}
