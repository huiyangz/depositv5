package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.facade.bo.ST075InputBO;
import com.dcits.depsit.facade.bo.ST075OutputBO;

/**
 * ST075 设置账户执行利率 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST075-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST075（inputs 绑定的 ST075.md）。
 * 本步骤无依赖调用（不调用组件接口、规则或其他步骤），不设 Mockito 桩，
 * 直接实例化真实执行；无业务失败场景，全部用例 succeed=true。
 * 断言使用 BigDecimal.equals（同时比较数值与标度），不使用忽略标度的 compareTo。
 */
public class ST075PbcTest {

    private final ST075Pbc st075 = new ST075Pbc();

    // REQ-001-S01 正常赋值：输入 0.017（正数代表值，标度 3），输出数值与标度完全一致，输入对象不被修改
    @Test
    public void testST075T01() {
        ST075InputBO input = new ST075InputBO();
        input.setRealRate(new BigDecimal("0.017"));

        ST075OutputBO output = st075.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal("0.017"), output.getRealRate());
        assertEquals(new BigDecimal("0.017"), input.getRealRate());
    }

    // REQ-001-S02 无分支的值传递：输入 -0.002（负数代表值，标度 3），不因符号分支、过滤或改写，原值传递
    @Test
    public void testST075T02() {
        ST075InputBO input = new ST075InputBO();
        input.setRealRate(new BigDecimal("-0.002"));

        ST075OutputBO output = st075.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal("-0.002"), output.getRealRate());
    }

    // 零值原值传递：输入 0.000（零值，标度 3），不因取值为零分支或归一化，零值与标度原样传递
    @Test
    public void testST075T03() {
        ST075InputBO input = new ST075InputBO();
        input.setRealRate(new BigDecimal("0.000"));

        ST075OutputBO output = st075.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal("0.000"), output.getRealRate());
    }

    // 标度保持：输入 0.0350（标度 4，含尾随零），数值与标度均不变，尾随零保留
    @Test
    public void testST075T04() {
        ST075InputBO input = new ST075InputBO();
        input.setRealRate(new BigDecimal("0.0350"));

        ST075OutputBO output = st075.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal("0.0350"), output.getRealRate());
    }
}
