package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST009InputBO;
import com.dcits.depsit.facade.bo.ST009OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST009 检查是否存在止付限制。
 *
 * 步骤1 以输入{账号}与限制状态"A-生效"（RestraintsStatus.A）查询【账户限制信息】
 * （RB_BUS_RESTRAINTS，零条或多条），逐条执行步骤2、步骤3：步骤2 以账户限制类型与
 * 状态"A-生效"（Status.A）查询【限制类型表】（RB_RESTRAINT_TYPE，至多一条），
 * 步骤3 判定借贷方控制标志是否为"D-禁止借方"（DrCrCtlFlag.D，严格相等比较）；
 * 任一条满足即返回"是"并输出该条命中字段，全部不满足或无记录时输出"否"且其余
 * 输出字段为空。只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST009Pbc implements IST009 {

    /** 止付标志取值：是 */
    private static final String STOP_FLAG_YES = "是";

    /** 止付标志取值：否 */
    private static final String STOP_FLAG_NO = "否";

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Autowired
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @Override
    public ST009OutputBO execute(ST009InputBO input) {
        ST009OutputBO output = new ST009OutputBO();

        // 步骤1 获取账户限制信息：查询为空（无记录或状态均非"生效"）时不执行步骤2、步骤3
        List<RbBusRestraintsEO> restraintsList = findEffectiveRestraints(input.getBaseAcctNo());

        if (restraintsList != null) {
            // 步骤1 逐条处理：任一条满足步骤3判定即返回，不再处理其余记录（处理顺序需求未约束，按返回顺序）
            for (RbBusRestraintsEO restraints : restraintsList) {
                // 步骤2 获取借贷方控制标志
                RbRestraintTypeEO restraintType = findEffectiveRestraintType(restraints.getRestraintType());
                // 步骤3 检查是否存在止付限制
                if (isStopPayment(restraintType)) {
                    fillHitOutput(output, restraints, restraintType);
                    output.setSucceed(true);
                    return output;
                }
            }
        }

        // 全部不满足或无生效限制记录：止付标志"否"，其余输出字段保持空
        output.setStopFlag(STOP_FLAG_NO);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户限制信息：以{账号}与限制状态"A-生效"为条件查询 RB_BUS_RESTRAINTS，可能返回零条或多条。
     */
    private List<RbBusRestraintsEO> findEffectiveRestraints(String baseAcctNo) {
        RbBusRestraintsEO condition = new RbBusRestraintsEO();
        condition.setBaseAcctNo(baseAcctNo);
        condition.setRestraintsStatus(RestraintsStatus.A);
        return rbBusRestraintsBcc.findByEo(condition);
    }

    /**
     * 步骤2 获取借贷方控制标志：以账户限制类型与状态"A-生效"为条件查询 RB_RESTRAINT_TYPE，至多一条。
     * 查询不到生效类型记录时返回 null，该条记录无标志值可判，由步骤3按"否则"分支处理。
     */
    private RbRestraintTypeEO findEffectiveRestraintType(RestraintType restraintType) {
        RbRestraintTypeEO condition = new RbRestraintTypeEO();
        condition.setRestraintType(restraintType);
        condition.setStatus(Status.A);
        List<RbRestraintTypeEO> typeList = rbRestraintTypeBcc.findByEo(condition);
        if (typeList == null || typeList.isEmpty()) {
            return null;
        }
        return typeList.get(0);
    }

    /**
     * 步骤3 检查是否存在止付限制：借贷方控制标志等于"D-禁止借方"时判定满足
     * （严格相等比较，其他取值含未取得标志值均不满足）。
     */
    private boolean isStopPayment(RbRestraintTypeEO restraintType) {
        if (restraintType == null) {
            return false;
        }
        return restraintType.getDrCrCtlFlag() == DrCrCtlFlag.D;
    }

    /**
     * 步骤3 结果汇总：止付标志"是"，输出字段取该条满足记录及其对应生效限制类型的值。
     */
    private void fillHitOutput(ST009OutputBO output, RbBusRestraintsEO restraints, RbRestraintTypeEO restraintType) {
        output.setStopFlag(STOP_FLAG_YES);
        // 限制编号、账户限制类型、限制状态取自该条 RB_BUS_RESTRAINTS 记录
        output.setResSeqNo(restraints.getResSeqNo());
        output.setRestraintType(restraints.getRestraintType());
        output.setRestraintsStatus(restraints.getRestraintsStatus());
        // 借贷方控制标志、状态取自该条对应的 RB_RESTRAINT_TYPE 生效记录
        output.setDrCrCtlFlag(restraintType.getDrCrCtlFlag());
        output.setStatus(restraintType.getStatus());
    }
}
