package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST007InputBO;
import com.dcits.depsit.facade.bo.ST007OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST007 检查质押类限制。
 *
 * 步骤1 以输入{账号}与限制状态"A-生效"（RestraintsStatus.A）查询【账户限制信息】
 * （RB_BUS_RESTRAINTS，零条或多条），逐条执行步骤2：以记录的账户限制类型与状态
 * "A-生效"（Status.A）查询【限制类型表】（RB_RESTRAINT_TYPE，至多一条），取得
 * 质押标志赋值；全部记录处理完毕后步骤返回。无生效记录时正常返回且输出字段
 * 均为空。本步骤仅查询、赋值与返回，不含质押标志命中判定（职责边界见正式 Spec）；
 * 无业务失败场景，技术异常原样向上传播；只读步骤，无事务要求。
 */
@Service
public class ST007Pbc implements IST007 {

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Autowired
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @Override
    public ST007OutputBO execute(ST007InputBO input) {
        ST007OutputBO output = new ST007OutputBO();

        // 步骤1 获取账户限制信息：查询为空（无记录或状态均非"生效"）时不执行步骤2
        List<RbBusRestraintsEO> restraintsList = findEffectiveRestraints(input.getBaseAcctNo());

        if (restraintsList != null) {
            // 步骤1 逐条处理：每条记录均执行步骤2，全部处理完毕后才返回（无满足即返回的短路判定，处理顺序需求未约束）
            for (RbBusRestraintsEO restraints : restraintsList) {
                // 记录来源字段：限制编号、账户限制类型、限制状态取自该条 RB_BUS_RESTRAINTS 记录
                output.setResSeqNo(restraints.getResSeqNo());
                output.setRestraintType(restraints.getRestraintType());
                output.setRestraintsStatus(restraints.getRestraintsStatus());
                // 步骤2 获取质押标志：质押标志、状态取自该条对应的 RB_RESTRAINT_TYPE 生效记录；
                // 类型无生效记录时该条对这两个字段不产生赋值，不产生业务失败，继续处理其余记录
                RbRestraintTypeEO restraintType = findEffectiveRestraintType(restraints.getRestraintType());
                if (restraintType != null) {
                    output.setPledgedFlag(restraintType.getPledgedFlag());
                    output.setStatus(restraintType.getStatus());
                }
            }
        }

        // 各输出字段取最后一次赋值的结果，从未被赋值的字段保持 null；正常返回（含空结果）均 succeed=true
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
     * 步骤2 获取质押标志：以账户限制类型与状态"A-生效"为条件查询 RB_RESTRAINT_TYPE，
     * RESTRAINT_TYPE 为该表主键，至多命中一条生效记录。查询不到生效类型记录时返回 null，
     * 该条记录无质押标志可赋值。
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
}
