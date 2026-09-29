package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;

/**
 * ST007 检查质押类限制 输出BO。
 *
 * 字段定义来自正式 Spec ST007「输出」表。空值语义：无生效限制记录时 5 个字段均为 null；
 * 有生效记录时记录来源三字段（resSeqNo、restraintType、restraintsStatus）有值，
 * 类型来源两字段（pledgedFlag、status）仅在被处理记录的类型存在生效类型记录时被赋值，
 * 各字段取最后一次赋值的结果。本步骤仅查询赋值返回、不含质押标志命中判定；
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST007OutputBO extends StepResult {

    /** 限制编号，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private String resSeqNo;
    /** 账户限制类型，来源：对公存款账户限制表（RB_BUS_RESTRAINTS） */
    private RestraintType restraintType;
    /** 限制状态，来源：对公存款账户限制表（RB_BUS_RESTRAINTS），查询条件恒为"生效"，有值时恒为 RestraintsStatus.A */
    private RestraintsStatus restraintsStatus;
    /** 质押标志，来源：存款限制类型表（RB_RESTRAINT_TYPE），取值含义需求未定义，原样透传 */
    private String pledgedFlag;
    /** 状态，来源：存款限制类型表（RB_RESTRAINT_TYPE），查询条件恒为"生效"，有值时恒为 Status.A */
    private Status status;

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

    public String getPledgedFlag() {
        return pledgedFlag;
    }

    public void setPledgedFlag(String pledgedFlag) {
        this.pledgedFlag = pledgedFlag;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
