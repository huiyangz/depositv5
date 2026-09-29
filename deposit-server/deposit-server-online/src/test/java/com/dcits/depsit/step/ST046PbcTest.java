package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dcits.client.ExternalTaskClient;
import com.dcits.client.ExternalTaskClient.QueryProductInfoResult;
import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.facade.bo.ST046InputBO;
import com.dcits.depsit.facade.bo.ST046OutputBO;

/**
 * ST046 检查币种 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST046-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST046（inputs 绑定的 ST046.md）。
 * 无业务输出字段，仅以 StepResult 状态断言；失败时 errorMessage 文案需求未约定，不做等值断言。
 *
 * 工程约束：本步骤唯一依赖 {@link ExternalTaskClient} 为具体类（无接口）。测试 JVM 为 JDK 26，
 * 工程继承的 Spring Boot 3.4.3 所管 Mockito 5.15.x / ByteBuddy 1.15.11 的 inline mock maker
 * 无法改造具体类（java.lang.Object 类文件版本 70 超出其支持范围，报 Could not modify all
 * classes，见 outputs/test-results.md 本轮失败记录）。POM 与依赖不可修改，故本测试不使用
 * Mockito @Mock，改为手写 ExternalTaskClient 子类桩（复写非 final 的 queryProductInfo）：
 * 仅当步骤以精确的 prodNo + attrKey 请求时返回既定结果，不命中返回 null——与用例文档
 * 精确值桩未命中的语义一致，步骤传错 prodNo/attrKey 时用例失败，间接核对请求映射；
 * 被测实例经构造器直接装配。
 */
public class ST046PbcTest {

    /** 参数KEY值（用例示例值；实际KEY由产品定义登记决定，需求未规定固定常量） */
    private static final String ATTR_KEY = "CCY";

    /**
     * 构造《查询产品信息》桩：仅当以精确的 prodNo + ATTR_KEY 请求时返回既定结果，
     * 其余入参返回 null（与 Mockito 精确值桩未命中返回默认值的语义一致）
     */
    private static ExternalTaskClient productInfoClient(String prodNo, QueryProductInfoResult result) {
        return new ExternalTaskClient() {
            @Override
            public QueryProductInfoResult queryProductInfo(String calledProdNo, String calledAttrKey) {
                if (prodNo.equals(calledProdNo) && ATTR_KEY.equals(calledAttrKey)) {
                    return result;
                }
                return null;
            }
        };
    }

    /** 构造输入：attrValue 必填但正文未引用，按输入契约传入确定值 */
    private static ST046InputBO input(Ccy ccy, String prodNo, String attrValue) {
        ST046InputBO input = new ST046InputBO();
        input.setCcy(ccy);
        input.setProdNo(prodNo);
        input.setAttrKey(ATTR_KEY);
        input.setAttrValue(attrValue);
        return input;
    }

    /** 构造《查询产品信息》返回结果：仅设置本步骤消费的币种集合 */
    private static QueryProductInfoResult result(List<String> ccyList) {
        QueryProductInfoResult result = new QueryProductInfoResult();
        result.setCcyList(ccyList);
        return result;
    }

    // REQ-001-S01 + REQ-002-S01 以 prodNo="P0001"、attrKey="CCY" 查询取得币种集合 ["CNY","USD"]，
    // ccy=CNY（代码值 "CNY"）在集合内：succeed=true、错误字段均为 null
    @Test
    public void testST046T01() {
        ST046Pbc st046 = new ST046Pbc(productInfoClient("P0001", result(Arrays.asList("CNY", "USD"))));

        ST046OutputBO output = st046.execute(input(Ccy.CNY, "P0001", "CNY,USD"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // REQ-002-S01 合法变体：产品 P0002 币种集合 ["USD","EUR","HKD"]，ccy=HKD 命中集合非首位元素：
    // succeed=true、错误字段均为 null
    @Test
    public void testST046T02() {
        ST046Pbc st046 = new ST046Pbc(productInfoClient("P0002", result(Arrays.asList("USD", "EUR", "HKD"))));

        ST046OutputBO output = st046.execute(input(Ccy.HKD, "P0002", "USD,EUR,HKD"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // REQ-002-S03 边界：《查询产品信息》返回币种集合为空集合，ccy=CNY 不被包含，落入「否则」分支：
    // succeed=false、errorCode="ER0023"（errorMessage 文案需求未约定，不做等值断言）
    @Test
    public void testST046T03() {
        // 该字段默认亦为空列表，显式设置明示意图
        ST046Pbc st046 = new ST046Pbc(productInfoClient("P0001", result(Collections.emptyList())));

        ST046OutputBO output = st046.execute(input(Ccy.CNY, "P0001", "CNY,USD"));

        assertFalse(output.isSucceed());
        assertEquals("ER0023", output.getErrorCode());
    }

    // REQ-002-S02 产品 P0001 币种集合 ["CNY","USD"]，ccy=JPY（代码值 "JPY"）不在集合内：
    // succeed=false、errorCode="ER0023"（errorMessage 文案需求未约定，不做等值断言）
    @Test
    public void testST046T04() {
        ST046Pbc st046 = new ST046Pbc(productInfoClient("P0001", result(Arrays.asList("CNY", "USD"))));

        ST046OutputBO output = st046.execute(input(Ccy.JPY, "P0001", "CNY,USD"));

        assertFalse(output.isSucceed());
        assertEquals("ER0023", output.getErrorCode());
    }
}
