package com.dcits.depsit.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.TermType;

/**
 * BR005 计算到期日期 单元测试
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（BR005-TC001 ~ TC013），预期结果来自正式 Spec BR005。
 * 断言按 年-月-日 粒度，时刻部分不断言。
 */
public class BR005Test {

	/** 构造 年-月-日 粒度的 java.util.Date */
	private static Date dateOf(int year, int month, int day) {
		return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/** 按 年-月-日 粒度描述日期，忽略时刻部分 */
	private static String toYmd(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
	}

	// REQ-001-S01 日口径常规加天：2026-01-10 加 90 天，无任何调整，预期 2026-04-10
	@Test
	public void test_01() {
		Date result = BR005.execute(dateOf(2026, 1, 10), "90", TermType.D);
		assertEquals("2026-04-10", toYmd(result));
	}

	// REQ-001-S02 日口径月末日期加一天跨月：2026-02-28 加 1 天仅顺延，不因输入是月末而调整，预期 2026-03-01
	@Test
	public void test_02() {
		Date result = BR005.execute(dateOf(2026, 2, 28), "1", TermType.D);
		assertEquals("2026-03-01", toYmd(result));
	}

	// REQ-001-S03 日口径跨年加天：2026-12-31 加 1 天进位到下一年，预期 2027-01-01
	@Test
	public void test_03() {
		Date result = BR005.execute(dateOf(2026, 12, 31), "1", TermType.D);
		assertEquals("2027-01-01", toYmd(result));
	}

	// REQ-002-S01 月口径常规加月：2026-01-15 加 6 月，月末与加月后无效两条件均不满足，日保持 15，预期 2026-07-15
	@Test
	public void test_04() {
		Date result = BR005.execute(dateOf(2026, 1, 15), "6", TermType.M);
		assertEquals("2026-07-15", toYmd(result));
	}

	// REQ-002-S02 月口径加月后日期无效（需求原文示例）：2026-01-31 加 1 月，目标月无 31 日，调整为目标月最后一天，预期 2026-02-28
	@Test
	public void test_05() {
		Date result = BR005.execute(dateOf(2026, 1, 31), "1", TermType.M);
		assertEquals("2026-02-28", toYmd(result));
	}

	// REQ-002-S03 月口径非月末但加月后日期无效：2026-01-30（非 1 月月末）加 1 月，目标月无 30 日仍调整，预期 2026-02-28
	@Test
	public void test_06() {
		Date result = BR005.execute(dateOf(2026, 1, 30), "1", TermType.M);
		assertEquals("2026-02-28", toYmd(result));
	}

	// REQ-002-S04 月口径系统日期是月末且加月后日期有效：2026-02-28 加 1 月，虽 2026-03-28 有效仍调整为目标月最后一天，预期 2026-03-31
	@Test
	public void test_07() {
		Date result = BR005.execute(dateOf(2026, 2, 28), "1", TermType.M);
		assertEquals("2026-03-31", toYmd(result));
	}

	// REQ-002-S05 月口径闰年 2 月 29 日（月末）加月：2024-02-29 加 1 月调整为目标月最后一天而非 3 月 29 日，预期 2024-03-31
	@Test
	public void test_08() {
		Date result = BR005.execute(dateOf(2024, 2, 29), "1", TermType.M);
		assertEquals("2024-03-31", toYmd(result));
	}

	// REQ-002-S06 月口径加月跨年且目标日期无效：2026-01-31 加 13 月跨到 2027 年 2 月，调整为目标月最后一天，预期 2027-02-28
	@Test
	public void test_09() {
		Date result = BR005.execute(dateOf(2026, 1, 31), "13", TermType.M);
		assertEquals("2027-02-28", toYmd(result));
	}

	// REQ-003-S01 年口径常规加年：2026-03-15 加 2 年，不触发 2 月 29 日调整，预期 2028-03-15
	@Test
	public void test_10() {
		Date result = BR005.execute(dateOf(2026, 3, 15), "2", TermType.Y);
		assertEquals("2028-03-15", toYmd(result));
	}

	// REQ-003-S02 年口径 2 月 29 日加年至非闰年：2024-02-29 加 1 年，2025 年非闰年，调整为 2 月 28 日，预期 2025-02-28
	@Test
	public void test_11() {
		Date result = BR005.execute(dateOf(2024, 2, 29), "1", TermType.Y);
		assertEquals("2025-02-28", toYmd(result));
	}

	// REQ-003-S03 年口径 2 月 29 日加年至闰年：2024-02-29 加 4 年，2028 年是闰年到期日期有效，不做调整，预期 2028-02-29
	@Test
	public void test_12() {
		Date result = BR005.execute(dateOf(2024, 2, 29), "4", TermType.Y);
		assertEquals("2028-02-29", toYmd(result));
	}

	// REQ-003-S04 年口径非 2 月 29 日的月末日期加年：2026-02-28 加 2 年仅加年，日保持 28，不做月末调整，预期 2028-02-28
	@Test
	public void test_13() {
		Date result = BR005.execute(dateOf(2026, 2, 28), "2", TermType.Y);
		assertEquals("2028-02-28", toYmd(result));
	}
}
