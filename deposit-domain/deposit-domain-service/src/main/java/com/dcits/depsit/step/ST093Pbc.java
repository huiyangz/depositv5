package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.facade.bo.ST093InputBO;
import com.dcits.depsit.facade.bo.ST093OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST093 检查存入账户账户属性。
 *
 * 步骤1 以输入{账号}等值查询【账户信息】（RB_BUS_ACCT）取得$账户属性$（acctNatureNo）；
 * 步骤2 判定[账户属性]是否为"验资户"（AcctNatureNo.VALUE_17）或"临时存款账户"
 * （AcctNatureNo.VALUE_11003）：命中时输出该账户属性并正常返回（向《检查账户到期日》
 * 的跳转由交易编排依据该输出执行，非本步骤行为）；否则返回检查结果"通过"，
 * 即正常返回且不输出账户属性。只读检查步骤，无业务失败场景（失败仅由技术异常
 * 向上传播表达），无事务要求。
 */
@Service
public class ST093Pbc implements IST093 {

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Override
    public ST093OutputBO execute(ST093InputBO input) {
        ST093OutputBO output = new ST093OutputBO();

        // 步骤1 获取账户属性：按{账号}等值查询 RB_BUS_ACCT，取得该账户记录的 acctNatureNo
        AcctNatureNo acctNatureNo = findAcctNatureNo(input.getBaseAcctNo());

        // 步骤2 检查账户属性：命中"验资户"或"临时存款账户"时输出账户属性，供交易编排跳转《检查账户到期日》
        if (acctNatureNo == AcctNatureNo.VALUE_17 || acctNatureNo == AcctNatureNo.VALUE_11003) {
            output.setAcctNatureNo(acctNatureNo);
        }

        // 命中与未命中均为正常返回；未命中即检查结果"通过"（不输出账户属性），不构成业务失败
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取账户属性：以{账号}为等值条件查询 RB_BUS_ACCT，取得该账户记录的账户属性。
     * 需求按单一账户记录表述（不覆盖事项 2），BASE_ACCT_NO 等值查询即该单条记录；
     * 查无记录或账户属性为空时 Spec 未定义归属（不覆盖事项 1），此时无[账户属性]可判，
     * 不属于两类命中属性，由步骤2按"否则"分支返回检查结果"通过"。
     */
    private AcctNatureNo findAcctNatureNo(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);
        if (acctList == null || acctList.isEmpty()) {
            return null;
        }
        return acctList.get(0).getAcctNatureNo();
    }
}
