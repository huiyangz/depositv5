package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST056InputBO;
import com.dcits.depsit.facade.bo.ST056OutputBO;

/**
 * ST056 检查利率浮动类型。
 *
 * 对输入的账户利率浮动百分点（acctSpreadRate）、账户利率浮动百分比（acctPercentRate）、
 * 账户固定利率（acctFixedRate）做必输性（互斥性）检查：三者中恰有一个不为空时检查通过；
 * 否则以错误码"ER0032"业务失败返回。纯输入判定：无数据访问、无外部服务调用、
 * 无写入副作用，无事务要求。
 */
public interface IST056 {

    /**
     * 检查利率浮动类型。
     *
     * @param input 输入BO：acctSpreadRate、acctPercentRate、acctFixedRate 均非必填，
     *              任意空/非空组合均可上送，组合是否合法由本步骤判定
     * @return 输出BO：三者恰有一个不为空时 succeed=true、错误字段为 null；
     *         不为空字段数量为 0、2 或 3 时 succeed=false、errorCode="ER0032"、
     *         errorMessage 为工程错误码资源 ER0032 既有文案
     */
    ST056OutputBO execute(ST056InputBO input);
}
