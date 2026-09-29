package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST101 匹配限额场景 输出BO。
 *
 * 字段定义来自正式 Spec ST101「输出」表。空值语义：checkResult 恒有值
 * （"已匹配到限额场景"/"未匹配到限额场景"）；ruleRelationExpr 取步骤1主键查询所得
 * 记录的表达式，因子对应记录不存在时为 null；limitSceneNo、validFlag 仅在
 * "已匹配到限额场景"时取命中定义记录的值，"未匹配到限额场景"时均为 null。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST101OutputBO extends StepResult {

    /** 检查结果，取值"已匹配到限额场景"/"未匹配到限额场景"（步骤3判定结果） */
    private String checkResult;
    /** 关系规则表达式，来源：限额规则关系表（RB_LIMIT_RULE_RELATION） */
    private String ruleRelationExpr;
    /** 限额场景编码，来源：限额场景定义表（RB_LIMIT_SCENE_DEF） */
    private String limitSceneNo;
    /** 启用标志，来源：限额场景定义表（RB_LIMIT_SCENE_DEF），因查询条件恒为"Y" */
    private String validFlag;

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public String getRuleRelationExpr() {
        return ruleRelationExpr;
    }

    public void setRuleRelationExpr(String ruleRelationExpr) {
        this.ruleRelationExpr = ruleRelationExpr;
    }

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public String getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(String validFlag) {
        this.validFlag = validFlag;
    }
}
