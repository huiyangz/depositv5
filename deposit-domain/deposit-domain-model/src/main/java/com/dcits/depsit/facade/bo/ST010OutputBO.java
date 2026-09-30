package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;

/**
 * ST010 检查是否存在转账不收不付限制 输出BO。
 *
 * 字段定义来自正式 Spec ST010「输出」表。空值语义：transferNoRecvNoPayFlag
 * 每次成功执行均返回（"是"/"否"）；其余 7 个字段仅在命中时取第一条命中记录
 * 及其对应生效限制类型记录的值，未命中时需求未规定其取值（本实现不赋值，保持 null）。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST010OutputBO extends StepResult {

    /** 转账不收不付标志，取值"是"/"否" */
    private String transferNoRecvNoPayFlag;
    /** 限制编号，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private String resSeqNo;
    /** 账户限制类型，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private RestraintType restraintType;
    /** 限制状态，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private RestraintsStatus restraintsStatus;
    /** 借方贷方控制标志，来源：存款限制类型表（RB_RESTRAINT_TYPE） */
    private DrCrCtlFlag drCrCtlFlag;
    /** 状态，来源：存款限制类型表（RB_RESTRAINT_TYPE） */
    private Status status;
    /** 转账标志，来源：存款限制类型表（RB_RESTRAINT_TYPE） */
    private String transferFlag;
    /** 止付标志，来源：存款限制类型表（RB_RESTRAINT_TYPE） */
    private String stopFlag;

    public String getTransferNoRecvNoPayFlag() {
        return transferNoRecvNoPayFlag;
    }

    public void setTransferNoRecvNoPayFlag(String transferNoRecvNoPayFlag) {
        this.transferNoRecvNoPayFlag = transferNoRecvNoPayFlag;
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

    public DrCrCtlFlag getDrCrCtlFlag() {
        return drCrCtlFlag;
    }

    public void setDrCrCtlFlag(DrCrCtlFlag drCrCtlFlag) {
        this.drCrCtlFlag = drCrCtlFlag;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTransferFlag() {
        return transferFlag;
    }

    public void setTransferFlag(String transferFlag) {
        this.transferFlag = transferFlag;
    }

    public String getStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(String stopFlag) {
        this.stopFlag = stopFlag;
    }
}
