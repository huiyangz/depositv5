package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.TranBranch;

/**
 * ST018 检查交易机构 输出BO。
 *
 * 业务字段定义来自正式 Spec ST018「输出」表：acctBranch（账户开立行行号，非必填）。
 * 空值语义：acctBranch 仅取决于步骤1查询结果（命中记录的账户开立行行号），
 * 不依赖步骤2分支结论，两条分支下均按此输出；$通存标志$ 不进入输出。
 * gotoStepName 为跳转信号承载字段（工程公共契约
 * com.dcits.common.step.GotoStepCondition：运行时按步骤返回结果的 gotoStepName 值
 * 决定是否跳转及跳转目标）：仅{交易机构}与[账户开立行行号]不一致分支取值
 * 《检查存入账户通存标志》，一致分支（检查结果"通过"）为 null。
 * 本步骤无业务失败场景，成功时错误码与错误信息为 null（跳转非业务失败）。
 */
public class ST018OutputBO extends StepResult {

    /** 账户开立行行号，来源：对公存款账户主表（RB_BUS_ACCT） */
    private TranBranch acctBranch;
    /** 跳转目标步骤名称：仅不一致分支取"检查存入账户通存标志"，一致分支为 null（不跳转） */
    private String gotoStepName;

    public TranBranch getAcctBranch() {
        return acctBranch;
    }

    public void setAcctBranch(TranBranch acctBranch) {
        this.acctBranch = acctBranch;
    }

    public String getGotoStepName() {
        return gotoStepName;
    }

    public void setGotoStepName(String gotoStepName) {
        this.gotoStepName = gotoStepName;
    }
}
