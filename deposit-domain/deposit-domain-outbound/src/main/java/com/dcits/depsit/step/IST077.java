package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST077InputBO;
import com.dcits.depsit.facade.bo.ST077OutputBO;

/**
 * ST077 检查账户用途。
 *
 * 以 RB_BUS_ACCT 来源的币种、账户用途、核准件编号、账户属性四项输入为依据：
 * 先对人民币资本项下账户执行核准件编号、账户属性两项前置检查，再按账户属性
 * 路由至对应用途检查子步骤。纯内存判定：无数据访问、无外部调用、无状态变更，
 * 无事务要求。
 */
public interface IST077 {

	/**
	 * 检查账户用途。
	 *
	 * @param input 输入BO，账户币种（acctCcy）必填（由上游保证）
	 * @return 输出BO：无额外业务输出字段；检查全部通过或无检查可执行时
	 *         succeed=true、错误字段为 null；任一检查命中错误条件时
	 *         succeed=false、errorCode 为 "ER0012"～"ER0016" 之一。
	 */
	ST077OutputBO execute(ST077InputBO input);
}
