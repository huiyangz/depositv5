package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST075InputBO;
import com.dcits.depsit.facade.bo.ST075OutputBO;

/**
 * ST075 设置账户执行利率。
 *
 * 单一指令步骤"设置账户执行利率：赋值账户$执行利率$为[执行利率]"：将输入执行利率
 * （realRate）原值赋值到输出执行利率（绑定 RB_BUS_ACCT_INT_DETAIL 的执行利率字段，
 * 仅作实体绑定说明，本步骤不触发数据库访问）。原值传递，不换算、不舍入、不做
 * 标度调整、空值转换或条件分支。无依赖调用，无业务失败场景，失败仅由技术异常传播表达。
 */
@Service
public class ST075Pbc implements IST075 {

    @Override
    public ST075OutputBO execute(ST075InputBO input) {
        ST075OutputBO output = new ST075OutputBO();
        // 设置账户执行利率：赋值账户$执行利率$为[执行利率]，原值传递
        output.setRealRate(input.getRealRate());
        output.setSucceed(true);
        return output;
    }
}
