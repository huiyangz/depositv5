package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST010InputBO;
import com.dcits.depsit.facade.bo.ST010OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST010 检查是否存在转账不收不付限制。
 *
 * 步骤1 以输入{账号}与限制状态"A-生效"（RestraintsStatus.A）查询【账户限制信息】
 * （RB_BUS_RESTRAINTS，零条或多条），按返回顺序逐条执行步骤2、步骤3：步骤2 以
 * 账户限制类型与状态"A-生效"（Status.A）查询【限制类型表】（RB_RESTRAINT_TYPE，
 * 至多一条），步骤3 判定借贷方控制标志是否为"A-禁止借贷方"（DrCrCtlFlag.A）且
 * 转账标志是否为"N-禁止转账"；任一条满足即输出"是"并输出第一条命中记录及其类型
 * 信息，全部不满足或无记录时输出"否"。只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST010Pbc implements IST010 {

    /** 转账不收不付标志取值：是 */
    private static final String TRANSFER_NO_RECV_NO_PAY_FLAG_YES = "是";

    /** 转账不收不付标志取值：否 */
    private static final String TRANSFER_NO_RECV_NO_PAY_FLAG_NO = "否";

    /** 转账标志取值：N-禁止转账（需求文本常量，无枚举绑定） */
    private static final String TRANSFER_FLAG_NOT_ALLOWED = "N";

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Autowired
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @Override
    public ST010OutputBO execute(ST010InputBO input) {
        ST010OutputBO output = new ST010OutputBO();

        // 步骤1 获取账户限制信息：查询为空（无记录或状态均非"生效"）时不执行步骤2、步骤3
        List<RbBusRestraintsEO> restraintsList = findEffectiveRestraints(input.getBaseAcctNo());

        if (restraintsList != null) {
            // 按步骤1返回顺序逐条处理：第一条命中即返回，后续命中记录不改变输出
            for (RbBusRestraintsEO restraints : restraintsList) {
                // 步骤2 获取账户限制类型信息
                RbRestraintTypeEO restraintType = findEffectiveRestraintType(restraints.getRestraintType());
                // 步骤3 判定是否存在转账不收不付限制
                if (isTransferNoRecvNoPay(restraintType)) {
                    fillHitOutput(output, restraints, restraintType);
                    output.setSucceed(true);
                    return output;
                }
            }
        }

        // 全部不满足或无生效限制记录：转账不收不付标志"否"，其余输出字段不赋值
        output.setTransferNoRecvNoPayFlag(TRANSFER_NO_RECV_NO_PAY_FLAG_NO);
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
     * 步骤2 获取账户限制类型信息：以账户限制类型与状态"A-生效"为条件查询 RB_RESTRAINT_TYPE，至多一条。
     * 查询不到生效类型记录时返回 null，该条记录无标志值可判，由步骤3按未命中处理。
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
     * 步骤3 判定是否存在转账不收不付限制：借贷方控制标志等于"A-禁止借贷方"且转账标志等于"N-禁止转账"时判定命中。
     */
    private boolean isTransferNoRecvNoPay(RbRestraintTypeEO restraintType) {
        if (restraintType == null) {
            return false;
        }
        return restraintType.getDrCrCtlFlag() == DrCrCtlFlag.A
                && TRANSFER_FLAG_NOT_ALLOWED.equals(restraintType.getTransferFlag());
    }

    /**
     * 步骤3 命中结果组装：转账不收不付标志"是"，输出字段取第一条命中记录及其对应生效限制类型的值。
     */
    private void fillHitOutput(ST010OutputBO output, RbBusRestraintsEO restraints, RbRestraintTypeEO restraintType) {
        output.setTransferNoRecvNoPayFlag(TRANSFER_NO_RECV_NO_PAY_FLAG_YES);
        // 限制编号、账户限制类型、限制状态取自该条 RB_BUS_RESTRAINTS 记录
        output.setResSeqNo(restraints.getResSeqNo());
        output.setRestraintType(restraints.getRestraintType());
        output.setRestraintsStatus(restraints.getRestraintsStatus());
        // 借贷方控制标志、状态、转账标志、止付标志取自该条对应的 RB_RESTRAINT_TYPE 生效记录
        output.setDrCrCtlFlag(restraintType.getDrCrCtlFlag());
        output.setStatus(restraintType.getStatus());
        output.setTransferFlag(restraintType.getTransferFlag());
        output.setStopFlag(restraintType.getStopFlag());
    }
}
