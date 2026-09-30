package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RbAcctType;
import com.dcits.depsit.facade.bo.ST081InputBO;
import com.dcits.depsit.facade.bo.ST081OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST081 检查账户类型。
 *
 * 步骤1 以输入{账号}为唯一条件等值查询【账户信息】（RB_BUS_ACCT）取得存款账户类型，
 * 查无记录时按业务失败结束（错误码留空待补），不执行步骤2；步骤2 判定该类型既不是
 * "T-定期账户"（RbAcctType.T）也不是"A-AIO账户"（RbAcctType.A）则通过，否则返回
 * 错误码"ER0052"；两种判定结果下均输出步骤1查得的存款账户类型。只读步骤，无事务要求。
 */
@Service
public class ST081Pbc implements IST081 {

    /** 步骤2 判定不通过错误码 */
    private static final String ERROR_CODE_ACCT_TYPE = "ER0052";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Override
    public ST081OutputBO execute(ST081InputBO input) {
        ST081OutputBO output = new ST081OutputBO();

        // 步骤1 获取账户信息：查无记录按业务失败结束，不执行步骤2判定
        RbBusAcctEO acctInfo = findAcctInfo(input.getBaseAcctNo());
        if (acctInfo == null) {
            // 错误码留空待补（不设置，保持 null），errorMessage 用失败描述原文
            output.setErrorMessage("按账号查无账户信息记录");
            return output;
        }

        // 步骤1 取得$账户类型$并作为输出字段输出（无论步骤2判定通过与否）
        RbAcctType acctType = acctInfo.getRbAcctType();
        output.setRbAcctType(acctType);

        // 步骤2 检查账户类型：既不是"T-定期账户"也不是"A-AIO账户"时判定通过
        if (acctType != RbAcctType.T && acctType != RbAcctType.A) {
            output.setSucceed(true);
            return output;
        }
        output.setErrorCode(ERROR_CODE_ACCT_TYPE);
        output.setErrorMessage(ERROR_CODE_ACCT_TYPE + "::存款账户类型为定期账户或AIO账户，检查不通过");
        return output;
    }

    /**
     * 步骤1 获取账户信息：以{账号}为唯一条件等值查询 RB_BUS_ACCT，查询条件不含其他字段。
     * "{账号}唯一对应一条账户信息记录"为已确认数据前提，查询结果至多一条，取返回首条。
     */
    private RbBusAcctEO findAcctInfo(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);
        if (acctList == null || acctList.isEmpty()) {
            return null;
        }
        return acctList.get(0);
    }
}
