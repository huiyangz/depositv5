package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.common.exception.TransException;
import com.dcits.depsit.enums.AcctCcy;
import com.dcits.depsit.facade.bo.ST080InputBO;
import com.dcits.depsit.facade.bo.ST080OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST080 检查交易币种。
 *
 * 步骤1 以输入{账号}等值查询【账户信息】（RB_BUS_ACCT）：结果恰为 1 条且[账户币种]非空时
 * 取得$账户币种$；查无记录、多条记录或账户币种为空均属数据异常，按技术异常抛出向调用方传播，
 * 不继续步骤2、不生成步骤结果、不设置业务错误码。
 * 步骤2 将取得的账户币种与{交易币种}按币种代码（枚举 value）比较：不相等返回业务失败
 * （succeed=false、errorCode="ER0051"），相等时检查结果"通过"（succeed=true、错误字段为 null）；
 * 两条业务路径下账户币种均赋值到输出 acctCcy。只读步骤，无事务要求。
 */
@Service
public class ST080Pbc implements IST080 {

    /** 币种不一致错误码（Spec 规范常量） */
    private static final String ERROR_CODE_CCY_MISMATCH = "ER0051";

    /** 业务说明：账户币种与交易币种不一致 */
    private static final String MSG_CCY_MISMATCH = "ER0051::账户币种与交易币种不一致";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Override
    public ST080OutputBO execute(ST080InputBO input) {
        ST080OutputBO output = new ST080OutputBO();

        // 步骤1 获取账户币种：数据异常（查无记录、多条记录、币种为空）按技术异常处理，不继续比较
        AcctCcy acctCcy = findAcctCcy(input.getBaseAcctNo());

        // 两条业务路径下账户币种均赋值到输出（赋值不随检查结果变化）
        output.setAcctCcy(acctCcy);

        // 步骤2 检查币种一致性：按币种代码（枚举 value）比较
        if (!acctCcy.getValue().equals(input.getTranCcy().getValue())) {
            output.setSucceed(false);
            output.setErrorCode(ERROR_CODE_CCY_MISMATCH);
            output.setErrorMessage(MSG_CCY_MISMATCH);
            return output;
        }

        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户币种：以{账号}等值查询 RB_BUS_ACCT，结果须恰为 1 条且账户币种非空；
     * 否则属数据异常，抛技术异常向调用方传播（不设业务错误码）。
     */
    private AcctCcy findAcctCcy(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);

        int recordCount = acctList == null ? 0 : acctList.size();
        if (recordCount != 1) {
            throw new TransException(null,
                    "账号[" + baseAcctNo + "]查询对公存款账户主表记录数不为1：" + recordCount);
        }

        AcctCcy acctCcy = acctList.get(0).getAcctCcy();
        if (acctCcy == null) {
            throw new TransException(null, "账号[" + baseAcctNo + "]对公存款账户主表记录账户币种为空");
        }
        return acctCcy;
    }
}
