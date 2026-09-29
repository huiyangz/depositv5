package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.facade.bo.ST040InputBO;
import com.dcits.depsit.facade.bo.ST040OutputBO;
import com.dcits.depsit.facade.components.IRbTranDefBcc;
import com.dcits.depsit.facade.eo.RbTranDefEO;

/**
 * ST040 检查现金支取交易权限。
 *
 * 步骤1 按输入{交易类型}查询【交易类型定义】（RB_TRAN_DEF，主键查询至多一条），
 * 取得借贷标志、现金交易标志、冲正交易标志并写入输出（查无记录时输出保持空）。
 * 步骤2 判定是否为现金支取交易：借贷标志为"D借方"（CrDrInd.D）、现金交易标志为
 * "Y是"、冲正交易标志为"N否"三者同时成立时，本步骤以错误码 ER0070 失败返回；
 * 否则返回检查结果"通过"（步骤成功返回）。只读步骤，无写操作，无事务要求。
 */
@Service
public class ST040Pbc implements IST040 {

    /** 现金支取判定失败错误码 */
    private static final String ERROR_CODE_CASH_WITHDRAW = "ER0070";

    /** 错误码 ER0070 配置文案（deposit-application/src/main/resources/errorcodes.properties） */
    private static final String CASH_WITHDRAW_CONFIG_MESSAGE = "“11002-一般存款账户”不允许现金支取";

    /** 失败错误信息：按“错误码::业务说明”格式拼装错误码与配置文案 */
    private static final String ERROR_MESSAGE_CASH_WITHDRAW =
            ERROR_CODE_CASH_WITHDRAW + "::" + CASH_WITHDRAW_CONFIG_MESSAGE;

    /** 现金交易标志判定值：Y-是 */
    private static final String CASH_TRAN_FLAG_YES = "Y";

    /** 冲正交易标志判定值：N-否 */
    private static final String REVERSAL_NO = "N";

    @Autowired
    private IRbTranDefBcc rbTranDefBcc;

    @Override
    public ST040OutputBO execute(ST040InputBO input) {
        ST040OutputBO output = new ST040OutputBO();

        // 步骤1 获取交易类型定义：按{交易类型}主键查询 RB_TRAN_DEF，至多一条，未查到返回 null
        RbTranDefEO tranDef = rbTranDefBcc.findByTranType(input.getTranType());

        // 查询到记录时输出三个标志，与步骤2判定分支无关；查无记录时输出保持空
        if (tranDef != null) {
            output.setCrDrInd(tranDef.getCrDrInd());
            output.setCashTranFlag(tranDef.getCashTranFlag());
            output.setReversal(tranDef.getReversal());
        }

        // 步骤2 检查现金支取交易权限：借方 + 现金交易 + 非冲正 同时成立时命中判定
        if (isCashWithdrawTran(tranDef)) {
            output.setSucceed(false);
            output.setErrorCode(ERROR_CODE_CASH_WITHDRAW);
            output.setErrorMessage(ERROR_MESSAGE_CASH_WITHDRAW);
            return output;
        }

        // 否则：检查结果"通过"（含查无记录时三条件无法同时成立的"否则"分支）
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤2 现金支取判定：借贷标志等于"D借方"、现金交易标志等于"Y是"、冲正交易标志
     * 等于"N否"三者同时成立时判定命中；查无记录（null）时三条件无法同时成立。
     */
    private boolean isCashWithdrawTran(RbTranDefEO tranDef) {
        if (tranDef == null) {
            return false;
        }
        return tranDef.getCrDrInd() == CrDrInd.D
                && CASH_TRAN_FLAG_YES.equals(tranDef.getCashTranFlag())
                && REVERSAL_NO.equals(tranDef.getReversal());
    }
}
