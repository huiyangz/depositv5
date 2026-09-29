package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.facade.bo.ST108InputBO;
import com.dcits.depsit.facade.bo.ST108OutputBO;

/**
 * ST108 检查增加限制起始日期 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST108-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST108（inputs 绑定的 ST108.md）。
 * 纯判定步骤无外部依赖（无 BCC、规则、客户端），无需设桩；本步骤无业务失败场景，
 * 全部用例 succeed=true，"不通过"是业务结果不是失败。
 * 日期按 年-月-日 粒度本地零点构造（GregorianCalendar），与 Spec 比较口径一致。
 */
@ExtendWith(MockitoExtension.class)
public class ST108PbcTest {

    @InjectMocks
    private ST108Pbc st108;

    /** 按 年-月-日 粒度构造本地零点日期 */
    private Date date(int year, int month, int day) {
        return new GregorianCalendar(year, month, day).getTime();
    }

    /** 构造输入：runDate=2026-09-30、endDate=2026-12-31（Spec 场景公共示例值），startDate 按用例指定 */
    private ST108InputBO input(Date startDate) {
        ST108InputBO input = new ST108InputBO();
        input.setRunDate(date(2026, Calendar.SEPTEMBER, 30));
        input.setStartDate(startDate);
        input.setEndDate(date(2026, Calendar.DECEMBER, 31));
        return input;
    }

    /** ST108-TC001（REQ-001-S01）：开始日期早于系统日期，违规条件1成立，检查结果"不通过"，正常返回 */
    @Test
    public void testST108T01() {
        ST108OutputBO output = st108.execute(input(date(2026, Calendar.SEPTEMBER, 29)));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("不通过", output.getCheckResult());
    }

    /** ST108-TC002（REQ-001-S02）：开始日期晚于结束日期，违规条件2成立，检查结果"不通过"，正常返回 */
    @Test
    public void testST108T02() {
        ST108OutputBO output = st108.execute(input(date(2027, Calendar.JANUARY, 1)));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("不通过", output.getCheckResult());
    }

    /** ST108-TC003（REQ-001-S03）：开始日期等于系统日期（下边界），相等不触发违规条件，检查结果"通过" */
    @Test
    public void testST108T03() {
        ST108OutputBO output = st108.execute(input(date(2026, Calendar.SEPTEMBER, 30)));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("通过", output.getCheckResult());
    }

    /** ST108-TC004（REQ-001-S04）：开始日期等于结束日期（上边界），相等不触发违规条件，检查结果"通过" */
    @Test
    public void testST108T04() {
        ST108OutputBO output = st108.execute(input(date(2026, Calendar.DECEMBER, 31)));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("通过", output.getCheckResult());
    }

    /** ST108-TC005（REQ-001-S05）：开始日期严格位于系统日期与结束日期之间，两违规条件均不成立，检查结果"通过" */
    @Test
    public void testST108T05() {
        ST108OutputBO output = st108.execute(input(date(2026, Calendar.OCTOBER, 15)));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("通过", output.getCheckResult());
    }
}
