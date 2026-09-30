package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST081InputBO;
import com.dcits.depsit.facade.bo.ST081OutputBO;

/**
 * ST081 检查账户类型。
 *
 * 以输入{账号}查询【账户信息】（对公存款账户主表 RB_BUS_ACCT）取得存款账户类型，
 * 判定该类型既不是"T-定期账户"也不是"A-AIO账户"。只读检查步骤：无数据写入副作用，
 * 无事务要求。
 */
public interface IST081 {

    /**
     * 检查账户类型。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：判定通过时 succeed=true、errorCode=null；判定不通过
     *         （类型为 T-定期账户或 A-AIO账户）时 succeed=false、errorCode="ER0052"；
     *         两种结果下 rbAcctType 均为查得记录的存款账户类型。查无账户记录时按
     *         业务失败返回：succeed=false、errorCode 留空待补（保持 null）、rbAcctType=null。
     */
    ST081OutputBO execute(ST081InputBO input);
}
