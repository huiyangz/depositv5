package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintLevel;
import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST015InputBO;
import com.dcits.depsit.facade.bo.ST015OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;

/**
 * ST015 检查是否存在属性限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST015-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST015（inputs 绑定的 ST015.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"否"是业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST015PbcTest {

    /** 示例账号（Spec 场景示例数据，不代表真实业务数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @InjectMocks
    private ST015Pbc st015;

    /** 构造公共输入：账号 */
    private ST015InputBO input() {
        ST015InputBO input = new ST015InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        return input;
    }

    /** 构造命中记录：限制状态均为"生效"（RestraintsStatus.A）、限制级别均为"账户属性限制"（RestraintLevel.NATURE） */
    private RbBusRestraintsEO restraint(String resSeqNo, RestraintType restraintType) {
        RbBusRestraintsEO eo = new RbBusRestraintsEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setResSeqNo(resSeqNo);
        eo.setRestraintType(restraintType);
        eo.setRestraintsStatus(RestraintsStatus.A);
        eo.setRestraintLevel(RestraintLevel.NATURE);
        return eo;
    }

    // 匹配器 lambda 带 null 防护：argThat 以真实参数调用匹配，生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 账号+限制状态=RestraintsStatus.A+限制级别=RestraintLevel.NATURE 三条件核对请求并返回记录列表 */
    private void stubRestraints(List<RbBusRestraintsEO> records) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo())
                        && eo.getRestraintsStatus() == RestraintsStatus.A
                        && eo.getRestraintLevel() == RestraintLevel.NATURE)))
                .thenReturn(records);
    }

    /** "否"路径公共断言：succeed=true、错误字段 null、natureRestraintFlag="否"、其余 4 字段 null */
    private void assertNegative(ST015OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("否", output.getNatureRestraintFlag());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getRestraintLevel());
    }

    // REQ-001-S01 + REQ-002-S02 单条命中：判定"是"并输出该条完整字段
    @Test
    public void testST015T01() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260928000001", RestraintType.VALUE_21)));

        ST015OutputBO output = st015.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getNatureRestraintFlag());
        assertEquals("RES20260928000001", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_21, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(RestraintLevel.NATURE, output.getRestraintLevel());
    }

    // REQ-001-S02 + REQ-002-S01 账号无任何限制记录：查询结果为空，判定"否"且其余 4 个输出字段为空
    @Test
    public void testST015T02() {
        stubRestraints(Collections.emptyList());

        ST015OutputBO output = st015.execute(input());

        assertNegative(output);
    }

    // REQ-001-S03 + REQ-002-S01 限制状态非"生效"（业务背景：该账号仅有 1 条限制级别 NATURE 但状态为"E-已终止"的记录）：
    // 被步骤1"限制状态=A"查询条件排除，查询结果为空，判定"否"且输出为空
    @Test
    public void testST015T03() {
        stubRestraints(Collections.emptyList());

        ST015OutputBO output = st015.execute(input());

        assertNegative(output);
    }

    // REQ-001-S04 + REQ-002-S01 限制级别非"账户属性限制"（业务背景：该账号仅有 1 条状态"生效"但级别为"ACCT-账户级别"的记录）：
    // 被步骤1"限制级别=NATURE"查询条件排除，查询结果为空，判定"否"且输出为空
    @Test
    public void testST015T04() {
        stubRestraints(Collections.emptyList());

        ST015OutputBO output = st015.execute(input());

        assertNegative(output);
    }

    // REQ-002-S03 多条命中：判定"是"，4 个输出字段取其中一条且编号与类型同条对应，断言不依赖选取顺序
    @Test
    public void testST015T05() {
        stubRestraints(Arrays.asList(
                restraint("RES20260928000001", RestraintType.VALUE_21),
                restraint("RES20260928000002", RestraintType.VALUE_24)));

        ST015OutputBO output = st015.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("是", output.getNatureRestraintFlag());
        boolean firstHit = "RES20260928000001".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_21;
        boolean secondHit = "RES20260928000002".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_24;
        assertTrue(firstHit || secondHit);
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(RestraintLevel.NATURE, output.getRestraintLevel());
    }
}
