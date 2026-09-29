package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.Ccy;

/**
 * ST023 检查机构币种交易权限 输出BO。
 *
 * 字段定义来自正式 Spec ST023「输出」表。空值语义：检查"通过"时 ccy 为命中记录的币种
 * （主键（归属机构号、币种）决定至多一条命中，值等于输入{交易币种}）；检查"不通过"
 * （含列表为空）时无命中记录，ccy 为 null（不赋值）。检查"通过"/"不通过"按 StepResult
 * 契约表达：成功时错误码与错误信息为 null，不通过时 succeed=false 且 errorCode="ER0047"。
 */
public class ST023OutputBO extends StepResult {

    /** 币种，来源：机构币种表（FM_BRANCH_CCY） */
    private Ccy ccy;

    public Ccy getCcy() {
        return ccy;
    }

    public void setCcy(Ccy ccy) {
        this.ccy = ccy;
    }
}
