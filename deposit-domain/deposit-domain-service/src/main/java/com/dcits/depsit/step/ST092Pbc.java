package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.facade.bo.ST092InputBO;
import com.dcits.depsit.facade.bo.ST092OutputBO;
import com.dcits.depsit.facade.components.IRbCommissionRegisterBcc;
import com.dcits.depsit.facade.eo.RbCommissionRegisterEO;

/**
 * ST092 登记代办人信息。
 *
 * {代办人证件号码}不为空（非 null 且非空字符串）时，在代办人登记表
 * （RB_COMMISSION_REGISTER）新增一条登记记录：主键（渠道流水号、客户号）与
 * 账户内部键值取输入，14 项业务字段（代办人客户号、代办人名称、代办人证件类型、
 * 代办人证件号码、代办人证件开始日期、代办人证件到期日期、代办人电话、国家、
 * 代办人关系类型、核实结果、核实电话号码、代办核实时间、代办核实员工号1、
 * 代办核实员工号2）逐项取输入值写入，输入空值按原样登记、不补默认值；
 * {代办人证件号码}为空（null 或空字符串）时不执行登记写入。
 * 输出：发生登记时 14 项登记字段返回本次登记写入的值，reference 与
 * commissionReason 不在登记写入范围、输出为空；未发生登记时全部输出字段为空。
 * 本步骤无业务失败场景，登记与不登记分支均成功返回，技术异常向调用方传播。
 */
@Service
public class ST092Pbc implements IST092 {

	/** 代办人登记表数据服务 */
	@Autowired
	private IRbCommissionRegisterBcc rbCommissionRegisterBcc;

	@Override
	@Transactional
	public ST092OutputBO execute(ST092InputBO input) {
		ST092OutputBO output = new ST092OutputBO();

		// 登记条件判定：仅依据{代办人证件号码}是否为空，其他输入字段为空不构成不登记的理由
		if (isPresent(input.getCommissionDocumentId())) {
			// 新增登记记录：主键（渠道流水号、客户号）、账户内部键值及 14 项业务字段取输入值；
			// reference、commissionReason 不在登记写入范围（已接受的需求处理结论），
			// 创建/修改时间戳等技术字段需求未约定取值口径，不补默认值
			RbCommissionRegisterEO registerEo = buildRegisterEo(input);
			rbCommissionRegisterBcc.create(registerEo);
			// 输出返回本次登记写入的值；reference、commissionReason 输出保持空
			fillRegisteredOutput(output, registerEo);
		}

		// 登记与不登记分支均以成功结束；无业务失败场景，技术异常不捕获、向调用方传播
		output.setSucceed(true);
		return output;
	}

	/**
	 * 登记条件判定：{代办人证件号码}不为空指非 null 且非空字符串（长度大于 0），
	 * null 与空字符串均为"为空"。
	 */
	private boolean isPresent(String commissionDocumentId) {
		return commissionDocumentId != null && commissionDocumentId.length() > 0;
	}

	/**
	 * 构造登记记录：主键（渠道流水号、客户号）、账户内部键值及 14 项业务字段
	 * 逐项取输入值，输入空值按原样落入对应记录字段（不转换、不补默认值）。
	 */
	private RbCommissionRegisterEO buildRegisterEo(ST092InputBO input) {
		RbCommissionRegisterEO eo = new RbCommissionRegisterEO();
		// 主键与表必需字段：渠道流水号、客户号、账户内部键值
		eo.setChannelSeqNo(input.getChannelSeqNo());
		eo.setClientNo(input.getClientNo());
		eo.setInternalKey(input.getInternalKey());
		// 14 项业务字段（按登记字段映射表）
		eo.setCommissionClientNo(input.getCommissionClientNo());
		eo.setCommissionClientName(input.getCommissionClientName());
		eo.setCommissionDocumentType(input.getCommissionDocumentType());
		eo.setCommissionDocumentId(input.getCommissionDocumentId());
		eo.setCommissionStartDate(input.getCommissionStartDate());
		eo.setCommissionExpireDate(input.getCommissionExpireDate());
		eo.setCommissionClientTel(input.getCommissionClientTel());
		eo.setCountry(input.getCountry());
		eo.setCommissionRelation(input.getCommissionRelation());
		eo.setCommissionConfirmResult(input.getCommissionConfirmResult());
		eo.setCommissionConfirmTel(input.getCommissionConfirmTel());
		eo.setCommissionConfirmTime(input.getCommissionConfirmTime());
		eo.setCommissionConfirmUserIdKey1(input.getCommissionConfirmUserIdKey1());
		eo.setCommissionConfirmUserIdKey2(input.getCommissionConfirmUserIdKey2());
		return eo;
	}

	/**
	 * 填充输出：14 项登记字段返回本次登记写入的值；reference、commissionReason
	 * 不在登记写入范围，对应输出保持空。
	 */
	private void fillRegisteredOutput(ST092OutputBO output, RbCommissionRegisterEO registerEo) {
		output.setCommissionClientNo(registerEo.getCommissionClientNo());
		output.setCommissionClientName(registerEo.getCommissionClientName());
		output.setCommissionDocumentType(registerEo.getCommissionDocumentType());
		output.setCommissionDocumentId(registerEo.getCommissionDocumentId());
		output.setCommissionStartDate(registerEo.getCommissionStartDate());
		output.setCommissionExpireDate(registerEo.getCommissionExpireDate());
		output.setCommissionClientTel(registerEo.getCommissionClientTel());
		output.setCountry(registerEo.getCountry());
		output.setCommissionRelation(registerEo.getCommissionRelation());
		output.setCommissionConfirmResult(registerEo.getCommissionConfirmResult());
		output.setCommissionConfirmTel(registerEo.getCommissionConfirmTel());
		output.setCommissionConfirmTime(registerEo.getCommissionConfirmTime());
		output.setCommissionConfirmUserIdKey1(registerEo.getCommissionConfirmUserIdKey1());
		output.setCommissionConfirmUserIdKey2(registerEo.getCommissionConfirmUserIdKey2());
	}
}
