package com.dcits.depsit.facade.bo;

import java.util.Date;

import com.dcits.common.step.StepResult;

/**
 * ST103 检查限额场景配置是否有效 输出BO。
 *
 * 字段定义来自正式 Spec ST103「输出」表，5 个字段均非必填，来源实体均为
 * 限额控制配置表（RB_LIMIT_CTRL_CONF）。空值语义：仅在查询到配置、四个控制
 * 区间字段齐全且区间判定满足时取命中配置记录的值（limitSceneNo 与查询所用
 * 输入值相同，主键同值）；未查询到配置、任一控制区间字段为空（配置无效）
 * 或区间判定不满足时，5 个字段均为 null（不赋值）。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST103OutputBO extends StepResult {

    /** 限额场景编码，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private String limitSceneNo;
    /** 限额控制开始日期，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private Date limitCtrlBgnDate;
    /** 限额控制结束日期，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private Date limitCtrlEndDate;
    /** 限额控制开始时间，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private Date limitCtrlBgnTime;
    /** 限额控制结束时间，来源：限额控制配置表（RB_LIMIT_CTRL_CONF） */
    private Date limitCtrlEndTime;

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public Date getLimitCtrlBgnDate() {
        return limitCtrlBgnDate;
    }

    public void setLimitCtrlBgnDate(Date limitCtrlBgnDate) {
        this.limitCtrlBgnDate = limitCtrlBgnDate;
    }

    public Date getLimitCtrlEndDate() {
        return limitCtrlEndDate;
    }

    public void setLimitCtrlEndDate(Date limitCtrlEndDate) {
        this.limitCtrlEndDate = limitCtrlEndDate;
    }

    public Date getLimitCtrlBgnTime() {
        return limitCtrlBgnTime;
    }

    public void setLimitCtrlBgnTime(Date limitCtrlBgnTime) {
        this.limitCtrlBgnTime = limitCtrlBgnTime;
    }

    public Date getLimitCtrlEndTime() {
        return limitCtrlEndTime;
    }

    public void setLimitCtrlEndTime(Date limitCtrlEndTime) {
        this.limitCtrlEndTime = limitCtrlEndTime;
    }
}
