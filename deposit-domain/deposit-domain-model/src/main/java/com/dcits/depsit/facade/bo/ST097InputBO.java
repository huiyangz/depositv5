package com.dcits.depsit.facade.bo;

import java.math.BigDecimal;

/**
 * ST097 计算限额累计金额 输入BO。
 *
 * 字段定义来自正式 Spec ST097「输入」表：limitSceneNo（限额场景编码）、tranAmt（交易金额）、
 * clientNo（客户号）、checkObjVal（限额检查对象值），均必填。
 * 需求未定义输入缺失或非法时的处理（无业务失败场景），本 BO 不做校验。
 */
public class ST097InputBO {

    /** 限额场景编码 */
    private String limitSceneNo;
    /** 交易金额 */
    private BigDecimal tranAmt;
    /** 客户号（客户维度定位【限额控制客户自定义配置表】） */
    private String clientNo;
    /** 限额检查对象值（按【限额场景定义表】的检查对象类型取对应值，如客户号/账号/卡号） */
    private String checkObjVal;

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public String getCheckObjVal() {
        return checkObjVal;
    }

    public void setCheckObjVal(String checkObjVal) {
        this.checkObjVal = checkObjVal;
    }
}
