package com.dcits.depsit.step;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST006InputBO;
import com.dcits.depsit.facade.bo.ST006OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST006 检查是否存在转账止付限制（交易执行步骤）
 *
 * <p>依据正式 Spec ST006：第 1 步以账号与限制状态"A-生效"查询【账户限制信息】；
 * 第 2 步对每条限制记录的账户限制类型查询【限制类型表】，仅当匹配记录状态为"A-生效"
 * 时取得其借贷方控制标志与转账标志；第 3 步逐条检查，任一记录的限制类型对应借贷方控制标志
 * 为"D-禁止借方"且转账标志为"N-不允许转账"时，转账止付标志为"是"并回显第一条满足条件
 * 记录的相关字段，否则（含无生效限制记录、限制类型表对应记录未生效）转账止付标志为"否"
 * 且回显字段置空。本步骤只读无写库；无业务失败场景，技术异常按原样向调用方传播。</p>
 */
@Service
public class ST006Pbc implements IST006 {

    /** 转账止付标志规范常量："是"（Spec「输出」表） */
    private static final String STOP_FLAG_YES = "是";

    /** 转账止付标志规范常量："否"（Spec「输出」表） */
    private static final String STOP_FLAG_NO = "否";

    /** 转账标志代码值"N-不允许转账"（Spec「取值映射」，无枚举绑定） */
    private static final String TRANSFER_FLAG_NOT_ALLOWED = "N";

    private final IRbBusRestraintsBcc rbBusRestraintsBcc;

    private final IRbRestraintTypeBcc rbRestraintTypeBcc;

    public ST006Pbc(IRbBusRestraintsBcc rbBusRestraintsBcc, IRbRestraintTypeBcc rbRestraintTypeBcc) {
        this.rbBusRestraintsBcc = rbBusRestraintsBcc;
        this.rbRestraintTypeBcc = rbRestraintTypeBcc;
    }

    @Override
    public ST006OutputBO execute(ST006InputBO input) {
        ST006OutputBO output = new ST006OutputBO();
        // 第 1 步（REQ-001）：按账号与限制状态"A-生效"查询账户限制信息；无匹配记录时结果为空集合，不构成失败
        List<RbBusRestraintsEO> restraints = queryEffectiveRestraints(input.getBaseAcctNo());
        // 第 2、3 步（REQ-002、REQ-003）：逐条取得限制类型表生效记录的两个标志并检查，
        // 命中第一条满足条件的记录即完成判定与回显，否则转账止付标志为"否"且回显字段置空
        resolveTransferStopFlag(restraints, output);
        // 无业务失败场景（REQ-004）：全部业务情形（含转账止付标志为"是"与"否"）均成功结束，
        // 不设置业务错误码与错误信息；技术异常按原样向调用方传播
        output.setSucceed(true);
        return output;
    }

    /**
     * 第 1 步（REQ-001）：以{账号}与限制状态"A-生效"为条件查询【账户限制信息】，
     * 返回该账号下限制状态为"A-生效"的记录集合（每条含账户限制类型与限制编号），空集合不构成失败。
     */
    private List<RbBusRestraintsEO> queryEffectiveRestraints(String baseAcctNo) {
        RbBusRestraintsEO queryEo = new RbBusRestraintsEO();
        queryEo.setBaseAcctNo(baseAcctNo);
        queryEo.setRestraintsStatus(RestraintsStatus.A);
        return rbBusRestraintsBcc.findByEo(queryEo);
    }

    /**
     * 第 2 步（REQ-002）+ 第 3 步（REQ-003）：对[账户限制信息]中每条记录的账户限制类型
     * 独立查询【限制类型表】，仅当匹配记录存在且状态为"A-生效"时取得其借贷方控制标志与
     * 转账标志；取得的标志为"D-禁止借方"且"N-不允许转账"时满足条件，转账止付标志置"是"，
     * 回显字段取该条记录（resSeqNo、restraintType、restraintsStatus）及其账户限制类型对应的
     * 限制类型表生效记录（drCrCtlFlag、status、transferFlag）后结束检查；全部记录检查完毕
     * 仍无满足条件记录时（含无生效限制记录、限制类型表对应记录未生效或无匹配、标志组合不满足），
     * 转账止付标志置"否"，6 个回显字段全部置空（null）。
     */
    private void resolveTransferStopFlag(List<RbBusRestraintsEO> restraints, ST006OutputBO output) {
        for (RbBusRestraintsEO restraint : restraints) {
            // 第 2 步（REQ-002）：按账户限制类型查询限制类型表（主键查询，至多一条）
            RbRestraintTypeEO restraintType = rbRestraintTypeBcc.findByRestraintType(restraint.getRestraintType());
            // 无匹配记录或状态非"A-生效"：不取得两个标志，继续下一条，不构成失败
            if (restraintType == null || restraintType.getStatus() != Status.A) {
                continue;
            }
            // 第 3 步（REQ-003）：借贷方控制标志"D-禁止借方"且转账标志"N-不允许转账"时满足条件
            if (restraintType.getDrCrCtlFlag() == DrCrCtlFlag.D
                    && TRANSFER_FLAG_NOT_ALLOWED.equals(restraintType.getTransferFlag())) {
                output.setStopFlag(STOP_FLAG_YES);
                output.setResSeqNo(restraint.getResSeqNo());
                output.setRestraintType(restraint.getRestraintType());
                output.setRestraintsStatus(restraint.getRestraintsStatus());
                output.setDrCrCtlFlag(restraintType.getDrCrCtlFlag());
                output.setStatus(restraintType.getStatus());
                output.setTransferFlag(restraintType.getTransferFlag());
                return;
            }
        }
        output.setStopFlag(STOP_FLAG_NO);
    }
}
