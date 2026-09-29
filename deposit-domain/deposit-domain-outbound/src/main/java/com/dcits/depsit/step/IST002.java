package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST002InputBO;
import com.dcits.depsit.facade.bo.ST002OutputBO;

/**
 * ST002 检查限制豁免 步骤接口。
 *
 * 依据正式 Spec ST002：依据渠道类型、账户限制类型、交易类型、摘要码、产品类型，
 * 查询 FM_CHANNEL 与 RB_RESTRAINT_CONTROL_DETAILS，判定该笔交易针对账户限制检查的豁免结果，
 * 返回 checkResult（不检查限制 / 需检查限制 / 豁免 / 不豁免）及匹配明细回填字段。
 *
 * <p>本步骤无业务失败场景（REQ-006）：完成执行的路径均 succeed=true；
 * 数据服务技术异常原样向上传播。仅涉及本地数据查询，无事务要求。</p>
 */
public interface IST002 {

	/**
	 * 执行检查限制豁免步骤。
	 *
	 * @param input 五个输入字段全部必填（空值输入行为由上游保证）
	 * @return checkResult 与 counterFlag 恒有值；七个明细回填字段有匹配时回填、无匹配时为空
	 */
	ST002OutputBO execute(ST002InputBO input);
}
