package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST083InputBO;
import com.dcits.depsit.facade.bo.ST083OutputBO;

/**
 * ST083 登记现金交易明细。
 *
 * 通过数据服务接口 IRbBusTranJnlBcc 的创建方法在对公存款账户金融交易流水表
 * （RB_BUS_TRAN_JNL）中创建一条现金交易明细记录，写入交易类型、币种、借贷标志、
 * 交易金额 4 个字段并作为步骤输出提供给后续步骤使用。
 */
public interface IST083 {

    /**
     * 登记现金交易明细。
     *
     * @param input 输入BO：tranType（交易类型）、ccy（币种）、crDrInd（借贷标志）、
     *              tranAmt（交易金额）均为必填，按原值写入，不做转换、不设默认值
     * @return 输出BO：tranType、ccy、crDrInd、tranAmt 取所创建记录的对应字段。
     *         本步骤无业务失败场景；创建调用抛出的技术异常原样向上传播。
     *         本步骤在本地事务中执行 RB_BUS_TRAN_JNL 记录创建，调用方如需与其他
     *         本地写入保持同一事务，须在携带事务的上下文中调用本方法。
     */
    ST083OutputBO execute(ST083InputBO input);
}
