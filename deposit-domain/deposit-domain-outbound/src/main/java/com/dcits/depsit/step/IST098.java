package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST098InputBO;
import com.dcits.depsit.facade.bo.ST098OutputBO;

/**
 * ST098 检查限额。
 *
 * 按输入{限额机构编码}和{限额场景编码}（联合主键）查询【限额控制配置】取得限额控制金额与
 * 限额控制笔数，再以输入的限额累计金额、限额累计笔数执行超限判定，输出限额控制值、限额
 * 累计值（透传）与限额检查结果（"超限"/"未超限"）。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST098 {

    /**
     * 检查限额。
     *
     * @param input 输入BO：limitBranchId（限额机构编码）、limitSceneNo（限额场景编码）、
     *              limitSumAmt（限额累计金额）、limitSumNum（限额累计笔数）均必填
     * @return 输出BO：limitCtrlAmt、limitCtrlNum 取查询结果（无记录或字段为空时为 null）；
     *         limitSumAmt、limitSumNum 原样透传输入；limitCheckResult 恒有值（"超限"/"未超限"）。
     *         本步骤无业务失败场景，"超限"是正常业务结果；失败仅由技术异常向上传播表达。
     */
    ST098OutputBO execute(ST098InputBO input);
}
