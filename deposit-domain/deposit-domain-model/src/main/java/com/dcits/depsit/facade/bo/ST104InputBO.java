package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

/**
 * ST104 更新累计限额 输入BO。
 *
 * 字段定义来自正式 Spec ST104「输入」表（6 个字段，全部必填）。
 * 字段名"限额检查结果"与"否"为需求输入表原名字段（"否"即限额累计笔数，
 * 与工程数据模型 RbLimitSumInfoEO 既有命名一致，Spec 明确不更名）。
 * 必填性由上送方保证；需求未定义输入缺失、为空或非法值时的处理（无业务失败场景），
 * 本 BO 不做校验。clientNo 为声明必填输入，本步骤对其无使用。
 */
public class ST104InputBO {

    /** 限额检查结果，按字面值与"未超限"比较，工程内无对应枚举类 */
    private String 限额检查结果;
    /** 账号，作为限额累计信息表主键成员之一的限额检查对象值（checkObjVal）使用 */
    private String baseAcctNo;
    /** 限额场景编码，限额累计信息表主键成员之二（limitSceneNo） */
    private String limitSceneNo;
    /** 客户号，来源实体：限额累计信息表（RB_LIMIT_SUM_INFO），本步骤无使用 */
    private String clientNo;
    /** 限额累计金额，参与触发条件判定（大于 0），满足条件时作为写入限额累计金额的值 */
    private BigDecimal limitSumAmt;
    /** 限额累计笔数（字段名"否"为数据模型既有命名），仅参与触发条件判定（大于 0），不作为更新内容 */
    private Integer 否;

    public String get限额检查结果() {
        return 限额检查结果;
    }

    public void set限额检查结果(String 限额检查结果) {
        this.限额检查结果 = 限额检查结果;
    }

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
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
