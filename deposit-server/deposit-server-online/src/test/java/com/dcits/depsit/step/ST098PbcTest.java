package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST098InputBO;
import com.dcits.depsit.facade.bo.ST098OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST098 检查限额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST098-TC001 ~ TC012），
 * 预期结果来自正式 Spec ST098（inputs 绑定的 ST098.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"超限"是正常业务结果不是失败。
 */
@ExtendWith(MockitoExtension.class)
public class ST098PbcTest {

    @Mock
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @InjectMocks
    private ST098Pbc st098;

    /** 构造输入：四字段按用例赋值，金额以字符串构造 BigDecimal 确定标度 */
    private ST098InputBO input(String limitBranchId, String limitSceneNo, String limitSumAmt, Integer limitSumNum) {
        ST098InputBO input = new ST098InputBO();
        input.setLimitBranchId(limitBranchId);
        input.setLimitSceneNo(limitSceneNo);
        input.setLimitSumAmt(new BigDecimal(limitSumAmt));
        input.setLimitSumNum(limitSumNum);
        return input;
    }

    /** 构造限额控制配置记录：联合主键取查询键对应值还原记录真实性（判定与输出不使用），控制金额/笔数按用例 */
    private RbLimitCtrlConfEO confRecord(TranBranch limitBranchId, String limitSceneNo,
                                         String limitCtrlAmt, Integer limitCtrlNum) {
        RbLimitCtrlConfEO eo = new RbLimitCtrlConfEO();
        eo.setLimitBranchId(limitBranchId);
        eo.setLimitSceneNo(limitSceneNo);
        eo.setLimitCtrlAmt(limitCtrlAmt == null ? null : new BigDecimal(limitCtrlAmt));
        eo.setLimitCtrlNum(limitCtrlNum);
        return eo;
    }

    /** 步骤1 桩：按输入查询键精确实参匹配返回配置记录（或 null），同时验证查询键来源于输入 */
    private void stubFindByPrimaryKey(String limitBranchId, String limitSceneNo, RbLimitCtrlConfEO record) {
        lenient().when(rbLimitCtrlConfBcc.findByPrimaryKey(limitBranchId, limitSceneNo)).thenReturn(record);
    }

    /** 公共断言：succeed=true、错误字段 null（无业务失败场景） */
    private void assertSucceed(ST098OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // REQ-001-S01+REQ-002-S01+REQ-003-S01 配置存在两控制值均有值，仅金额超限（1500.00>1000.00，10≤100），"超限"，5 字段全断言
    @Test
    public void testST098T01() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", 100));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "1500.00", 10));

        assertSucceed(output);
        assertEquals("超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(100), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("1500.00"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(10), output.getLimitSumNum());
    }

    // REQ-002-S02 仅笔数超限（999.99≤1000.00，11>10），"超限"
    @Test
    public void testST098T02() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", 10));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "999.99", 11));

        assertSucceed(output);
        assertEquals("超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(11), output.getLimitSumNum());
    }

    // REQ-002-S03 金额与笔数均超限（1500.00>1000.00 且 11>10）；以不同机构/场景键（351156/SCN0002）验证查询键透传
    @Test
    public void testST098T03() {
        stubFindByPrimaryKey("351156", "SCN0002",
                confRecord(TranBranch.VALUE_351156, "SCN0002", "1000.00", 10));

        ST098OutputBO output = st098.execute(input("351156", "SCN0002", "1500.00", 11));

        assertSucceed(output);
        assertEquals("超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("1500.00"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(11), output.getLimitSumNum());
    }

    // REQ-002-S04 金额与笔数均未超（999.99≤1000.00，9≤10），"未超限"
    @Test
    public void testST098T04() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", 10));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "999.99", 9));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(9), output.getLimitSumNum());
    }

    // REQ-002-S05 累计金额与控制金额数值相等但标度不同（1000.0 对 1000.00），BigDecimal 按数值比较不构成超限；透传保持标度 1 不变
    @Test
    public void testST098T05() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", 10));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "1000.0", 9));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("1000.0"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(9), output.getLimitSumNum());
    }

    // REQ-002-S06+REQ-003-S02 累计笔数等于控制笔数（10=10，严格大于不成立），金额未超，"未超限"，5 字段全断言
    @Test
    public void testST098T06() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", 10));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "999.99", 10));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(10), output.getLimitSumNum());
    }

    // REQ-002-S07 记录存在但两控制字段均为空，两分支均不参与判定（不论累计值多大），"未超限"，两控制值输出 null
    @Test
    public void testST098T07() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", null, null));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "99999.99", 99999));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertNull(output.getLimitCtrlAmt());
        assertNull(output.getLimitCtrlNum());
        assertEquals(new BigDecimal("99999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(99999), output.getLimitSumNum());
    }

    // REQ-001-S02+REQ-002-S07+REQ-003-S03 无配置记录（查询返回 null），两控制值为 null，"未超限"，5 字段全断言
    @Test
    public void testST098T08() {
        stubFindByPrimaryKey("351155", "SCN0001", null);

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "500.00", 3));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertNull(output.getLimitCtrlAmt());
        assertNull(output.getLimitCtrlNum());
        assertEquals(new BigDecimal("500.00"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(3), output.getLimitSumNum());
    }

    // REQ-001-S03 配置存在但限额控制金额为空，金额分支不参与判定（与累计金额 99999.99 大小无关），笔数 9≤10 不成立，"未超限"
    @Test
    public void testST098T09() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", null, 10));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "99999.99", 9));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertNull(output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("99999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(9), output.getLimitSumNum());
    }

    // REQ-002 空值守护组合：控制金额为空、笔数分支单独成立（11>10），仍"超限"
    @Test
    public void testST098T10() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", null, 10));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "99999.99", 11));

        assertSucceed(output);
        assertEquals("超限", output.getLimitCheckResult());
        assertNull(output.getLimitCtrlAmt());
        assertEquals(Integer.valueOf(10), output.getLimitCtrlNum());
        assertEquals(new BigDecimal("99999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(11), output.getLimitSumNum());
    }

    // REQ-002 空值守护组合：控制笔数为空、金额分支单独成立（1000.01>1000.00），仍"超限"
    @Test
    public void testST098T11() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", null));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "1000.01", 99999));

        assertSucceed(output);
        assertEquals("超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertNull(output.getLimitCtrlNum());
        assertEquals(new BigDecimal("1000.01"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(99999), output.getLimitSumNum());
    }

    // REQ-002 空值守护组合：控制笔数为空、金额分支 999.99≤1000.00 不成立，"未超限"
    @Test
    public void testST098T12() {
        stubFindByPrimaryKey("351155", "SCN0001",
                confRecord(TranBranch.VALUE_351155, "SCN0001", "1000.00", null));

        ST098OutputBO output = st098.execute(input("351155", "SCN0001", "999.99", 99999));

        assertSucceed(output);
        assertEquals("未超限", output.getLimitCheckResult());
        assertEquals(new BigDecimal("1000.00"), output.getLimitCtrlAmt());
        assertNull(output.getLimitCtrlNum());
        assertEquals(new BigDecimal("999.99"), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(99999), output.getLimitSumNum());
    }
}
