package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.TranBranch;

/**
 * ST107 检查是否跨法人 输入BO。
 *
 * 字段定义来自正式 Spec ST107「输入」表：baseAcctNo（账号，必填）、
 * branch（归属机构号，必填，TranBranch 枚举绑定）。
 * 来源实体列表示输入值在上游交易数据中的来源，不代表本步骤的查询对象。
 * 需求未定义任一输入缺失或为空时的处理（无业务失败场景），本 BO 不做校验。
 */
public class ST107InputBO {

    /** 账号 */
    private String baseAcctNo;

    /** 归属机构号 */
    private TranBranch branch;

    public String getBaseAcctNo() {
        return baseAcctNo;
    }

    public void setBaseAcctNo(String baseAcctNo) {
        this.baseAcctNo = baseAcctNo;
    }

    public TranBranch getBranch() {
        return branch;
    }

    public void setBranch(TranBranch branch) {
        this.branch = branch;
    }
}
