package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST056InputBO;
import com.dcits.depsit.facade.bo.ST056OutputBO;

/**
 * ST056 检查利率浮动类型。
 *
 * 检查利率浮动信息必输性（互斥性）：统计{账户利率浮动百分点}（acctSpreadRate）、
 * {账户利率浮动百分比}（acctPercentRate）、{账户固定利率}（acctFixedRate）三者中
 * 不为空（值非 null，数值 0 亦计为不为空）的字段数量；数量等于 1 时检查结果"通过"，
 * 步骤成功返回；数量为 0、2 或 3 时以错误码"ER0032"业务失败返回。
 * 纯输入判定步骤：无数据访问、无外部服务调用、无写入副作用，无事务要求。
 */
@Service
public class ST056Pbc implements IST056 {

    /** 错误码：三者只能上送一个（需求正文明确的规范常量） */
    private static final String ERROR_CODE_EXCLUSIVE = "ER0032";

    /** ER0032 既有文案（工程错误码资源 errorcodes.properties:32，原样消费） */
    private static final String ERROR_MESSAGE_EXCLUSIVE = "账户利率浮动百分点，账户利率浮动百分比，账户固定利率只能上送一个";

    @Override
    public ST056OutputBO execute(ST056InputBO input) {
        ST056OutputBO output = new ST056OutputBO();

        // 检查利率浮动信息必输性：统计三者中不为空（非 null，数值 0 亦计为不为空）的字段数量
        int notNullCount = countNotNull(input);

        // 三者中恰有一个不为空→检查结果"通过"
        if (notNullCount == 1) {
            output.setSucceed(true);
            return output;
        }

        // 否则（0 个、2 个或 3 个不为空）→以错误码"ER0032"业务失败返回
        output.setErrorCode(ERROR_CODE_EXCLUSIVE);
        output.setErrorMessage(ERROR_MESSAGE_EXCLUSIVE);
        return output;
    }

    /**
     * 统计三个利率字段中不为空（值非 null，数值 0 亦计为不为空）的字段数量。
     */
    private int countNotNull(ST056InputBO input) {
        int count = 0;
        if (input.getAcctSpreadRate() != null) {
            count++;
        }
        if (input.getAcctPercentRate() != null) {
            count++;
        }
        if (input.getAcctFixedRate() != null) {
            count++;
        }
        return count;
    }
}
