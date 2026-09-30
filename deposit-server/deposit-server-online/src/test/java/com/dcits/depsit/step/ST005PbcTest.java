package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST005InputBO;
import com.dcits.depsit.facade.bo.ST005OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;

/**
 * ST005 检查账户是否存在限制 单元测试
 *
 * <p>依据正式 Spec ST005 与测试用例 ST005-TC001～TC008：按输入账号查询【账户信息】判定
 * 子账户（约定示例值："1" 表明子账户、"0"/null 未表明，待业务确认后同步替换）并设置待查账户，
 * 按待查账户与限制状态"A-生效"查询【账户限制信息】，三个集合同下标逐条赋值输出；
 * 无业务失败场景，技术异常原样传播。仅对两个 BCC 设桩，被测步骤真实执行。</p>
 */
@ExtendWith(MockitoExtension.class)
class ST005PbcTest {

    /** Spec 示例主账户账号 */
    private static final String MAIN_ACCT_NO = "MAIN200010001";

    /** Spec 示例子账户账号 */
    private static final String SUB_ACCT_NO = "SUB200010002";

    /** Spec 示例上级账户内部键（主账户记录主键） */
    private static final Integer PARENT_INTERNAL_KEY = 90001;

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @InjectMocks
    private ST005Pbc st005Pbc;

    private ST005InputBO buildInput(String baseAcctNo) {
        ST005InputBO input = new ST005InputBO();
        input.setBaseAcctNo(baseAcctNo);
        return input;
    }

    /** 构造账户信息记录（账号、主账户标志、账户内部键值、上级账户内部键） */
    private RbBusAcctEO acct(String baseAcctNo, String leadAcctFlag, Integer internalKey, Integer parentInternalKey) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setLeadAcctFlag(leadAcctFlag);
        eo.setInternalKey(internalKey);
        eo.setParentInternalKey(parentInternalKey);
        return eo;
    }

    /** 构造生效账户限制记录（账号、限制状态"A-生效"、限制编号、账户限制类型） */
    private RbBusRestraintsEO restraint(String baseAcctNo, String resSeqNo, RestraintType restraintType) {
        RbBusRestraintsEO eo = new RbBusRestraintsEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setRestraintsStatus(RestraintsStatus.A);
        eo.setResSeqNo(resSeqNo);
        eo.setRestraintType(restraintType);
        return eo;
    }

    /** 账户信息查询桩：argThat 强制查询条件携带账号，实现条件缺失则桩不匹配 */
    private void stubAcctFindByEo(String baseAcctNo, List<RbBusAcctEO> result) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo -> eo != null
                && baseAcctNo.equals(eo.getBaseAcctNo())))).thenReturn(result);
    }

    /** 限制信息查询桩：argThat 强制查询条件同时携带账号与限制状态"A-生效"，实现条件缺失则桩不匹配 */
    private void stubRestraintsFindByEo(String baseAcctNo, List<RbBusRestraintsEO> result) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo -> eo != null
                && baseAcctNo.equals(eo.getBaseAcctNo())
                && RestraintsStatus.A == eo.getRestraintsStatus()))).thenReturn(result);
    }

    // REQ-001-S01 + REQ-002-S02 + REQ-003-S01 + REQ-004-S01 + REQ-005-S01：非子账户仅按账号查询账户信息，待查账户=输入账号，查得 3 条生效限制，三个集合逐条对应赋值输出
    @Test
    void testST005T01() {
        stubAcctFindByEo(MAIN_ACCT_NO, List.of(acct(MAIN_ACCT_NO, "0", 80001, PARENT_INTERNAL_KEY)));
        stubRestraintsFindByEo(MAIN_ACCT_NO, List.of(
                restraint(MAIN_ACCT_NO, "R0001", RestraintType.VALUE_13),
                restraint(MAIN_ACCT_NO, "R0002", RestraintType.VALUE_56),
                restraint(MAIN_ACCT_NO, "R0003", RestraintType.DX2)));

        ST005OutputBO output = st005Pbc.execute(buildInput(MAIN_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(MAIN_ACCT_NO, output.getBaseAcctNo());
        assertEquals("0", output.getLeadAcctFlag());
        assertEquals(3, output.getResSeqNo().size());
        assertTrue(output.getResSeqNo().contains("R0001"));
        assertTrue(output.getResSeqNo().contains("R0002"));
        assertTrue(output.getResSeqNo().contains("R0003"));
        assertTrue(output.getRestraintType().containsAll(List.of(
                RestraintType.VALUE_13, RestraintType.VALUE_56, RestraintType.DX2)));
        assertEquals(3, output.getRestraintType().size());
        assertEquals(3, output.getRestraintsStatus().size());
        // 顺序无关的同下标同源断言：每个下标三集合元素来自同一条记录，restraintsStatus 均为 A
        Map<String, RestraintType> expectedMapping = Map.of(
                "R0001", RestraintType.VALUE_13,
                "R0002", RestraintType.VALUE_56,
                "R0003", RestraintType.DX2);
        for (int i = 0; i < output.getResSeqNo().size(); i++) {
            assertEquals(expectedMapping.get(output.getResSeqNo().get(i)), output.getRestraintType().get(i));
            assertEquals(RestraintsStatus.A, output.getRestraintsStatus().get(i));
        }
    }

    // REQ-001-S02 + REQ-002-S01 + REQ-005-S01：子账户先按账号查询账户信息，再按上级账户内部键取得主账户账号作为待查账户；输出 baseAcctNo/leadAcctFlag 仍回显子账户记录
    @Test
    void testST005T02() {
        stubAcctFindByEo(SUB_ACCT_NO, List.of(acct(SUB_ACCT_NO, "1", 80002, PARENT_INTERNAL_KEY)));
        lenient().when(rbBusAcctBcc.findByPrimaryKey(PARENT_INTERNAL_KEY))
                .thenReturn(acct(MAIN_ACCT_NO, "0", PARENT_INTERNAL_KEY, null));
        // 限制查询桩仅匹配主账户账号：实现误用子账户账号查询则桩不匹配，验证待查账户路由
        stubRestraintsFindByEo(MAIN_ACCT_NO, List.of(
                restraint(MAIN_ACCT_NO, "R0001", RestraintType.VALUE_5)));

        ST005OutputBO output = st005Pbc.execute(buildInput(SUB_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(SUB_ACCT_NO, output.getBaseAcctNo());
        assertEquals("1", output.getLeadAcctFlag());
        assertEquals(1, output.getResSeqNo().size());
        assertEquals("R0001", output.getResSeqNo().get(0));
        assertEquals(RestraintType.VALUE_5, output.getRestraintType().get(0));
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus().get(0));
    }

    // REQ-003-S02 + REQ-005-S01：待查账户存在 2 条"A-生效"、1 条"E-已终止"、1 条"F-未生效"记录，仅 2 条生效记录进入查询结果与输出
    @Test
    void testST005T03() {
        stubAcctFindByEo(MAIN_ACCT_NO, List.of(acct(MAIN_ACCT_NO, "0", 80001, PARENT_INTERNAL_KEY)));
        stubRestraintsFindByEo(MAIN_ACCT_NO, List.of(
                restraint(MAIN_ACCT_NO, "R0001", RestraintType.VALUE_5),
                restraint(MAIN_ACCT_NO, "R0002", RestraintType.VALUE_13)));

        ST005OutputBO output = st005Pbc.execute(buildInput(MAIN_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(2, output.getResSeqNo().size());
        assertEquals(2, output.getRestraintType().size());
        assertEquals(2, output.getRestraintsStatus().size());
        Map<String, RestraintType> expectedMapping = Map.of(
                "R0001", RestraintType.VALUE_5,
                "R0002", RestraintType.VALUE_13);
        for (int i = 0; i < output.getResSeqNo().size(); i++) {
            assertEquals(expectedMapping.get(output.getResSeqNo().get(i)), output.getRestraintType().get(i));
            assertEquals(RestraintsStatus.A, output.getRestraintsStatus().get(i));
        }
    }

    // REQ-003-S03 + REQ-004-S02 + REQ-005-S01：待查账户无任何限制记录，三集合为空集合（size=0 非 null），步骤成功结束，账号与主账户标志仍正常输出
    @Test
    void testST005T04() {
        stubAcctFindByEo(MAIN_ACCT_NO, List.of(acct(MAIN_ACCT_NO, "0", 80001, PARENT_INTERNAL_KEY)));
        stubRestraintsFindByEo(MAIN_ACCT_NO, List.of());

        ST005OutputBO output = st005Pbc.execute(buildInput(MAIN_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(MAIN_ACCT_NO, output.getBaseAcctNo());
        assertEquals("0", output.getLeadAcctFlag());
        assertNotNull(output.getResSeqNo());
        assertNotNull(output.getRestraintType());
        assertNotNull(output.getRestraintsStatus());
        assertEquals(0, output.getResSeqNo().size());
        assertEquals(0, output.getRestraintType().size());
        assertEquals(0, output.getRestraintsStatus().size());
    }

    // 输出表 leadAcctFlag「记录字段为空则输出为空」+ REQ-001 分支语义 + REQ-005-S01：记录 leadAcctFlag=null 未表明子账户，按非子账户路径继续，输出 leadAcctFlag=null
    @Test
    void testST005T05() {
        stubAcctFindByEo(MAIN_ACCT_NO, List.of(acct(MAIN_ACCT_NO, null, 80001, null)));
        stubRestraintsFindByEo(MAIN_ACCT_NO, List.of(
                restraint(MAIN_ACCT_NO, "R0001", RestraintType.VALUE_13)));

        ST005OutputBO output = st005Pbc.execute(buildInput(MAIN_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getLeadAcctFlag());
        assertEquals(MAIN_ACCT_NO, output.getBaseAcctNo());
        assertEquals(1, output.getResSeqNo().size());
        assertEquals("R0001", output.getResSeqNo().get(0));
        assertEquals(RestraintType.VALUE_13, output.getRestraintType().get(0));
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus().get(0));
    }

    // REQ-005-S02：按输入账号查询【账户信息】抛技术异常，异常原样向调用方传播，不转译为业务失败，不产生步骤输出
    @Test
    void testST005T06() {
        stubAcctFindByEoThrow(MAIN_ACCT_NO, new RuntimeException("数据访问异常-账户信息查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st005Pbc.execute(buildInput(MAIN_ACCT_NO)));

        assertEquals("数据访问异常-账户信息查询", ex.getMessage());
    }

    // REQ-005（任一依赖查询）+ REQ-001-S02 前段：子账户判定成立后按上级账户内部键查询【账户信息】抛技术异常，异常原样传播
    @Test
    void testST005T07() {
        stubAcctFindByEo(SUB_ACCT_NO, List.of(acct(SUB_ACCT_NO, "1", 80002, PARENT_INTERNAL_KEY)));
        lenient().when(rbBusAcctBcc.findByPrimaryKey(PARENT_INTERNAL_KEY))
                .thenThrow(new RuntimeException("数据访问异常-主账户查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st005Pbc.execute(buildInput(SUB_ACCT_NO)));

        assertEquals("数据访问异常-主账户查询", ex.getMessage());
    }

    // REQ-005（任一依赖查询）：按待查账户 + "A-生效"查询【账户限制信息表】抛技术异常，异常原样传播
    @Test
    void testST005T08() {
        stubAcctFindByEo(MAIN_ACCT_NO, List.of(acct(MAIN_ACCT_NO, "0", 80001, PARENT_INTERNAL_KEY)));
        stubRestraintsFindByEoThrow(MAIN_ACCT_NO, new RuntimeException("数据访问异常-账户限制信息查询"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st005Pbc.execute(buildInput(MAIN_ACCT_NO)));

        assertEquals("数据访问异常-账户限制信息查询", ex.getMessage());
    }

    private void stubAcctFindByEoThrow(String baseAcctNo, RuntimeException exception) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo -> eo != null
                && baseAcctNo.equals(eo.getBaseAcctNo())))).thenThrow(exception);
    }

    private void stubRestraintsFindByEoThrow(String baseAcctNo, RuntimeException exception) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo -> eo != null
                && baseAcctNo.equals(eo.getBaseAcctNo())
                && RestraintsStatus.A == eo.getRestraintsStatus()))).thenThrow(exception);
    }
}
