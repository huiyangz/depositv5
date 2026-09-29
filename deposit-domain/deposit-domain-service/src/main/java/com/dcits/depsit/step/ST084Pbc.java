package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.facade.bo.ST084InputBO;
import com.dcits.depsit.facade.bo.ST084OutputBO;

/**
 * ST084 检查存入交易类型 步骤实现。
 *
 * 业务定义来源：docs/specs/ST084.md（REQ-001 存入交易类型检查）。
 * 在活期现金存入交易的执行编排中，对上送的{交易类型}与"现金存入"
 * （OthTranType.VALUE_1000，值"1000"）做相等判定：不相等时步骤以
 * [错误码]"ER0049"失败结束；相等时以检查结果"通过"成功结束。
 * 相等判定仅对枚举常量 VALUE_1000 成立，其他名称含"现金存入"字样的
 * 常量（如 VALUE_1001"现金存入-冲销"）均命中失败路径（精确相等语义）。
 * 纯内存判定：无数据访问、无外部调用、无副作用；errorMessage 需求未定义，
 * 仅按工程约定携带业务说明，不作契约约束。
 */
@Service
public class ST084Pbc implements IST084 {

    /** 错误码：交易类型不等于"现金存入"（需求文本常量，工程内暂无错误码表登记） */
    private static final String ERROR_CODE_TRAN_TYPE_MISMATCH = "ER0049";

    /** 失败路径业务说明（Spec 未定义错误信息文案，内容为失败条件描述，不作断言依据） */
    private static final String ERROR_MESSAGE_TRAN_TYPE_MISMATCH = "ER0049::交易类型不等于现金存入";

    @Override
    public ST084OutputBO execute(ST084InputBO input) {
        ST084OutputBO output = new ST084OutputBO();

        // 步骤描述 第1条 检查交易类型：{交易类型}不等于"现金存入"（OthTranType.VALUE_1000）时，以[错误码]"ER0049"失败结束
        if (input.getTranType() != OthTranType.VALUE_1000) {
            output.setErrorCode(ERROR_CODE_TRAN_TYPE_MISMATCH);
            output.setErrorMessage(ERROR_MESSAGE_TRAN_TYPE_MISMATCH);
            return output;
        }

        // 步骤描述 第1条 检查交易类型：相等时返回检查结果"通过"，成功结束（无独立业务输出字段）
        output.setSucceed(true);
        return output;
    }
}
