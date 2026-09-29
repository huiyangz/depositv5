package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.common.step.GotoStepCondition;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST018InputBO;
import com.dcits.depsit.facade.bo.ST018OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST018 检查交易机构。
 *
 * 步骤1 以输入{账号}等值查询【账户信息】（RB_BUS_ACCT），读取 $账户开立行行号$
 * 与 $通存标志$，并将 $账户开立行行号$ 赋值到输出 acctBranch；$通存标志$ 仅随
 * 步骤1读取（供流转目标步骤使用），不判定、不进入输出。步骤2 比较{交易机构}与
 * [账户开立行行号]：一致（同一枚举值，即同一内部机构编号）返回检查结果"通过"；
 * 不一致跳转步骤《检查存入账户通存标志》（跳转信号由 gotoStepName 承载，
 * 非业务失败）。只读步骤，无业务失败场景，失败仅由技术异常传播表达，无事务要求。
 */
@Service
// 类注解中引用本类常量须使用类限定名：javac 解析类级注解时简单名不在作用域（本轮编译失败修复）
@GotoStepCondition(candidateSteps = { ST018Pbc.GOTO_STEP_CHECK_ALL_DEP_IND })
public class ST018Pbc implements IST018 {

    /** 跳转目标步骤名称：《检查存入账户通存标志》（交易编排后续步骤，其行为不在本步骤范围） */
    public static final String GOTO_STEP_CHECK_ALL_DEP_IND = "检查存入账户通存标志";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Override
    public ST018OutputBO execute(ST018InputBO input) {
        ST018OutputBO output = new ST018OutputBO();

        // 步骤1 获取账户信息：读取 $账户开立行行号$ 与 $通存标志$，acctBranch 输出仅取决于本查询结果
        RbBusAcctEO acctInfo = findAccountInfo(input.getBaseAcctNo());
        TranBranch acctBranch = acctInfo.getAcctBranch();
        // $通存标志$（acctInfo.getAllDepInd()）仅随步骤1读取，本步骤不判定、不进入输出
        output.setAcctBranch(acctBranch);

        // 步骤2 检查交易机构：一致即{交易机构}与[账户开立行行号]为同一枚举值（同一内部机构编号）
        if (input.getTranBranch() == acctBranch) {
            // 一致分支：返回检查结果"通过"，不发生跳转
            output.setSucceed(true);
            return output;
        }

        // 不一致分支：跳转《检查存入账户通存标志》，跳转非业务失败；acctBranch 输出不受分支结论影响
        output.setGotoStepName(GOTO_STEP_CHECK_ALL_DEP_IND);
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户信息：以{账号}为条件等值查询 RB_BUS_ACCT，读取账户开立行行号与通存标志。
     * 数据前提：{账号}至多命中一条账户记录（Spec「数据与依赖关系」，按单值获取表述）；
     * 前提不成立（无命中）时的行为需求未定义（Spec 不覆盖事项），本方法不发明处理，
     * get(0) 抛出的技术异常按 REQ-003 原样向上传播。
     */
    private RbBusAcctEO findAccountInfo(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);
        return acctList.get(0);
    }
}
