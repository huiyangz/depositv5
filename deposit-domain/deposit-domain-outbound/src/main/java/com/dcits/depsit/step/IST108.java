package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST108InputBO;
import com.dcits.depsit.facade.bo.ST108OutputBO;

/**
 * ST108 检查增加限制起始日期。
 *
 * 对上送的{开始日期}与{系统日期}（核心运行日期）、{结束日期}做区间判定并输出检查结果。
 * 纯判定步骤：无数据表查询、无外部服务调用、无数据写入副作用，无事务要求。
 */
public interface IST108 {

    /**
     * 检查增加限制起始日期。
     *
     * @param input 输入BO：runDate（系统日期）、startDate（开始日期）、endDate（结束日期）均必填
     * @return 输出BO：checkResult 恒有值，{开始日期}严格早于{系统日期}或严格晚于
     *         {结束日期}任一成立时为"不通过"，否则为"通过"。判定为"不通过"仍是正常返回
     *         （succeed=true，无错误码），本步骤无业务失败场景。
     */
    ST108OutputBO execute(ST108InputBO input);
}
