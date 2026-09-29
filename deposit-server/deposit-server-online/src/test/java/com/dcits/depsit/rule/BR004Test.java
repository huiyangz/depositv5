package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.TranBranch;

/** BR004 检查交易机构和基本户开户机构互斥性 单元测试 */
class BR004Test {

    /** 场景：机构相同且账户属性为一般存款账户（REQ-001-S01），预期返回"不通过”（false） */
    @Test
    void test_01() {
        assertFalse(BR004.execute(TranBranch.VALUE_351155, AcctNatureNo.VALUE_11002, TranBranch.VALUE_351155));
    }

    /** 场景：机构相同但账户属性不为一般存款账户（REQ-001-S02），预期返回“通过”（true） */
    @Test
    void test_02() {
        assertTrue(BR004.execute(TranBranch.VALUE_351155, AcctNatureNo.VALUE_11001, TranBranch.VALUE_351155));
    }

    /** 场景：机构不同且账户属性为一般存款账户（REQ-001-S03），预期返回“通过”（true） */
    @Test
    void test_03() {
        assertTrue(BR004.execute(TranBranch.VALUE_351155, AcctNatureNo.VALUE_11002, TranBranch.VALUE_351156));
    }

    /** 场景：机构不同且账户属性不为一般存款账户（REQ-001-S04），预期返回“通过”（true） */
    @Test
    void test_04() {
        assertTrue(BR004.execute(TranBranch.VALUE_351155, AcctNatureNo.VALUE_11001, TranBranch.VALUE_351156));
    }
}
