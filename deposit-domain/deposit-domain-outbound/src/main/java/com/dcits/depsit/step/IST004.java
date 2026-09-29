package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST004InputBO;
import com.dcits.depsit.facade.bo.ST004OutputBO;

/**
 * ST004 检查转账止收限制。
 *
 * 按输入{账号}查询限制状态为"生效"的账户限制记录，逐条关联限制类型表中"生效"
 * 的限制类型配置，判定是否存在"禁止贷方且不允许转账"的限制并输出转账止收标志。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST004 {

    /**
     * 检查转账止收限制。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：transferStopFlag 恒有值（"是"/"否"）；"是"时携带命中记录的
     *         resSeqNo、restraintType、restraintsStatus、drCrCtlFlag、status、
     *         transferFlag、stopFlag，"否"时该 7 个字段均为 null。本步骤无业务失败场景。
     */
    ST004OutputBO execute(ST004InputBO input);
}
