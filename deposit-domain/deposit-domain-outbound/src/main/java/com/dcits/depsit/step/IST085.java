package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST085InputBO;
import com.dcits.depsit.facade.bo.ST085OutputBO;

/**
 * ST085 检查存入客户黑名单。
 *
 * 按客户号查询客户副本表取得客户名称、证件类型、证件号码、发证国家，按凭证类型查询
 * 凭证类型定义表取得凭证种类，赋值事件类型"CRET"后以组件内步骤调用方式执行本组件
 * 步骤 ST105「检查黑名单」，并按其返回 dealFlow 映射[执行结果]表达检查结果："通过"或
 * "提醒"时检查结果为"通过"并以 dealFlow 表达（"通过"不赋值、"提醒"为 DealFlow.D），
 * "授权"时返回授权标志"是"，"拒绝"时失败返回错误码"ER0003"。只读检查步骤：无数据
 * 写入副作用，无外部服务调用，无事务要求。
 */
public interface IST085 {

    /**
     * 检查存入客户黑名单。
     *
     * @param input 输入BO，11 个字段定义见正式 Spec ST085「输入」表
     * @return 输出BO：eventType="CRET"及两次查询所得字段；[执行结果]为"提醒"时
     *         dealFlow=DealFlow.D，为"授权"时 authFlag="是"，"通过"分支两者为 null；
     *         "拒绝"分支 succeed=false、errorCode="ER0003"。
     */
    ST085OutputBO execute(ST085InputBO input);
}
