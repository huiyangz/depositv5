package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST108InputBO;
import com.dcits.depsit.facade.bo.ST108OutputBO;

/**
 * ST108 检查增加限制起始日期。
 *
 * 步骤1 对上送的{开始日期}与{系统日期}（核心运行日期）、{结束日期}做区间判定：
 * {开始日期}严格早于{系统日期}或严格晚于{结束日期}任一成立时检查结果为"不通过"，
 * 否则（含分别与{系统日期}、{结束日期}相等的边界）为"通过"并输出 [检查结果]。
 * 两个违规条件为"或者"关系，无优先级与顺序要求，单次判定即返回。
 * 纯判定步骤：无数据表查询、无外部调用、无写入副作用、无事务要求、无业务失败场景，
 * "不通过"是正常返回的检查结果，不以业务失败或错误码表达。
 */
@Service
public class ST108Pbc implements IST108 {

    /** 检查结果取值：通过（需求文本常量，无枚举绑定） */
    private static final String CHECK_RESULT_PASS = "通过";

    /** 检查结果取值：不通过（需求文本常量，无枚举绑定） */
    private static final String CHECK_RESULT_FAIL = "不通过";

    @Override
    public ST108OutputBO execute(ST108InputBO input) {
        ST108OutputBO output = new ST108OutputBO();

        // 步骤1 判定与返回：任一违规条件成立即"不通过"，否则"通过"；"小于/大于"为严格比较，相等不触发
        if (input.getStartDate().before(input.getRunDate())
                || input.getStartDate().after(input.getEndDate())) {
            output.setCheckResult(CHECK_RESULT_FAIL);
        } else {
            output.setCheckResult(CHECK_RESULT_PASS);
        }

        // "不通过"亦为正常返回的业务结果，成功标志显式置位，错误字段保持 null
        output.setSucceed(true);
        return output;
    }
}
