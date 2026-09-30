package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST106InputBO;
import com.dcits.depsit.facade.bo.ST106OutputBO;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST106 检查限制类型。
 *
 * 步骤1 以输入{限制类型}为条件主键查询【存款限制类型表】（RB_RESTRAINT_TYPE）获取
 * [限制类型信息]（至多一条，无匹配时为 null，不视为失败）；步骤2 检查限制类型存在性，
 * [限制类型信息]为空时检查结果"不通过"并结束检查（不执行状态检查）；步骤3 检查该记录
 * 的$状态$是否为"A-有效"（Status.A），是则"通过"，否则"不通过"。只读步骤，无业务失败
 * 场景，无事务要求，技术异常按原样向调用方传播。
 */
@Service
public class ST106Pbc implements IST106 {

    /** 检查结果取值：通过 */
    private static final String CHECK_RESULT_PASS = "通过";

    /** 检查结果取值：不通过 */
    private static final String CHECK_RESULT_NOT_PASS = "不通过";

    @Autowired
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @Override
    public ST106OutputBO execute(ST106InputBO input) {
        ST106OutputBO output = new ST106OutputBO();

        // 步骤1 获取限制类型信息：以输入{限制类型}为条件主键查询【存款限制类型表】，查询条件不含状态
        RbRestraintTypeEO restraintTypeInfo = rbRestraintTypeBcc.findByRestraintType(input.getRestraintType());

        // 步骤2 检查限制类型存在性：[限制类型信息]为空时返回"不通过"并结束检查，不执行步骤3（短路）
        if (restraintTypeInfo == null) {
            output.setCheckResult(CHECK_RESULT_NOT_PASS);
            output.setSucceed(true);
            return output;
        }

        // 步骤3 检查限制类型状态：$状态$为"A-有效"（Status.A）时"通过"，否则"不通过"
        if (restraintTypeInfo.getStatus() == Status.A) {
            output.setCheckResult(CHECK_RESULT_PASS);
        } else {
            output.setCheckResult(CHECK_RESULT_NOT_PASS);
        }

        // 本步骤无业务失败场景：全部业务路径均成功结束，错误码与错误信息保持 null
        output.setSucceed(true);
        return output;
    }
}
