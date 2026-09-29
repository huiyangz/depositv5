package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST018InputBO;
import com.dcits.depsit.facade.bo.ST018OutputBO;

/**
 * ST018 检查交易机构。
 *
 * 按输入{账号}查询【账户信息】取得账户开立行行号与通存标志（通存标志仅随本步骤
 * 读取，不判定、不输出），比较{交易机构}与[账户开立行行号]：一致返回检查结果
 * "通过"；不一致跳转步骤《检查存入账户通存标志》（跳转信号由返回结果的
 * gotoStepName 承载，非业务失败）。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST018 {

    /**
     * 检查交易机构。
     *
     * @param input 输入BO，账号（baseAcctNo）、交易机构号（tranBranch）必填
     * @return 输出BO：acctBranch 取查询命中记录的账户开立行行号（不受分支结论影响）；
     *         交易机构与账户开立行行号一致时检查结果"通过"（gotoStepName 为 null），
     *         不一致时 gotoStepName="检查存入账户通存标志"（succeed 仍为 true，
     *         跳转非业务失败）。本步骤无业务失败场景，失败仅由技术异常向上传播表达。
     */
    ST018OutputBO execute(ST018InputBO input);
}
