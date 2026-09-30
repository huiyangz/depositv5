package com.dcits.depsit.step;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST098InputBO;
import com.dcits.depsit.facade.bo.ST098OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST098 检查限额。
 *
 * 步骤1 以输入{限额机构编码}和{限额场景编码}（联合主键，至多命中一条）查询【限额控制配置】
 * （RB_LIMIT_CTRL_CONF），取得$限额控制金额$与$限额控制笔数$，无记录或记录字段为空时该
 * 控制值为 null；步骤2 以输入的$限额累计金额$、$限额累计笔数$执行超限判定：（控制金额不为空
 * 且累计金额大于控制金额）或（控制笔数不为空且累计笔数大于控制笔数）成立即"超限"，否则
 * "未超限"；大于为严格大于，金额按 BigDecimal 数值语义比较（与标度无关）。输出控制值、
 * 累计值（透传）与限额检查结果。只读步骤，无业务失败场景（"超限"是正常业务结果），
 * 技术异常向上传播，无事务要求。
 */
@Service
public class ST098Pbc implements IST098 {

    /** 限额检查结果取值：超限（需求文本常量，无枚举绑定） */
    private static final String LIMIT_CHECK_RESULT_EXCEEDED = "超限";

    /** 限额检查结果取值：未超限（需求文本常量，无枚举绑定） */
    private static final String LIMIT_CHECK_RESULT_NOT_EXCEEDED = "未超限";

    @Autowired
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Override
    public ST098OutputBO execute(ST098InputBO input) {
        ST098OutputBO output = new ST098OutputBO();

        // 步骤1 获取限额控制值：以{限额机构编码}+{限额场景编码}联合主键查询，至多一条；无记录时两控制值均为 null
        RbLimitCtrlConfEO limitCtrlConf = findLimitCtrlConf(input.getLimitBranchId(), input.getLimitSceneNo());
        BigDecimal limitCtrlAmt = limitCtrlConf == null ? null : limitCtrlConf.getLimitCtrlAmt();
        Integer limitCtrlNum = limitCtrlConf == null ? null : limitCtrlConf.getLimitCtrlNum();

        // 步骤2 检查限额：金额分支与笔数分支任一严格大于即"超限"；控制值为空的分支不参与判定；
        // 金额用 compareTo 按数值大小比较（与标度无关，如 1000.0 与 1000.00 相等，不构成超限）
        boolean amountExceeded = limitCtrlAmt != null && input.getLimitSumAmt().compareTo(limitCtrlAmt) > 0;
        boolean numExceeded = limitCtrlNum != null && input.getLimitSumNum() > limitCtrlNum;
        String limitCheckResult = amountExceeded || numExceeded
                ? LIMIT_CHECK_RESULT_EXCEEDED : LIMIT_CHECK_RESULT_NOT_EXCEEDED;

        // 输出组装：控制值取步骤1结果（无记录或字段为空为 null），累计值原样透传，检查结果恒有值
        output.setLimitCtrlAmt(limitCtrlAmt);
        output.setLimitCtrlNum(limitCtrlNum);
        output.setLimitSumAmt(input.getLimitSumAmt());
        output.setLimitSumNum(input.getLimitSumNum());
        output.setLimitCheckResult(limitCheckResult);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取限额控制值：按联合主键（限额机构编码、限额场景编码）查询 RB_LIMIT_CTRL_CONF，
     * 至多命中一条；查询无记录时返回 null。
     */
    private RbLimitCtrlConfEO findLimitCtrlConf(String limitBranchId, String limitSceneNo) {
        return rbLimitCtrlConfBcc.findByPrimaryKey(limitBranchId, limitSceneNo);
    }
}
