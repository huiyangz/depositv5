package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST089InputBO;
import com.dcits.depsit.facade.bo.ST089OutputBO;

/**
 * ST089 检查存入账户黑名单。
 *
 * 按{凭证类型}查询凭证类型定义表（TB_VOUCHER_DEF）取得[凭证种类]，赋值[事件类型]
 * 为常量 "CRET" 后，以{账号}、{交易类型}、[事件类型]、{服务代码}、{接口服务代码}、
 * {接口服务类型}、{渠道类型}、[凭证种类] 8 项映射调用组件内步骤 ST105《检查黑名单》，
 * 取得[执行结果]（业务结果 dealFlow，不取调用成功标志），并按"通过/提醒/授权/拒绝"
 * 四值分支处置：通过、提醒继续执行正常完成；授权返回授权标志"是"并等待授权结果；
 * 拒绝返回错误码 "ER0057" 非成功返回。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST089 {

    /**
     * 检查存入账户黑名单。
     *
     * @param input 输入BO，9 个字段定义见正式 Spec ST089「输入」表
     * @return 输出BO：eventType 固定 "CRET"、docClass 为子步骤 1 查询所得凭证种类；
     *         dealFlow 取 ST105 返回值（"通过"时为 null）；"授权"分支 authFlag="是"；
     *         "拒绝"分支为非成功返回（errorCode="ER0057"），其余分支成功时错误字段为 null
     */
    ST089OutputBO execute(ST089InputBO input);
}
