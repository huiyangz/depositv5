package com.dcits.depsit.facade.bo;

import com.dcits.common.step.StepResult;
import com.dcits.depsit.enums.CrDrInd;

/**
 * ST017 设置贷记交易的借贷标志 输出BO
 *
 * <p>依据正式 Spec ST017「输出」：crDrInd 借贷标志（数据项别名：借贷标志、
 * 输出数据-借贷标志），类型 com.dcits.depsit.enums.CrDrInd，取值域为代码[借贷标志]
 * （C-贷、D-借）；本步骤成功执行后恒为 CrDrInd.C（"C-贷方"）。
 * 成功/错误字段继承 {@link StepResult}。</p>
 */
public class ST017OutputBO extends StepResult {

    /** 借贷标志（数据项别名：借贷标志、输出数据-借贷标志）；本步骤成功执行后恒为 CrDrInd.C（"C-贷方"） */
    private CrDrInd crDrInd;

    public CrDrInd getCrDrInd() {
        return crDrInd;
    }

    public void setCrDrInd(CrDrInd crDrInd) {
        this.crDrInd = crDrInd;
    }
}
