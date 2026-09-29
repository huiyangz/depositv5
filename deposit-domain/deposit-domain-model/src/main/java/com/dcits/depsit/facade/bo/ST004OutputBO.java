package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;

/**
 * ST004 检查转账止收限制 输出BO。
 *
 * 字段定义来自正式 Spec ST004「输出」表。空值语义：transferStopFlag 恒有值（"是"/"否"）；
 * 其余 7 个字段仅在转账止收标志为"是"时取命中记录的值，为"否"时均为 null。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST004OutputBO extends StepResult {

    /** 转账止收标志，取值"是"/"否" */
    private String transferStopFlag;
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

    public String getTransferStopFlag() {
        return transferStopFlag;
    }

    public void setTransferStopFlag(String transferStopFlag) {
        this.transferStopFlag = transferStopFlag;
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
