package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST105InputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;

/**
 * ST105 检查黑名单。
 *
 * 按接口服务代码与接口服务类型定位核心服务定义表的黑名单检查开关，开关开启后按客户号
 * 查询生效黑名单，并经名单类型→黑名单检查规则编号→名单限制规则（检查类判定）→名单
 * 不检查范围→介质→机构范围逐层过滤，任一层未命中返回"通过"；全部命中时按名单限制
 * 规则的处理方式输出 dealFlow（拒绝 B / 授权 A / 提醒 D）。只读检查步骤：无数据写入
 * 副作用，无事务要求。
 */
public interface IST105 {

    /**
     * 检查黑名单。
     *
     * @param input 输入BO，15 个字段定义见正式 Spec ST105「输入」表
     * @return 输出BO：检查结果为"拒绝"/"授权"/"提醒"时 dealFlow 取 DealFlow.B/A/D；
     *         检查结果为"通过"时 dealFlow 为 null。本步骤无业务失败场景，succeed 恒为 true。
     */
    ST105OutputBO execute(ST105InputBO input);
}
