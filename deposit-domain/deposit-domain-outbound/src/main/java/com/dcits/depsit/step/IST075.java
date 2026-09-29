package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST075InputBO;
import com.dcits.depsit.facade.bo.ST075OutputBO;

/**
 * ST075 设置账户执行利率。
 *
 * 将调用方传入的执行利率（realRate）原值赋值到账户执行利率并通过步骤输出返回，
 * 供后续步骤使用。纯赋值步骤：无数据库读写、无组件调用、无事务要求。
 */
public interface IST075 {

    /**
     * 设置账户执行利率。
     *
     * @param input 输入BO，执行利率（realRate）必填（调用方契约，本步骤不校验）
     * @return 输出BO：realRate 与输入数值和标度完全一致。本步骤无业务失败场景，
     *         正常返回 succeed=true 且错误字段为 null；失败仅以技术异常向上传播
     */
    ST075OutputBO execute(ST075InputBO input);
}
