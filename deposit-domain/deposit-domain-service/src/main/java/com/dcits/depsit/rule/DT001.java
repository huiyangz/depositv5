package com.dcits.depsit.rule;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.AcctStatus;
import com.dcits.depsit.enums.RbBusAcctPurpose;

/**
 * DT001 根据核准类型设置账户状态。
 *
 * 决策类规则：依据账户属性、对公存款账户用途、境内境外标志、企业标志，
 * 判定并返回对公存款账户的账户状态：
 * 新建（AcctStatus.N，"N"）、预开户（AcctStatus.I，"I"）、
 * 无（返回 null，不设置账户状态，不向 RB_BUS_ACCT 账户状态字段赋值）。
 */
public class DT001 {

    /** 境内境外标志取值：境内 */
    private static final String INLAND = "境内";

    /** 企业标志取值：是 */
    private static final String CORPORATION_YES = "是";

    /**
     * 根据核准类型设置账户状态。
     *
     * @param rbBusAcctPurpose 对公存款账户用途，非必填，可为空
     * @param acctNatureNo 账户属性，必填
     * @param corporationFlag 企业标志，必填，按需求文本以"是"/"否"判定
     * @param inlandOffshore 境内境外标志，必填，按需求文本以"境内"/"境外"判定
     * @return 账户状态："N" 新建、"I" 预开户；规则结果为"无"时返回 null（不设置账户状态）
     */
    public static String execute(RbBusAcctPurpose rbBusAcctPurpose, AcctNatureNo acctNatureNo,
                                 String corporationFlag, String inlandOffshore) {
        boolean inland = INLAND.equals(inlandOffshore);
        boolean corporation = CORPORATION_YES.equals(corporationFlag);

        // 规则描述 第1条：账户属性为基本户
        if (acctNatureNo == AcctNatureNo.VALUE_11001) {
            if (inland) {
                // 1a 境内+是→新建；1b 境内+否→预开户
                return corporation ? AcctStatus.N.getValue() : AcctStatus.I.getValue();
            }
            // 1c 境外+是→预开户；1d 境外+否→新建
            return corporation ? AcctStatus.I.getValue() : AcctStatus.N.getValue();
        }

        // 规则描述 第2条：账户属性为一般户，无论境内境外标志、企业标志，均为新建
        if (acctNatureNo == AcctNatureNo.VALUE_11002) {
            return AcctStatus.N.getValue();
        }

        // 规则描述 第3条：账户属性为专用户
        if (acctNatureNo == AcctNatureNo.VALUE_11004) {
            // 3.1 账户用途为预算单位专用存款户
            if (rbBusAcctPurpose == RbBusAcctPurpose.VALUE_4) {
                if (inland) {
                    // 3.1a 境内+是→无；3.1b 境内+否→预开户
                    return corporation ? null : AcctStatus.I.getValue();
                }
                // 3.1c 境外+是→无；3.1d 境外+否→新建
                return corporation ? null : AcctStatus.N.getValue();
            }
            // 3.2 账户用途不为预算单位专用存款户（含为空）→新建，不引用两标志
            return AcctStatus.N.getValue();
        }

        // 规则描述 第4条：账户属性为临时户
        if (acctNatureNo == AcctNatureNo.VALUE_11003) {
            if (inland) {
                // 4a 境内+是→新建；4b 境内+否→预开户
                return corporation ? AcctStatus.N.getValue() : AcctStatus.I.getValue();
            }
            // 4c 境外+是→无；4d 境外+否→新建
            return corporation ? null : AcctStatus.N.getValue();
        }

        // 规则描述 第5条：账户属性为验资户
        if (acctNatureNo == AcctNatureNo.VALUE_17) {
            if (inland) {
                // 5a 境内+是→新建；5b 境内+否→新建
                return AcctStatus.N.getValue();
            }
            // 5c 境外+是→无；5d 境外+否→新建
            return corporation ? null : AcctStatus.N.getValue();
        }

        // 规则描述 第6条：账户属性为其他取值（五类以外）→新建
        return AcctStatus.N.getValue();
    }
}
