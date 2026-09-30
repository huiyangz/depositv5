package com.dcits.depsit.step;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.SettleAcctClass;
import com.dcits.depsit.facade.bo.SettleAcctDTO;
import com.dcits.depsit.facade.bo.ST078InputBO;
import com.dcits.depsit.facade.bo.ST078OutputBO;

/**
 * ST078 检查利息资本化标志。
 *
 * 步骤1 检查利息资本化标志：{利息资本化标志}等于"N-否"时继续执行步骤2，
 * 否则直接返回检查结果"通过"（短路，不执行步骤2，无论结算账户数组内容如何均不产生
 * ER0034）。步骤2 检查利息入账结算账户存在性：上送的结算账户数组中存在{结算账户类型}
 * 为"INT-利息入账账户"（SettleAcctClass.INT）的账户时返回检查结果"通过"，
 * 不存在（含空数组）时返回错误码"ER0034"。
 * 纯内存判定步骤：无库表访问、无外部调用、无写入副作用，无业务失败以外的错误分支。
 */
@Service
public class ST078Pbc implements IST078 {

    /** 利息资本化标志取值：N-否（需求文本常量，无枚举绑定） */
    private static final String INT_CAP_FLAG_NO = "N";

    /** 错误码：结算账户数组中不存在"INT-利息入账账户"（ER0034） */
    private static final String ERROR_CODE_NO_INT_SETTLE_ACCT = "ER0034";

    @Override
    public ST078OutputBO execute(ST078InputBO input) {
        ST078OutputBO output = new ST078OutputBO();

        // 步骤1 检查利息资本化标志：不等于"N-否"时直接返回检查结果"通过"，不执行步骤2
        if (!INT_CAP_FLAG_NO.equals(input.getIntCapFlag())) {
            output.setSucceed(true);
            return output;
        }

        // 步骤2 检查利息入账结算账户存在性：存在{结算账户类型}为"INT-利息入账账户"的账户时通过
        if (hasIntSettleAcct(input.getSettleAcctList())) {
            output.setSucceed(true);
            return output;
        }

        // 步骤2 存在性不满足（含空数组）：返回错误码 ER0034；错误信息需求未定义，不发明
        output.setSucceed(false);
        output.setErrorCode(ERROR_CODE_NO_INT_SETTLE_ACCT);
        return output;
    }

    /**
     * 步骤2 存在性判定：数组中存在任一元素{结算账户类型}等于 SettleAcctClass.INT 即满足，
     * 满足元素数量无约束；空数组（0 个元素）不存在"INT"账户，不满足。
     */
    private boolean hasIntSettleAcct(List<SettleAcctDTO> settleAcctList) {
        for (SettleAcctDTO settleAcct : settleAcctList) {
            if (settleAcct.getSettleAcctClass() == SettleAcctClass.INT) {
                return true;
            }
        }
        return false;
    }
}
