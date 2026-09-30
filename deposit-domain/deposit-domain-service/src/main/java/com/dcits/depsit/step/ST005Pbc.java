package com.dcits.depsit.step;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST005InputBO;
import com.dcits.depsit.facade.bo.ST005OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;

/**
 * ST005 检查账户是否存在限制（交易执行步骤）
 *
 * <p>依据正式 Spec ST005：第 1 步以输入账号查询【账户信息】，取得该记录的主账户标志与
 * 上级账户内部键并判定子账户，输出账号与主账户标志取自本次查询记录（判定为子账户时也
 * 不改取主账户记录的值）；第 2 步设置待查账户（子账户取按上级账户内部键查得的主账户账号，
 * 否则取输入账号）；第 3 步按待查账户与限制状态"A-生效"查询【账户限制信息】；第 4 步将
 * 每条生效限制的限制编号、账户限制类型、限制状态逐条赋值到三个集合输出，未查得生效限制
 * 时均为空集合。本步骤只读无写库；无业务失败场景，技术异常按原样向调用方传播。</p>
 */
@Service
public class ST005Pbc implements IST005 {

    /**
     * 主账户标志表明为子账户的取值。
     * Spec REQ-001 注：子账户/非子账户具体取值需求未声明，已按「已接受的需求处理结论」第 1 条
     * 放行、待业务确认；本实现与测试用例采用同一约定示例值："1" 表明为子账户，其余（含 "0"、null）
     * 未表明为子账户，业务确认取值后仅需同步替换该常量与用例桩数据字面值。
     */
    private static final String SUB_ACCOUNT_FLAG = "1";

    private final IRbBusAcctBcc rbBusAcctBcc;

    private final IRbBusRestraintsBcc rbBusRestraintsBcc;

    public ST005Pbc(IRbBusAcctBcc rbBusAcctBcc, IRbBusRestraintsBcc rbBusRestraintsBcc) {
        this.rbBusAcctBcc = rbBusAcctBcc;
        this.rbBusRestraintsBcc = rbBusRestraintsBcc;
    }

    @Override
    public ST005OutputBO execute(ST005InputBO input) {
        ST005OutputBO output = new ST005OutputBO();
        // 第 1 步（REQ-001）：以输入{账号}为条件查询【账户信息】，取得该记录的主账户标志与上级账户内部键
        RbBusAcctEO acctRecord = queryAcctByBaseAcctNo(input.getBaseAcctNo());
        // 输出[账号]与[主账户标志]取自本次查询所得记录的字段值（判定为子账户时也不改取主账户记录的值）
        output.setBaseAcctNo(acctRecord.getBaseAcctNo());
        output.setLeadAcctFlag(acctRecord.getLeadAcctFlag());
        // 第 2 步（REQ-002）：按同一子账户判定设置[待查账户]；主账户标志未表明为子账户时不执行按上级账户内部键的查询
        String targetAcctNo = resolveTargetAcctNo(input.getBaseAcctNo(), acctRecord);
        // 第 3 步（REQ-003）：以[待查账户]与限制状态"A-生效"为条件查询【账户限制信息】；未查得匹配记录不构成失败
        List<RbBusRestraintsEO> restraints = queryEffectiveRestraints(targetAcctNo);
        // 第 4 步（REQ-004）：每条生效限制一个元素、同一下标三集合元素来自同一条记录；未查得时均为空集合
        assignRestraintOutputs(output, restraints);
        // 无业务失败场景（REQ-005）：全部业务情形（含未查得任何生效限制）均成功结束，不设置业务错误码与错误信息
        output.setSucceed(true);
        return output;
    }

    /**
     * 第 1 步（REQ-001）：按账号等值查询【账户信息】，返回该账号的账户记录。
     */
    private RbBusAcctEO queryAcctByBaseAcctNo(String baseAcctNo) {
        RbBusAcctEO queryEo = new RbBusAcctEO();
        queryEo.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(queryEo);
        return acctList.get(0);
    }

    /**
     * 第 2 步（REQ-002）：按与第 1 步相同的子账户判定设置[待查账户]。
     * 主账户标志表明为子账户时，以$上级账户内部键$（主账户记录主键）查询【账户信息】
     * 取得$主账户账号$作为待查账户；未表明为子账户时不执行该查询，待查账户为输入{账号}。
     */
    private String resolveTargetAcctNo(String inputBaseAcctNo, RbBusAcctEO acctRecord) {
        if (!SUB_ACCOUNT_FLAG.equals(acctRecord.getLeadAcctFlag())) {
            return inputBaseAcctNo;
        }
        RbBusAcctEO mainAcctRecord = rbBusAcctBcc.findByPrimaryKey(acctRecord.getParentInternalKey());
        return mainAcctRecord.getBaseAcctNo();
    }

    /**
     * 第 3 步（REQ-003）：以待查账户与限制状态"A-生效"为条件查询【账户限制信息表】，
     * 返回全部匹配的生效限制记录，空结果不构成失败。
     */
    private List<RbBusRestraintsEO> queryEffectiveRestraints(String targetAcctNo) {
        RbBusRestraintsEO queryEo = new RbBusRestraintsEO();
        queryEo.setBaseAcctNo(targetAcctNo);
        queryEo.setRestraintsStatus(RestraintsStatus.A);
        return rbBusRestraintsBcc.findByEo(queryEo);
    }

    /**
     * 第 4 步（REQ-004）：将[账户限制信息]逐条赋值到输出三个集合，同一下标元素来自同一条
     * 生效限制记录；未查得生效限制时三个集合均为不含任何元素的空集合。
     */
    private void assignRestraintOutputs(ST005OutputBO output, List<RbBusRestraintsEO> restraints) {
        List<String> resSeqNoList = new ArrayList<>();
        List<RestraintType> restraintTypeList = new ArrayList<>();
        List<RestraintsStatus> restraintsStatusList = new ArrayList<>();
        for (RbBusRestraintsEO restraint : restraints) {
            resSeqNoList.add(restraint.getResSeqNo());
            restraintTypeList.add(restraint.getRestraintType());
            restraintsStatusList.add(restraint.getRestraintsStatus());
        }
        output.setResSeqNo(resSeqNoList);
        output.setRestraintType(restraintTypeList);
        output.setRestraintsStatus(restraintsStatusList);
    }
}
