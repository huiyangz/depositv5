package com.dcits.depsit.rule;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.TranBranch;

/**
 * BR004 检查交易机构和基本户开户机构互斥性（断言类）。
 *
 * <p>依据 docs/specs/BR004.md：若交易机构号等于账户开立行行号（按内部机构编号取值相等）
 * 且账户属性为 11002-一般存款账户，则执行结果为“不通过”；否则执行结果为“通过”。</p>
 */
public class BR004 {

    private BR004() {
    }

    /**
     * 互斥性断言判定。
     *
     * @param tranBranch   交易机构号，取值来源于代码[内部机构编号]
     * @param acctNatureNo 账户属性，取值来源于代码[账户属性]
     * @param acctBranch   账户开立行行号，取值来源于代码[内部机构编号]
     * @return true 表示执行结果为“通过”；false 表示执行结果为“不通过”
     */
    public static boolean execute(TranBranch tranBranch, AcctNatureNo acctNatureNo, TranBranch acctBranch) {
        boolean branchEqual = tranBranch.getValue().equals(acctBranch.getValue());
        boolean generalDepositAcct = AcctNatureNo.VALUE_11002.getValue().equals(acctNatureNo.getValue());
        return !(branchEqual && generalDepositAcct);
    }
}
