package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST017InputBO;
import com.dcits.depsit.facade.bo.ST017OutputBO;

/**
 * ST017 设置贷记交易的借贷标志 步骤接口
 *
 * <p>依据正式 Spec ST017：执行时将输出数据项 [借贷标志]（crDrInd）无条件
 * 赋值为固定值 "C-贷方"（CrDrInd.C），作为步骤输出供交易后续环节使用；
 * 赋值不依赖任何输入、数据状态或外部调用。</p>
 *
 * <p>本步骤为常量赋值，无数据实体读写与外部服务调用，对调用方无事务要求；
 * 需求未定义业务失败场景，不存在置 false 的分支，技术异常按原样向调用方传播。</p>
 */
public interface IST017 {

    /**
     * 执行设置贷记交易的借贷标志步骤
     *
     * @param input 输入BO（需求未定义输入字段，空 BO）
     * @return 输出BO（crDrInd 借贷标志，恒为 CrDrInd.C（"C-贷方"）；succeed=true）
     */
    ST017OutputBO execute(ST017InputBO input);
}
