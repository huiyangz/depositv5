package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST103InputBO;
import com.dcits.depsit.facade.bo.ST103OutputBO;

/**
 * ST103 检查限额场景配置是否有效。
 *
 * 按输入{限额机构编码}、[限额场景编码]主键等值查询【限额控制配置】取得四个
 * 控制区间字段，检查{交易日期}、{交易时间}是否均落在对应控制闭区间内；均在
 * 区间内时判定限额场景配置有效并输出场景编码及四个区间字段，否则（含未查询
 * 到配置、任一区间字段为空、区间判定不满足）全部输出为空。
 * 只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST103 {

    /**
     * 检查限额场景配置是否有效。
     *
     * @param input 输入BO：交易日期（tranDate）、交易时间戳（tranTimestamp，
     *              格式 HHmmss）、限额机构编码（limitBranchId）、限额场景编码
     *              （limitSceneNo）均必填
     * @return 输出BO：配置有效时 limitSceneNo（与查询所用输入值相同）及
     *         limitCtrlBgnDate、limitCtrlEndDate、limitCtrlBgnTime、
     *         limitCtrlEndTime 取命中记录值；未查询到配置、任一控制区间字段
     *         为空（配置无效）或区间判定不满足时 5 个字段均为 null。
     *         本步骤无业务失败场景，失败仅由技术异常传播表达。
     */
    ST103OutputBO execute(ST103InputBO input);
}
