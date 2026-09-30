package com.dcits.depsit.facade.bo;

/**
 * ST099 处理限额 输入BO。
 *
 * 字段定义来自正式 Spec ST099「步骤输入」表：limitBranchId（限额机构编码）、
 * limitSceneNo（限额场景编码），均必填，两字段为 RB_LIMIT_CTRL_CONF 主键。
 * 需求仅标记必填、未定义校验行为（无业务失败场景），本 BO 不做校验。
 */
public class ST099InputBO {

    /** 限额机构编码 */
    private String limitBranchId;

    /** 限额场景编码 */
    private String limitSceneNo;

    public String getLimitBranchId() {
        return limitBranchId;
    }

    public void setLimitBranchId(String limitBranchId) {
        this.limitBranchId = limitBranchId;
    }

    public String getLimitSceneNo() {
        return limitSceneNo;
    }

    public void setLimitSceneNo(String limitSceneNo) {
        this.limitSceneNo = limitSceneNo;
    }
}
