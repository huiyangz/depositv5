package com.dcits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dcits.client.ExternalTaskClient.AcctDailyLoanAmtResult;
import com.dcits.client.ExternalTaskClient.GenAcctNoResult;
import com.dcits.client.ExternalTaskClient.ProductInfoResult;
import com.dcits.client.ExternalTaskClient.ProductIntRateResult;
import com.dcits.depsit.facade.components.IMbProdDefineBcc;
import com.dcits.depsit.facade.components.IMbProdIntBcc;
import com.dcits.depsit.facade.eo.MbProdDefineEO;
import com.dcits.depsit.facade.eo.MbProdIntEO;

/**
 * 既定外部系统接口 mock：暴露路径与 ExternalTaskClient 调用地址一一对应。
 * 查表经工程内表访问组件（Bcc）查询；查不到返回空串；只 logger.debug、不抛异常。
 * mock 逻辑来源：知识《外部接口清单》。
 */
@RestController
public class MockExternalTask {

    private static final Logger logger = LoggerFactory.getLogger(MockExternalTask.class);

    private final IMbProdDefineBcc mbProdDefineBcc;
    private final IMbProdIntBcc mbProdIntBcc;
    /** 生成账号 mock 的自增序号 */
    private final AtomicInteger acctSeq = new AtomicInteger();

    public MockExternalTask(IMbProdDefineBcc mbProdDefineBcc, IMbProdIntBcc mbProdIntBcc) {
        this.mbProdDefineBcc = mbProdDefineBcc;
        this.mbProdIntBcc = mbProdIntBcc;
    }

    /** 产品管理 · 查询产品信息：按产品编号 + ATTR_KEY 查 MB_PROD_DEFINE 取 ATTR_VALUE */
    @GetMapping("/productManagement/queryProductInfo")
    public ProductInfoResult queryProductInfo(@RequestParam("prodNo") String prodNo,
            @RequestParam("attrKey") String attrKey) {
        ProductInfoResult result = emptyProductInfo();
        try {
            MbProdDefineEO eo = new MbProdDefineEO();
            eo.setProdNo(prodNo);
            eo.setAttrKey(attrKey);
            List<MbProdDefineEO> records = mbProdDefineBcc.findByEo(eo);
            String attrValue = records.isEmpty() || records.get(0).getAttrValue() == null ? ""
                    : records.get(0).getAttrValue();
            fillByAttrKey(result, attrKey, attrValue);
            logger.debug("mock queryProductInfo prodNo={} attrKey={} attrValue={}", prodNo, attrKey, attrValue);
        } catch (Exception e) {
            logger.debug("mock queryProductInfo 查询失败 prodNo={} attrKey={}", prodNo, attrKey, e);
        }
        return result;
    }

    /** 产品管理 · 查询产品利率信息：按产品编号查 MB_PROD_INT 取首条记录 */
    @GetMapping("/productManagement/queryProductInterestRate")
    public ProductIntRateResult queryProductInterestRate(@RequestParam("prodNo") String prodNo) {
        ProductIntRateResult result = new ProductIntRateResult();
        result.setIntTypeList(new ArrayList<>());
        result.setProdIntRate("");
        result.setMinExecRate("");
        result.setMaxExecRate("");
        try {
            MbProdIntEO eo = new MbProdIntEO();
            eo.setProdNo(prodNo);
            List<MbProdIntEO> records = mbProdIntBcc.findByEo(eo);
            if (!records.isEmpty()) {
                List<String> intTypes = new ArrayList<>();
                for (MbProdIntEO record : records) {
                    String intType = record.getIntType() == null ? "" : record.getIntType().getValue();
                    if (!intTypes.contains(intType)) {
                        intTypes.add(intType);
                    }
                }
                result.setIntTypeList(intTypes);
                MbProdIntEO first = records.get(0);
                result.setProdIntRate(first.getMinRate() == null ? "" : first.getMinRate().toPlainString());
                result.setMinExecRate(first.getMinRate() == null ? "" : first.getMinRate().toPlainString());
                result.setMaxExecRate(first.getMaxRate() == null ? "" : first.getMaxRate().toPlainString());
            }
            logger.debug("mock queryProductInterestRate prodNo={} records={}", prodNo, records.size());
        } catch (Exception e) {
            logger.debug("mock queryProductInterestRate 查询失败 prodNo={}", prodNo, e);
        }
        return result;
    }

    /** 基础公共 · 生成账号：不查表，按固定前缀 + 交易机构 + 序号拼装账号（an..32） */
    @GetMapping("/basicCommon/genAcctNo")
    public GenAcctNoResult genAcctNo(@RequestParam("acctGenRuleType") String acctGenRuleType,
            @RequestParam("branch") String branch,
            @RequestParam(value = "prodNo", required = false) String prodNo) {
        GenAcctNoResult result = new GenAcctNoResult();
        try {
            String trimmedBranch = branch.length() > 25 ? branch.substring(0, 25) : branch;
            String acctNo = "GN" + trimmedBranch + String.format("%05d", acctSeq.incrementAndGet());
            result.setAcctNo(acctNo);
            logger.debug("mock genAcctNo acctGenRuleType={} branch={} prodNo={} acctNo={}", acctGenRuleType, branch,
                    prodNo, acctNo);
        } catch (Exception e) {
            logger.debug("mock genAcctNo 生成失败 acctGenRuleType={} branch={} prodNo={}", acctGenRuleType, branch,
                    prodNo, e);
        }
        return result;
    }

    /** 贷款 · 计算账号当日放款金额合计：不查表，按账号返回固定值 0.00 */
    @GetMapping("/loan/calcAcctDailyLoanAmt")
    public AcctDailyLoanAmtResult calcAcctDailyLoanAmt(@RequestParam("acctNo") String acctNo) {
        AcctDailyLoanAmtResult result = new AcctDailyLoanAmtResult();
        result.setDailyOverdraftAmt("0.00");
        logger.debug("mock calcAcctDailyLoanAmt acctNo={} dailyOverdraftAmt=0.00", acctNo);
        return result;
    }

    private static ProductInfoResult emptyProductInfo() {
        ProductInfoResult result = new ProductInfoResult();
        result.setAcctType("");
        result.setWithdrawalTypeList(new ArrayList<>());
        result.setCcyList(new ArrayList<>());
        result.setAllowSuspendFlag("");
        result.setAllDepFlag("");
        result.setAllDraFlag("");
        result.setClientType("");
        result.setInlandOffshoreFlag("");
        result.setBranchList(new ArrayList<>());
        result.setAcctAttr("");
        return result;
    }

    /** 按 ATTR_KEY 定案取值把属性值填进对应出参，集合类出参按逗号拆分为列表 */
    private static void fillByAttrKey(ProductInfoResult result, String attrKey, String attrValue) {
        if (attrValue == null || attrValue.isEmpty()) {
            return;
        }
        switch (attrKey) {
            case "CLIENT_TYPE" -> result.setClientType(attrValue);
            case "ACCT_TYPE" -> result.setAcctType(attrValue);
            case "INLAND_OFFSHORE" -> result.setInlandOffshoreFlag(attrValue);
            case "PROD_BRANCH" -> result.setBranchList(splitValues(attrValue));
            case "ACCT_NATURE" -> result.setAcctAttr(attrValue);
            case "ALL_DEP_FLAG" -> result.setAllDepFlag(attrValue);
            case "ALL_DRA_FLAG" -> result.setAllDraFlag(attrValue);
            case "WITHDRAWAL_TYPE" -> result.setWithdrawalTypeList(splitValues(attrValue));
            case "CCY" -> result.setCcyList(splitValues(attrValue));
            case "ALLOW_SUSPEND_FLAG" -> result.setAllowSuspendFlag(attrValue);
            default -> {
                // 非定案 ATTR_KEY，保持空串
            }
        }
    }

    private static List<String> splitValues(String attrValue) {
        return Arrays.stream(attrValue.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
    }
}
