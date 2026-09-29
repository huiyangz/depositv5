package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.facade.bo.ST109InputBO;
import com.dcits.depsit.facade.bo.ST109OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;

/**
 * ST109 登记账户限制信息。
 *
 * 以 8 个必填输入登记账户的【限制信息】：将其中 7 个输入（baseAcctNo、
 * restraintType、startDate、endDate、term、termType、tranDate）按同名同类型直接
 * 赋值写入登记目标实体 RbBusRestraintsEO（RB_BUS_RESTRAINTS），创建一条账户
 * 限制登记记录；runDate 为必填输入但实体无同名字段，不产生实体字段写入。
 * 实体其余字段（含 7 个 @NotNull 必填字段）的取值来源需求未定义（待需求方确认），
 * 本步骤不赋值、不默认填零/空串/随机值，亦不将 runDate 映射到 channelDate 等字段。
 * 写入绑定 createSelective（按 EO 中不为空的属性写入）。无业务失败场景，
 * 失败仅由技术异常传播表达。
 */
@Service
public class ST109Pbc implements IST109 {

    @Autowired
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    /**
     * 登记账户限制信息：构造登记记录，7 个输入一一赋值到实体同名字段后创建写入。
     * runDate 仅作为输入接收，不参与任何实体字段赋值（Spec REQ-002）。
     */
    @Override
    @Transactional
    public ST109OutputBO execute(ST109InputBO input) {
        RbBusRestraintsEO restraint = new RbBusRestraintsEO();
        restraint.setBaseAcctNo(input.getBaseAcctNo());
        restraint.setRestraintType(input.getRestraintType());
        restraint.setStartDate(input.getStartDate());
        restraint.setEndDate(input.getEndDate());
        restraint.setTerm(input.getTerm());
        restraint.setTermType(input.getTermType());
        restraint.setTranDate(input.getTranDate());

        rbBusRestraintsBcc.createSelective(restraint);

        ST109OutputBO output = new ST109OutputBO();
        output.setSucceed(true);
        return output;
    }
}
