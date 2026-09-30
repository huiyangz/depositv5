package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST008InputBO;
import com.dcits.depsit.facade.bo.ST008OutputBO;

/**
 * ST008 检查是否存在现金止收限制。
 *
 * 按输入{账号}查询限制状态为"A-生效"的账户限制记录，逐条关联限制类型表中状态
 * 为"A-生效"的限制类型记录，判定是否存在"禁止贷方且不允许现金"的限制并输出
 * 现金止收标志。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST008 {

    /**
     * 检查是否存在现金止收限制。
     *
     * @param input 输入BO，账号（baseAcctNo）必填，必填性由调用方保证
     * @return 输出BO：cashStopFlag 恒有值（"是"/"否"）；"是"时携带首条命中记录的
     *         resSeqNo、restraintType、restraintsStatus 及其对应生效类型记录的
     *         status、drCrCtlFlag、cashFlag、stopFlag，"否"时该 7 个字段均为 null。
     *         本步骤无业务失败场景，技术异常向调用方原样传播。
     */
    ST008OutputBO execute(ST008InputBO input);
}
