package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.TranBranch;

/**
 * BR004 检查交易机构和基本户开户机构互斥性 单元测试。
 * 用例来源：outputs/测试用例.md（BR004-TC001 至 TC004），预期结果来自 docs/specs/BR004.md。
 * 覆盖“机构相等与否 × 账户属性是否为 11002”的完整真值表 4 种组合。
 */
class BR004Test {

    // 场景：机构相同且账户属性为一般存款账户，互斥条件成立（REQ-001-S01）；预期返回 false（不通过）
    @Test
    void test_01() {
        TranBranch tranBranch = TranBranch.VALUE_351155; // "351155"-神州数码股份有限公司太原新民中街支行
        AcctNatureNo acctNatureNo = AcctNatureNo.VALUE_11002; // "11002"-一般存款账户
        TranBranch acctBranch = TranBranch.VALUE_351155; // "351155"-与 tranBranch 同一机构编号
        assertFalse(BR004.execute(tranBranch, acctNatureNo, acctBranch));
    }

    // 场景：机构不同且账户属性为一般存款账户，走“否则”分支（REQ-001-S02）；预期返回 true（通过）
    @Test
    void test_02() {
        TranBranch tranBranch = TranBranch.VALUE_351155; // "351155"-神州数码股份有限公司太原新民中街支行
        AcctNatureNo acctNatureNo = AcctNatureNo.VALUE_11002; // "11002"-一般存款账户
        TranBranch acctBranch = TranBranch.VALUE_351156; // "351156"-神州数码股份有限公司太原坞城路支行，机构编号不同
        assertTrue(BR004.execute(tranBranch, acctNatureNo, acctBranch));
    }

    // 场景：机构相同但账户属性非一般存款账户（11001-基本存款账户），否定账户属性子条件（REQ-001-S03）；预期返回 true（通过）
    @Test
    void test_03() {
        TranBranch tranBranch = TranBranch.VALUE_351155; // "351155"-神州数码股份有限公司太原新民中街支行
        AcctNatureNo acctNatureNo = AcctNatureNo.VALUE_11001; // "11001"-基本存款账户（非 11002 的样本值）
        TranBranch acctBranch = TranBranch.VALUE_351155; // "351155"-与 tranBranch 同一机构编号
        assertTrue(BR004.execute(tranBranch, acctNatureNo, acctBranch));
    }

    // 场景：机构不同且账户属性非一般存款账户（11003-临时存款账户），两子条件同时不成立（REQ-001-S04）；预期返回 true（通过）
    @Test
    void test_04() {
        TranBranch tranBranch = TranBranch.VALUE_351155; // "351155"-神州数码股份有限公司太原新民中街支行
        AcctNatureNo acctNatureNo = AcctNatureNo.VALUE_11003; // "11003"-临时存款账户（非 11002 的样本值）
        TranBranch acctBranch = TranBranch.VALUE_351156; // "351156"-神州数码股份有限公司太原坞城路支行，机构编号不同
        assertTrue(BR004.execute(tranBranch, acctNatureNo, acctBranch));
    }
}
