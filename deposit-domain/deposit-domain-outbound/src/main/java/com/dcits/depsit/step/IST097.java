package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST097InputBO;
import com.dcits.depsit.facade.bo.ST097OutputBO;

/**
 * ST097 计算限额累计金额。
 *
 * 按输入{限额场景编码}在【限额控制配置表】获取启用有效的配置记录（多条时取最后更新时间戳
 * 最新一条），按其自定义相关标志决定[限额信息]取数来源，再按[限额信息]的限额控制类型与
 * 配置记录的累计类型代码查询当前累计情况，计入本次{交易金额}后计算限额累计金额与笔数。
 * 只读计算步骤：无数据写入副作用，无事务要求。
 */
public interface IST097 {

    /**
     * 计算限额累计金额。
     *
     * @param input 输入BO，限额场景编码（limitSceneNo）、交易金额（tranAmt）、客户号（clientNo）、
     *              限额检查对象值（checkObjVal）必填
     * @return 输出BO：limitSumAmt 与 limitSumNum 同时有值或同时为 null（未取到启用有效配置记录、
     *         按来源未取到[限额信息]、或限额控制类型为 O-单笔金额时为 null）。本步骤无业务失败
     *         场景，失败仅由技术异常传播表达。
     */
    ST097OutputBO execute(ST097InputBO input);
}
