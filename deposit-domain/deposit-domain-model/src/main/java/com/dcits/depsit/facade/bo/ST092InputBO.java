package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.CommissionRelation;
import com.dcits.depsit.enums.IssCountry;
import com.dcits.depsit.enums.ThawDocumentType2;

/**
 * ST092 登记代办人信息 输入BO。
 *
 * 字段定义来自正式 Spec ST092「输入契约」表（19 个字段）。必填性是输入契约，
 * 由上游交易保证；需求未定义缺必填输入时的校验与错误返回，本 BO 不做校验。
 */
public class ST092InputBO {

	/** 交易参考号 */
	private String reference;
	/** 代办人名称 */
	private String commissionClientName;
	/** 代办人客户号 */
	private String commissionClientNo;
	/** 代办人证件号码（登记条件字段） */
	private String commissionDocumentId;
	/** 代办人证件类型 */
	private ThawDocumentType2 commissionDocumentType;
	/** 国家 */
	private IssCountry country;
	/** 代办人证件开始日期 */
	private java.util.Date commissionStartDate;
	/** 代办人证件到期日期 */
	private java.util.Date commissionExpireDate;
	/** 代办人电话 */
	private String commissionClientTel;
	/** 代办原因 */
	private String commissionReason;
	/** 代办人关系类型 */
	private CommissionRelation commissionRelation;
	/** 代办核实员工号1 */
	private String commissionConfirmUserIdKey1;
	/** 代办核实员工号2 */
	private String commissionConfirmUserIdKey2;
	/** 核实电话号码 */
	private String commissionConfirmTel;
	/** 代办核实时间 */
	private String commissionConfirmTime;
	/** 代办人核实结果 */
	private String commissionConfirmResult;
	/** 渠道流水号 */
	private String channelSeqNo;
	/** 客户号 */
	private String clientNo;
	/** 账户内部键值 */
	private Integer internalKey;

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getCommissionClientName() {
		return commissionClientName;
	}

	public void setCommissionClientName(String commissionClientName) {
		this.commissionClientName = commissionClientName;
	}

	public String getCommissionClientNo() {
		return commissionClientNo;
	}

	public void setCommissionClientNo(String commissionClientNo) {
		this.commissionClientNo = commissionClientNo;
	}

	public String getCommissionDocumentId() {
		return commissionDocumentId;
	}

	public void setCommissionDocumentId(String commissionDocumentId) {
		this.commissionDocumentId = commissionDocumentId;
	}

	public ThawDocumentType2 getCommissionDocumentType() {
		return commissionDocumentType;
	}

	public void setCommissionDocumentType(ThawDocumentType2 commissionDocumentType) {
		this.commissionDocumentType = commissionDocumentType;
	}

	public IssCountry getCountry() {
		return country;
	}

	public void setCountry(IssCountry country) {
		this.country = country;
	}

	public java.util.Date getCommissionStartDate() {
		return commissionStartDate;
	}

	public void setCommissionStartDate(java.util.Date commissionStartDate) {
		this.commissionStartDate = commissionStartDate;
	}

	public java.util.Date getCommissionExpireDate() {
		return commissionExpireDate;
	}

	public void setCommissionExpireDate(java.util.Date commissionExpireDate) {
		this.commissionExpireDate = commissionExpireDate;
	}

	public String getCommissionClientTel() {
		return commissionClientTel;
	}

	public void setCommissionClientTel(String commissionClientTel) {
		this.commissionClientTel = commissionClientTel;
	}

	public String getCommissionReason() {
		return commissionReason;
	}

	public void setCommissionReason(String commissionReason) {
		this.commissionReason = commissionReason;
	}

	public CommissionRelation getCommissionRelation() {
		return commissionRelation;
	}

	public void setCommissionRelation(CommissionRelation commissionRelation) {
		this.commissionRelation = commissionRelation;
	}

	public String getCommissionConfirmUserIdKey1() {
		return commissionConfirmUserIdKey1;
	}

	public void setCommissionConfirmUserIdKey1(String commissionConfirmUserIdKey1) {
		this.commissionConfirmUserIdKey1 = commissionConfirmUserIdKey1;
	}

	public String getCommissionConfirmUserIdKey2() {
		return commissionConfirmUserIdKey2;
	}

	public void setCommissionConfirmUserIdKey2(String commissionConfirmUserIdKey2) {
		this.commissionConfirmUserIdKey2 = commissionConfirmUserIdKey2;
	}

	public String getCommissionConfirmTel() {
		return commissionConfirmTel;
	}

	public void setCommissionConfirmTel(String commissionConfirmTel) {
		this.commissionConfirmTel = commissionConfirmTel;
	}

	public String getCommissionConfirmTime() {
		return commissionConfirmTime;
	}

	public void setCommissionConfirmTime(String commissionConfirmTime) {
		this.commissionConfirmTime = commissionConfirmTime;
	}

	public String getCommissionConfirmResult() {
		return commissionConfirmResult;
	}

	public void setCommissionConfirmResult(String commissionConfirmResult) {
		this.commissionConfirmResult = commissionConfirmResult;
	}

	public String getChannelSeqNo() {
		return channelSeqNo;
	}

	public void setChannelSeqNo(String channelSeqNo) {
		this.channelSeqNo = channelSeqNo;
	}

	public String getClientNo() {
		return clientNo;
	}

	public void setClientNo(String clientNo) {
		this.clientNo = clientNo;
	}

	public Integer getInternalKey() {
		return internalKey;
	}

	public void setInternalKey(Integer internalKey) {
		this.internalKey = internalKey;
	}
}
