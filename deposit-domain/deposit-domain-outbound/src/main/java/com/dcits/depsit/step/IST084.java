package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST084InputBO;
import com.dcits.depsit.facade.bo.ST084OutputBO;

/**
 * ST084 检查存入交易类型 步骤接口。
 *
 * 对输入{交易类型}与"现金存入"（OthTranType.VALUE_1000，值"1000"）做相等判定：
 * 相等时以检查结果"通过"成功结束（succeed=true、错误字段为 null）；
 * 不相等时以错误码"ER0049"失败结束（succeed=false、errorCode="ER0049"）。
 * 本步骤为纯内存判定，无数据访问与外部调用，无事务要求。
 */
public interface IST084 {

    /**
     * 执行 ST084 检查存入交易类型。
     *
     * @param input 步骤输入，tranType 必填（必填性由上送方保证）
     * @return 步骤输出：无独立业务输出字段，结果经 StepResult 的 succeed/errorCode 表达
     */
    ST084OutputBO execute(ST084InputBO input);
}
