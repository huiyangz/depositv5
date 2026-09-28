package com.dcits.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 跨组件外部任务客户端。
 *
 * 目标服务统一按 http://localhost:8980 部署，部署时按环境调整。
 * 方法名、调用地址、入出参见建模定案《外部接口清单》。
 */
@Component
public class ExternalTaskClient {

    /** 目标服务地址，部署时按环境调整 */
    private static final String BASE_URL = "http://localhost:8980";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 产品管理 · 查询产品信息。
     * 按「产品编号 + 参数KEY值」的 KV 结构查询产品定义，返回该 key 对应的属性值。
     * attrKey 取值：CLIENT_TYPE、ACCT_TYPE、INLAND_OFFSHORE、PROD_BRANCH、ACCT_NATURE、
     * ALL_DEP_FLAG、ALL_DRA_FLAG、WITHDRAWAL_TYPE、CCY、ALLOW_SUSPEND_FLAG。
     * 出参：acctType、withdrawalTypeList、ccyList、allowSuspendFlag、allDepFlag、allDraFlag、
     * clientType、inlandOffshoreFlag、branchList、acctAttr。
     */
    public Map<String, Object> queryProductInfo(String prodNo, String attrKey) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/productManagement/queryProductInfo")
                .queryParam("prodNo", prodNo)
                .queryParam("attrKey", attrKey);
        return getForMap(builder);
    }

    /**
     * 产品管理 · 查询产品利率信息。
     * 按「产品编号」查询产品利率信息。
     * 出参：intTypeList（利率类型列表）、prodIntRate（产品利率，取最小执行利率占位）、
     * maxExecRate（最大执行利率）、minExecRate（最小执行利率）。
     */
    public Map<String, Object> queryProductInterestRate(String prodNo) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/productManagement/queryProductInterestRate")
                .queryParam("prodNo", prodNo);
        return getForMap(builder);
    }

    /**
     * 基础公共 · 生成账号。
     * 按「账号生成规则类型 + 交易机构 + 产品编号」生成账号。
     * 出参：acctNo（an..32）。
     */
    public String genAcctNo(String acctGenRuleType, String branch, String prodNo) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/basicCommon/genAcctNo")
                .queryParam("acctGenRuleType", acctGenRuleType)
                .queryParam("branch", branch);
        if (prodNo != null) {
            builder.queryParam("prodNo", prodNo);
        }
        Map<String, Object> body = getForMap(builder);
        return body.get("acctNo") == null ? null : String.valueOf(body.get("acctNo"));
    }

    /**
     * 贷款 · 计算账号当日放款金额合计。
     * 按「账号」获取当日累计透支额度。
     * 出参：dailyOverdraftAmt（当日累计透支额度）。
     */
    public String calcAcctDailyLoanAmt(String acctNo) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/loan/calcAcctDailyLoanAmt")
                .queryParam("acctNo", acctNo);
        Map<String, Object> body = getForMap(builder);
        return body.get("dailyOverdraftAmt") == null ? null : String.valueOf(body.get("dailyOverdraftAmt"));
    }

    private Map<String, Object> getForMap(UriComponentsBuilder builder) {
        String uri = builder.encode().toUriString();
        return restTemplate.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }).getBody();
    }
}
