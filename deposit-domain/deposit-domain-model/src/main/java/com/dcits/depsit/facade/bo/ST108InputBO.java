package com.dcits.depsit.facade.bo;

import java.util.Date;

/**
 * ST108 检查增加限制起始日期 输入BO。
 *
 * 字段定义来自正式 Spec ST108「输入」表：runDate（系统日期，必填，上送方按
 * 系统日期表 FM_DATE 核心运行日期提供）、startDate（开始日期，必填，上送）、
 * endDate（结束日期，必填，上送）。
 * 需求未定义任一输入缺失或为 null 时的处理（无业务失败场景），本 BO 不做校验，
 * 必填性由上送方保证。
 */
public class ST108InputBO {

    /** 系统日期（核心运行日期） */
    private Date runDate;

    /** 开始日期 */
    private Date startDate;

    /** 结束日期 */
    private Date endDate;

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
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
}
