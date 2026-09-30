package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST080InputBO;
import com.dcits.depsit.facade.bo.ST080OutputBO;

/**
 * ST080 检查交易币种。
 *
 * 按输入{账号}查询【账户信息】取得$账户币种$，与{交易币种}按币种代码比较：一致时检查结果
 * 为"通过"，不一致时返回错误码"ER0051"（业务失败）；两条业务路径下账户币种均赋值到输出 acctCcy。
 * 查无记录、多条记录或账户币种为空属数据异常，按技术异常向调用方传播，不生成步骤结果。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST080 {

    /**
     * 检查交易币种。
     *
     * @param input 输入BO，账号（baseAcctNo）、交易币种（tranCcy）必填
     * @return 输出BO：币种一致时 succeed=true、错误字段为 null；不一致时 succeed=false、
     *         errorCode="ER0051"；acctCcy 恒为已取得的账户币种
     * @throws com.dcits.common.exception.TransException 查无记录、多条记录或账户币种为空的
     *         数据异常，按技术异常向调用方传播，不生成步骤结果
     */
    ST080OutputBO execute(ST080InputBO input);
}
