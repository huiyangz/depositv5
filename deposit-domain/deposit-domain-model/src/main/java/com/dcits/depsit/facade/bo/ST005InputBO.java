package com.dcits.depsit.facade.bo;

/**
 * ST005 检查账户是否存在限制 输入BO
 *
 * <p>依据正式 Spec ST005「输入」：账号必填，必填性由上游交易输入保证，
 * 本步骤不定义 baseAcctNo 为空时的校验分支。</p>
 */
public class ST005InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
