package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST106InputBO;
import com.dcits.depsit.facade.bo.ST106OutputBO;

/**
 * ST106 检查限制类型。
 *
 * 按输入{限制类型}主键查询【存款限制类型表】获取[限制类型信息]，先检查限制类型是否存在
 * （不存在则检查结果"不通过"并结束检查），再检查$状态$是否为"A-有效"，据此输出检查结果
 * checkResult。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST106 {

    /**
     * 检查限制类型。
     *
     * @param input 输入BO，账户限制类型（restraintType）必填
     * @return 输出BO：checkResult 恒有值（"通过"/"不通过"）。本步骤无业务失败场景，
     *         检查结果为"不通过"时仍为步骤成功（succeed=true）；技术异常按原样传播。
     */
    ST106OutputBO execute(ST106InputBO input);
}
