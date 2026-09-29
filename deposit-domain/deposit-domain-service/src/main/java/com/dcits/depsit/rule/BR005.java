package com.dcits.depsit.rule;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.dcits.depsit.enums.TermType;

/**
 * 计算到期日期（规则类型：计算类）
 *
 * 依据正式 Spec BR005：以核心运行日期为基准，按期限类型（日/月/年）与存期期限计算存款到期日期。
 * 日期口径为 年-月-日，时刻部分不参与计算；闰年按公历（格里高利历）判定。
 */
public class BR005 {

	/**
	 * 计算到期日期
	 *
	 * @param runDate 核心运行日期（{系统日期}），日期计算基准
	 * @param term 存期期限（{期限}），纯十进制正整数字符串，单位由期限类型决定（日=天、月=月、年=年）
	 * @param periodType 期限类型，本规则仅对 日(D)、月(M)、年(Y) 定义计算结果
	 * @return maturityDate 到期日期
	 */
	public static Date execute(Date runDate, String term, TermType periodType) {
		LocalDate baseDate = runDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int amount = Integer.parseInt(term);
		LocalDate maturityDate;
		switch (periodType) {
			case D:
				// a 项：到期日期 = 系统日期 + 期限天，不做任何月末或钳位调整
				maturityDate = baseDate.plusDays(amount);
				break;
			case M:
				maturityDate = plusMonths(baseDate, amount);
				break;
			case Y:
				maturityDate = plusYears(baseDate, amount);
				break;
			default:
				// 期限类型 周(W)、季(Q)、半年(H) 无业务定义（Spec 验收范围第 1 条，已接受结论放行），无计算结果
				return null;
		}
		return Date.from(maturityDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * b 项：到期日期 = 系统日期 + 期限月。
	 * 若系统日期是月末（"日"等于所在月份最后一天），或加月后日期无效（目标月份无相同的"日"），
	 * 则将"日"调整为目标月份的最后一天；两个条件均不满足时"日"保持不变。
	 */
	private static LocalDate plusMonths(LocalDate baseDate, int months) {
		LocalDate plusResult = baseDate.plusMonths(months);
		boolean baseIsMonthEnd = baseDate.getDayOfMonth() == baseDate.lengthOfMonth();
		boolean invalidAfterPlusMonths = plusResult.getDayOfMonth() != baseDate.getDayOfMonth();
		if (baseIsMonthEnd || invalidAfterPlusMonths) {
			return plusResult.withDayOfMonth(plusResult.lengthOfMonth());
		}
		return plusResult;
	}

	/**
	 * c 项：到期日期 = 系统日期 + 期限年。
	 * 仅当系统日期是 2月29日 且到期年份不是闰年时，调整为该年 2月28日；其余情形不做任何调整（不适用月口径的月末调整）。
	 */
	private static LocalDate plusYears(LocalDate baseDate, int years) {
		LocalDate plusResult = baseDate.plusYears(years);
		boolean baseIsFeb29 = baseDate.getMonthValue() == 2 && baseDate.getDayOfMonth() == 29;
		if (baseIsFeb29 && !plusResult.isLeapYear()) {
			return plusResult.withDayOfMonth(28);
		}
		return plusResult;
	}
}
