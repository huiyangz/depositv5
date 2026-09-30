package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RbAcctType;
import com.dcits.depsit.facade.bo.ST081InputBO;
import com.dcits.depsit.facade.bo.ST081OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST081 检查账户类型 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST081-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST081（inputs 绑定的 ST081.md）。
 * errorMessage 需求未约定，全部用例不作断言；步骤2为纯判定无外部调用，不为其设桩；
 * 只读步骤，仅触达 findByEo，不使用 verify/times。
 */
@ExtendWith(MockitoExtension.class)
public class ST081PbcTest {

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @InjectMocks
    private ST081Pbc st081;

    /** 构造输入：账号 */
    private ST081InputBO input(String baseAcctNo) {
        ST081InputBO input = new ST081InputBO();
        input.setBaseAcctNo(baseAcctNo);
        return input;
    }

    /** 构造账户信息记录：账号 + 存款账户类型 */
    private RbBusAcctEO acctRecord(String baseAcctNo, RbAcctType rbAcctType) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setRbAcctType(rbAcctType);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：argThat 可能以占位 null 真实调用 mock，不防护会 NPE；
    // rbAcctType==null 核对对应 REQ-001"查询条件不含其他字段"（条件 EO 仅携带账号）
    /** 步骤1 桩：按 账号等值、条件 EO 无其他字段 核对请求并返回账户记录列表 */
    private void stubFindAcct(String baseAcctNo, List<RbBusAcctEO> records) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && baseAcctNo.equals(eo.getBaseAcctNo()) && eo.getRbAcctType() == null)))
                .thenReturn(records);
    }

    // REQ-001-S01 + REQ-002-S01 账号命中唯一账户记录且存款账户类型为 C-结算账户：检查通过
    @Test
    public void testST081T01() {
        stubFindAcct("6200000000000001", Collections.singletonList(
                acctRecord("6200000000000001", RbAcctType.C)));

        ST081OutputBO output = st081.execute(input("6200000000000001"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertEquals(RbAcctType.C, output.getRbAcctType());
    }

    // REQ-002-S02 存款账户类型为 S-储蓄账户（不等于 T 且不等于 A 的另一通过取值）：检查通过
    @Test
    public void testST081T02() {
        stubFindAcct("6200000000000002", Collections.singletonList(
                acctRecord("6200000000000002", RbAcctType.S)));

        ST081OutputBO output = st081.execute(input("6200000000000002"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertEquals(RbAcctType.S, output.getRbAcctType());
    }

    // REQ-002-S03 存款账户类型为 T-定期账户：判定不通过返回 ER0052，仍输出查得类型
    @Test
    public void testST081T03() {
        stubFindAcct("6200000000000003", Collections.singletonList(
                acctRecord("6200000000000003", RbAcctType.T)));

        ST081OutputBO output = st081.execute(input("6200000000000003"));

        assertFalse(output.isSucceed());
        assertEquals("ER0052", output.getErrorCode());
        assertEquals(RbAcctType.T, output.getRbAcctType());
    }

    // REQ-002-S04 存款账户类型为 A-AIO账户：判定不通过返回 ER0052，仍输出查得类型
    @Test
    public void testST081T04() {
        stubFindAcct("6200000000000004", Collections.singletonList(
                acctRecord("6200000000000004", RbAcctType.A)));

        ST081OutputBO output = st081.execute(input("6200000000000004"));

        assertFalse(output.isSucceed());
        assertEquals("ER0052", output.getErrorCode());
        assertEquals(RbAcctType.A, output.getRbAcctType());
    }

    // REQ-001-S02 账号查无账户记录：按业务失败结束，errorCode 留空待补（保持 null）、rbAcctType=null，不执行步骤2
    @Test
    public void testST081T05() {
        stubFindAcct("6200000000000009", Collections.emptyList());

        ST081OutputBO output = st081.execute(input("6200000000000009"));

        assertFalse(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getRbAcctType());
    }
}
