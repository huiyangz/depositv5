package com.dcits.depsit.facade.bo;

/**
 * ST006 检查是否存在转账止付限制 输入BO
 *
 * <p>依据正式 Spec ST006「输入」：账号必填，必填性由上游交易输入保证，
 * 需求未定义 baseAcctNo 为空时的处理，本步骤不发明校验分支。</p>
 */
public class ST006InputBO {

    /** 账号 */
    private String baseAcctNo;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }
}
