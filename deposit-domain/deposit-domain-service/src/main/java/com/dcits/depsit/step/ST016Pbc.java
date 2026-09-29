package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST016InputBO;
import com.dcits.depsit.facade.bo.ST016OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST016 检查是否存在现金止付限制。
 *
 * 步骤1 以输入{账号}与限制状态"A-生效"（RestraintsStatus.A）查询【账户限制信息】
 * （RB_BUS_RESTRAINTS，零条或多条），取得的记录包括账户限制类型与限制编号；逐条执行
 * 步骤2、步骤3：步骤2 以账户限制类型与状态"A-生效"（Status.A）查询【限制类型表】
 * （RB_RESTRAINT_TYPE，至多一条），步骤3 判定借贷方控制标志是否为"D-禁止借方"
 * （DrCrCtlFlag.D）且现金标志是否为"N-不允许现金"；任一条满足即输出现金止付标志
 * "是"，全部不满足或无记录时输出"否"。限制编号仅为需求要求的取得字段，本步骤内
 * 无判定用途。只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST016Pbc implements IST016 {

    /** 现金止付标志取值：是 */
    private static final String CASH_STOP_FLAG_YES = "是";

    /** 现金止付标志取值：否 */
    private static final String CASH_STOP_FLAG_NO = "否";

    /** 现金标志取值：N-不允许现金（需求文本常量，无枚举绑定） */
    private static final String CASH_FLAG_NOT_ALLOWED = "N";

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Autowired
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @Override
    public ST016OutputBO execute(ST016InputBO input) {
        ST016OutputBO output = new ST016OutputBO();

        // 步骤1 获取账户限制信息：查询为空（无记录或状态均非"生效"）时不执行步骤2、步骤3
        List<RbBusRestraintsEO> restraintsList = findEffectiveRestraints(input.getBaseAcctNo());

        if (restraintsList != null) {
            // 步骤1 逐条处理：任一条满足步骤3判定即返回，不再处理其余记录（处理顺序需求未约束，按返回顺序）
            for (RbBusRestraintsEO restraints : restraintsList) {
                // 步骤2 获取账户限制类型信息
                RbRestraintTypeEO restraintType = findEffectiveRestraintType(restraints.getRestraintType());
                // 步骤3 检查是否存在现金止付限制
                if (isCashStop(restraintType)) {
                    output.setCashStopFlag(CASH_STOP_FLAG_YES);
                    output.setSucceed(true);
                    return output;
                }
            }
        }

        // 无生效限制记录或全部不满足：现金止付标志"否"
        output.setCashStopFlag(CASH_STOP_FLAG_NO);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户限制信息：以{账号}与限制状态"A-生效"为条件查询 RB_BUS_RESTRAINTS，
     * 可能返回零条或多条；取得的记录（含账户限制类型、限制编号）随整条 EO 返回。
     */
    private List<RbBusRestraintsEO> findEffectiveRestraints(String baseAcctNo) {
        RbBusRestraintsEO condition = new RbBusRestraintsEO();
        condition.setBaseAcctNo(baseAcctNo);
        condition.setRestraintsStatus(RestraintsStatus.A);
        return rbBusRestraintsBcc.findByEo(condition);
    }

    /**
     * 步骤2 获取账户限制类型信息：以账户限制类型与状态"A-生效"为条件查询 RB_RESTRAINT_TYPE，至多一条。
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
     * 步骤3 检查是否存在现金止付限制：借贷方控制标志等于"D-禁止借方"且现金标志等于"N-不允许现金"时判定满足。
     */
    private boolean isCashStop(RbRestraintTypeEO restraintType) {
        if (restraintType == null) {
            return false;
        }
        return restraintType.getDrCrCtlFlag() == DrCrCtlFlag.D
                && CASH_FLAG_NOT_ALLOWED.equals(restraintType.getCashFlag());
    }
}
