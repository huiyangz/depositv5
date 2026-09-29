package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.facade.bo.ST101InputBO;
import com.dcits.depsit.facade.bo.ST101OutputBO;
import com.dcits.depsit.facade.components.IRbLimitRuleRelationBcc;
import com.dcits.depsit.facade.components.IRbLimitSceneDefBcc;
import com.dcits.depsit.facade.eo.RbLimitRuleRelationEO;
import com.dcits.depsit.facade.eo.RbLimitSceneDefEO;

/**
 * ST101 匹配限额场景 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST101-TC001 ~ TC006），
 * 预期结果来自正式 Spec ST101（inputs 绑定的 ST101.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"未匹配到限额场景"是业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST101PbcTest {

    /** 示例因子名称/规则编号、表达式、限额场景编码（Spec 场景示例数据） */
    private static final String RULE_ID_R0001 = "R0001";
    private static final String RULE_ID_R0002 = "R0002";
    private static final String RULE_ID_R9999 = "R9999";
    private static final String EXPR_01 = "EXPR_DEPOSIT_01";
    private static final String EXPR_02 = "EXPR_DEPOSIT_02";

    /** 启用标志"Y-启用"（无枚举绑定，java.lang.String 常量） */
    private static final String VALID_FLAG_Y = "Y";

    @Mock
    private IRbLimitRuleRelationBcc rbLimitRuleRelationBcc;

    @Mock
    private IRbLimitSceneDefBcc rbLimitSceneDefBcc;

    @InjectMocks
    private ST101Pbc st101;

    /** 构造限额规则关系记录 */
    private RbLimitRuleRelationEO relation(String ruleId, String ruleRelationExpr, String limitSceneNo) {
        RbLimitRuleRelationEO eo = new RbLimitRuleRelationEO();
        eo.setRuleId(ruleId);
        eo.setRuleRelationExpr(ruleRelationExpr);
        eo.setLimitSceneNo(limitSceneNo);
        return eo;
    }

    /** 构造限额场景定义记录 */
    private RbLimitSceneDefEO sceneDef(String limitSceneNo, String validFlag) {
        RbLimitSceneDefEO eo = new RbLimitSceneDefEO();
        eo.setLimitSceneNo(limitSceneNo);
        eo.setValidFlag(validFlag);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤2 桩：按 ruleRelationExpr=表达式 等值条件核对请求并返回关系记录列表 */
    private void stubRelationsByExpr(String ruleRelationExpr, List<RbLimitRuleRelationEO> records) {
        lenient().when(rbLimitRuleRelationBcc.findByEo(argThat(eo ->
                eo != null && ruleRelationExpr.equals(eo.getRuleRelationExpr()))))
                .thenReturn(records);
    }

    /** 步骤3 桩：按 limitSceneNo=编码 且 validFlag="Y" 组合条件核对请求并返回配置数据列表 */
    private void stubSceneDef(String limitSceneNo, List<RbLimitSceneDefEO> records) {
        lenient().when(rbLimitSceneDefBcc.findByEo(argThat(eo ->
                eo != null && limitSceneNo.equals(eo.getLimitSceneNo()) && VALID_FLAG_Y.equals(eo.getValidFlag()))))
                .thenReturn(records);
    }

    /** 构造公共输入：因子名称 */
    private ST101InputBO input(String factorName) {
        ST101InputBO input = new ST101InputBO();
        input.setFactorName(factorName);
        return input;
    }

    /** "已匹配"路径公共断言：succeed=true、错误字段 null、四字段取命中值 */
    private void assertMatched(ST101OutputBO output, String ruleRelationExpr, String limitSceneNo) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("已匹配到限额场景", output.getCheckResult());
        assertEquals(ruleRelationExpr, output.getRuleRelationExpr());
        assertEquals(limitSceneNo, output.getLimitSceneNo());
        assertEquals(VALID_FLAG_Y, output.getValidFlag());
    }

    // REQ-001-S01+REQ-002-S02+REQ-003-S01 因子记录存在取得表达式，表达式仅命中因子自身记录（单元素编码列表），该编码启用，命中返回已匹配
    @Test
    public void testST101T01() {
        lenient().when(rbLimitRuleRelationBcc.findByPrimaryKey(RULE_ID_R0002))
                .thenReturn(relation(RULE_ID_R0002, EXPR_02, "LSC001"));
        stubRelationsByExpr(EXPR_02, Collections.singletonList(
                relation(RULE_ID_R0002, EXPR_02, "LSC001")));
        stubSceneDef("LSC001", Collections.singletonList(sceneDef("LSC001", VALID_FLAG_Y)));

        ST101OutputBO output = st101.execute(input(RULE_ID_R0002));

        assertMatched(output, EXPR_02, "LSC001");
    }

    // REQ-002-S01+REQ-003-S02+REQ-004-S01 表达式命中 3 条关系记录，未启用编码不命中，继续遍历命中启用编码，全字段断言
    @Test
    public void testST101T02() {
        lenient().when(rbLimitRuleRelationBcc.findByPrimaryKey(RULE_ID_R0001))
                .thenReturn(relation(RULE_ID_R0001, EXPR_01, "LSC001"));
        stubRelationsByExpr(EXPR_01, Arrays.asList(
                relation(RULE_ID_R0001, EXPR_01, "LSC001"),
                relation(RULE_ID_R0002, EXPR_01, "LSC002"),
                relation("R0003", EXPR_01, "LSC003")));
        // LSC001/LSC003 存在 validFlag="N" 记录，组合条件无命中（是否被查询取决于遍历顺序，lenient）
        stubSceneDef("LSC001", Collections.emptyList());
        stubSceneDef("LSC003", Collections.emptyList());
        stubSceneDef("LSC002", Collections.singletonList(sceneDef("LSC002", VALID_FLAG_Y)));

        ST101OutputBO output = st101.execute(input(RULE_ID_R0001));

        assertMatched(output, EXPR_01, "LSC002");
    }

    // REQ-003-S03 编码 LSC009 在定义表无记录，[配置数据]为空不命中，继续遍历命中同列表另一启用编码 LSC002
    @Test
    public void testST101T03() {
        lenient().when(rbLimitRuleRelationBcc.findByPrimaryKey(RULE_ID_R0001))
                .thenReturn(relation(RULE_ID_R0001, EXPR_01, "LSC009"));
        stubRelationsByExpr(EXPR_01, Arrays.asList(
                relation(RULE_ID_R0001, EXPR_01, "LSC009"),
                relation("R0004", EXPR_01, "LSC002")));
        stubSceneDef("LSC009", Collections.emptyList());
        stubSceneDef("LSC002", Collections.singletonList(sceneDef("LSC002", VALID_FLAG_Y)));

        ST101OutputBO output = st101.execute(input(RULE_ID_R0001));

        assertMatched(output, EXPR_01, "LSC002");
    }

    // REQ-003-S04+REQ-004-S03 两个编码均有启用配置，遍历在首个处理到的编码处中断，命中其一且字段一致；断言不依赖遍历顺序
    @Test
    public void testST101T04() {
        lenient().when(rbLimitRuleRelationBcc.findByPrimaryKey(RULE_ID_R0001))
                .thenReturn(relation(RULE_ID_R0001, EXPR_01, "LSC001"));
        stubRelationsByExpr(EXPR_01, Arrays.asList(
                relation(RULE_ID_R0001, EXPR_01, "LSC001"),
                relation(RULE_ID_R0002, EXPR_01, "LSC002")));
        // 两编码均启用，仅其一被查询即中断（中断位置取决于遍历顺序，lenient 声明避免未用桩失败）
        stubSceneDef("LSC001", Collections.singletonList(sceneDef("LSC001", VALID_FLAG_Y)));
        stubSceneDef("LSC002", Collections.singletonList(sceneDef("LSC002", VALID_FLAG_Y)));

        ST101OutputBO output = st101.execute(input(RULE_ID_R0001));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("已匹配到限额场景", output.getCheckResult());
        assertEquals(EXPR_01, output.getRuleRelationExpr());
        assertTrue(Arrays.asList("LSC001", "LSC002").contains(output.getLimitSceneNo()));
        assertEquals(VALID_FLAG_Y, output.getValidFlag());
    }

    // REQ-004-S02 编码列表全部遍历完成且每次[配置数据]均为空（一条未启用、一条无记录），返回未匹配，场景字段为空、表达式仍取步骤1所得
    @Test
    public void testST101T05() {
        lenient().when(rbLimitRuleRelationBcc.findByPrimaryKey(RULE_ID_R0001))
                .thenReturn(relation(RULE_ID_R0001, EXPR_01, "LSC003"));
        stubRelationsByExpr(EXPR_01, Arrays.asList(
                relation(RULE_ID_R0001, EXPR_01, "LSC003"),
                relation("R0005", EXPR_01, "LSC009")));
        // LSC003 存在 validFlag="N" 记录、LSC009 无记录，全部遍历完成才输出未匹配，两桩必然触达
        stubSceneDef("LSC003", Collections.emptyList());
        stubSceneDef("LSC009", Collections.emptyList());

        ST101OutputBO output = st101.execute(input(RULE_ID_R0001));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("未匹配到限额场景", output.getCheckResult());
        assertEquals(EXPR_01, output.getRuleRelationExpr());
        assertNull(output.getLimitSceneNo());
        assertNull(output.getValidFlag());
    }

    // REQ-001-S02 因子对应关系记录不存在，无表达式可得、无编码可遍历，最终未匹配且全部业务输出字段为空
    @Test
    public void testST101T06() {
        lenient().when(rbLimitRuleRelationBcc.findByPrimaryKey(RULE_ID_R9999))
                .thenReturn(null);

        ST101OutputBO output = st101.execute(input(RULE_ID_R9999));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("未匹配到限额场景", output.getCheckResult());
        assertNull(output.getRuleRelationExpr());
        assertNull(output.getLimitSceneNo());
        assertNull(output.getValidFlag());
    }
}
