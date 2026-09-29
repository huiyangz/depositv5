package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST016InputBO;
import com.dcits.depsit.facade.bo.ST016OutputBO;

/**
 * ST016 检查是否存在现金止付限制。
 *
 * 按输入{账号}查询限制状态为"生效"的账户限制记录，逐条关联限制类型表中"生效"
 * 的限制类型配置，判定是否存在"禁止借方且不允许现金"的限制并输出现金止付标志。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST016 {

    /**
     * 检查是否存在现金止付限制。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：cashStopFlag 恒有值（"是"/"否"）。本步骤无业务失败场景。
     */
    ST016OutputBO execute(ST016InputBO input);
}
