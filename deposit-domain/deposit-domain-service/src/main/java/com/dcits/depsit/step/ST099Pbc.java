package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST099InputBO;
import com.dcits.depsit.facade.bo.ST099OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST099 处理限额。
 *
 * 步骤1 以[限额机构编码]与[限额场景编码]为查询条件查询【限额控制配置】
 * （RB_LIMIT_CTRL_CONF，两字段为表主键，精确匹配、至多命中一条记录）；
 * 步骤2 按命中记录的[处理方式]映射返回 dealFlow：拒绝返回 DealFlow.B、
 * 提醒返回 DealFlow.D、授权返回 DealFlow.A；未命中记录、或命中但处理方式
 * 为空时 dealFlow 为 null，步骤均正常结束、不产生业务失败。只读步骤，
 * 无写入副作用，无事务要求，技术异常原样向调用方传播。
 */
@Service
public class ST099Pbc implements IST099 {

    @Autowired
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Override
    public ST099OutputBO execute(ST099InputBO input) {
        ST099OutputBO output = new ST099OutputBO();

        // 步骤1 获取限额控制配置：以输入两键字段发起主键查询；未命中时 dealFlow 保持 null，正常结束
        RbLimitCtrlConfEO conf = rbLimitCtrlConfBcc.findByPrimaryKey(
                input.getLimitBranchId(), input.getLimitSceneNo());

        if (conf != null) {
            // 步骤2 按处理方式返回：实体 dealFlow 即仅含 A/B/D 三常量的 DealFlow 枚举，
            // 非空取值必属"授权/拒绝/提醒"三分支之一，直接作为输出；为空时保持 null
            output.setDealFlow(conf.getDealFlow());
        }

        output.setSucceed(true);
        return output;
    }
}
