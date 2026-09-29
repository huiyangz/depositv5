package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.AllDepInd;
import com.dcits.depsit.facade.bo.ST094InputBO;
import com.dcits.depsit.facade.bo.ST094OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST094 检查存入账户通存标志。
 *
 * 步骤1「获取通存标识」以输入{账号}等值查询【账户信息】（RB_BUS_ACCT），取得
 * $通存标识$并赋值输出 allDepInd；账号无匹配记录或匹配记录 ALL_DEP_IND 为空时
 * 未取得，allDepInd 为 null，该情形不构成步骤失败。步骤2「检查通存标识」判定
 * 该标识是否等于"N001-允许全行存入"（AllDepInd.N001）：等于时步骤正常结束
 * （继续执行，后续步骤由交易编排负责）；不等于（含未取得的 null，完备二分）
 * 时返回错误码"ER0055"。只读步骤，无事务要求；allDepInd 输出取值不因判定
 * 结果改变。
 */
@Service
public class ST094Pbc implements IST094 {

    /** 步骤2 错误码：[通存标识]不等于"N001-允许全行存入" */
    private static final String ERROR_CODE_ALL_DEP_NOT_ALLOWED = "ER0055";

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Override
    public ST094OutputBO execute(ST094InputBO input) {
        ST094OutputBO output = new ST094OutputBO();

        // 步骤1 获取通存标识：取得$通存标识$并赋值输出，先于步骤2判定；未取得时为 null，不填充默认值
        AllDepInd allDepInd = findAllDepInd(input.getBaseAcctNo());
        output.setAllDepInd(allDepInd);

        // 步骤2 检查通存标识：不等于"N001-允许全行存入"（含 null）时返回错误码 ER0055，步骤结束
        if (allDepInd != AllDepInd.N001) {
            output.setErrorCode(ERROR_CODE_ALL_DEP_NOT_ALLOWED);
            return output;
        }

        // 等于"N001-允许全行存入"：正常结束，交易继续执行后续步骤（后续步骤不属于本任务）
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取通存标识：以 BASE_ACCT_NO={账号} 等值查询 RB_BUS_ACCT，
     * 返回匹配记录的 ALL_DEP_IND；无匹配记录或字段为空时未取得，返回 null。
     */
    private AllDepInd findAllDepInd(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> acctList = rbBusAcctBcc.findByEo(condition);
        if (acctList == null || acctList.isEmpty()) {
            return null;
        }
        return acctList.get(0).getAllDepInd();
    }
}
