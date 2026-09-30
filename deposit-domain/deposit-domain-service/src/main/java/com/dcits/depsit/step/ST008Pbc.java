package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST008InputBO;
import com.dcits.depsit.facade.bo.ST008OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST008 检查是否存在现金止收限制。
 *
 * 步骤1 以输入{账号}与限制状态"A-生效"（RestraintsStatus.A）查询【账户限制信息】
 * （RB_BUS_RESTRAINTS，结果集），遍历全部记录逐条执行步骤2、步骤3：步骤2 以记录的
 * 账户限制类型与状态"A-生效"（Status.A）查询【限制类型表】（RB_RESTRAINT_TYPE，
 * 至多一条），步骤3 判定借贷方控制标志是否为"C-禁止贷方"（DrCrCtlFlag.C）且现金标志
 * 是否为"N-不允许现金"；任一条命中即输出"是"并按首条命中记录聚合输出，无生效记录或
 * 全部未命中时输出"否"且其余输出字段为空。只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST008Pbc implements IST008 {

    /** 现金止收标志取值：是 */
    private static final String CASH_STOP_FLAG_YES = "是";

    /** 现金止收标志取值：否 */
    private static final String CASH_STOP_FLAG_NO = "否";

    /** 现金标志取值：N-不允许现金（需求文本常量，无枚举绑定） */
    private static final String CASH_FLAG_NOT_ALLOWED = "N";

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Autowired
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @Override
    public ST008OutputBO execute(ST008InputBO input) {
        ST008OutputBO output = new ST008OutputBO();

        // 步骤1 获取账户生效限制记录：结果集为空（无记录或状态均非"生效"）时不进入类型判定
        List<RbBusRestraintsEO> restraintsList = findEffectiveRestraints(input.getBaseAcctNo());

        if (restraintsList != null) {
            // 步骤1 遍历全部记录，按返回顺序逐条执行步骤2、步骤3；首条命中即聚合输出并结束
            // （命中后是否继续遍历需求未约束，输出不因实现选择而变化）
            for (RbBusRestraintsEO restraints : restraintsList) {
                // 步骤2 获取账户限制类型的生效信息
                RbRestraintTypeEO restraintType = findEffectiveRestraintType(restraints.getRestraintType());
                // 步骤3 现金止收判定
                if (isCashStop(restraintType)) {
                    fillHitOutput(output, restraints, restraintType);
                    output.setSucceed(true);
                    return output;
                }
            }
        }

        // 无生效记录或全部记录均未命中：现金止收标志"否"，其余 7 个输出字段保持空
        output.setCashStopFlag(CASH_STOP_FLAG_NO);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户生效限制记录：以{账号}与限制状态"A-生效"为条件查询 RB_BUS_RESTRAINTS，
     * 返回该账号全部生效限制记录的结果集。
     */
    private List<RbBusRestraintsEO> findEffectiveRestraints(String baseAcctNo) {
        RbBusRestraintsEO condition = new RbBusRestraintsEO();
        condition.setBaseAcctNo(baseAcctNo);
        condition.setRestraintsStatus(RestraintsStatus.A);
        return rbBusRestraintsBcc.findByEo(condition);
    }

    /**
     * 步骤2 获取账户限制类型的生效信息：以账户限制类型与状态"A-生效"为条件查询
     * RB_RESTRAINT_TYPE，至多一条。未取得生效类型记录时返回 null，该条记录不判定为
     * 命中，遍历继续处理后续记录，且不构成业务失败。
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
     * 步骤3 现金止收判定：借贷方控制标志等于"C-禁止贷方"且现金标志等于"N-不允许现金"
     * 两条件同时成立时判定命中。
     */
    private boolean isCashStop(RbRestraintTypeEO restraintType) {
        if (restraintType == null) {
            return false;
        }
        return restraintType.getDrCrCtlFlag() == DrCrCtlFlag.C
                && CASH_FLAG_NOT_ALLOWED.equals(restraintType.getCashFlag());
    }

    /**
     * 步骤3 首条命中聚合：现金止收标志"是"，resSeqNo、restraintType、restraintsStatus
     * 取首条命中记录的值，status、drCrCtlFlag、cashFlag、stopFlag 取其对应生效类型记录的值。
     */
    private void fillHitOutput(ST008OutputBO output, RbBusRestraintsEO restraints, RbRestraintTypeEO restraintType) {
        output.setCashStopFlag(CASH_STOP_FLAG_YES);
        // 限制编号、账户限制类型、限制状态取自首条命中的 RB_BUS_RESTRAINTS 记录
        output.setResSeqNo(restraints.getResSeqNo());
        output.setRestraintType(restraints.getRestraintType());
        output.setRestraintsStatus(restraints.getRestraintsStatus());
        // 状态、借贷方控制标志、现金标志、止付标志取自该条对应的 RB_RESTRAINT_TYPE 生效记录
        output.setStatus(restraintType.getStatus());
        output.setDrCrCtlFlag(restraintType.getDrCrCtlFlag());
        output.setCashFlag(restraintType.getCashFlag());
        output.setStopFlag(restraintType.getStopFlag());
    }
}
