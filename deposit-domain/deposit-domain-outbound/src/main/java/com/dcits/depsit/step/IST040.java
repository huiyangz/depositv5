package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST040InputBO;
import com.dcits.depsit.facade.bo.ST040OutputBO;

/**
 * ST040 检查现金支取交易权限。
 *
 * 按输入{交易类型}查询【交易类型定义】（RB_TRAN_DEF）取得借贷标志、现金交易标志、
 * 冲正交易标志，并判定该交易类型是否为现金支取交易。只读检查步骤：无数据写入
 * 副作用，无事务要求。
 */
public interface IST040 {

    /**
     * 检查现金支取交易权限。
     *
     * @param input 输入BO，交易类型（tranType）必填，由调用方契约保证
     * @return 输出BO：crDrInd、cashTranFlag、reversal 取查询记录对应字段值，
     *         查无记录时为 null，且与判定分支无关。借贷标志为"D借方"、现金交易
     *         标志为"Y是"、冲正交易标志为"N否"三者同时成立时，本步骤失败返回
     *         （succeed=false、errorCode="ER0070"）；否则检查结果"通过"
     *         （succeed=true，错误码与错误信息为 null）。
     */
    ST040OutputBO execute(ST040InputBO input);
}
