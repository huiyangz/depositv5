package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST100InputBO;
import com.dcits.depsit.facade.bo.ST100OutputBO;

/**
 * ST100 获取累计限额。
 *
 * 按限额场景编码、客户号、账号（账号作为限额检查对象值 checkObjVal）三个条件
 * 等值查询限额累计信息表（RB_LIMIT_SUM_INFO），返回命中记录的限额累计金额与
 * 限额累计笔数等信息。只读查询步骤：无数据写入副作用，无事务要求。
 */
public interface IST100 {

    /**
     * 获取累计限额。
     *
     * @param input 输入BO：baseAcctNo（账号，必填）、clientNo（客户号，必填）、
     *              limitSceneNo（限额场景编码，必填），必填性由上送方保证
     * @return 输出BO：命中记录时 clientNo、limitSceneNo、limitSumAmt、否 取该记录
     *         对应列存值（可空列存值为 NULL 时输出亦为 null）；未命中时正常返回
     *         且四字段均为 null。本步骤无业务失败场景，失败仅由技术异常传播表达。
     */
    ST100OutputBO execute(ST100InputBO input);
}
