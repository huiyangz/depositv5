package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST096InputBO;
import com.dcits.depsit.facade.bo.ST096OutputBO;

/**
 * ST096 检查账户机构是否可匹配到限额场景配置。
 *
 * 以输入{账号}查询【账户信息】取得账户开立行行号，先以该行号作为限额机构编码查询
 * 【限额控制配置表】中启用标志为"Y-启用"的限额场景配置，直接命中即返回；未命中时
 * 沿【机构信息表】逐级向上收集上级机构，按机构层级从大到小依次查询，首个命中即返回；
 * 全部未命中时限额场景配置组输出为空。只读检查步骤：无数据写入副作用，无事务要求。
 */
public interface IST096 {

    /**
     * 检查账户机构是否可匹配到限额场景配置。
     *
     * @param input 输入BO，账号（baseAcctNo）必填
     * @return 输出BO：命中启用限额控制配置记录时 limitSceneNo、limitBranchId、
     *         limitBranchRange、validFlag 取自该记录，未命中时均为 null；
     *         baseAcctNo、acctBranch 为步骤1查询结果透传；branch、attachedTo 仅在
     *         进入上级机构流程且账户开立行在【机构信息表】存在记录时取自该记录，
     *         直接命中或记录不存在时为 null。本步骤无业务失败场景。
     */
    ST096OutputBO execute(ST096InputBO input);
}
