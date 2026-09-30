package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.enums.DocClass;

/**
 * ST089 检查存入账户黑名单 输出BO。
 *
 * 字段定义来自正式 Spec ST089「输出」表。空值语义（规范常量）：四个分支 eventType
 * 均为 "CRET"、docClass 均为子步骤 1 查询所得凭证种类（子步骤 1、2 先于分支判定
 * 无条件执行）；通过分支 dealFlow 为 null（不赋值），提醒/授权/拒绝分支 dealFlow
 * 分别取 DealFlow.D/A/B；仅授权分支 authFlag="是"；拒绝分支为非成功返回
 * （errorCode="ER0057"），其余分支成功时错误码与错误信息为 null。
 */
public class ST089OutputBO extends StepResult {

    /** 事件类型，固定 "CRET"（子步骤 2 常量赋值，无枚举绑定） */
    private String eventType;
    /** 处理方式，取 ST105 返回的 dealFlow（"通过"时为 null，不赋值） */
    private DealFlow dealFlow;
    /** 授权标志，"授权"分支为 "是"（需求文本常量，无枚举绑定） */
    private String authFlag;
    /** 凭证种类，子步骤 1 查询凭证类型定义表（TB_VOUCHER_DEF）所得 */
    private DocClass docClass;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public DealFlow getDealFlow() {
        return dealFlow;
    }

    public void setDealFlow(DealFlow dealFlow) {
        this.dealFlow = dealFlow;
    }

    public String getAuthFlag() {
        return authFlag;
    }

    public void setAuthFlag(String authFlag) {
        this.authFlag = authFlag;
    }

    public DocClass getDocClass() {
        return docClass;
    }

    public void setDocClass(DocClass docClass) {
        this.docClass = docClass;
    }
}
