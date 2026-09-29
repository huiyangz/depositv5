package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.TermType;
import com.dcits.depsit.facade.bo.ST109InputBO;
import com.dcits.depsit.facade.bo.ST109OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;

/**
 * ST109 登记账户限制信息 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST109-TC001 ~ TC003），
 * 预期结果来自正式 Spec ST109（inputs 绑定的 ST109.md）。
 * 本步骤为单一写入路径且无业务失败场景，全部用例 succeed=true；断言针对
 * createSelective 捕获的写入 EO 业务数据，实体 7 个 @NotNull 字段（internalKey、
 * resSeqNo、channelSeqNo、createTimestamp、lastUpdTimestamp、channelDate、company）
 * 及其余可空字段取值 Spec 明确不覆盖，均不作断言。
 */
@ExtendWith(MockitoExtension.class)
public class ST109PbcTest {

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @InjectMocks
    private ST109Pbc st109;

    /** 桩：createSelective 记录收到的写入 EO 到 capturedRef 并返回 1（写入 1 行） */
    private void stubCreateSelective(AtomicReference<RbBusRestraintsEO> capturedRef) {
        lenient().when(rbBusRestraintsBcc.createSelective(any(RbBusRestraintsEO.class)))
                .thenAnswer(invocation -> {
                    capturedRef.set(invocation.getArgument(0));
                    return 1;
                });
    }

    /** 构造确定日期 */
    private Date date(int year, int month, int day) {
        return new GregorianCalendar(year, month, day).getTime();
    }

    /** 构造 TC001/TC002 输入：账号 1002003004005006、VALUE_13、期限"12"、TermType.M，四个日期互异 */
    private ST109InputBO inputTc001() {
        ST109InputBO input = new ST109InputBO();
        input.setBaseAcctNo("1002003004005006");
        input.setRestraintType(RestraintType.VALUE_13);
        input.setStartDate(date(2026, Calendar.SEPTEMBER, 15));
        input.setEndDate(date(2027, Calendar.SEPTEMBER, 15));
        input.setTerm("12");
        input.setTermType(TermType.M);
        input.setTranDate(date(2026, Calendar.SEPTEMBER, 28));
        input.setRunDate(date(2026, Calendar.SEPTEMBER, 30));
        return input;
    }

    /** 构造 TC003 输入：账号 6200300200100090、VALUE_16、期限"3"、TermType.D，另一组互异日期 */
    private ST109InputBO inputTc003() {
        ST109InputBO input = new ST109InputBO();
        input.setBaseAcctNo("6200300200100090");
        input.setRestraintType(RestraintType.VALUE_16);
        input.setStartDate(date(2026, Calendar.JANUARY, 10));
        input.setEndDate(date(2026, Calendar.APRIL, 10));
        input.setTerm("3");
        input.setTermType(TermType.D);
        input.setTranDate(date(2026, Calendar.JANUARY, 15));
        input.setRunDate(date(2026, Calendar.JANUARY, 20));
        return input;
    }

    /** 成功路径公共断言：succeed=true、错误码/错误信息为 null（无业务失败场景） */
    private void assertSuccess(ST109OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // REQ-001-S01 正常登记：8 个必填输入齐备，7 个输入写入实体同名字段；四个日期互异，可逐字段捕获映射错位
    @Test
    public void testST109T01() {
        AtomicReference<RbBusRestraintsEO> capturedRef = new AtomicReference<>();
        stubCreateSelective(capturedRef);
        ST109InputBO input = inputTc001();

        ST109OutputBO output = st109.execute(input);

        assertSuccess(output);
        RbBusRestraintsEO captured = capturedRef.get();
        assertEquals("1002003004005006", captured.getBaseAcctNo());
        assertEquals(RestraintType.VALUE_13, captured.getRestraintType());
        assertEquals(input.getStartDate(), captured.getStartDate());
        assertEquals(input.getEndDate(), captured.getEndDate());
        assertEquals("12", captured.getTerm());
        assertEquals(TermType.M, captured.getTermType());
        assertEquals(input.getTranDate(), captured.getTranDate());
    }

    // REQ-002-S01 runDate 不映射：runDate=2026-09-30 与 tranDate=2026-09-28 取值不同，登记记录 tranDate 等于输入{交易日期}而非 runDate；实体无 runDate 同名字段，runDate 不产生任何实体字段写入
    @Test
    public void testST109T02() {
        AtomicReference<RbBusRestraintsEO> capturedRef = new AtomicReference<>();
        stubCreateSelective(capturedRef);
        ST109InputBO input = inputTc001();

        ST109OutputBO output = st109.execute(input);

        assertSuccess(output);
        RbBusRestraintsEO captured = capturedRef.get();
        assertEquals(input.getTranDate(), captured.getTranDate());
        assertEquals(input.getStartDate(), captured.getStartDate());
        assertEquals(input.getEndDate(), captured.getEndDate());
    }

    // REQ-001 正常登记（第二组可区分示例数据）：换用不同账号、VALUE_16、期限"3"、TermType.D 与另一组互异日期，验证同名映射与枚举按类型直接传递不依赖特定取值
    @Test
    public void testST109T03() {
        AtomicReference<RbBusRestraintsEO> capturedRef = new AtomicReference<>();
        stubCreateSelective(capturedRef);
        ST109InputBO input = inputTc003();

        ST109OutputBO output = st109.execute(input);

        assertSuccess(output);
        RbBusRestraintsEO captured = capturedRef.get();
        assertEquals("6200300200100090", captured.getBaseAcctNo());
        assertEquals(RestraintType.VALUE_16, captured.getRestraintType());
        assertEquals(input.getStartDate(), captured.getStartDate());
        assertEquals(input.getEndDate(), captured.getEndDate());
        assertEquals("3", captured.getTerm());
        assertEquals(TermType.D, captured.getTermType());
        assertEquals(input.getTranDate(), captured.getTranDate());
    }
}
