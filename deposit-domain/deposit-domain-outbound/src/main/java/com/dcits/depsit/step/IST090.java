package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST090InputBO;
import com.dcits.depsit.facade.bo.ST090OutputBO;

/**
 * ST090 更新存入后账户余额。
 *
 * 对资金入账账户（由输入账号标识）完成存入后的余额更新：按{账号}查询
 * 【对公存款账户主表】取得$账户内部键值$，再以该键值查询【账户余额】取得
 * $汇总金额$与$账户可用余额$基值，最后以"原值+{交易金额}"同步更新该记录的
 * 两个余额字段并返回更新后数值。本步骤含本地数据库更新（RB_BUS_ACCT_BALANCE
 * 单记录两字段），调用方须在事务中执行本方法；事务边界由调用方与运行平台
 * 事务管理决定。本步骤无业务失败场景，技术异常原样传播。
 */
public interface IST090 {

    /**
     * 更新存入后账户余额。
     *
     * @param input 输入BO：baseAcctNo（账号）、tranAmt（交易金额）均必填
     * @return 输出BO：totalAmount、acctAvailBal 为更新后数值。本步骤无业务失败场景，
     *         成功时 errorCode 与 errorMessage 为 null
     */
    ST090OutputBO execute(ST090InputBO input);
}
