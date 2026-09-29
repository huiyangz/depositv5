package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST101InputBO;
import com.dcits.depsit.facade.bo.ST101OutputBO;
import com.dcits.depsit.facade.components.IRbLimitRuleRelationBcc;
import com.dcits.depsit.facade.components.IRbLimitSceneDefBcc;
import com.dcits.depsit.facade.eo.RbLimitRuleRelationEO;
import com.dcits.depsit.facade.eo.RbLimitSceneDefEO;

/**
 * ST101 匹配限额场景。
 *
 * 步骤1 以输入{因子名称}（其值对应主键 ruleId）查询【限额规则关系表】
 * （RB_LIMIT_RULE_RELATION，至多一条）取得$规则关系表达式$；步骤2 以该表达式等值查询
 * 同一张表（零条或多条），将命中记录的$限额场景编码$组成[限额场景编码列表]；步骤3 对
 * 列表中每个编码以 limitSceneNo=编码 且启用标志="Y-启用"查询【限额场景定义表】
 * （RB_LIMIT_SCENE_DEF）取[配置数据]，[配置数据]不为空即中断遍历并返回"已匹配到限额
 * 场景"，列表全部遍历完成且每次[配置数据]均为空时返回"未匹配到限额场景"。因子对应
 * 记录不存在时无表达式可得、无编码可遍历，检查结果为"未匹配到限额场景"。
 * 只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST101Pbc implements IST101 {

    /** 检查结果取值：已匹配到限额场景 */
    private static final String CHECK_RESULT_MATCHED = "已匹配到限额场景";

    /** 检查结果取值：未匹配到限额场景 */
    private static final String CHECK_RESULT_NOT_MATCHED = "未匹配到限额场景";

    /** 启用标志"Y-启用"（需求文本常量，无枚举绑定） */
    private static final String VALID_FLAG_ENABLED = "Y";

    @Autowired
    private IRbLimitRuleRelationBcc rbLimitRuleRelationBcc;

    @Autowired
    private IRbLimitSceneDefBcc rbLimitSceneDefBcc;

    @Override
    public ST101OutputBO execute(ST101InputBO input) {
        ST101OutputBO output = new ST101OutputBO();

        // 步骤1 获取规则关系表达式：以{因子名称}作为主键 ruleId 查询，至多一条
        RbLimitRuleRelationEO factorRelation = rbLimitRuleRelationBcc.findByPrimaryKey(input.getFactorName());
        String ruleRelationExpr = factorRelation == null ? null : factorRelation.getRuleRelationExpr();
        output.setRuleRelationExpr(ruleRelationExpr);

        // 因子对应记录不存在（无表达式可得）时步骤2无查询键、步骤3无编码可遍历，检查结果为"未匹配到限额场景"
        if (ruleRelationExpr == null) {
            output.setCheckResult(CHECK_RESULT_NOT_MATCHED);
            output.setSucceed(true);
            return output;
        }

        // 步骤2 获取限额场景编码列表：以步骤1所得表达式等值查询，可能命中多条
        List<RbLimitRuleRelationEO> relationList = findRelationsByExpr(ruleRelationExpr);

        if (relationList != null) {
            // 步骤3 逐个编码匹配启用限额场景（遍历顺序需求未约束，按返回顺序）
            for (RbLimitRuleRelationEO relation : relationList) {
                RbLimitSceneDefEO sceneDef = findEnabledSceneDef(relation.getLimitSceneNo());
                // [配置数据]不为空→该编码命中，中断本次遍历（不再对列表其余编码查询定义表）
                if (sceneDef != null) {
                    fillMatchedOutput(output, sceneDef);
                    output.setSucceed(true);
                    return output;
                }
            }
        }

        // 列表全部遍历完成且每次[配置数据]均为空→"未匹配到限额场景"，场景字段保持空
        output.setCheckResult(CHECK_RESULT_NOT_MATCHED);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤2 获取限额场景编码列表：以$关系规则表达式$等值条件查询 RB_LIMIT_RULE_RELATION，
     * 命中记录的 limitSceneNo 组成[限额场景编码列表]，可能返回零条或多条。
     */
    private List<RbLimitRuleRelationEO> findRelationsByExpr(String ruleRelationExpr) {
        RbLimitRuleRelationEO condition = new RbLimitRuleRelationEO();
        condition.setRuleRelationExpr(ruleRelationExpr);
        return rbLimitRuleRelationBcc.findByEo(condition);
    }

    /**
     * 步骤3 查询启用配置：以 limitSceneNo=编码 且启用标志="Y-启用"组合条件查询 RB_LIMIT_SCENE_DEF，
     * 至多一条记录；[配置数据]为空（编码不存在或未启用）时返回 null，该编码不命中。
     */
    private RbLimitSceneDefEO findEnabledSceneDef(String limitSceneNo) {
        RbLimitSceneDefEO condition = new RbLimitSceneDefEO();
        condition.setLimitSceneNo(limitSceneNo);
        condition.setValidFlag(VALID_FLAG_ENABLED);
        List<RbLimitSceneDefEO> sceneDefList = rbLimitSceneDefBcc.findByEo(condition);
        if (sceneDefList == null || sceneDefList.isEmpty()) {
            return null;
        }
        return sceneDefList.get(0);
    }

    /**
     * 步骤3 结果填充：检查结果"已匹配到限额场景"，限额场景编码与启用标志取命中定义记录的值。
     */
    private void fillMatchedOutput(ST101OutputBO output, RbLimitSceneDefEO sceneDef) {
        output.setCheckResult(CHECK_RESULT_MATCHED);
        output.setLimitSceneNo(sceneDef.getLimitSceneNo());
        output.setValidFlag(sceneDef.getValidFlag());
    }
}
