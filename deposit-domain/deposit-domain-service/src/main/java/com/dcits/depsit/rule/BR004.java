package com.dcits.depsit.rule;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.TranBranch;

/** BR004 检查交易机构和基本户开户机构互斥性 */
public class BR004 {

    /**
     * 若交易机构号等于账户开立行行号（基本户的开户机构行号，同一内部机构编号枚举实例）
     * 且账户属性为 11002-一般存款账户，则返回执行结果"不通过"（false）；
     * 其余全部合法输入组合返回执行结果"通过"（true）。
     *
     * @param tranBranch 交易机构号（内部机构编号）
     * @param acctNatureNo 账户属性
     * @param acctBranch 账户开立行行号（基本户的开户机构行号）
     * @return true="通过"；false="不通过"
     */
    public static boolean execute(TranBranch tranBranch, AcctNatureNo acctNatureNo, TranBranch acctBranch) {
        return !(tranBranch == acctBranch && acctNatureNo == AcctNatureNo.VALUE_11002);
    }
}
