package com.dcits.depsit.rule;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.TranBranch;

/**
 * BR004 检查交易机构和基本户开户机构互斥性（断言类）。
 * 规则描述：若交易机构号等于账户开立行行号且账户属性为 11002-一般存款账户，
 * 则执行结果为“不通过”，否则执行结果为“通过”。
 * 执行结果“通过”对应返回 true，“不通过”对应返回 false；只做判定，无副作用。
 */
public class BR004 {

    /** 账户属性判定常量：11002-一般存款账户（Spec 规范常量） */
    private static final AcctNatureNo GENERAL_DEPOSIT_ACCT = AcctNatureNo.VALUE_11002;

    /**
     * 判定交易机构与账户开立行的互斥性。
     *
     * @param tranBranch 交易机构号（必填，代码[内部机构编号]）
     * @param acctNatureNo 账户属性（必填，代码[账户属性]）
     * @param acctBranch 账户开立行行号（必填，代码[内部机构编号]）
     * @return 执行结果：true-通过；false-不通过（机构编号相等且账户属性为 11002-一般存款账户）
     */
    public static boolean execute(TranBranch tranBranch, AcctNatureNo acctNatureNo, TranBranch acctBranch) {
        boolean sameBranch = tranBranch.getValue().equals(acctBranch.getValue());
        if (sameBranch && acctNatureNo == GENERAL_DEPOSIT_ACCT) {
            return false;
        }
        return true;
    }
}
