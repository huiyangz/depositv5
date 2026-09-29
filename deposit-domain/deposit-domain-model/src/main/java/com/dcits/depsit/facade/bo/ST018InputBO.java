package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.TranBranch;

/**
 * ST018 检查交易机构 输入BO。
 *
 * 字段定义来自正式 Spec ST018「输入」表：baseAcctNo（账号，必填）、
 * tranBranch（交易机构号，必填）。
 * 需求未定义必填输入缺失或为空时的处理（无业务失败场景），本 BO 不做校验。
 */
public class ST018InputBO {

    /** 账号，与 RB_BUS_ACCT.BASE_ACCT_NO（VARCHAR）等值匹配 */
    private String baseAcctNo;
    /** 交易机构号（内部机构编号） */
    private TranBranch tranBranch;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public TranBranch getTranBranch() {
        return tranBranch;
    }

    public void setTranBranch(TranBranch tranBranch) {
        this.tranBranch = tranBranch;
    }
}
