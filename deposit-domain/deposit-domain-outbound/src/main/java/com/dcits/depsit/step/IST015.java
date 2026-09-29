package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST015InputBO;
import com.dcits.depsit.facade.bo.ST015OutputBO;

/**
 * ST015 检查是否存在属性限制。
 *
 * 按输入{账号}查询限制状态为"生效"且限制级别为"账户属性限制"的账户限制记录，
 * 判定该账号是否存在属性限制并输出属性限制标志。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST015 {

    /**
     * 检查是否存在属性限制。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：natureRestraintFlag 两个分支均赋值（"是"/"否"）；"是"时携带
     *         命中记录的 resSeqNo、restraintType、restraintsStatus、restraintLevel
     *         （多条命中任取一条），"否"时该 4 个字段均为 null。本步骤无业务失败场景。
     */
    ST015OutputBO execute(ST015InputBO input);
}
