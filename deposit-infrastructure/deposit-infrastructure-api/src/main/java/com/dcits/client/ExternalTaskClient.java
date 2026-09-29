package com.dcits.client;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 跨组件外部任务客户端：既定外部系统接口的统一调用入口。
 * 接口定义来源：知识《外部接口清单》（建模定案），方法名、调用地址、入参照抄定案。
 * 目标服务统一按本地 8980 部署，部署时按环境调整 BASE_URL。
 */
@Component
public class ExternalTaskClient {

    /** 目标服务地址，部署时按环境调整 */
    private static final String BASE_URL = "http://localhost:8980";

    private final RestTemplate restTemplate;

    public ExternalTaskClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** 产品管理 · 查询产品信息：按产品编号 + 参数KEY值查询产品属性 */
    public ProductInfoResult queryProductInfo(String prodNo, String attrKey) {
        return restTemplate.getForObject(UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/productManagement/queryProductInfo")
                .queryParam("prodNo", prodNo)
                .queryParam("attrKey", attrKey)
                .encode()
                .toUriString(), ProductInfoResult.class);
    }

    /** 产品管理 · 查询产品利率信息：按产品编号查询产品利率 */
    public ProductIntRateResult queryProductInterestRate(String prodNo) {
        return restTemplate.getForObject(UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/productManagement/queryProductInterestRate")
                .queryParam("prodNo", prodNo)
                .encode()
                .toUriString(), ProductIntRateResult.class);
    }

    /** 基础公共 · 生成账号：按账号生成规则类型 + 交易机构 + 产品编号生成账号 */
    public GenAcctNoResult genAcctNo(String acctGenRuleType, String branch, String prodNo) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/basicCommon/genAcctNo")
                .queryParam("acctGenRuleType", acctGenRuleType)
                .queryParam("branch", branch);
        if (prodNo != null) {
            builder.queryParam("prodNo", prodNo);
        }
        return restTemplate.getForObject(builder.encode().toUriString(), GenAcctNoResult.class);
    }

    /** 贷款 · 计算账号当日放款金额合计：按账号获取当日累计透支额度 */
    public AcctDailyLoanAmtResult calcAcctDailyLoanAmt(String acctNo) {
        return restTemplate.getForObject(UriComponentsBuilder
                .fromHttpUrl(BASE_URL + "/loan/calcAcctDailyLoanAmt")
                .queryParam("acctNo", acctNo)
                .encode()
                .toUriString(), AcctDailyLoanAmtResult.class);
    }

    /** 产品管理 · 查询产品信息 出参 */
    public static class ProductInfoResult {
        /** 账户类型 */
        private String acctType;
        /** 支取方式列表（集合） */
        private List<String> withdrawalTypeList;
        /** 币种列表（集合） */
        private List<String> ccyList;
        /** 是否允许转久悬 */
        private String allowSuspendFlag;
        /** 通存标志 */
        private String allDepFlag;
        /** 通兑标志 */
        private String allDraFlag;
        /** 客户类型 */
        private String clientType;
        /** 境内境外标志 */
        private String inlandOffshoreFlag;
        /** 机构列表（集合） */
        private List<String> branchList;
        /** 账户属性 */
        private String acctAttr;

        public String getAcctType() {
            return acctType;
        }

        public void setAcctType(String acctType) {
            this.acctType = acctType;
        }

        public List<String> getWithdrawalTypeList() {
            return withdrawalTypeList;
        }

        public void setWithdrawalTypeList(List<String> withdrawalTypeList) {
            this.withdrawalTypeList = withdrawalTypeList;
        }

        public List<String> getCcyList() {
            return ccyList;
        }

        public void setCcyList(List<String> ccyList) {
            this.ccyList = ccyList;
        }

        public String getAllowSuspendFlag() {
            return allowSuspendFlag;
        }

        public void setAllowSuspendFlag(String allowSuspendFlag) {
            this.allowSuspendFlag = allowSuspendFlag;
        }

        public String getAllDepFlag() {
            return allDepFlag;
        }

        public void setAllDepFlag(String allDepFlag) {
            this.allDepFlag = allDepFlag;
        }

        public String getAllDraFlag() {
            return allDraFlag;
        }

        public void setAllDraFlag(String allDraFlag) {
            this.allDraFlag = allDraFlag;
        }

        public String getClientType() {
            return clientType;
        }

        public void setClientType(String clientType) {
            this.clientType = clientType;
        }

        public String getInlandOffshoreFlag() {
            return inlandOffshoreFlag;
        }

        public void setInlandOffshoreFlag(String inlandOffshoreFlag) {
            this.inlandOffshoreFlag = inlandOffshoreFlag;
        }

        public List<String> getBranchList() {
            return branchList;
        }

        public void setBranchList(List<String> branchList) {
            this.branchList = branchList;
        }

        public String getAcctAttr() {
            return acctAttr;
        }

        public void setAcctAttr(String acctAttr) {
            this.acctAttr = acctAttr;
        }
    }

    /** 产品管理 · 查询产品利率信息 出参 */
    public static class ProductIntRateResult {
        /** 利率类型列表（集合） */
        private List<String> intTypeList;
        /** 产品利率（表内无此字段，取最小执行利率占位） */
        private String prodIntRate;
        /** 最小执行利率 */
        private String minExecRate;
        /** 最大执行利率 */
        private String maxExecRate;

        public List<String> getIntTypeList() {
            return intTypeList;
        }

        public void setIntTypeList(List<String> intTypeList) {
            this.intTypeList = intTypeList;
        }

        public String getProdIntRate() {
            return prodIntRate;
        }

        public void setProdIntRate(String prodIntRate) {
            this.prodIntRate = prodIntRate;
        }

        public String getMinExecRate() {
            return minExecRate;
        }

        public void setMinExecRate(String minExecRate) {
            this.minExecRate = minExecRate;
        }

        public String getMaxExecRate() {
            return maxExecRate;
        }

        public void setMaxExecRate(String maxExecRate) {
            this.maxExecRate = maxExecRate;
        }
    }

    /** 基础公共 · 生成账号 出参 */
    public static class GenAcctNoResult {
        /** 账号（an..32） */
        private String acctNo;

        public String getAcctNo() {
            return acctNo;
        }

        public void setAcctNo(String acctNo) {
            this.acctNo = acctNo;
        }
    }

    /** 贷款 · 计算账号当日放款金额合计 出参 */
    public static class AcctDailyLoanAmtResult {
        /** 当日累计透支额度 */
        private String dailyOverdraftAmt;

        public String getDailyOverdraftAmt() {
            return dailyOverdraftAmt;
        }

        public void setDailyOverdraftAmt(String dailyOverdraftAmt) {
            this.dailyOverdraftAmt = dailyOverdraftAmt;
        }
    }
}
