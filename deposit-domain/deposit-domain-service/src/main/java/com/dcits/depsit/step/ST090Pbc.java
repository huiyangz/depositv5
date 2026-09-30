package com.dcits.depsit.step;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.facade.bo.ST090InputBO;
import com.dcits.depsit.facade.bo.ST090OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBalanceBcc;
import com.dcits.depsit.facade.eo.RbBusAcctBalanceEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST090 更新存入后账户余额 步骤实现。
 *
 * 业务定义来源：docs/specs/ST090.md。
 * 子步骤顺序与数据传递：按{账号}等值查【对公存款账户主表】（RB_BUS_ACCT）取得
 * $账户内部键值$ → 以该键值查【账户余额】（RB_BUS_ACCT_BALANCE）取得$汇总金额$、
 * $账户可用余额$基值 → 以"原值+{交易金额}"按主键同步更新两余额字段并输出更新后数值。
 * 写库范围仅 RB_BUS_ACCT_BALANCE 单记录的 TOTAL_AMOUNT、ACCT_AVAIL_BAL 两字段。
 * 本步骤无业务失败场景，技术异常原样向上传播，不捕获、不转换为业务失败结果。
 */
@Service
public class ST090Pbc implements IST090 {

    private final IRbBusAcctBcc rbBusAcctBcc;

    private final IRbBusAcctBalanceBcc rbBusAcctBalanceBcc;

    public ST090Pbc(IRbBusAcctBcc rbBusAcctBcc, IRbBusAcctBalanceBcc rbBusAcctBalanceBcc) {
        this.rbBusAcctBcc = rbBusAcctBcc;
        this.rbBusAcctBalanceBcc = rbBusAcctBalanceBcc;
    }

    /**
     * 子步骤 3 涉及本地数据库更新，方法须在事务中执行；
     * 事务边界由调用方与运行平台事务管理决定（Spec「明确不覆盖」第 6 项）。
     */
    @Override
    @Transactional
    public ST090OutputBO execute(ST090InputBO input) {
        ST090OutputBO output = new ST090OutputBO();

        // 子步骤1（REQ-001）：按{账号}查询【对公存款账户主表】取得$账户内部键值$
        Integer internalKey = findInternalKey(input.getBaseAcctNo());

        // 子步骤2（REQ-002）：以$账户内部键值$查询【账户余额】取得两字段更新基值
        RbBusAcctBalanceEO balance = rbBusAcctBalanceBcc.findByPrimaryKey(internalKey);

        // 子步骤3（REQ-003）：$汇总金额$=[汇总金额]+{交易金额}、$账户可用余额$=[账户可用余额]+{交易金额}，精确加法
        BigDecimal newTotalAmount = balance.getTotalAmount().add(input.getTranAmt());
        BigDecimal newAcctAvailBal = balance.getAcctAvailBal().add(input.getTranAmt());
        modifyBalance(internalKey, newTotalAmount, newAcctAvailBal);

        // 输出为更新后数值；无业务失败场景（REQ-004），成功结束时错误字段保持 null
        output.setTotalAmount(newTotalAmount);
        output.setAcctAvailBal(newAcctAvailBal);
        output.setSucceed(true);
        return output;
    }

    /**
     * 子步骤1：以{账号}为等值条件查询 RB_BUS_ACCT，取返回记录的 INTERNAL_KEY。
     * BASE_ACCT_NO 非主键，无主键查询可用；无匹配记录与多记录取舍需求未定义
     * （Spec「明确不覆盖」第 1、2 项），不发明处理分支，查询结果为空时产生的
     * 技术异常按 REQ-004-S02 原样传播。
     */
    private Integer findInternalKey(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> records = rbBusAcctBcc.findByEo(condition);
        return records.get(0).getInternalKey();
    }

    /**
     * 子步骤3：按主键 INTERNAL_KEY 选择性更新 RB_BUS_ACCT_BALANCE 的两个余额字段。
     * 更新 EO 仅携带 internalKey 与两个新值，选择性更新只写非空字段，落实
     * 「除两字段外不修改其他字段」；更新影响行数的处理需求未定义
     * （Spec「明确不覆盖」第 3 项），不检查返回值。
     */
    private void modifyBalance(Integer internalKey, BigDecimal totalAmount, BigDecimal acctAvailBal) {
        RbBusAcctBalanceEO updateEo = new RbBusAcctBalanceEO();
        updateEo.setInternalKey(internalKey);
        updateEo.setTotalAmount(totalAmount);
        updateEo.setAcctAvailBal(acctAvailBal);
        rbBusAcctBalanceBcc.modifyByPrimaryKeySelective(updateEo);
    }
}
