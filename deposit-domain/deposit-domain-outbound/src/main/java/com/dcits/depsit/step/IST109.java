package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST109InputBO;
import com.dcits.depsit.facade.bo.ST109OutputBO;

/**
 * ST109 登记账户限制信息。
 *
 * 以 8 个必填输入登记账户的【限制信息】：将其中 7 个输入（baseAcctNo、
 * restraintType、startDate、endDate、term、termType、tranDate）写入登记目标实体
 * RbBusRestraintsEO（RB_BUS_RESTRAINTS）的同名字段，形成一条账户限制登记记录；
 * runDate 为必填输入但实体无同名字段，不映射写入。
 * 本步骤无业务失败场景，失败仅由技术异常传播表达。
 *
 * 事务要求：本步骤向本地数据库新增记录，实现标注 Spring @Transactional，
 * 须在 Spring 事务环境中调用。
 */
public interface IST109 {

    /**
     * 登记账户限制信息。
     *
     * @param input 输入BO，8 个字段（baseAcctNo、restraintType、startDate、endDate、
     *              term、termType、tranDate、runDate）均为必填
     * @return 输出BO：无业务字段，仅状态字段；登记成功 succeed=true、错误字段为 null
     */
    ST109OutputBO execute(ST109InputBO input);
}
