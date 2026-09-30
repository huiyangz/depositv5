package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.DealFlow;

/**
 * ST099 处理限额 输出BO。
 *
 * 字段定义来自正式 Spec ST099「步骤输出」表。空值语义：dealFlow 非必填，
 * 查询未命中配置记录、或命中记录但处理方式为空时为 null。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null。
 */
public class ST099OutputBO extends StepResult {

    /** 处理方式（A-授权 B-拒绝 D-提醒），供交易编排后续处置使用 */
    private DealFlow dealFlow;

    public DealFlow getDealFlow() {
        return dealFlow;
    }

    public void setDealFlow(DealFlow dealFlow) {
        this.dealFlow = dealFlow;
    }
}
