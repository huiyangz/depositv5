package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST088InputBO;
import com.dcits.depsit.facade.bo.ST088OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST088 检查账户存在性。
 *
 * 步骤1 以输入{账号}为条件等值查询【账户信息】（RB_BUS_ACCT），获取[账户信息]，
 * 查询结果可能为空（0 条记录），空结果不是技术错误，正常进入步骤2判定。
 * 步骤2 检查账户存在性：[账户信息]不存在（查询结果为空）时返回错误码"ER0048"，
 * 不向输出 baseAcctNo 赋值；存在时检查结果为"通过"，将命中记录的账号（等值
 * 查询下恒等于输入{账号}）作为输出返回。只读步骤，仅判定记录存在性，不判定
 * 账户状态等其他字段，无事务要求。
 */
@Service
public class ST088Pbc implements IST088 {

    /** 账户不存在错误码（Spec「步骤结果与错误码（规范常量）」） */
    private static final String ERROR_CODE_ACCT_NOT_EXIST = "ER0048";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Override
    public ST088OutputBO execute(ST088InputBO input) {
        ST088OutputBO output = new ST088OutputBO();

        // 步骤1 获取账户信息：以{账号}为条件等值查询 RB_BUS_ACCT
        List<RbBusAcctEO> acctList = findBusAcct(input.getBaseAcctNo());

        // 步骤2 检查账户存在性：查询结果为空（0 条记录）判为不存在
        if (acctList == null || acctList.isEmpty()) {
            output.setSucceed(false);
            output.setErrorCode(ERROR_CODE_ACCT_NOT_EXIST);
            output.setErrorMessage(ERROR_CODE_ACCT_NOT_EXIST + "::账户信息不存在");
            return output;
        }

        // 步骤2"否则"分支：账户存在，检查结果为"通过"，输出命中记录的账号
        //（BASE_ACCT_NO 非主键但查询为等值匹配，命中记录的 baseAcctNo 恒等于输入{账号}，条数不影响取值）
        output.setBaseAcctNo(acctList.get(0).getBaseAcctNo());
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户信息：以{账号}（BASE_ACCT_NO）为条件等值查询 RB_BUS_ACCT，
     * 可能返回零条或多条记录。
     */
    private List<RbBusAcctEO> findBusAcct(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        return rbBusAcctBcc.findByEo(condition);
    }
}
