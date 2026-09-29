package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.dcits.depsit.enums.AcctStatus;
import com.dcits.depsit.facade.bo.ST088InputBO;
import com.dcits.depsit.facade.bo.ST088OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST088 检查账户存在性 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST088-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST088（inputs 绑定的 ST088.md）。
 * 本步骤仅判定记录存在性，不判定账户状态；唯一错误码为"ER0048"（账户不存在），
 * errorMessage 文案需求未定义，不作断言。
 */
@ExtendWith(MockitoExtension.class)
public class ST088PbcTest {

    /** 账号存在路径示例账号（Spec 场景示例数据） */
    private static final String HIT_ACCT_NO = "6100230010001";

    /** 账号不存在路径示例账号（Spec 场景示例数据） */
    private static final String MISS_ACCT_NO = "6100299900001";

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @InjectMocks
    private ST088Pbc st088;

    /** 构造输入：指定账号 */
    private ST088InputBO input(String baseAcctNo) {
        ST088InputBO input = new ST088InputBO();
        input.setBaseAcctNo(baseAcctNo);
        return input;
    }

    /** 构造账户记录：账号均为 HIT_ACCT_NO，主键与账户状态按参数区分 */
    private RbBusAcctEO acct(Integer internalKey, AcctStatus acctStatus) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(HIT_ACCT_NO);
        eo.setInternalKey(internalKey);
        eo.setAcctStatus(acctStatus);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：argThat 以占位 null 真实调用 mock，不防护会 NPE；
    // 生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 BASE_ACCT_NO 等值条件核对请求并返回账户记录列表 */
    private void stubFindByAcctNo(String baseAcctNo, List<RbBusAcctEO> records) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && baseAcctNo.equals(eo.getBaseAcctNo()))))
                .thenReturn(records);
    }

    // REQ-001-S01 → REQ-002-S01 账号命中单条账户记录：检查通过，输出该账号（与输入一致）
    @Test
    public void testST088T01() {
        stubFindByAcctNo(HIT_ACCT_NO, Collections.singletonList(
                acct(2000011001, null)));

        ST088OutputBO output = st088.execute(input(HIT_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(HIT_ACCT_NO, output.getBaseAcctNo());
    }

    // REQ-001-S02 → REQ-002-S02 账号无任何账户记录：空结果不是技术错误，返回 ER0048 且输出 baseAcctNo 为空
    @Test
    public void testST088T02() {
        stubFindByAcctNo(MISS_ACCT_NO, Collections.emptyList());

        ST088OutputBO output = st088.execute(input(MISS_ACCT_NO));

        assertFalse(output.isSucceed());
        assertEquals("ER0048", output.getErrorCode());
        assertNull(output.getBaseAcctNo());
    }

    // 边界：同一账号命中两条物理记录（主键不同）：命中条数不影响可观察结果，仍通过且输出仍为该账号
    @Test
    public void testST088T03() {
        stubFindByAcctNo(HIT_ACCT_NO, Arrays.asList(
                acct(2000011001, null),
                acct(2000011002, null)));

        ST088OutputBO output = st088.execute(input(HIT_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(HIT_ACCT_NO, output.getBaseAcctNo());
    }

    // 边界：命中记录账户状态为"关闭"（AcctStatus.C，销户后状态）：仅判定记录存在性，同样判为"通过"
    @Test
    public void testST088T04() {
        stubFindByAcctNo(HIT_ACCT_NO, Collections.singletonList(
                acct(2000011003, AcctStatus.C)));

        ST088OutputBO output = st088.execute(input(HIT_ACCT_NO));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(HIT_ACCT_NO, output.getBaseAcctNo());
    }
}
