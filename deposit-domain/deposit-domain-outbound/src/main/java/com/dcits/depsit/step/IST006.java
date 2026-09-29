package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST006InputBO;
import com.dcits.depsit.facade.bo.ST006OutputBO;

/**
 * ST006 检查是否存在转账止付限制 步骤接口
 *
 * <p>依据正式 Spec ST006：第 1 步以账号与限制状态"A-生效"查询【账户限制信息】；
 * 第 2 步对每条限制记录的账户限制类型查询【限制类型表】，仅当匹配记录状态为"A-生效"
 * 时取得其借贷方控制标志与转账标志；第 3 步逐条检查，任一记录的限制类型对应借贷方控制标志
 * 为"D-禁止借方"且转账标志为"N-不允许转账"时，转账止付标志为"是"并回显第一条满足条件
 * 记录的相关字段，否则转账止付标志为"否"且回显字段置空。</p>
 *
 * <p>本步骤只读、无写库、无外部服务调用，对调用方无事务要求；
 * 无业务失败场景，全部业务情形（含转账止付标志为"是"与"否"）均成功结束，
 * 技术异常按原样向调用方传播。</p>
 */
public interface IST006 {

    /**
     * 执行检查是否存在转账止付限制步骤
     *
     * @param input 输入BO（baseAcctNo 账号，必填，由上游交易输入保证）
     * @return 输出BO（stopFlag 转账止付标志，取值"是"/"否"；stopFlag="是" 时回显字段取
     *         第一条满足条件记录及其账户限制类型对应的限制类型表生效记录，stopFlag="否" 时
     *         回显字段全部为 null；succeed=true）
     */
    ST006OutputBO execute(ST006InputBO input);
}
