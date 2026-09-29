package com.dcits.depsit.step;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.client.ExternalTaskClient;
import com.dcits.client.ExternalTaskClient.QueryProductInfoResult;
import com.dcits.depsit.facade.bo.ST046InputBO;
import com.dcits.depsit.facade.bo.ST046OutputBO;

/**
 * ST046 检查币种。
 *
 * 步骤1（REQ-001）以输入{产品编号}与{参数KEY值}调用业务组件《产品管理》业务功能
 * 《查询产品信息》，从返回结果取得产品币种集合（ccyList）；步骤2（REQ-002）判断
 * 输入{币种}的币种代码值是否包含于该集合：包含时检查结果"通过"（succeed=true、
 * 错误字段为空），不包含（含集合为空）时返回错误码 ER0023（succeed=false）。
 * 无业务输出字段。《查询产品信息》调用失败或异常时的处理需求未规定，不设默认行为。
 * 只读检查步骤，无事务要求。
 */
@Service
public class ST046Pbc implements IST046 {

    /** 币种不在产品币种集合内时的错误码（正式 Spec ST046「输出」映射表） */
    private static final String ERROR_CODE_CCY_NOT_IN_PRODUCT = "ER0023";

    private final ExternalTaskClient externalTaskClient;

    public ST046Pbc(ExternalTaskClient externalTaskClient) {
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public ST046OutputBO execute(ST046InputBO input) {
        ST046OutputBO output = new ST046OutputBO();

        // 步骤1 获取产品币种集合：先于包含性检查执行
        List<String> ccyList = queryProductCcyList(input.getProdNo(), input.getAttrKey());

        // 步骤2 检查币种是否在产品配置范围内并映射步骤结果
        if (ccyList.contains(input.getCcy().getValue())) {
            output.setSucceed(true);
            return output;
        }

        // 否则分支：币种不在集合内（含空集合），返回 ER0023（errorMessage 文案需求未约定）
        output.setErrorCode(ERROR_CODE_CCY_NOT_IN_PRODUCT);
        output.setErrorMessage(ERROR_CODE_CCY_NOT_IN_PRODUCT + "::币种不在产品币种集合内");
        return output;
    }

    /**
     * 步骤1 获取产品币种集合：调用《产品管理·查询产品信息》，消费返回结果中的币种集合
     * （币种代码字符串列表，字段默认空列表）。
     */
    private List<String> queryProductCcyList(String prodNo, String attrKey) {
        QueryProductInfoResult result = externalTaskClient.queryProductInfo(prodNo, attrKey);
        return result.getCcyList();
    }
}
