package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.TranBranch;

/**
 * BR004 检查交易机构和基本户开户机构互斥性 单元测试。
 *
 * <p>用例来源：outputs/测试用例.md（BR004-TC001~TC004），预期结果来自 docs/specs/BR004.md。</p>
 */
class BR004Test {

    // REQ-001-S01（BR004-TC001）：交易机构号与账户开立行行号相等（均为 351001-总行）且账户属性为 11002-一般存款账户，预期执行结果“不通过”
    @Test
    void test_01() {
        assertFalse(BR004.execute(TranBranch.VALUE_351001, AcctNatureNo.VALUE_11002, TranBranch.VALUE_351001));
    }

    // REQ-001-S02（BR004-TC002）：账户属性为 11002-一般存款账户，但交易机构号（351001-总行）与账户开立行行号（351155-太原新民中街支行）不相等，预期执行结果“通过”
    @Test
    void test_02() {
        assertTrue(BR004.execute(TranBranch.VALUE_351001, AcctNatureNo.VALUE_11002, TranBranch.VALUE_351155));
    }

    // REQ-001-S03（BR004-TC003）：交易机构号与账户开立行行号相等（均为 351001-总行），但账户属性为 11001-基本存款账户（非 11002 合法取值），预期执行结果“通过”
    @Test
    void test_03() {
        assertTrue(BR004.execute(TranBranch.VALUE_351001, AcctNatureNo.VALUE_11001, TranBranch.VALUE_351001));
    }

    // REQ-001-S04（BR004-TC004）：交易机构号（351155-太原新民中街支行）与账户开立行行号（351001-总行）不相等，且账户属性为 11003-临时存款账户（非 11002 合法取值），预期执行结果“通过”
    @Test
    void test_04() {
        assertTrue(BR004.execute(TranBranch.VALUE_351155, AcctNatureNo.VALUE_11003, TranBranch.VALUE_351001));
    }
}
