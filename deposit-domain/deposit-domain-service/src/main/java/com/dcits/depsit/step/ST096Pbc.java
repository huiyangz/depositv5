package com.dcits.depsit.step;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST096InputBO;
import com.dcits.depsit.facade.bo.ST096OutputBO;
import com.dcits.depsit.facade.components.IFmBranchBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.FmBranchEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST096 检查账户机构是否可匹配到限额场景配置。
 *
 * 步骤1 以输入{账号}查询【账户信息】（RB_BUS_ACCT）获取[账户开立行行号]；
 * 步骤2 以该行号作为限额机构编码查询【限额控制配置表】（RB_LIMIT_CTRL_CONF）中
 * $启用标志$="Y-启用"的记录（多条启用记录取查询结果第一条）；
 * 步骤3 取得启用记录即返回，未取得时跳转《获取上级机构集合》；
 * 步骤4 以[账户开立行行号]查询【机构信息表】（FM_BRANCH）取$归属上级机构号$，
 * 沿其逐级向上收集各级上级机构的$机构号$与$机构层级$构成[上级机构集合]（不含开立行自身，
 * $归属上级机构号$为空或按其查询无记录时停止，结果可为空集合）；
 * 步骤5 按$机构层级$从大到小依次以各上级机构$机构号$查询【限额控制配置表】的启用记录，
 * 首个命中即中断返回，全部未命中时限额场景配置组输出为空。
 * 只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST096Pbc implements IST096 {

    /** 启用标志取值：Y-启用（需求文本常量，未绑定枚举） */
    private static final String VALID_FLAG_ENABLED = "Y";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Autowired
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Autowired
    private IFmBranchBcc fmBranchBcc;

    @Override
    public ST096OutputBO execute(ST096InputBO input) {
        ST096OutputBO output = new ST096OutputBO();

        // 步骤1 获取账户开立行行号（REQ-001）：以{账号}等值查询 RB_BUS_ACCT
        RbBusAcctEO busAcct = findBusAcct(input.getBaseAcctNo());
        if (busAcct == null || busAcct.getAcctBranch() == null) {
            // Spec 未定义账号无记录或记录无开立行行号时的处理（「明确不覆盖」），无机构编号可查，
            // 不进行限额场景匹配，正常结束且限额场景配置组输出为空
            output.setSucceed(true);
            return output;
        }
        // 步骤1 透传输出：账号与账户开立行行号取自查询结果
        output.setBaseAcctNo(busAcct.getBaseAcctNo());
        output.setAcctBranch(busAcct.getAcctBranch());

        // 步骤2 获取限额场景编码（REQ-002）：账户开立行直接匹配启用限额场景
        RbLimitCtrlConfEO hitConf = findEnabledLimitConf(busAcct.getAcctBranch());
        if (hitConf == null) {
            // 步骤3 [限额场景编码]为空：跳转《获取上级机构集合》（步骤4），不再返回
            List<FmBranchEO> parentBranches = collectParentBranches(busAcct.getAcctBranch(), output);
            // 步骤5 检查上级机构匹配的限额场景（REQ-004）
            hitConf = matchLimitConfByHierarchy(parentBranches);
        }
        // 步骤2 直接命中时 branch、attachedTo 保持为空（未查询【机构信息表】）
        fillLimitConfOutput(output, hitConf);

        // 无业务失败场景：命中与全部未命中均正常成功结束
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户开立行行号（REQ-001）：以{账号}为条件等值查询【账户信息】（RB_BUS_ACCT），
     * 返回首条查询结果；无匹配记录时返回 null。
     */
    private RbBusAcctEO findBusAcct(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);
        if (acctList == null || acctList.isEmpty()) {
            return null;
        }
        return acctList.get(0);
    }

    /**
     * 步骤2 / 步骤5.1 查询启用限额场景配置：以机构编号作为限额机构编码（limitBranchId）与
     * $启用标志$="Y-启用"为条件查询【限额控制配置表】；存在多条启用记录时取查询结果的第一条
     * （排序口径需求未定义），无启用记录时返回 null。
     */
    private RbLimitCtrlConfEO findEnabledLimitConf(TranBranch branchId) {
        RbLimitCtrlConfEO condition = new RbLimitCtrlConfEO();
        condition.setLimitBranchId(branchId);
        condition.setValidFlag(VALID_FLAG_ENABLED);
        List<RbLimitCtrlConfEO> confList = rbLimitCtrlConfBcc.findByEo(condition);
        if (confList == null || confList.isEmpty()) {
            return null;
        }
        return confList.get(0);
    }

    /**
     * 步骤4 获取上级机构集合（REQ-003）：以[账户开立行行号]查询【机构信息表】（FM_BRANCH）
     * 取$归属上级机构号$，再沿其逐级向上查询，收集各级上级机构的$机构号$（该机构在【机构信息表】
     * 的归属机构号值）与$机构层级$构成[上级机构集合]；集合不含账户开立行自身，当前记录
     * $归属上级机构号$为空或按其查询无记录时停止收集（停止前已收集条目保留），结果可为空集合。
     * 输出 branch、attachedTo 取开立行自身记录（无记录时无值可取、输出为空）。
     */
    private List<FmBranchEO> collectParentBranches(TranBranch acctBranch, ST096OutputBO output) {
        List<FmBranchEO> parentBranches = new ArrayList<>();
        FmBranchEO self = fmBranchBcc.findByBranch(acctBranch);
        if (self == null) {
            // 账户开立行自身无机构记录：空集合，branch、attachedTo 输出为空
            return parentBranches;
        }
        output.setBranch(self.getBranch());
        output.setAttachedTo(self.getAttachedTo());
        TranBranch parentNo = self.getAttachedTo();
        while (parentNo != null) {
            FmBranchEO parent = fmBranchBcc.findByBranch(parentNo);
            if (parent == null) {
                // 上级编号在【机构信息表】无记录：停止收集，保留已收集条目
                break;
            }
            parentBranches.add(parent);
            parentNo = parent.getAttachedTo();
        }
        return parentBranches;
    }

    /**
     * 步骤5 检查上级机构匹配的限额场景（REQ-004）：[上级机构集合]按$机构层级$从大到小排序
     * （"2"支行＞"1"分行＞"0"总行＞"-1"虚拟机构层级）后，依次以各上级机构$机构号$作为限额机构
     * 编码查询启用限额场景配置，首个命中即中断遍历并返回该记录；全部未命中或集合为空时返回 null。
     * 层级相同条目的先后需求未约束，按排序稳定顺序遍历。
     */
    private RbLimitCtrlConfEO matchLimitConfByHierarchy(List<FmBranchEO> parentBranches) {
        List<FmBranchEO> ordered = new ArrayList<>(parentBranches);
        ordered.sort(Comparator.comparingInt((FmBranchEO eo) ->
                Integer.parseInt(eo.getHierarchyCode().getValue())).reversed());
        for (FmBranchEO parent : ordered) {
            // 步骤5.1 以$机构号$查询【限额控制配置表】启用记录
            RbLimitCtrlConfEO hit = findEnabledLimitConf(parent.getBranch());
            if (hit != null) {
                // 步骤5.2 取得启用记录：中断本次遍历并返回
                return hit;
            }
        }
        // 步骤5.3 遍历结束仍未取得：[限额场景编码]为空
        return null;
    }

    /**
     * 限额场景配置组输出（REQ-005）：命中启用限额控制配置记录时 limitSceneNo、limitBranchId、
     * limitBranchRange、validFlag 四字段取自该记录；未命中（null）时不赋值，保持为空。
     */
    private void fillLimitConfOutput(ST096OutputBO output, RbLimitCtrlConfEO hitConf) {
        if (hitConf == null) {
            return;
        }
        output.setLimitSceneNo(hitConf.getLimitSceneNo());
        output.setLimitBranchId(hitConf.getLimitBranchId());
        output.setLimitBranchRange(hitConf.getLimitBranchRange());
        output.setValidFlag(hitConf.getValidFlag());
    }
}
