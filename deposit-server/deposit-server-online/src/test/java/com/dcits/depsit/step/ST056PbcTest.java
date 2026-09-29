package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.facade.bo.ST056InputBO;
import com.dcits.depsit.facade.bo.ST056OutputBO;

/**
 * ST056 检查利率浮动类型 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST056-TC001 ~ TC009），
 * 预期结果来自正式 Spec ST056（fileInputs.spec 绑定的 ST056.md）。
 * 本步骤为纯输入判定，无 BCC、规则、组件客户端等依赖，无需设桩，
 * 直接真实执行 execute；失败断言以 succeed=false + errorCode="ER0032" 为准，
 * errorMessage 为工程资源 errorcodes.properties:32 既有文案，不做等值断言。
 */
@ExtendWith(MockitoExtension.class)
public class ST056PbcTest {

    /** 错误码：三者只能上送一个（Spec 需求正文明确的规范常量） */
    private static final String ERROR_CODE_EXCLUSIVE = "ER0032";

    @InjectMocks
    private ST056Pbc st056;

    // 仅账户利率浮动百分点非空（0.5000），不为空字段数量为 1，检查通过（REQ-001-S01）
    @Test
    public void testST056T01() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctSpreadRate(new BigDecimal("0.5000"));

        ST056OutputBO output = st056.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // 仅账户利率浮动百分比非空（1.25），不为空字段数量为 1，检查通过（REQ-001-S02）
    @Test
    public void testST056T02() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctPercentRate(new BigDecimal("1.25"));

        ST056OutputBO output = st056.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // 仅账户固定利率非空（2.175），不为空字段数量为 1，检查通过（REQ-001-S03）
    @Test
    public void testST056T03() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctFixedRate(new BigDecimal("2.175"));

        ST056OutputBO output = st056.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // 唯一非空字段取值为 0（BigDecimal.ZERO），数值 0 非 null 仍计为不为空，检查通过（REQ-001-S04，零值边界）
    @Test
    public void testST056T04() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctSpreadRate(BigDecimal.ZERO);

        ST056OutputBO output = st056.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // 三者均为空（数量 0），检查不通过，以错误码 ER0032 业务失败返回（REQ-002-S01，全空边界）
    @Test
    public void testST056T05() {
        ST056InputBO input = new ST056InputBO();

        ST056OutputBO output = st056.execute(input);

        assertFalse(output.isSucceed());
        assertEquals(ERROR_CODE_EXCLUSIVE, output.getErrorCode());
    }

    // 百分点与百分比同时非空（数量 2），检查不通过，以错误码 ER0032 业务失败返回（REQ-002-S02）
    @Test
    public void testST056T06() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctSpreadRate(new BigDecimal("0.5000"));
        input.setAcctPercentRate(new BigDecimal("1.25"));

        ST056OutputBO output = st056.execute(input);

        assertFalse(output.isSucceed());
        assertEquals(ERROR_CODE_EXCLUSIVE, output.getErrorCode());
    }

    // 百分点与固定利率同时非空（数量 2），检查不通过，以错误码 ER0032 业务失败返回（REQ-002-S03）
    @Test
    public void testST056T07() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctSpreadRate(new BigDecimal("0.5000"));
        input.setAcctFixedRate(new BigDecimal("2.175"));

        ST056OutputBO output = st056.execute(input);

        assertFalse(output.isSucceed());
        assertEquals(ERROR_CODE_EXCLUSIVE, output.getErrorCode());
    }

    // 百分比与固定利率同时非空（数量 2），检查不通过，以错误码 ER0032 业务失败返回（REQ-002-S04）
    @Test
    public void testST056T08() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctPercentRate(new BigDecimal("1.25"));
        input.setAcctFixedRate(new BigDecimal("2.175"));

        ST056OutputBO output = st056.execute(input);

        assertFalse(output.isSucceed());
        assertEquals(ERROR_CODE_EXCLUSIVE, output.getErrorCode());
    }

    // 三者均非空（数量 3），检查不通过，以错误码 ER0032 业务失败返回（REQ-002-S05）
    @Test
    public void testST056T09() {
        ST056InputBO input = new ST056InputBO();
        input.setAcctSpreadRate(new BigDecimal("0.5000"));
        input.setAcctPercentRate(new BigDecimal("1.25"));
        input.setAcctFixedRate(new BigDecimal("2.175"));

        ST056OutputBO output = st056.execute(input);

        assertFalse(output.isSucceed());
        assertEquals(ERROR_CODE_EXCLUSIVE, output.getErrorCode());
    }
}
