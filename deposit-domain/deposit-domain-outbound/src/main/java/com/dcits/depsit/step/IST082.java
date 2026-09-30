package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST082InputBO;
import com.dcits.depsit.facade.bo.ST082OutputBO;

/**
 * ST082 登记交易流水。
 *
 * 将输入的交易类型、币种、借贷标志、交易金额登记为一条【交易流水】
 * （RB_BUS_TRAN_JNL），四个登记字段与对应输入值一一相同；登记完成后
 * 步骤输出回自该登记记录。写入步骤：每次执行新增一条流水记录，
 * 本步骤无业务失败场景。
 */
public interface IST082 {

    /**
     * 登记交易流水。
     *
     * 事务要求：本步骤向本地数据库新增记录，实现以 Spring @Transactional 执行，
     * 调用方须传递并遵守该事务要求；仅返回 succeed=false 不会自动回滚
     * （本步骤无业务失败场景，失败仅由技术异常传播表达）。
     *
     * @param input 输入BO：交易类型（tranType）、币种（ccy）、借贷标志（crDrInd）、
     *              交易金额（tranAmt）均必填
     * @return 输出BO：crDrInd、ccy、tranType、tranAmt 回自本次登记记录（与对应
     *         输入一致）；channelSeqNo 为非必填输出字段，取值来源需求未定义，
     *         本步骤不赋值。成功时 succeed=true、错误码与错误信息为 null。
     */
    ST082OutputBO execute(ST082InputBO input);
}
