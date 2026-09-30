package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST092InputBO;
import com.dcits.depsit.facade.bo.ST092OutputBO;

/**
 * ST092 登记代办人信息。
 *
 * {代办人证件号码}不为空（非 null 且非空字符串）时，在代办人登记表
 * （RB_COMMISSION_REGISTER）新增一条登记记录（主键为渠道流水号+客户号，
 * 账户内部键值及 14 项业务字段取输入值），并按输出契约返回登记结果信息；
 * {代办人证件号码}为空时不执行登记写入。
 */
public interface IST092 {

	/**
	 * 登记代办人信息。
	 *
	 * 事务要求：本步骤向本地表 RB_COMMISSION_REGISTER 新增记录（写库步骤），
	 * 实现使用 Spring 声明式事务（@Transactional），按 Spring 事务传播语义运行；
	 * 本步骤无业务失败场景，登记写入失败以技术异常向调用方传播，
	 * 不以 succeed=false 返回。
	 *
	 * @param input 输入BO：19 个输入字段的必填性由输入契约约定（上游交易保证），
	 *              本步骤不做运行时必填校验；commissionDocumentId 为登记条件字段
	 * @return 输出BO：发生登记时 14 项登记字段返回本次登记写入的值，
	 *         reference、commissionReason 为空；未发生登记时全部 16 个输出字段
	 *         为空；两个分支均 succeed=true、errorCode/errorMessage 为 null
	 */
	ST092OutputBO execute(ST092InputBO input);
}
