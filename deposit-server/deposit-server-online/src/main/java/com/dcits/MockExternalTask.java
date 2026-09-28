package com.dcits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dcits.depsit.facade.components.IMbProdDefineBcc;
import com.dcits.depsit.facade.components.IMbProdIntBcc;
import com.dcits.depsit.facade.eo.MbProdDefineEO;
import com.dcits.depsit.facade.eo.MbProdIntEO;

/**
 * 既定外部系统 mock：暴露与 {@code ExternalTaskClient} 调用地址一致的 GET 路径。
 * 查表经工程内表访问组件（Bcc），查不到返回空串；不查表的按清单返回拼装值或固定值。
 * 只 logger.debug，不抛异常。
 */
@RestController
public class MockExternalTask {

    private static final Logger logger = LoggerFactory.getLogger(MockExternalTask.class);

    /** 生成账号 mock 固定前缀 */
    private static final String ACCT_NO_PREFIX = "AC";

    /** 生成账号 mock 序号 */
    private static final AtomicLong ACCT_NO_SEQ = new AtomicLong(0);

    @Autowired
    private IMbProdDefineBcc mbProdDefineBcc;

    @Autowired
    private IMbProdIntBcc mbProdIntBcc;

    /**
     * 产品管理 · 查询产品信息：按 产品编号 + ATTR_KEY 查产品定义表 MB_PROD_DEFINE 取属性值 ATTR_VALUE。
     */
    @GetMapping("/productManagement/queryProductInfo")
    public Map<String, Object> queryProductInfo(@RequestParam String prodNo, @RequestParam String attrKey) {
        Map<String, Object> result = emptyProductInfo();
        try {
            MbProdDefineEO query = new MbProdDefineEO();
            query.setProdNo(prodNo);
            query.setAttrKey(attrKey);
            List<MbProdDefineEO> rows = mbProdDefineBcc.findByEo(query);
            if (rows == null || rows.isEmpty()) {
                logger.debug("mock queryProductInfo 未查到产品属性 prodNo={} attrKey={}", prodNo, attrKey);
                return result;
            }
            String attrValue = rows.get(0).getAttrValue() == null ? "" : rows.get(0).getAttrValue();
            fillProductInfo(result, attrKey, attrValue);
            logger.debug("mock queryProductInfo prodNo={} attrKey={} attrValue={}", prodNo, attrKey, attrValue);
        } catch (Exception ex) {
            logger.debug("mock queryProductInfo 查询异常 prodNo={} attrKey={}", prodNo, attrKey, ex);
        }
        return result;
    }

    /**
     * 产品管理 · 查询产品利率信息：按产品编号查产品利率信息表 MB_PROD_INT 取首条记录。
     */
    @GetMapping("/productManagement/queryProductInterestRate")
    public Map<String, Object> queryProductInterestRate(@RequestParam String prodNo) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("intTypeList", new ArrayList<String>());
        result.put("prodIntRate", "");
        result.put("maxExecRate", "");
        result.put("minExecRate", "");
        try {
            MbProdIntEO query = new MbProdIntEO();
            query.setProdNo(prodNo);
            List<MbProdIntEO> rows = mbProdIntBcc.findByEo(query);
            if (rows == null || rows.isEmpty()) {
                logger.debug("mock queryProductInterestRate 未查到产品利率 prodNo={}", prodNo);
                return result;
            }
            List<String> intTypeList = new ArrayList<>();
            for (MbProdIntEO row : rows) {
                if (row.getIntType() != null) {
                    intTypeList.add(row.getIntType().getValue());
                }
            }
            MbProdIntEO first = rows.get(0);
            result.put("intTypeList", intTypeList);
            result.put("prodIntRate", toPlainString(first.getMinRate()));
            result.put("minExecRate", toPlainString(first.getMinRate()));
            result.put("maxExecRate", toPlainString(first.getMaxRate()));
            logger.debug("mock queryProductInterestRate prodNo={} 首条记录 minRate={} maxRate={}",
                    prodNo, first.getMinRate(), first.getMaxRate());
        } catch (Exception ex) {
            logger.debug("mock queryProductInterestRate 查询异常 prodNo={}", prodNo, ex);
        }
        return result;
    }

    /**
     * 基础公共 · 生成账号：不查表，按三个入参拼装账号返回（固定前缀 + 交易机构 + 序号）。
     */
    @GetMapping("/basicCommon/genAcctNo")
    public Map<String, Object> genAcctNo(@RequestParam String acctGenRuleType, @RequestParam String branch,
            @RequestParam(required = false) String prodNo) {
        String acctNo = ACCT_NO_PREFIX + branch + String.format("%06d", ACCT_NO_SEQ.incrementAndGet());
        logger.debug("mock genAcctNo acctGenRuleType={} branch={} prodNo={} acctNo={}",
                acctGenRuleType, branch, prodNo, acctNo);
        return Map.of("acctNo", acctNo);
    }

    /**
     * 贷款 · 计算账号当日放款金额合计：不查表，按账号返回固定值 0.00。
     */
    @GetMapping("/loan/calcAcctDailyLoanAmt")
    public Map<String, Object> calcAcctDailyLoanAmt(@RequestParam String acctNo) {
        logger.debug("mock calcAcctDailyLoanAmt acctNo={} 返回固定值 0.00", acctNo);
        return Map.of("dailyOverdraftAmt", "0.00");
    }

    /** 产品信息出参骨架：10 类出参默认空值，命中的 attrKey 再覆盖 */
    private Map<String, Object> emptyProductInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acctType", "");
        result.put("withdrawalTypeList", new ArrayList<String>());
        result.put("ccyList", new ArrayList<String>());
        result.put("allowSuspendFlag", "");
        result.put("allDepFlag", "");
        result.put("allDraFlag", "");
        result.put("clientType", "");
        result.put("inlandOffshoreFlag", "");
        result.put("branchList", new ArrayList<String>());
        result.put("acctAttr", "");
        return result;
    }

    /** 按 attrKey 将属性值填入对应出参字段，集合类出参按逗号拆分为列表 */
    private void fillProductInfo(Map<String, Object> result, String attrKey, String attrValue) {
        switch (attrKey) {
        case "ACCT_TYPE" -> result.put("acctType", attrValue);
        case "WITHDRAWAL_TYPE" -> result.put("withdrawalTypeList", splitList(attrValue));
        case "CCY" -> result.put("ccyList", splitList(attrValue));
        case "ALLOW_SUSPEND_FLAG" -> result.put("allowSuspendFlag", attrValue);
        case "ALL_DEP_FLAG" -> result.put("allDepFlag", attrValue);
        case "ALL_DRA_FLAG" -> result.put("allDraFlag", attrValue);
        case "CLIENT_TYPE" -> result.put("clientType", attrValue);
        case "INLAND_OFFSHORE" -> result.put("inlandOffshoreFlag", attrValue);
        case "PROD_BRANCH" -> result.put("branchList", splitList(attrValue));
        case "ACCT_NATURE" -> result.put("acctAttr", attrValue);
        default -> logger.debug("mock queryProductInfo 未登记的 attrKey={}，出参保持空值", attrKey);
        }
    }

    private List<String> splitList(String attrValue) {
        if (attrValue == null || attrValue.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(attrValue.split(",")));
    }

    private String toPlainString(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
