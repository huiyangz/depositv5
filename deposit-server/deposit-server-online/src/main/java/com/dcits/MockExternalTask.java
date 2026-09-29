package com.dcits;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dcits.client.ExternalTaskClient.CalcAcctDailyLoanAmtResult;
import com.dcits.client.ExternalTaskClient.GenAcctNoResult;
import com.dcits.client.ExternalTaskClient.QueryProductInfoResult;
import com.dcits.client.ExternalTaskClient.QueryProductInterestRateResult;
import com.dcits.depsit.facade.components.IMbProdDefineBcc;
import com.dcits.depsit.facade.components.IMbProdIntBcc;
import com.dcits.depsit.facade.eo.MbProdDefineEO;
import com.dcits.depsit.facade.eo.MbProdIntEO;

/**
 * 既定外部系统接口 mock：暴露与 ExternalTaskClient 调用地址一致的 GET 路径。
 * 查表经工程内现成的表访问组件（Bcc），查不到返回空串；只 logger.debug、不抛异常。
 */
@RestController
public class MockExternalTask {

	private static final Logger logger = LoggerFactory.getLogger(MockExternalTask.class);

	/** 生成账号 mock 的固定前缀与序号 */
	private static final String ACCT_NO_PREFIX = "ACCT";
	private static final AtomicLong ACCT_SEQ = new AtomicLong();

	@Autowired
	private IMbProdDefineBcc mbProdDefineBcc;

	@Autowired
	private IMbProdIntBcc mbProdIntBcc;

	/**
	 * 产品管理 · 查询产品信息
	 * 按 产品编号 + ATTR_KEY 查产品定义表 MB_PROD_DEFINE 取属性值 ATTR_VALUE；查不到返回空串
	 */
	@GetMapping("/productManagement/queryProductInfo")
	public QueryProductInfoResult queryProductInfo(@RequestParam String prodNo, @RequestParam String attrKey) {
		QueryProductInfoResult result = new QueryProductInfoResult();
		try {
			MbProdDefineEO query = new MbProdDefineEO();
			query.setProdNo(prodNo);
			query.setAttrKey(attrKey);
			List<MbProdDefineEO> rows = mbProdDefineBcc.findByEo(query);
			String attrValue = rows.isEmpty() || rows.get(0).getAttrValue() == null ? "" : rows.get(0).getAttrValue();
			fillMatchedField(result, attrKey, attrValue);
			logger.debug("queryProductInfo mock: prodNo={}, attrKey={}, attrValue={}", prodNo, attrKey, attrValue);
		} catch (Exception ex) {
			logger.debug("queryProductInfo mock 查表异常: prodNo={}, attrKey={}", prodNo, attrKey, ex);
		}
		return result;
	}

	/**
	 * 产品管理 · 查询产品利率信息
	 * 按产品编号查产品利率信息表 MB_PROD_INT 取首条记录：利率类型列表＝利率类型（多条 → 列表）、
	 * 产品利率＝MIN_RATE、最小执行利率＝MIN_RATE、最大执行利率＝MAX_RATE；查不到返回空串
	 */
	@GetMapping("/productManagement/queryProductInterestRate")
	public QueryProductInterestRateResult queryProductInterestRate(@RequestParam String prodNo) {
		QueryProductInterestRateResult result = new QueryProductInterestRateResult();
		try {
			MbProdIntEO query = new MbProdIntEO();
			query.setProdNo(prodNo);
			List<MbProdIntEO> rows = mbProdIntBcc.findByEo(query);
			if (!rows.isEmpty()) {
				for (MbProdIntEO row : rows) {
					if (row.getIntType() != null) {
						result.getIntTypeList().add(row.getIntType().getValue());
					}
				}
				MbProdIntEO first = rows.get(0);
				result.setProdIntRate(first.getMinRate() == null ? "" : first.getMinRate().toPlainString());
				result.setMinExecRate(first.getMinRate() == null ? "" : first.getMinRate().toPlainString());
				result.setMaxExecRate(first.getMaxRate() == null ? "" : first.getMaxRate().toPlainString());
			}
			logger.debug("queryProductInterestRate mock: prodNo={}, rows={}", prodNo, rows.size());
		} catch (Exception ex) {
			logger.debug("queryProductInterestRate mock 查表异常: prodNo={}", prodNo, ex);
		}
		return result;
	}

	/**
	 * 基础公共 · 生成账号
	 * 不查表，按三个入参拼装账号返回（固定前缀 + 交易机构 + 序号）
	 */
	@GetMapping("/basicCommon/genAcctNo")
	public GenAcctNoResult genAcctNo(@RequestParam String acctGenRuleType, @RequestParam String branch,
			@RequestParam String prodNo) {
		String acctNo = ACCT_NO_PREFIX + (branch == null ? "" : branch)
				+ String.format("%08d", ACCT_SEQ.incrementAndGet());
		logger.debug("genAcctNo mock: acctGenRuleType={}, branch={}, prodNo={}, acctNo={}", acctGenRuleType, branch,
				prodNo, acctNo);
		GenAcctNoResult result = new GenAcctNoResult();
		result.setAcctNo(acctNo);
		return result;
	}

	/**
	 * 贷款 · 计算账号当日放款金额合计
	 * 不查表，按账号返回固定值 0.00
	 */
	@GetMapping("/loan/calcAcctDailyLoanAmt")
	public CalcAcctDailyLoanAmtResult calcAcctDailyLoanAmt(@RequestParam String acctNo) {
		logger.debug("calcAcctDailyLoanAmt mock: acctNo={}, dailyOverdraftAmt=0.00", acctNo);
		CalcAcctDailyLoanAmtResult result = new CalcAcctDailyLoanAmtResult();
		result.setDailyOverdraftAmt("0.00");
		return result;
	}

	/** 按 ATTR_KEY 把属性值填进对应出参字段，集合类字段按逗号拆分 */
	private void fillMatchedField(QueryProductInfoResult result, String attrKey, String attrValue) {
		switch (attrKey == null ? "" : attrKey) {
		case "CLIENT_TYPE":
			result.setClientType(attrValue);
			break;
		case "ACCT_TYPE":
			result.setAcctType(attrValue);
			break;
		case "INLAND_OFFSHORE":
			result.setInlandOffshoreFlag(attrValue);
			break;
		case "PROD_BRANCH":
			result.setBranchList(splitToList(attrValue));
			break;
		case "ACCT_NATURE":
			result.setAcctAttr(attrValue);
			break;
		case "ALL_DEP_FLAG":
			result.setAllDepFlag(attrValue);
			break;
		case "ALL_DRA_FLAG":
			result.setAllDraFlag(attrValue);
			break;
		case "WITHDRAWAL_TYPE":
			result.setWithdrawalTypeList(splitToList(attrValue));
			break;
		case "CCY":
			result.setCcyList(splitToList(attrValue));
			break;
		case "ALLOW_SUSPEND_FLAG":
			result.setAllowSuspendFlag(attrValue);
			break;
		default:
			logger.debug("queryProductInfo mock: 未登记的 attrKey={}", attrKey);
			break;
		}
	}

	private List<String> splitToList(String value) {
		if (value == null || value.isEmpty()) {
			return List.of();
		}
		return List.of(value.split(","));
	}
}
