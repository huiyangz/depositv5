package com.dcits.depsit.rule;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.enums.ClientType;
import com.dcits.depsit.enums.TaxResidentFlag;

/**
 * 设置自贸区种类
 * 依据客户身份类输入（对私客户标志、税收居民标识、客户类型、境内境外标志）判定并返回[自贸区种类]，
 * 即对公存款账户主表（RB_BUS_ACCT）账户属性 acctNatureNo，取值为五个自由贸易账户种类之一；
 * 不满足规则描述 a–e 任一条件时返回 null（不设置自贸区种类）。
 */
public class BR006 {

	/**
	 * 规则描述 a：对私客户标志"Y"且税收居民标识属于{1-中国税收居民, 3-双重税收居民} → FTI-区内个人自由贸易账户(3605)；
	 * 规则描述 b：客户类型 100-个人 且税收居民标识 2-非中国税收居民 → FTF-区内境外个人自由贸易账户(3606)；
	 * 规则描述 c：客户类型 200-对公 且境内境外标志"Y" → FTE-区内机构自由贸易账户(3603)；
	 * 规则描述 d：客户类型 200-对公 且境内境外标志"N" → FTN-境外机构自由贸易账户(3604)；
	 * 规则描述 e：客户类型 300-同业 且境内境外标志"N" → FTU-同业机构自由贸易账户(3607)；
	 * 均不满足 → 返回 null。枚举按代码值比较（枚举常量等值即同常量），String 按字面值比较。
	 */
	public static AcctNatureNo execute(String isIndividual, TaxResidentFlag taxResidentFlag, ClientType clientType,
			String inlandOffshore) {
		// a：仅评估 isIndividual 与 taxResidentFlag
		if ("Y".equals(isIndividual)
				&& (taxResidentFlag == TaxResidentFlag.VALUE_1 || taxResidentFlag == TaxResidentFlag.VALUE_3)) {
			return AcctNatureNo.VALUE_3605;
		}
		// b：仅评估 clientType 与 taxResidentFlag
		if (clientType == ClientType.VALUE_100 && taxResidentFlag == TaxResidentFlag.VALUE_2) {
			return AcctNatureNo.VALUE_3606;
		}
		// c：仅评估 clientType 与 inlandOffshore
		if (clientType == ClientType.VALUE_200 && "Y".equals(inlandOffshore)) {
			return AcctNatureNo.VALUE_3603;
		}
		// d：仅评估 clientType 与 inlandOffshore
		if (clientType == ClientType.VALUE_200 && "N".equals(inlandOffshore)) {
			return AcctNatureNo.VALUE_3604;
		}
		// e：仅评估 clientType 与 inlandOffshore
		if (clientType == ClientType.VALUE_300 && "N".equals(inlandOffshore)) {
			return AcctNatureNo.VALUE_3607;
		}
		return null;
	}
}
