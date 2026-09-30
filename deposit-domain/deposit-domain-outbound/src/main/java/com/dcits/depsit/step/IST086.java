package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST086InputBO;
import com.dcits.depsit.facade.bo.ST086OutputBO;

/**
 * ST086 检查客户类型。
 *
 * 按输入{客户号}主键查询【客户信息】（客户副本表 FM_CLIENT_COPY）取得客户类型并输出，
 * 随后判定客户类型是否为"公司"（ClientType.VALUE_200）。只读检查步骤：无数据写入副作用，
 * 无外部业务组件调用，无事务要求。
 */
public interface IST086 {

    /**
     * 检查客户类型。
     *
     * @param input 输入BO，客户号（clientNo）必填，必填性由上游上送方保证
     * @return 输出BO：clientType 为步骤1 取得的客户类型（查询无记录时为 null，不填充默认值，
     *         不因判定结果清空或改写）。客户类型为"公司"时 succeed=true、errorCode=null；
     *         不为"公司"（含任一非 VALUE_200 取值及未取得情形）时 succeed=false、errorCode="ER0042"。
     */
    ST086OutputBO execute(ST086InputBO input);
}
