package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;

/**
 * ST001 检查客户是否存在限制 步骤接口。
 *
 * 按输入客户号查询【客户限制表】（RB_CLIENT_RESTRAINTS），仅选取限制状态为
 * "A-生效"的记录；命中多条时按创建时间戳从新到旧取第一条，将限制编号、
 * 账户限制类型、限制状态赋值到步骤输出返回；未命中时正常返回空输出。
 * 本步骤仅查询本地数据、无业务失败场景，对数据库只读，无事务要求；
 * 技术异常向上传播，由上层统一处理。
 */
public interface IST001 {

	/**
	 * 执行 ST001 检查客户是否存在限制。
	 *
	 * @param input 步骤输入，clientNo 必填（必填性由上送方保证）
	 * @return 步骤输出：命中生效记录时三字段为选中记录值；未命中时三字段均为 null 且 succeed=true
	 */
	ST001OutputBO execute(ST001InputBO input);
}
