package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.facade.bo.ST082InputBO;
import com.dcits.depsit.facade.bo.ST082OutputBO;
import com.dcits.depsit.facade.components.IRbBusTranJnlBcc;
import com.dcits.depsit.facade.eo.RbBusTranJnlEO;

/**
 * ST082 登记交易流水。
 *
 * 步骤1 登记金融交易流水：将输入的交易类型、币种、借贷标志、交易金额登记为
 * 一条【交易流水】（RB_BUS_TRAN_JNL），四个登记字段与对应输入值一一相同
 * （身份映射：枚举按其 value 写入，金额原值写入、不舍入、不变换），每次执行
 * 新增一条记录；登记完成后步骤输出回自该登记记录。流水记录其余字段（含
 * channelSeqNo 及主键、流水号、时间戳等）需求未要求登记，不赋值。
 * 本步骤无业务失败场景，失败仅由技术异常传播表达。
 */
@Service
public class ST082Pbc implements IST082 {

    @Autowired
    private IRbBusTranJnlBcc rbBusTranJnlBcc;

    @Override
    @Transactional
    public ST082OutputBO execute(ST082InputBO input) {
        ST082OutputBO output = new ST082OutputBO();

        // 步骤1 登记金融交易流水：新增一条记录并取得该登记记录
        RbBusTranJnlEO jnl = registerTranJnl(input);

        // 输出回自该登记记录：四个输出字段取登记记录对应字段值（与本次输入一致）
        output.setCrDrInd(jnl.getCrDrInd());
        output.setCcy(jnl.getCcy());
        output.setTranType(jnl.getTranType());
        output.setTranAmt(jnl.getTranAmt());

        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 登记金融交易流水：向 RB_BUS_TRAN_JNL 新增一条记录，tranType、ccy、
     * crDrInd、tranAmt 分别登记为对应输入值（枚举按其 value 写入、金额原值写入）。
     * createSelective 按 EO 非空属性写入，其余字段不赋值、不约束。
     */
    private RbBusTranJnlEO registerTranJnl(ST082InputBO input) {
        RbBusTranJnlEO jnl = new RbBusTranJnlEO();
        jnl.setTranType(input.getTranType());
        jnl.setCcy(input.getCcy());
        jnl.setCrDrInd(input.getCrDrInd());
        jnl.setTranAmt(input.getTranAmt());
        rbBusTranJnlBcc.createSelective(jnl);
        return jnl;
    }
}
