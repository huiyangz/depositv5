package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.facade.bo.ST083InputBO;
import com.dcits.depsit.facade.bo.ST083OutputBO;
import com.dcits.depsit.facade.components.IRbBusTranJnlBcc;
import com.dcits.depsit.facade.eo.RbBusTranJnlEO;

/**
 * ST083 登记现金交易明细。
 *
 * 在对公存款账户金融交易流水表（RB_BUS_TRAN_JNL）中创建一条记录：
 * 交易类型、币种、借贷标志、交易金额 4 个字段取步骤输入原值写入（无转换、无默认值），
 * 并将该 4 个字段作为步骤输出提供给后续步骤使用。
 * 本步骤无业务失败场景；创建调用抛出的技术异常原样向上传播，不捕获、不转换、不重试、无补偿。
 */
@Service
public class ST083Pbc implements IST083 {

    /** 对公存款账户金融交易流水表（RB_BUS_TRAN_JNL）数据服务接口 */
    @Autowired
    private IRbBusTranJnlBcc rbBusTranJnlBcc;

    /**
     * 登记现金交易明细：一次步骤执行登记一条记录，发起恰好一次创建调用。
     * 创建契约其余必填字段（seqNo、tranDate、clientNo、internalKey、othInternalKey、
     * createTimestamp、lastUpdTimestamp）的取值来源正式需求未定义并已放行，本步骤不规定。
     */
    @Override
    @Transactional
    public ST083OutputBO execute(ST083InputBO input) {
        ST083OutputBO output = new ST083OutputBO();

        // 登记现金交易明细：构造 RB_BUS_TRAN_JNL 记录，4 个绑定字段取输入原值写入
        RbBusTranJnlEO record = new RbBusTranJnlEO();
        record.setTranType(input.getTranType());
        record.setCcy(input.getCcy());
        record.setCrDrInd(input.getCrDrInd());
        record.setTranAmt(input.getTranAmt());
        rbBusTranJnlBcc.createSelective(record);

        // 输出回写：4 个输出字段取所创建记录的对应字段
        output.setTranType(record.getTranType());
        output.setCcy(record.getCcy());
        output.setCrDrInd(record.getCrDrInd());
        output.setTranAmt(record.getTranAmt());
        output.setSucceed(true);
        return output;
    }
}
