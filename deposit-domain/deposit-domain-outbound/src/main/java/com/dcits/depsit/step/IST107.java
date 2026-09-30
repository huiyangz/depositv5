package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST107InputBO;
import com.dcits.depsit.facade.bo.ST107OutputBO;

/**
 * ST107 检查是否跨法人。
 *
 * 以输入{账号}查询【账户信息】取管理机构号字段作账户[归属机构号]，再分别查询
 * 【机构信息】取得账户法人与交易机构法人，比较两者是否一致并输出检查结果。
 * 只读检查步骤：无数据写入副作用，无外部服务调用，无事务要求。
 */
public interface IST107 {

    /**
     * 检查是否跨法人。
     *
     * @param input 输入BO，账号（baseAcctNo）与归属机构号（branch）必填
     * @return 输出BO：checkResult 恒有值，账户法人与交易机构法人一致为"通过"，
     *         不一致为"不通过"。本步骤无业务失败场景，成功时错误码与错误信息为 null，
     *         失败仅由技术异常传播表达。
     */
    ST107OutputBO execute(ST107InputBO input);
}
