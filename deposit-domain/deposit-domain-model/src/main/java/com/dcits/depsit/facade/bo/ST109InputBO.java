package com.dcits.depsit.facade.bo;

import java.util.Date;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.TermType;

/**
 * ST109 登记账户限制信息 输入BO。
 *
 * 字段定义来自正式 Spec ST109「输入」表：8 个字段均为必填。
 * 其中 runDate（核心运行日期）为必填输入，但登记目标实体 RbBusRestraintsEO
 * 无同名字段，不映射写入（Spec REQ-002）。
 * 需求未定义任一输入缺失或为空时的处理（无业务失败场景），本 BO 不做校验。
 */
public class ST109InputBO {

    /** 账号（步骤原文{账号}） */
    private String baseAcctNo;
    /** 账户限制类型（步骤原文{限制类型}） */
    private RestraintType restraintType;
    /** 开始日期（步骤原文{开始日期}） */
    private Date startDate;
    /** 结束日期（步骤原文{结束日期}） */
    private Date endDate;
    /** 存期期限（步骤原文{限制期限}） */
    private String term;
    /** 周期类型（步骤原文{限制期限类型}，枚举代码名"期限类型"） */
    private TermType termType;
    /** 交易日期（步骤原文{交易日期}） */
    private Date tranDate;
    /** 核心运行日期；实体无同名字段，不映射写入（Spec REQ-002） */
    private Date runDate;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public RestraintType getRestraintType() {
        return restraintType;
    }

    public void setRestraintType(RestraintType restraintType) {
        this.restraintType = restraintType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public Date getTranDate() {
        return tranDate;
    }

    public void setTranDate(Date tranDate) {
        this.tranDate = tranDate;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }
}
