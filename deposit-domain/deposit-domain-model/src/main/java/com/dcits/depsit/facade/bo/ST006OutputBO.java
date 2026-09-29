package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DrCrCtlFlag;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;

/**
 * ST006 检查是否存在转账止付限制 输出BO
 *
 * <p>依据正式 Spec ST006「输出」：stopFlag 为第 3 步计算结果（取值"是"/"否"，全部业务情形下均有值）；
 * 6 个回显字段中 resSeqNo、restraintType、restraintsStatus 取自【账户限制信息】满足条件记录，
 * drCrCtlFlag、status、transferFlag 取自其账户限制类型对应的【限制类型表】生效记录；
 * stopFlag="否" 时回显字段全部置空（null）。成功/错误字段继承 {@link StepResult}。</p>
 */
public class ST006OutputBO extends StepResult {

    /** 限制编号（回显，取自对公存款账户限制表；stopFlag="否" 时为 null） */
    private String resSeqNo;

    /** 账户限制类型（回显，取自对公存款账户限制表；stopFlag="否" 时为 null） */
    private RestraintType restraintType;

    /** 限制状态（回显，取自对公存款账户限制表；stopFlag="否" 时为 null） */
    private RestraintsStatus restraintsStatus;

    /** 借方贷方控制标志（回显，取自存款限制类型表生效记录；stopFlag="否" 时为 null） */
    private DrCrCtlFlag drCrCtlFlag;

    /** 状态（回显，取自存款限制类型表生效记录；stopFlag="否" 时为 null） */
    private Status status;

    /** 转账标志（回显，取自存款限制类型表生效记录；stopFlag="否" 时为 null） */
    private String transferFlag;

    /** 转账止付标志，第 3 步计算结果，取值"是"/"否" */
    private String stopFlag;

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
