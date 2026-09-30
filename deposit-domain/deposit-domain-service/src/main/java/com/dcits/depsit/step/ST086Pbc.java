package com.dcits.depsit.step;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.ClientType;
import com.dcits.depsit.facade.bo.ST086InputBO;
import com.dcits.depsit.facade.bo.ST086OutputBO;
import com.dcits.depsit.facade.components.IFmClientCopyBcc;
import com.dcits.depsit.facade.eo.FmClientCopyEO;

/**
 * ST086 检查客户类型。
 *
 * 步骤1 以输入{客户号}为主键查询【客户信息】（客户副本表 FM_CLIENT_COPY，至多一条），
 * 取得$客户类型$并赋值到输出；查询无记录时客户类型未取得，输出 clientType=null，不填充默认值。
 * 步骤2 判定客户类型：等于"公司"（ClientType.VALUE_200）时检查结果"通过"（succeed=true）；
 * 不为"公司"（含任一非 VALUE_200 取值及未取得即 null 的情形）时返回错误码"ER0042"（succeed=false）。
 * 输出 clientType 保持步骤1 取得的值，不因判定结果清空或改写。只读步骤，无事务要求。
 */
@Service
public class ST086Pbc implements IST086 {

    /** 客户类型不为"公司"时的错误码 */
    private static final String ERROR_CODE_NOT_CORPORATE = "ER0042";

    /** 错误信息，格式：错误码::业务说明，文案取自工程错误码资源 errorcodes.properties（ER0042） */
    private static final String ERROR_MESSAGE_NOT_CORPORATE = "ER0042::当前客户不为对公客户";

    @Autowired
    private IFmClientCopyBcc fmClientCopyBcc;

    @Override
    public ST086OutputBO execute(ST086InputBO input) {
        ST086OutputBO output = new ST086OutputBO();

        // 步骤1 获取客户类型：按主键客户号查询，无记录时客户类型未取得（null）
        ClientType clientType = findClientType(input.getClientNo());
        output.setClientType(clientType);

        // 步骤2 检查客户类型：等于"公司"（VALUE_200）通过，否则（含未取得）返回 ER0042
        if (clientType == ClientType.VALUE_200) {
            output.setSucceed(true);
            return output;
        }
        output.setSucceed(false);
        output.setErrorCode(ERROR_CODE_NOT_CORPORATE);
        output.setErrorMessage(ERROR_MESSAGE_NOT_CORPORATE);
        return output;
    }

    /**
     * 步骤1 获取客户类型：以{客户号}为主键查询客户副本表 FM_CLIENT_COPY（至多一条），
     * 返回命中记录的客户类型；无记录时返回 null，不填充默认值。
     */
    private ClientType findClientType(String clientNo) {
        FmClientCopyEO clientCopy = fmClientCopyBcc.findByPrimaryKey(clientNo);
        if (clientCopy == null) {
            return null;
        }
        return clientCopy.getClientType();
    }
}
