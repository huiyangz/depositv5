package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST010InputBO;
import com.dcits.depsit.facade.bo.ST010OutputBO;

/**
 * ST010 检查是否存在转账不收不付限制。
 *
 * 按输入{账号}查询限制状态为"生效"的账户限制记录，逐条关联限制类型表中"生效"
 * 的限制类型配置，判定是否存在"禁止借贷方且禁止转账"的限制并输出转账不收不付标志。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST010 {

    /**
     * 检查是否存在转账不收不付限制。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：transferNoRecvNoPayFlag 每次成功执行均返回（"是"/"否"）；
     *         "是"时携带第一条命中记录的 resSeqNo、restraintType、restraintsStatus
     *         及其对应生效限制类型记录的 drCrCtlFlag、status、transferFlag、stopFlag；
     *         "否"时该 7 个字段需求未规定取值。本步骤无业务失败场景，查询发生的
     *         技术异常向调用方原样传播。
     */
    ST010OutputBO execute(ST010InputBO input);
}
