package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.OthTranType;
import com.dcits.depsit.facade.bo.ST084InputBO;
import com.dcits.depsit.facade.bo.ST084OutputBO;

/**
 * ST084 检查存入交易类型 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST084-TC001 ~ TC003），
 * 预期结果来自正式 Spec ST084（inputs 绑定的 ST084.md，REQ-001 全部 3 个验收场景）。
 * 纯内存判定，无 BCC/客户端/规则依赖，无需设桩；
 * errorMessage 需求未定义，Spec 明确不纳入断言。
 */
@ExtendWith(MockitoExtension.class)
public class ST084PbcTest {

    @InjectMocks
    private ST084Pbc st084;

    /** 构造公共输入：交易类型 */
    private ST084InputBO input(OthTranType tranType) {
        ST084InputBO input = new ST084InputBO();
        input.setTranType(tranType);
        return input;
    }

    // REQ-001-S01 交易类型等于判定基准 VALUE_1000（"现金存入"，值"1000"）：检查通过，succeed=true 且错误字段为 null
    @Test
    public void testST084T01() {
        ST084OutputBO output = st084.execute(input(OthTranType.VALUE_1000));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // REQ-001-S02 交易类型为其他业务类型 VALUE_1003（"现金支取"，值"1003"）：不等于"现金存入"，返回 ER0049
    @Test
    public void testST084T02() {
        ST084OutputBO output = st084.execute(input(OthTranType.VALUE_1003));

        assertFalse(output.isSucceed());
        assertEquals("ER0049", output.getErrorCode());
    }

    // REQ-001-S03 交易类型名称含"现金存入"字样但不为判定基准 VALUE_1001（"现金存入-冲销"，值"1001"）：精确相等判定不成立，返回 ER0049
    @Test
    public void testST084T03() {
        ST084OutputBO output = st084.execute(input(OthTranType.VALUE_1001));

        assertFalse(output.isSucceed());
        assertEquals("ER0049", output.getErrorCode());
    }
}
