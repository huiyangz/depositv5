package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST032InputBO;
import com.dcits.depsit.facade.bo.ST032OutputBO;

/**
 * ST032 设置借记交易的借贷标志。
 *
 * 为借记交易将输出数据[借贷标志]无条件赋值为"D-借方"（CrDrInd.D）并作为
 * 步骤输出返回。纯赋值步骤：无输入字段、无数据访问、无外部调用、无状态
 * 变更副作用，无事务要求。
 */
public interface IST032 {

    /**
     * 设置借记交易的借贷标志。
     *
     * @param input 输入BO（正式 Spec 声明无输入字段，不承载业务数据）
     * @return 输出BO：crDrInd 恒为 CrDrInd.D（"D-借方"）。本步骤无业务失败场景，
     *         失败仅由技术异常原样传播表达。
     */
    ST032OutputBO execute(ST032InputBO input);
}
