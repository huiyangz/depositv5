package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST003InputBO;
import com.dcits.depsit.facade.bo.ST003OutputBO;

/**
 * ST003 检查有权机关冻结限制 步骤接口
 *
 * <p>依据正式 Spec ST003：先以账号与限制状态"A-生效"为条件查询【账户限制信息】，
 * 再对每条限制记录的账户限制类型查询【限制类型表】，仅当匹配记录状态为"A-生效"时
 * 取得其有权机关冻结标志；查得任一不为空的标志值时赋值给输出 ahBuFlag 返回，
 * 未查得时输出为空（null）。</p>
 *
 * <p>本步骤只读、无写库、无外部服务调用，对调用方无事务要求；
 * 无业务失败场景，全部业务情形均成功结束，技术异常按原样向调用方传播。</p>
 */
public interface IST003 {

    /**
     * 执行检查有权机关冻结限制步骤
     *
     * @param input 输入BO（baseAcctNo 账号，必填，由上游交易输入保证）
     * @return 输出BO（ahBuFlag 有权机关冻结标志，未查得非空值时为 null；succeed=true）
     */
    ST003OutputBO execute(ST003InputBO input);
}
