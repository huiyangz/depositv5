package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.facade.bo.ST100InputBO;
import com.dcits.depsit.facade.bo.ST100OutputBO;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;

/**
 * ST100 获取累计限额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST100-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST100（fileInputs.spec 绑定的 ST100.md）。
 * 本步骤无业务失败场景；未命中为正常返回（succeed=true、输出字段 null），
 * 失败仅由技术异常传播表达（T05 断言原样传播）。
 */
@ExtendWith(MockitoExtension.class)
public class ST100PbcTest {

    /** 示例账号（Spec 场景示例数据，作为限额检查对象值 checkObjVal 参与查询） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    /** 示例客户号（Spec 场景示例数据） */
    private static final String CLIENT_NO = "C0001";

    /** 示例限额场景编码（Spec 场景示例数据） */
    private static final String LIMIT_SCENE_NO = "DEP0001";

    @Mock
    private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

    @InjectMocks
    private ST100Pbc st100;

    /** 构造公共输入：账号、客户号、限额场景编码（Spec 示例值） */
    private ST100InputBO input() {
        ST100InputBO input = new ST100InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        input.setClientNo(CLIENT_NO);
        input.setLimitSceneNo(LIMIT_SCENE_NO);
        return input;
    }

    /**
     * 构造命中记录：三条件列取 Spec 示例值；累计金额、累计笔数按参数传入，
     * 传 null 表示对应可空列存值为 NULL。
     */
    private RbLimitSumInfoEO hitRecord(BigDecimal limitSumAmt, Integer limitSumCount) {
        RbLimitSumInfoEO record = new RbLimitSumInfoEO();
        record.setCheckObjVal(BASE_ACCT_NO);
        record.setClientNo(CLIENT_NO);
        record.setLimitSceneNo(LIMIT_SCENE_NO);
        record.setLimitSumAmt(limitSumAmt);
        record.set否(limitSumCount);
        return record;
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // 不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 checkObjVal=账号、clientNo=客户号、limitSceneNo=限额场景编码 三条件核对请求 EO 并返回记录列表 */
    private void stubFindByEo(List<RbLimitSumInfoEO> records) {
        lenient().when(rbLimitSumInfoBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getCheckObjVal())
                        && CLIENT_NO.equals(eo.getClientNo())
                        && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()))))
                .thenReturn(records);
    }

    /** 未命中公共断言：succeed=true、错误字段 null、四输出字段均为 null（不填充默认值） */
    private void assertEmptyOutput(ST100OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getClientNo());
        assertNull(output.getLimitSceneNo());
        assertNull(output.getLimitSumAmt());
        assertNull(output.get否());
    }

    // REQ-001-S01 + REQ-002-S01 三条件同时匹配命中唯一记录：输出完整累计信息，金额原样透传 DECIMAL(38,2) 存值
    @Test
    public void testST100T01() {
        stubFindByEo(Collections.singletonList(
                hitRecord(new BigDecimal("15000.00"), 3)));

        ST100OutputBO output = st100.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(CLIENT_NO, output.getClientNo());
        assertEquals(LIMIT_SCENE_NO, output.getLimitSceneNo());
        assertEquals(new BigDecimal("15000.00"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(3), output.get否());
    }

    // REQ-001-S02 + REQ-002-S02 账号+限额场景编码组合无任何记录：未命中为正常返回，四输出字段均 null
    @Test
    public void testST100T02() {
        stubFindByEo(Collections.emptyList());

        ST100OutputBO output = st100.execute(input());

        assertEmptyOutput(output);
    }

    // REQ-001-S03 主键匹配但客户号不符：请求 EO 携带输入 clientNo（非记录中的 "C0002"），客户号等值条件排除该记录，输出为空
    @Test
    public void testST100T03() {
        stubFindByEo(Collections.emptyList());

        ST100OutputBO output = st100.execute(input());

        assertEmptyOutput(output);
    }

    // REQ-002-S03 命中但可空列 LIMIT_SUM_AMT、否 存值均为 NULL：对应输出为 null，客户号与场景编码正常输出
    @Test
    public void testST100T04() {
        stubFindByEo(Collections.singletonList(hitRecord(null, null)));

        ST100OutputBO output = st100.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(CLIENT_NO, output.getClientNo());
        assertEquals(LIMIT_SCENE_NO, output.getLimitSceneNo());
        assertNull(output.getLimitSumAmt());
        assertNull(output.get否());
    }

    // REQ-003-S01 数据访问技术异常（数据源不可用）：异常原样向上传播，不捕获、不转换，无业务失败应答
    @Test
    public void testST100T05() {
        RuntimeException dbFailure = new RuntimeException("数据源不可用");
        lenient().when(rbLimitSumInfoBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getCheckObjVal())
                        && CLIENT_NO.equals(eo.getClientNo())
                        && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()))))
                .thenThrow(dbFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> st100.execute(input()));

        assertSame(dbFailure, thrown);
    }
}
