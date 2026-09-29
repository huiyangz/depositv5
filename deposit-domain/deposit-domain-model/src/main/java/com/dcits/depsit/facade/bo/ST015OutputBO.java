package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.RestraintLevel;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;

/**
 * ST015 检查是否存在属性限制 输出BO。
 *
 * 字段定义来自正式 Spec ST015「输出」表。空值语义：natureRestraintFlag 按输出表
 * 标记为非必填，但步骤2两个分支均对其赋值（"是"/"否"）；其余 4 个字段仅在属性
 * 限制标志为"是"时取命中记录的值，为"否"时均为 null（不赋值）。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST015OutputBO extends StepResult {

    /** 属性限制标志，取值"是"/"否" */
    private String natureRestraintFlag;
    /** 限制编号，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private String resSeqNo;
    /** 账户限制类型，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private RestraintType restraintType;
    /** 限制状态，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private RestraintsStatus restraintsStatus;
    /** 限制级别，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private RestraintLevel restraintLevel;

    public String getNatureRestraintFlag() {
        return natureRestraintFlag;
    }

    public void setNatureRestraintFlag(String natureRestraintFlag) {
        this.natureRestraintFlag = natureRestraintFlag;
    }

    public String getResSeqNo() {
        return resSeqNo;
    }

    public void setResSeqNo(String resSeqNo) {
        this.resSeqNo = resSeqNo;
    }

    public RestraintType getRestraintType() {
        return restraintType;
    }

    public void setRestraintType(RestraintType restraintType) {
        this.restraintType = restraintType;
    }

    public RestraintsStatus getRestraintsStatus() {
        return restraintsStatus;
    }

    public void setRestraintsStatus(RestraintsStatus restraintsStatus) {
        this.restraintsStatus = restraintsStatus;
    }

    public RestraintLevel getRestraintLevel() {
        return restraintLevel;
    }

    public void setRestraintLevel(RestraintLevel restraintLevel) {
        this.restraintLevel = restraintLevel;
    }
}
