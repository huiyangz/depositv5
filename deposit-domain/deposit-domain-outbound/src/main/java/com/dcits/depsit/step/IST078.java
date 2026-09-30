package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST078InputBO;
import com.dcits.depsit.facade.bo.ST078OutputBO;

/**
 * ST078 检查利息资本化标志。
 *
 * 按上送的{利息资本化标志}与结算账户数组执行两步检查：步骤1判定利息资本化标志，
 * 不等于"N-否"时短路返回检查结果"通过"；步骤2判定结算账户数组中是否存在
 * {结算账户类型}为"INT-利息入账账户"的账户。纯内存判定步骤：无库表访问、
 * 无外部服务调用、无数据写入副作用，无事务要求。
 */
public interface IST078 {

    /**
     * 检查利息资本化标志。
     *
     * @param input 输入BO：intCapFlag（利息资本化标志）、settleAcctList（结算账户数组，
     *              元素DTO含 settleAcctClass 字段）均必填，必填性由上送方保证
     * @return 输出BO：intCapFlag 不等于"N"时直接"通过"（不执行步骤2，不产生 ER0034）；
     *         等于"N"时数组存在 settleAcctClass=SettleAcctClass.INT 的账户则"通过"，
     *         不存在（含空数组）则 succeed=false、errorCode="ER0034"
     *         （errorMessage 需求未定义，不约束）。无其他业务输出字段。
     */
    ST078OutputBO execute(ST078InputBO input);
}
