package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.TranBranch;

/**
 * ST023 检查机构币种交易权限 输入BO。
 *
 * 字段定义来自正式 Spec ST023「输入」表：tranBranch（交易机构号）、tranCcy（交易币种），均必填。
 * 需求未定义{交易机构号}或{交易币种}缺失、为空时的处理，本 BO 不做校验。
 */
public class ST023InputBO {

    /** 交易机构号 */
    private TranBranch tranBranch;
    /** 交易币种 */
    private Ccy tranCcy;

    public TranBranch getTranBranch() {
        return tranBranch;
    }

    public void setTranBranch(TranBranch tranBranch) {
        this.tranBranch = tranBranch;
    }

    public Ccy getTranCcy() {
        return tranCcy;
    }

    public void setTranCcy(Ccy tranCcy) {
        this.tranCcy = tranCcy;
    }
}
