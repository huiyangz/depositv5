package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST093InputBO;
import com.dcits.depsit.facade.bo.ST093OutputBO;

/**
 * ST093 检查存入账户账户属性。
 *
 * 按输入{账号}等值查询【账户信息】（RB_BUS_ACCT）取得账户属性，判定其是否为
 * "验资户"或"临时存款账户"：命中时输出账户属性并正常返回（向《检查账户到期日》
 * 的跳转由交易编排依据该输出执行）；否则返回检查结果"通过"，即正常返回且
 * 不输出账户属性。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST093 {

    /**
     * 检查存入账户账户属性。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：查得的账户属性为"验资户"或"临时存款账户"时 acctNatureNo
     *         携带该枚举值，其余情形 acctNatureNo 为 null（检查结果"通过"的表达）。
     *         本步骤无业务失败场景，失败仅由技术异常向上传播。
     */
    ST093OutputBO execute(ST093InputBO input);
}
