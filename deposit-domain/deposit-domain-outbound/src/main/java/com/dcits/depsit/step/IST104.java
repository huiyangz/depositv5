package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST104InputBO;
import com.dcits.depsit.facade.bo.ST104OutputBO;

/**
 * ST104 更新累计限额。
 *
 * 依据上游限额检查结果与累计数据，条件性地按主键（限额检查对象值、限额场景编码）
 * 更新【限额累计信息表】（RB_LIMIT_SUM_INFO）的限额累计金额。触发条件不满足时不执行
 * 任何更新，输出 limitSumAmt 为 null。
 */
public interface IST104 {

    /**
     * 更新累计限额。
     *
     * @param input 输入BO，6 个字段全部必填（必填性由上送方保证，本步骤不做校验）
     * @return 输出BO：触发条件满足并执行更新时 limitSumAmt 等于写入值（输入的限额累计金额），
     *         未执行更新时为 null；全部业务情形均成功（succeed=true），无业务失败场景。
     *         本方法含本地数据库按主键更新（写），实现已启用 Spring 事务（@Transactional），
     *         调用方如需跨步骤事务请纳入自身事务边界；返回 succeed=true 不作为回滚依据。
     */
    ST104OutputBO execute(ST104InputBO input);
}
