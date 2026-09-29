package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST007InputBO;
import com.dcits.depsit.facade.bo.ST007OutputBO;

/**
 * ST007 检查质押类限制。
 *
 * 按输入{账号}查询限制状态为"生效"的账户限制记录，逐条以记录的账户限制类型查询
 * 限制类型表中"生效"的类型记录并取得质押标志，与限制编号、账户限制类型、限制状态、
 * 状态一并作为步骤输出返回；无生效记录时正常返回且输出字段均为空。本步骤仅查询、
 * 赋值与返回，不含质押标志命中判定，对返回数据的判定与使用由调用方负责。
 * 只读步骤：无数据写入副作用，无事务要求。
 */
public interface IST007 {

    /**
     * 检查质押类限制。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：resSeqNo、restraintType、restraintsStatus、pledgedFlag、status，
     *         无生效记录时均为 null。本步骤无业务失败场景，失败仅由技术异常传播表达。
     */
    ST007OutputBO execute(ST007InputBO input);
}
