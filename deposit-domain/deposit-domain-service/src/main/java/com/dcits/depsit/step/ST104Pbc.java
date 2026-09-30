package com.dcits.depsit.step;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.facade.bo.ST104InputBO;
import com.dcits.depsit.facade.bo.ST104OutputBO;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;

/**
 * ST104 更新累计限额。
 *
 * 当 [限额检查结果] 为"未超限"（字面值比较）且 [限额累计金额] 大于 0 或 [限额累计笔数]
 * 大于 0（或关系，与 0 数值比较）时，以 {账号} 作为限额检查对象值（checkObjVal）、
 * {限额场景编码} 作为限额场景编码（limitSceneNo），按主键将【限额累计信息表】
 * （RB_LIMIT_SUM_INFO）记录的 $限额累计金额$ 更新为输入 [限额累计金额]，并输出该值；
 * 仅更新 $限额累计金额$，其余字段不变更。触发条件不满足时不执行任何更新，输出为空。
 * 无业务失败场景，全部业务情形均成功；技术异常原样传播。
 */
@Service
public class ST104Pbc implements IST104 {

    /** 限额检查结果取值：未超限（需求文本规范常量，无枚举绑定） */
    private static final String LIMIT_CHECK_RESULT_NOT_EXCEEDED = "未超限";

    @Autowired
    private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

    @Override
    @Transactional
    public ST104OutputBO execute(ST104InputBO input) {
        ST104OutputBO output = new ST104OutputBO();

        // 触发条件判定："未超限"且（金额>0 或 笔数>0），不满足时不执行任何读写并正常返回
        if (isTriggerConditionMet(input)) {
            // 按主键（限额检查对象值、限额场景编码）更新 $限额累计金额$：selective 更新仅携带
            // 主键二值与金额，其余字段（限额累计笔数、客户号等）不写入、保持库中原值
            RbLimitSumInfoEO updateEO = new RbLimitSumInfoEO();
            updateEO.setCheckObjVal(input.getBaseAcctNo());
            updateEO.setLimitSceneNo(input.getLimitSceneNo());
            updateEO.setLimitSumAmt(input.getLimitSumAmt());
            rbLimitSumInfoBcc.modifyByPrimaryKeySelective(updateEO);
            // 更新执行时输出更新写入的值，即输入 [限额累计金额]
            output.setLimitSumAmt(input.getLimitSumAmt());
        }

        output.setSucceed(true);
        return output;
    }

    /**
     * 触发条件判定：限额检查结果等于"未超限"（String 字面值比较），且限额累计金额大于 0
     * 或限额累计笔数大于 0（两分支或关系，与 0 的比较为数值比较）。
     */
    private boolean isTriggerConditionMet(ST104InputBO input) {
        return LIMIT_CHECK_RESULT_NOT_EXCEEDED.equals(input.get限额检查结果())
                && (input.getLimitSumAmt().compareTo(BigDecimal.ZERO) > 0
                        || input.get否() > 0);
    }
}
