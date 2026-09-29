package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.facade.bo.ST017InputBO;
import com.dcits.depsit.facade.bo.ST017OutputBO;

/**
 * ST017 设置贷记交易的借贷标志（交易执行步骤）
 *
 * <p>依据正式 Spec ST017：执行时将输出数据项 [借贷标志]（crDrInd）无条件
 * 赋值为固定值 "C-贷方"（CrDrInd.C），作为步骤输出供交易后续环节使用。
 * 赋值不依赖任何输入、数据状态或外部调用；无数据实体读写，
 * 无业务失败场景，技术异常按原样向调用方传播。</p>
 */
@Service
public class ST017Pbc implements IST017 {

    @Override
    public ST017OutputBO execute(ST017InputBO input) {
        ST017OutputBO output = new ST017OutputBO();
        // 步骤描述 第1条（REQ-001）：无条件赋值[借贷标志]为 "C-贷方"（CrDrInd.C），不依赖输入、数据状态或外部调用
        output.setCrDrInd(CrDrInd.C);
        // 常量赋值无失败分支（REQ-001-S01）：成功结束，errorCode 与 errorMessage 保持 null
        output.setSucceed(true);
        return output;
    }
}
