package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST107InputBO;
import com.dcits.depsit.facade.bo.ST107OutputBO;
import com.dcits.depsit.facade.components.IFmBranchBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.FmBranchEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST107 检查是否跨法人。
 *
 * 步骤1 获取账户法人：以输入{账号}查询【账户信息】（RB_BUS_ACCT），取命中记录的
 * 管理机构号字段（homeBranch）作账户[归属机构号]（不取 acctBranch 等其他机构号字段），
 * 再以该机构号查询【机构信息】（FM_BRANCH）取法人（company）作账户法人。
 * 步骤2 获取交易机构法人：以输入{归属机构号}查询【机构信息】取法人作交易机构法人。
 * 步骤3 检查账户法人：两者不一致输出"不通过"，一致输出"通过"。
 * 只读步骤，无业务失败场景，无事务要求；无命中记录等未定义场景由技术异常传播表达。
 */
@Service
public class ST107Pbc implements IST107 {

    /** 检查结果取值：通过 */
    private static final String CHECK_RESULT_PASS = "通过";

    /** 检查结果取值：不通过 */
    private static final String CHECK_RESULT_FAIL = "不通过";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Autowired
    private IFmBranchBcc fmBranchBcc;

    @Override
    public ST107OutputBO execute(ST107InputBO input) {
        ST107OutputBO output = new ST107OutputBO();

        // 步骤1 获取账户法人：账号 → 账户记录管理机构号 → 账户归属机构的法人
        String accountCompany = findAccountCompany(input.getBaseAcctNo());
        // 步骤2 获取交易机构法人：输入归属机构号 → 该机构的法人
        String branchCompany = findCompanyByBranch(input.getBranch());
        // 步骤3 检查账户法人：账户法人与交易机构法人不一致时"不通过"，一致时"通过"
        output.setCheckResult(accountCompany.equals(branchCompany) ? CHECK_RESULT_PASS : CHECK_RESULT_FAIL);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户法人：以{账号}为条件查询 RB_BUS_ACCT，取命中记录的管理机构号字段（homeBranch）
     * 作账户[归属机构号]，再以该机构号查询【机构信息】取法人。
     * 需求按单一账户记录表述，命中记录取首条；无命中记录、命中多条或记录无管理机构号时的
     * 处理需求未定义（Spec「明确不覆盖」），由技术异常传播表达。
     */
    private String findAccountCompany(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);
        RbBusAcctEO acct = acctList.get(0);
        return findCompanyByBranch(acct.getHomeBranch());
    }

    /**
     * 以归属机构号（FM_BRANCH 主键，至多命中一条）查询【机构信息】，取法人字段（company）。
     * 无命中记录时的处理需求未定义（Spec「明确不覆盖」），由技术异常传播表达。
     */
    private String findCompanyByBranch(TranBranch branch) {
        FmBranchEO branchEo = fmBranchBcc.findByBranch(branch);
        return branchEo.getCompany();
    }
}
