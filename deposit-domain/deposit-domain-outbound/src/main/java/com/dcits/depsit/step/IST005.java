package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST005InputBO;
import com.dcits.depsit.facade.bo.ST005OutputBO;

/**
 * ST005 检查账户是否存在限制 步骤接口
 *
 * <p>依据正式 Spec ST005：以输入账号查询【账户信息】取得主账户标志与上级账户内部键并
 * 判定子账户；子账户时按上级账户内部键取得主账户账号；设置待查账户（子账户取主账户账号，
 * 否则取输入账号）；按待查账户查询限制状态为"A-生效"的【账户限制信息】；将账号、主账户
 * 标志与每条生效限制的限制编号、账户限制类型、限制状态赋值到步骤输出返回。</p>
 *
 * <p>本步骤只读、无写库、无状态变更、无外部服务调用，对调用方无事务要求；
 * 无业务失败场景，全部业务情形（含未查得任何生效限制）均成功结束，
 * 技术异常按原样向调用方传播。</p>
 */
public interface IST005 {

    /**
     * 执行检查账户是否存在限制步骤
     *
     * @param input 输入BO（baseAcctNo 账号，必填，由上游交易输入保证）
     * @return 输出BO（baseAcctNo/leadAcctFlag 取自按输入账号查询所得账户记录；
     *         resSeqNo/restraintType/restraintsStatus 每条生效限制一个元素、同下标同源记录，
     *         未查得生效限制时为空集合；succeed=true）
     */
    ST005OutputBO execute(ST005InputBO input);
}
