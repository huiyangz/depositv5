package com.dcits.depsit.facade.bo;

/**
 * ST100 获取累计限额 输入BO。
 *
 * 字段定义来自正式 Spec ST100「输入」表：baseAcctNo（账号，必填，作为限额检查
 * 对象值 checkObjVal 参与查询）、clientNo（客户号，必填）、limitSceneNo（限额
 * 场景编码，必填）。需求未定义必填输入缺失或为空时的处理（「失败处理」声明无
 * 业务失败场景），本 BO 不做校验，必填性由上送方保证。
 */
public class ST100InputBO {

    /** 账号，作为限额检查对象值（checkObjVal）与 RB_LIMIT_SUM_INFO.CHECK_OBJ_VAL 等值匹配 */
    private String baseAcctNo;

    /** 客户号，与 RB_LIMIT_SUM_INFO.CLIENT_NO 等值匹配 */
    private String clientNo;

    /** 限额场景编码，与 RB_LIMIT_SUM_INFO.LIMIT_SCENE_NO 等值匹配 */
    private String limitSceneNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

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
}
