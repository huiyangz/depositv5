package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.RbBusAcctPurpose;

/**
 * DT001 根据核准类型设置账户状态 单元测试。
 *
 * 依据正式 Spec（DT001，版本 1）与 outputs/测试用例.md（DT001-TC001～TC020），
 * 覆盖规则描述第 1～6 条全部 20 个验收场景。
 */
public class DT001Test {

    // REQ-001-S01：基本户·境内·企业→新建（规则描述第1条a），预期返回 "N"
    @Test
    public void test_01() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11001, "是", "境内");
        assertEquals("N", result);
    }

    // REQ-001-S02：基本户·境内·非企业→预开户（规则描述第1条b），预期返回 "I"
    @Test
    public void test_02() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11001, "否", "境内");
        assertEquals("I", result);
    }

    // REQ-001-S03：基本户·境外·企业→预开户（规则描述第1条c），预期返回 "I"
    @Test
    public void test_03() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11001, "是", "境外");
        assertEquals("I", result);
    }

    // REQ-001-S04：基本户·境外·非企业→新建（规则描述第1条d），预期返回 "N"
    @Test
    public void test_04() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11001, "否", "境外");
        assertEquals("N", result);
    }

    // REQ-002-S01：一般户→新建（规则描述第2条），结果不随境内境外标志、企业标志变化，预期返回 "N"
    @Test
    public void test_05() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11002, "否", "境内");
        assertEquals("N", result);
    }

    // REQ-003-S01：专用户·预算单位专用存款户·境内·企业→无（规则描述第3.1条a），预期返回 null（不设置账户状态）
    @Test
    public void test_06() {
        String result = DT001.execute(RbBusAcctPurpose.VALUE_4, AcctNatureNo.VALUE_11004, "是", "境内");
        assertNull(result);
    }

    // REQ-003-S02：专用户·预算单位专用存款户·境内·非企业→预开户（规则描述第3.1条b），预期返回 "I"
    @Test
    public void test_07() {
        String result = DT001.execute(RbBusAcctPurpose.VALUE_4, AcctNatureNo.VALUE_11004, "否", "境内");
        assertEquals("I", result);
    }

    // REQ-003-S03：专用户·预算单位专用存款户·境外·企业→无（规则描述第3.1条c），预期返回 null（不设置账户状态）
    @Test
    public void test_08() {
        String result = DT001.execute(RbBusAcctPurpose.VALUE_4, AcctNatureNo.VALUE_11004, "是", "境外");
        assertNull(result);
    }

    // REQ-003-S04：专用户·预算单位专用存款户·境外·非企业→新建（规则描述第3.1条d），预期返回 "N"
    @Test
    public void test_09() {
        String result = DT001.execute(RbBusAcctPurpose.VALUE_4, AcctNatureNo.VALUE_11004, "否", "境外");
        assertEquals("N", result);
    }

    // REQ-004-S01：专用户·用途为预算单位专用存款户以外非空值（VALUE_3）→新建（规则描述第3.2条，不为），预期返回 "N"
    @Test
    public void test_10() {
        String result = DT001.execute(RbBusAcctPurpose.VALUE_3, AcctNatureNo.VALUE_11004, "是", "境内");
        assertEquals("N", result);
    }

    // REQ-004-S02：专用户·用途为空（null，字段非必填）→新建（规则描述第3.2条，为空边界），预期返回 "N"
    @Test
    public void test_11() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11004, "是", "境内");
        assertEquals("N", result);
    }

    // REQ-005-S01：临时户·境内·企业→新建（规则描述第4条a），预期返回 "N"
    @Test
    public void test_12() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11003, "是", "境内");
        assertEquals("N", result);
    }

    // REQ-005-S02：临时户·境内·非企业→预开户（规则描述第4条b），预期返回 "I"
    @Test
    public void test_13() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11003, "否", "境内");
        assertEquals("I", result);
    }

    // REQ-005-S03：临时户·境外·企业→无（规则描述第4条c），预期返回 null（不设置账户状态）
    @Test
    public void test_14() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11003, "是", "境外");
        assertNull(result);
    }

    // REQ-005-S04：临时户·境外·非企业→新建（规则描述第4条d），预期返回 "N"
    @Test
    public void test_15() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11003, "否", "境外");
        assertEquals("N", result);
    }

    // REQ-006-S01：验资户·境内·企业→新建（规则描述第5条a），预期返回 "N"
    @Test
    public void test_16() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_17, "是", "境内");
        assertEquals("N", result);
    }

    // REQ-006-S02：验资户·境内·非企业→新建（规则描述第5条b），预期返回 "N"
    @Test
    public void test_17() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_17, "否", "境内");
        assertEquals("N", result);
    }

    // REQ-006-S03：验资户·境外·企业→无（规则描述第5条c），预期返回 null（不设置账户状态）
    @Test
    public void test_18() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_17, "是", "境外");
        assertNull(result);
    }

    // REQ-006-S04：验资户·境外·非企业→新建（规则描述第5条d），预期返回 "N"
    @Test
    public void test_19() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_17, "否", "境外");
        assertEquals("N", result);
    }

    // REQ-007-S01：账户属性为五类以外枚举值（VALUE_11005 对公人民币定期存款账户）→新建（规则描述第6条，兜底行），预期返回 "N"
    @Test
    public void test_20() {
        String result = DT001.execute(null, AcctNatureNo.VALUE_11005, "是", "境内");
        assertEquals("N", result);
    }
}
