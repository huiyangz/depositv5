package com.dcits.depsit.facade.bo;

import java.util.List;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;

/**
 * ST005 检查账户是否存在限制 输出BO
 *
 * <p>依据正式 Spec ST005「输出」：baseAcctNo 与 leadAcctFlag 取自按输入账号查询所得
 * 【账户信息】记录的字段值（判定为子账户时也不改取主账户记录的值，记录字段为空则输出为空）；
 * resSeqNo、restraintType、restraintsStatus 三个集合每条生效限制一个元素，同一下标元素
 * 来自同一条生效限制记录，未查得生效限制时均为不含任何元素的空集合（需求未规定元素顺序）。
 * 成功/错误字段继承 {@link StepResult}。</p>
 */
public class ST005OutputBO extends StepResult {

    /** 账号；取自按输入账号查询所得【账户信息】记录的字段值原样输出 */
    private String baseAcctNo;

    /** 主账户标志；同一次查询所得记录字段值原样输出，记录字段为空则输出为空 */
    private String leadAcctFlag;

    /** 限制编号集合；每条生效限制一个元素，未查得生效限制时为空集合 */
    private List<String> resSeqNo;

    /** 账户限制类型集合；每条生效限制一个元素，与 resSeqNo 同下标元素来自同一条记录 */
    private List<RestraintType> restraintType;

    /** 限制状态集合；因查询条件限定状态为"A-生效"，每个元素值均为 RestraintsStatus.A */
    private List<RestraintsStatus> restraintsStatus;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public String getLeadAcctFlag() {
        return leadAcctFlag;
    }

    public void setLeadAcctFlag(String leadAcctFlag) {
        this.leadAcctFlag = leadAcctFlag;
    }

    public List<String> getResSeqNo() {
        return resSeqNo;
    }

    public void setResSeqNo(List<String> resSeqNo) {
        this.resSeqNo = resSeqNo;
    }

    public List<RestraintType> getRestraintType() {
        return restraintType;
    }

    public void setRestraintType(List<RestraintType> restraintType) {
        this.restraintType = restraintType;
    }

    public List<RestraintsStatus> getRestraintsStatus() {
        return restraintsStatus;
    }

    public void setRestraintsStatus(List<RestraintsStatus> restraintsStatus) {
        this.restraintsStatus = restraintsStatus;
    }
}
