package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.ClientType;

/**
 * ST086 检查客户类型 输出BO。
 *
 * 字段定义来自正式 Spec ST086「输出」表。空值语义：clientType 非必填，
 * 步骤1 查询无记录时为 null（不填充默认值），有记录时取该记录 CLIENT_TYPE，
 * 且不因步骤2 判定结果清空或改写。检查结果绑定 StepResult：
 * 客户类型为"公司"时 succeed=true；不为"公司"（含未取得）时 succeed=false、errorCode="ER0042"。
 */
public class ST086OutputBO extends StepResult {

    /** 客户类型，来源：客户副本表（FM_CLIENT_COPY） */
    private ClientType clientType;

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }
}
