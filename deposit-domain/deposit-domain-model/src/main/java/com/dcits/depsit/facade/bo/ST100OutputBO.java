package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

import com.dcits.common.step.StepResult;

/**
 * ST100 获取累计限额 输出BO。
 *
 * 字段定义来自正式 Spec ST100「输出」表。空值语义：四个字段均非必填；
 * 命中记录时取该记录对应列存值（limitSumAmt、否两列本身可为空，存值为
 * NULL 时输出亦为 null）；未命中记录时四字段均为 null，不填充金额 0、
 * 笔数 0 等默认值。字段名「否」（限额累计笔数）为需求「输出」表、库表
 * DDL 与实体类一致的既有契约，按原样保留不改名。本步骤无业务失败场景，
 * 成功时错误码与错误信息为 null。
 */
public class ST100OutputBO extends StepResult {

    /** 客户号，来源：限额累计信息表（RB_LIMIT_SUM_INFO） */
    private String clientNo;

    /** 限额场景编码，来源：限额累计信息表（RB_LIMIT_SUM_INFO） */
    private String limitSceneNo;

    /** 限额累计金额，对应列 LIMIT_SUM_AMT（DECIMAL(38,2)，可为空），原样透传存值 */
    private BigDecimal limitSumAmt;

    /** 限额累计笔数（既有契约字段名），对应可空 INT 列，原样透传存值 */
    private Integer 否;

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public BigDecimal getLimitSumAmt() {
        return limitSumAmt;
    }

    public void setLimitSumAmt(BigDecimal limitSumAmt) {
        this.limitSumAmt = limitSumAmt;
    }

    public Integer get否() {
        return 否;
    }

    public void set否(Integer 否) {
        this.否 = 否;
    }
}
