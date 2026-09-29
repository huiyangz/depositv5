package com.dcits.depsit.step;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST003InputBO;
import com.dcits.depsit.facade.bo.ST003OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST003 检查有权机关冻结限制（交易执行步骤）
 *
 * <p>依据正式 Spec ST003：第 1 步以账号与限制状态"A-生效"查询【账户限制信息】；
 * 第 2 步对每条限制记录的账户限制类型查询【限制类型表】，仅当匹配记录状态为"A-生效"
 * 时取得其有权机关冻结标志，查得任一不为空的标志值时赋值输出，未查得时输出为空。
 * 本步骤只读无写库；无业务失败场景，技术异常按原样向调用方传播。</p>
 */
@Service
public class ST003Pbc implements IST003 {

    private final IRbBusRestraintsBcc rbBusRestraintsBcc;

    private final IRbRestraintTypeBcc rbRestraintTypeBcc;

    public ST003Pbc(IRbBusRestraintsBcc rbBusRestraintsBcc, IRbRestraintTypeBcc rbRestraintTypeBcc) {
        this.rbBusRestraintsBcc = rbBusRestraintsBcc;
        this.rbRestraintTypeBcc = rbRestraintTypeBcc;
    }

    @Override
    public ST003OutputBO execute(ST003InputBO input) {
        ST003OutputBO output = new ST003OutputBO();
        // 第 1 步（REQ-001）：按账号与限制状态"A-生效"查询账户限制信息；无匹配记录时结果为空集合，不构成失败
        List<RbBusRestraintsEO> restraints = queryEffectiveRestraints(input.getBaseAcctNo());
        // 第 2 步（REQ-002）：对每条限制记录的账户限制类型查询限制类型表，取得状态生效的有权机关冻结标志
        String ahBuFlag = resolveAhBuFlag(restraints);
        // 查得任一不为空的标志值时赋值；未查得时不赋值，输出为空（null）
        output.setAhBuFlag(ahBuFlag);
        // 无业务失败场景（REQ-003）：全部业务情形（含输出为空）均成功结束，不设置业务错误码与错误信息
        output.setSucceed(true);
        return output;
    }

    /**
     * 第 1 步（REQ-001）：以{账号}与限制状态"A-生效"为条件查询【账户限制信息】，
     * 返回该账号下限制状态为"A-生效"的记录集合（含账户限制类型与限制编号），空集合不构成失败。
     */
    private List<RbBusRestraintsEO> queryEffectiveRestraints(String baseAcctNo) {
        RbBusRestraintsEO queryEo = new RbBusRestraintsEO();
        queryEo.setBaseAcctNo(baseAcctNo);
        queryEo.setRestraintsStatus(RestraintsStatus.A);
        return rbBusRestraintsBcc.findByEo(queryEo);
    }

    /**
     * 第 2 步（REQ-002）：对[账户限制信息]中每条记录的账户限制类型独立查询【限制类型表】，
     * 仅当匹配记录状态为"A-生效"时取得其有权机关冻结标志；查得任一不为空的标志值时返回该值，
     * 未查得时返回 null。
     */
    private String resolveAhBuFlag(List<RbBusRestraintsEO> restraints) {
        String ahBuFlag = null;
        for (RbBusRestraintsEO restraint : restraints) {
            RbRestraintTypeEO restraintType = rbRestraintTypeBcc.findByRestraintType(restraint.getRestraintType());
            if (restraintType != null && restraintType.getStatus() == Status.A) {
                String flag = restraintType.getAhBuFlag();
                if (flag != null) {
                    ahBuFlag = flag;
                }
            }
        }
        return ahBuFlag;
    }
}
