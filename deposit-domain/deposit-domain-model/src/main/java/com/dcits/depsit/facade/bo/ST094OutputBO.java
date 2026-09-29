package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.AllDepInd;

/**
 * ST094 检查存入账户通存标志 输出BO。
 *
 * 字段定义来自正式 Spec ST094「输出」表。空值语义：allDepInd 为步骤1取得的
 * $通存标识$，未取得（账号无匹配记录，或匹配记录 ALL_DEP_IND 为空）时为 null，
 * 不填充默认值；其取值不因步骤2判定结果（返回错误码或继续执行）而改变。
 */
public class ST094OutputBO extends StepResult {

    /** 通存标识，来源：对公存款账户主表（RB_BUS_ACCT） */
    private AllDepInd allDepInd;

    public AllDepInd getAllDepInd() {
        return allDepInd;
    }

    public void setAllDepInd(AllDepInd allDepInd) {
        this.allDepInd = allDepInd;
    }
}
