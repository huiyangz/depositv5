package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST102InputBO;
import com.dcits.depsit.facade.bo.ST102OutputBO;

/**
 * ST102 登记累计限额。
 *
 * 当[限额检查结果]为"未超限"，且（[限额累计金额]等于 0 或[限额累计笔数]等于 0）时，
 * 向【限额累计信息表】（RB_LIMIT_SUM_INFO）新增一条限额累计记录（仅新增，不更新、
 * 不删除已有记录）：限额场景编码取输入[限额场景编码]；限额检查对象值依据按
 * [限额场景编码]查询【限额场景定义】（RbLimitSceneDef）取得的限额检查对象类型
 * 赋值为{账号}（账户级别 ACCT）或{客户号}（客户级别 CUST）；限额累计金额取
 * {交易金额}；限额累计笔数为 1；生效日期取{核心运行日期}；失效日期为{核心运行日期}
 * 加按[限额场景编码]查询【限额控制配置】（RbLimitCtrlConf）取得的周期类型与周期值
 * 所确定的周期。条件不满足时不登记，步骤正常返回且输出为空。
 */
public interface IST102 {

	/**
	 * 登记累计限额。
	 *
	 * 事务要求：本步骤向本地表 RB_LIMIT_SUM_INFO 新增记录（写库步骤），
	 * 实现使用 Spring 声明式事务（@Transactional），按 Spring 事务传播语义运行；
	 * 本步骤无业务失败场景（含条件不满足不登记的情形），失败仅由技术异常
	 * 向调用方传播，不以 succeed=false 返回。
	 *
	 * @param input 输入BO：9 个输入字段的必填性由输入契约约定（上送方保证），
	 *              本步骤不做运行时必填校验；limitCheckResult、limitSumAmt、
	 *              limitSumCnt 为登记条件判定字段
	 * @return 输出BO：发生登记时 limitSceneNo、checkObjVal、limitSumAmt、
	 *         limitSumCnt、effectDate、expireDate 六字段回显本次登记写入的取值；
	 *         未发生登记时六字段均为 null、不填充默认值；两个分支均 succeed=true、
	 *         errorCode/errorMessage 为 null
	 */
	ST102OutputBO execute(ST102InputBO input);
}
