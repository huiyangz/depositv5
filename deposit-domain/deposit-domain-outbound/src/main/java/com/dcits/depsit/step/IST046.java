package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST046InputBO;
import com.dcits.depsit.facade.bo.ST046OutputBO;

/**
 * ST046 检查币种。
 *
 * 依据产品配置校验账户币种是否在产品允许的币种范围内：先以{产品编号}与{参数KEY值}
 * 调用产品管理《查询产品信息》取得产品币种集合，再判断{币种}是否包含于该集合。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST046 {

    /**
     * 检查币种。
     *
     * @param input 输入BO：ccy（币种）、prodNo（产品编号）、attrKey（参数KEY值）、
     *              attrValue（属性值，必填但正文未引用）均必填
     * @return 输出BO：无业务输出字段；币种在集合内时 succeed=true、错误字段为空，
     *         不在集合内（含集合为空）时 succeed=false、errorCode="ER0023"
     */
    ST046OutputBO execute(ST046InputBO input);
}
