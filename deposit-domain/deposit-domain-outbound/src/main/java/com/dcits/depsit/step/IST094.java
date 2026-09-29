package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST094InputBO;
import com.dcits.depsit.facade.bo.ST094OutputBO;

/**
 * ST094 检查存入账户通存标志。
 *
 * 按输入{账号}等值查询【账户信息】（RB_BUS_ACCT）取得$通存标识$并赋值输出 allDepInd，
 * 随后判定该标识是否等于"N001-允许全行存入"：等于时步骤正常结束（继续执行后续步骤，
 * 由交易编排负责）；不等于（含未取得为 null 的全部情形）时返回错误码"ER0055"。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST094 {

    /**
     * 检查存入账户通存标志。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：allDepInd 为步骤1取得的通存标识（未取得为 null，不填充默认值，
     *         取值不因判定结果改变）；等于 AllDepInd.N001 时 succeed=true、errorCode=null，
     *         否则 succeed=false、errorCode="ER0055"。
     */
    ST094OutputBO execute(ST094InputBO input);
}
