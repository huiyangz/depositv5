package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST088InputBO;
import com.dcits.depsit.facade.bo.ST088OutputBO;

/**
 * ST088 检查账户存在性。
 *
 * 根据输入{账号}等值查询【账户信息】（RB_BUS_ACCT）并判定账户存在性：
 * 不存在时返回错误码"ER0048"；存在时检查结果为"通过"，输出命中记录的账号。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST088 {

    /**
     * 检查账户存在性。
     *
     * @param input 输入BO，账号（baseAcctNo）必填，必填性由上送方保证
     * @return 输出BO：账户存在时 succeed=true、错误字段 null、baseAcctNo 为命中
     *         记录的账号（等值查询下恒等于输入{账号}）；账户不存在时 succeed=false、
     *         errorCode="ER0048"、baseAcctNo 为 null。
     */
    ST088OutputBO execute(ST088InputBO input);
}
