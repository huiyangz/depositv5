package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST101InputBO;
import com.dcits.depsit.facade.bo.ST101OutputBO;

/**
 * ST101 匹配限额场景。
 *
 * 以输入{因子名称}（其值对应主键 ruleId）查询【限额规则关系表】取得规则关系表达式，
 * 以该表达式等值查询同一张表取得[限额场景编码列表]，逐个编码查询【限额场景定义表】中
 * 启用标志为"Y-启用"的[配置数据]，命中即中断遍历。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST101 {

    /**
     * 匹配限额场景。
     *
     * @param input 输入BO，因子名称（factorName）必填
     * @return 输出BO：checkResult 恒有值（"已匹配到限额场景"/"未匹配到限额场景"）；
     *         "已匹配到限额场景"时携带命中的 limitSceneNo、validFlag 及步骤1所得
     *         ruleRelationExpr；"未匹配到限额场景"时 limitSceneNo、validFlag 为 null，
     *         ruleRelationExpr 仍取步骤1所得表达式（因子记录不存在时为 null）。
     *         本步骤无业务失败场景。
     */
    ST101OutputBO execute(ST101InputBO input);
}
