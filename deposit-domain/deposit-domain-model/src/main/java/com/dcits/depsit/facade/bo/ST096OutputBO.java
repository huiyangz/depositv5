package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.ResBranchRange;
import com.dcits.depsit.enums.TranBranch;

/**
 * ST096 检查账户机构是否可匹配到限额场景配置 输出BO。
 *
 * 字段定义来自正式 Spec ST096「输出」两张表。空值语义：
 * 限额场景配置组（limitSceneNo、limitBranchId、limitBranchRange、validFlag）取自最终命中的
 * 启用限额控制配置记录（账户开立行直接命中或上级机构命中），未命中任何限额场景配置时均为 null；
 * baseAcctNo、acctBranch 为步骤1查询【账户信息】结果的透传；
 * branch、attachedTo 仅在步骤4执行（步骤2未直接命中）且账户开立行在【机构信息表】存在记录时
 * 取自该记录，账户开立行直接命中限额场景（未查询【机构信息表】）或该记录不存在时均为 null。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST096OutputBO extends StepResult {

    /** 限额场景编码，来源：限额控制配置表（RB_LIMIT_CTRL_CONF）命中启用记录 */
    private String limitSceneNo;
    /** 限额机构编码，来源：限额控制配置表（RB_LIMIT_CTRL_CONF）命中启用记录 */
    private TranBranch limitBranchId;
    /** 限额机构范围，来源：限额控制配置表（RB_LIMIT_CTRL_CONF）命中启用记录 */
    private ResBranchRange limitBranchRange;
    /** 启用标志，来源：限额控制配置表（RB_LIMIT_CTRL_CONF）命中启用记录 */
    private String validFlag;
    /** 账号，来源：对公存款账户主表（RB_BUS_ACCT）步骤1查询结果透传 */
    private String baseAcctNo;
    /** 账户开立行行号，来源：对公存款账户主表（RB_BUS_ACCT）步骤1查询结果透传 */
    private TranBranch acctBranch;
    /** 归属机构号，来源：机构信息表（FM_BRANCH）账户开立行自身记录 */
    private TranBranch branch;
    /** 归属上级机构号，来源：机构信息表（FM_BRANCH）账户开立行自身记录 */
    private TranBranch attachedTo;

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public TranBranch getLimitBranchId() {
        return limitBranchId;
    }

    public void setLimitBranchId(TranBranch limitBranchId) {
        this.limitBranchId = limitBranchId;
    }

    public ResBranchRange getLimitBranchRange() {
        return limitBranchRange;
    }

    public void setLimitBranchRange(ResBranchRange limitBranchRange) {
        this.limitBranchRange = limitBranchRange;
    }

    public String getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(String validFlag) {
        this.validFlag = validFlag;
    }

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public TranBranch getAcctBranch() {
        return acctBranch;
    }

    public void setAcctBranch(TranBranch acctBranch) {
        this.acctBranch = acctBranch;
    }

    public TranBranch getBranch() {
        return branch;
    }

    public void setBranch(TranBranch branch) {
        this.branch = branch;
    }

    public TranBranch getAttachedTo() {
        return attachedTo;
    }

    public void setAttachedTo(TranBranch attachedTo) {
        this.attachedTo = attachedTo;
    }
}
