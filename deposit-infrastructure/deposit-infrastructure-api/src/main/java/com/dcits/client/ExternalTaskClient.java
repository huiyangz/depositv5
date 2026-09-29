package com.dcits.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 跨组件客户端：非平台外部系统的既定接口。
 * 接口定义来源：建模定案《外部接口清单》（产品管理、基础公共、贷款）。
 * 目标服务统一按本地 8980 部署，部署时按环境调整。
 */
@Component
public class ExternalTaskClient {

    /** 目标服务地址，部署时按环境调整 */
    private static final String BASE_URL = "http://localhost:8980";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 产品管理 · 查询产品信息
     * 按「产品编号 + 参数KEY值」的 KV 结构查询产品定义表，返回该 key 对应的属性值
     */
    public QueryProductInfoResult queryProductInfo(String prodNo, String attrKey) {
        return restTemplate.getForObject(
                BASE_URL + "/productManagement/queryProductInfo?prodNo={prodNo}&attrKey={attrKey}",
                QueryProductInfoResult.class, prodNo, attrKey);
    }

    /**
     * 产品管理 · 查询产品利率信息
     * 按「产品编号」查产品利率信息表
     */
    public QueryProductInterestRateResult queryProductInterestRate(String prodNo) {
        return restTemplate.getForObject(
                BASE_URL + "/productManagement/queryProductInterestRate?prodNo={prodNo}",
                QueryProductInterestRateResult.class, prodNo);
    }

    /**
     * 基础公共 · 生成账号
     * 按「账号生成规则类型 + 交易机构 + 产品编号」生成账号
     */
    public String genAcctNo(String acctGenRuleType, String branch, String prodNo) {
        GenAcctNoResult result = restTemplate.getForObject(
                BASE_URL + "/basicCommon/genAcctNo?acctGenRuleType={acctGenRuleType}&branch={branch}&prodNo={prodNo}",
                GenAcctNoResult.class, acctGenRuleType, branch, prodNo);
        return result != null && result.getAcctNo() != null ? result.getAcctNo() : "";
    }

    /**
     * 贷款 · 计算账号当日放款金额合计
     * 按「账号」获取当日累计透支额度
     */
    public String calcAcctDailyLoanAmt(String acctNo) {
        CalcAcctDailyLoanAmtResult result = restTemplate.getForObject(
                BASE_URL + "/loan/calcAcctDailyLoanAmt?acctNo={acctNo}",
                CalcAcctDailyLoanAmtResult.class, acctNo);
        return result != null && result.getDailyOverdraftAmt() != null ? result.getDailyOverdraftAmt() : "";
    }

    /** 查询产品信息出参（10 类，均 String，集合类直接返回列表） */
    public static class QueryProductInfoResult {
        /** 账户类型 */
        private String acctType = "";
        /** 支取方式列表（集合） */
        private List<String> withdrawalTypeList = new ArrayList<>();
        /** 币种列表（集合） */
        private List<String> ccyList = new ArrayList<>();
        /** 是否允许转久悬 */
        private String allowSuspendFlag = "";
        /** 通存标志 */
        private String allDepFlag = "";
        /** 通兑标志 */
        private String allDraFlag = "";
        /** 客户类型 */
        private String clientType = "";
        /** 境内境外标志 */
        private String inlandOffshoreFlag = "";
        /** 机构列表（集合） */
        private List<String> branchList = new ArrayList<>();
        /** 账户属性 */
        private String acctAttr = "";

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

    /** 查询产品利率信息出参（均 String） */
    public static class QueryProductInterestRateResult {
        /** 利率类型列表（集合） */
        private List<String> intTypeList = new ArrayList<>();
        /** 产品利率（表内无此字段，取最小执行利率 MIN_RATE 占位） */
        private String prodIntRate = "";
        /** 最大执行利率 */
        private String maxExecRate = "";
        /** 最小执行利率 */
        private String minExecRate = "";

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

        public String getMaxExecRate() {
            return maxExecRate;
        }

        public void setMaxExecRate(String maxExecRate) {
            this.maxExecRate = maxExecRate;
        }

        public String getMinExecRate() {
            return minExecRate;
        }

        public void setMinExecRate(String minExecRate) {
            this.minExecRate = minExecRate;
        }
    }

    /** 生成账号出参 */
    public static class GenAcctNoResult {
        /** 账号（an..32） */
        private String acctNo = "";

        public String getAcctNo() {
            return acctNo;
        }

        public void setAcctNo(String acctNo) {
            this.acctNo = acctNo;
        }
    }

    /** 计算账号当日放款金额合计出参 */
    public static class CalcAcctDailyLoanAmtResult {
        /** 当日累计透支额度（固定 0.00） */
        private String dailyOverdraftAmt = "";

        public String getDailyOverdraftAmt() {
            return dailyOverdraftAmt;
        }

        public void setDailyOverdraftAmt(String dailyOverdraftAmt) {
            this.dailyOverdraftAmt = dailyOverdraftAmt;
        }
    }
}
