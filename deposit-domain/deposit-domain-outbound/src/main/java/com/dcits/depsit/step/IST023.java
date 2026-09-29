package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST023InputBO;
import com.dcits.depsit.facade.bo.ST023OutputBO;

/**
 * ST023 检查机构币种交易权限。
 *
 * 按输入{交易机构号}查询【机构币种信息】得到[机构币种列表]，判定{交易币种}是否在
 * 列表范围内。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST023 {

    /**
     * 检查机构币种交易权限。
     *
     * @param input 输入BO，交易机构号（tranBranch）、交易币种（tranCcy）均必填
     * @return 输出BO：{交易币种}在列表范围内时 succeed=true、错误字段为 null、
     *         ccy 为命中记录的币种（等于输入{交易币种}）；否则（列表为空或无记录
     *         币种等于{交易币种}）succeed=false、errorCode="ER0047"、ccy 为 null
     *         （errorMessage 内容需求未定义）。
     */
    ST023OutputBO execute(ST023InputBO input);
}
