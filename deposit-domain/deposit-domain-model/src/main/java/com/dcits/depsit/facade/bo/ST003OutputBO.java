package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;

/**
 * ST003 检查有权机关冻结限制 输出BO
 *
 * <p>依据正式 Spec ST003「输出」：有权机关冻结标志取自【限制类型表】按账户限制类型
 * 查得的生效记录；未查得任一不为空的标志值时不赋值，输出为空（null）。
 * 成功/错误字段继承 {@link StepResult}。</p>
 */
public class ST003OutputBO extends StepResult {

    /** 有权机关冻结标志；未查得非空值时不赋值，输出为空（null） */
    private String ahBuFlag;

    public String getAhBuFlag() {
        return ahBuFlag;
    }

    public void setAhBuFlag(String ahBuFlag) {
        this.ahBuFlag = ahBuFlag;
    }
}
