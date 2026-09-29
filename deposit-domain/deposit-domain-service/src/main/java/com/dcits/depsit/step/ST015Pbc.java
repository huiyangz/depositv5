package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RestraintLevel;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST015InputBO;
import com.dcits.depsit.facade.bo.ST015OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;

/**
 * ST015 检查是否存在属性限制。
 *
 * 步骤1 以输入{账号}、限制状态"A-生效"（RestraintsStatus.A）且限制级别
 * "NATURE-账户属性限制"（RestraintLevel.NATURE）为条件查询【账户限制信息】
 * （RB_BUS_RESTRAINTS，零条或多条）；步骤2 按查询结果判定：非空时属性限制标志
 * 为"是"并输出其中一条记录的限制编号、账户限制类型、限制状态、限制级别（多条
 * 命中任取一条），为空时属性限制标志为"否"且其余输出字段为空。只读步骤，无业务
 * 失败场景，无事务要求。
 */
@Service
public class ST015Pbc implements IST015 {

    /** 属性限制标志取值：是 */
    private static final String NATURE_RESTRAINT_FLAG_YES = "是";

    /** 属性限制标志取值：否 */
    private static final String NATURE_RESTRAINT_FLAG_NO = "否";

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Override
    public ST015OutputBO execute(ST015InputBO input) {
        ST015OutputBO output = new ST015OutputBO();

        // 步骤1 获取属性级限制：查询为空（无记录或被限制状态/限制级别条件排除）时进入步骤2空分支
        List<RbBusRestraintsEO> restraintsList = findNatureRestraints(input.getBaseAcctNo());

        // 步骤2 检查是否存在属性限制：非空判定为"是"并输出其中一条记录（多条任取一条），为空判定为"否"
        if (restraintsList != null && !restraintsList.isEmpty()) {
            RbBusRestraintsEO hit = restraintsList.get(0);
            output.setNatureRestraintFlag(NATURE_RESTRAINT_FLAG_YES);
            output.setResSeqNo(hit.getResSeqNo());
            output.setRestraintType(hit.getRestraintType());
            output.setRestraintsStatus(hit.getRestraintsStatus());
            output.setRestraintLevel(hit.getRestraintLevel());
        } else {
            output.setNatureRestraintFlag(NATURE_RESTRAINT_FLAG_NO);
        }
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取属性级限制：以{账号}、限制状态"A-生效"（RestraintsStatus.A）且
     * 限制级别"NATURE-账户属性限制"（RestraintLevel.NATURE）为条件查询 RB_BUS_RESTRAINTS，
     * 可能返回零条或多条。
     */
    private List<RbBusRestraintsEO> findNatureRestraints(String baseAcctNo) {
        RbBusRestraintsEO condition = new RbBusRestraintsEO();
        condition.setBaseAcctNo(baseAcctNo);
        condition.setRestraintsStatus(RestraintsStatus.A);
        condition.setRestraintLevel(RestraintLevel.NATURE);
        return rbBusRestraintsBcc.findByEo(condition);
    }
}
