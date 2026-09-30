package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST099InputBO;
import com.dcits.depsit.facade.bo.ST099OutputBO;

/**
 * ST099 处理限额。
 *
 * 按输入的限额机构编码与限额场景编码查询【限额控制配置】（RB_LIMIT_CTRL_CONF），
 * 取得命中记录的处理方式并作为 dealFlow 输出，供交易编排按"拒绝/提醒/授权"处置使用。
 * 只读查询步骤：无数据写入副作用，无事务要求。
 */
public interface IST099 {

    /**
     * 处理限额。
     *
     * @param input 输入BO，限额机构编码（limitBranchId）、限额场景编码（limitSceneNo）均必填
     * @return 输出BO：dealFlow 为命中记录的处理方式（DealFlow.B-拒绝 / DealFlow.D-提醒 /
     *         DealFlow.A-授权）；未命中记录或命中但处理方式为空时 dealFlow 为 null。
     *         本步骤无业务失败场景，失败仅以技术异常向调用方传播。
     */
    ST099OutputBO execute(ST099InputBO input);
}
