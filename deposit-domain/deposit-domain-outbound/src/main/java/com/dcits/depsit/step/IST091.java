package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST091InputBO;
import com.dcits.depsit.facade.bo.ST091OutputBO;

/**
 * ST091 检查客户限制 步骤接口。
 *
 * 以输入客户号调用本组件内步骤 ST001「检查客户是否存在限制」执行客户限制检查，
 * 获取[客户限制信息]；该信息为空（未命中生效限制，三字段均为空）时检查结果为"通过"，
 * 步骤成功返回且输出字段均为空；否则将限制编号、账户限制类型、限制状态透传到
 * 输出后正常返回，由调用方按输出字段处理限制。本步骤无业务失败场景、无本地数据库
 * 写入，无事务要求；技术异常向上传播，由上层统一处理。
 */
public interface IST091 {

	/**
	 * 执行 ST091 检查客户限制。
	 *
	 * @param input 步骤输入，clientNo 必填（必填性由上送方保证）
	 * @return 步骤输出：未命中生效限制时三字段均为 null 且 succeed=true；
	 *         命中时三字段为 ST001 返回值透传（枚举常量原样传递）
	 */
	ST091OutputBO execute(ST091InputBO input);
}
