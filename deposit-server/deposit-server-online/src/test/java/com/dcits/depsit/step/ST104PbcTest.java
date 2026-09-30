package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.facade.bo.ST104InputBO;
import com.dcits.depsit.facade.bo.ST104OutputBO;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;

/**
 * ST104 更新累计限额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST104-TC001 ~ TC007），
 * 预期结果来自正式 Spec ST104（inputs 绑定的 ST104.md）。
 * 本步骤无业务失败场景，触发与不触发路径均 succeed=true；不更新路径 limitSumAmt=null。
 */
@ExtendWith(MockitoExtension.class)
public class ST104PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    /** 示例限额场景编码（Spec 场景示例数据） */
    private static final String LIMIT_SCENE_NO = "LS20260001";

    /** 更新技术异常消息（示例） */
    private static final String UPDATE_EXCEPTION_MESSAGE = "数据访问异常-限额累计信息表更新";

    @Mock
    private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

    @InjectMocks
    private ST104Pbc st104Pbc;

    /** 构造触发条件满足的公共输入：检查结果"未超限"、金额 50000.00>0、笔数 3>0 */
    private ST104InputBO input() {
        ST104InputBO input = new ST104InputBO();
        input.set限额检查结果("未超限");
        input.setBaseAcctNo(BASE_ACCT_NO);
        input.setLimitSceneNo(LIMIT_SCENE_NO);
        input.setClientNo("C0001234567");
        input.setLimitSumAmt(new BigDecimal("50000.00"));
        input.set否(3);
        return input;
    }

    // 匹配器 lambda 需 null 防护：注册桩时 argThat 以占位 null 真实调用 mock，不防护会 NPE；
    // 若实现发送的主键值与匹配器不符，桩不命中且 captured[0] 保持 null，后续断言失败可定位
    /** 按主键更新桩：核对请求主键二值（checkObjVal、limitSceneNo）并捕获请求 EO，返回命中 1 行 */
    private RbLimitSumInfoEO[] stubCaptureUpdate() {
        RbLimitSumInfoEO[] captured = new RbLimitSumInfoEO[1];
        lenient().when(rbLimitSumInfoBcc.modifyByPrimaryKeySelective(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getCheckObjVal())
                        && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()))))
                .thenAnswer(invocation -> {
                    captured[0] = invocation.getArgument(0);
                    return 1;
                });
        return captured;
    }

    /** 成功路径公共断言：succeed=true、错误字段 null、limitSumAmt 等于更新写入值（输入值） */
    private void assertSuccess(ST104OutputBO output, BigDecimal expectedLimitSumAmt) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(expectedLimitSumAmt, output.getLimitSumAmt());
    }

    /** 捕获请求 EO 公共断言：仅携带主键二值与金额，不携带限额累计笔数（字段"否"）与客户号 */
    private void assertUpdateRequest(RbLimitSumInfoEO captured, BigDecimal expectedLimitSumAmt) {
        assertEquals(BASE_ACCT_NO, captured.getCheckObjVal());
        assertEquals(LIMIT_SCENE_NO, captured.getLimitSceneNo());
        assertEquals(expectedLimitSumAmt, captured.getLimitSumAmt());
        assertNull(captured.get否());
        assertNull(captured.getClientNo());
    }

    // REQ-001-S01+REQ-002-S01+REQ-003-S01："未超限"且金额 50000.00>0、笔数 3>0，触发条件双侧成立，
    // 按主键（1002003004005006、LS20260001）更新记录甲并输出更新后金额；请求 EO 仅携带主键二值与金额
    @Test
    public void testST104T01() {
        RbLimitSumInfoEO[] captured = stubCaptureUpdate();

        ST104OutputBO output = st104Pbc.execute(input());

        assertSuccess(output, new BigDecimal("50000.00"));
        assertUpdateRequest(captured[0], new BigDecimal("50000.00"));
    }

    // REQ-002-S02 仅 $限额累计金额$ 为更新目标：输入笔数 3 只参与触发条件判定，
    // 捕获的更新请求 EO 不携带字段"否"与 clientNo（selective 更新仅写非空属性，二者保持库中原值）
    @Test
    public void testST104T02() {
        RbLimitSumInfoEO[] captured = stubCaptureUpdate();

        ST104OutputBO output = st104Pbc.execute(input());

        assertUpdateRequest(captured[0], new BigDecimal("50000.00"));
        assertSuccess(output, new BigDecimal("50000.00"));
    }

    // REQ-001-S02 或分支·金额侧："未超限"、金额 50000.00>0、笔数=0（0 边界），触发条件仍满足，更新执行并输出 50000.00
    @Test
    public void testST104T03() {
        RbLimitSumInfoEO[] captured = stubCaptureUpdate();
        ST104InputBO input = input();
        input.set否(0);

        ST104OutputBO output = st104Pbc.execute(input);

        assertSuccess(output, new BigDecimal("50000.00"));
        assertUpdateRequest(captured[0], new BigDecimal("50000.00"));
    }

    // REQ-001-S03 或分支·笔数侧："未超限"、金额=0（0 边界）、笔数 3>0，触发条件仍满足；
    // $限额累计金额$ 更新为输入值 0，输出 limitSumAmt=0
    @Test
    public void testST104T04() {
        RbLimitSumInfoEO[] captured = stubCaptureUpdate();
        ST104InputBO input = input();
        input.setLimitSumAmt(new BigDecimal("0"));

        ST104OutputBO output = st104Pbc.execute(input);

        assertSuccess(output, new BigDecimal("0"));
        assertUpdateRequest(captured[0], new BigDecimal("0"));
    }

    // REQ-001-S04+REQ-003-S02："未超限"但金额=0 且笔数=0（两或分支均不成立），不执行任何更新，正常返回，limitSumAmt=null
    @Test
    public void testST104T05() {
        ST104InputBO input = input();
        input.setLimitSumAmt(new BigDecimal("0"));
        input.set否(0);

        ST104OutputBO output = st104Pbc.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getLimitSumAmt());
    }

    // REQ-001-S05+REQ-003-S02：限额检查结果="超限"（非"未超限"）短路，即便金额、笔数均>0 也不触发更新，limitSumAmt=null
    @Test
    public void testST104T06() {
        ST104InputBO input = input();
        input.set限额检查结果("超限");

        ST104OutputBO output = st104Pbc.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getLimitSumAmt());
    }

    // REQ-004-S02 触发条件满足、按主键更新执行时底层访问抛出技术异常（如数据源不可用）：
    // 异常原样向调用方传播，不捕获、不转译，不生成业务失败应答
    @Test
    public void testST104T07() {
        lenient().when(rbLimitSumInfoBcc.modifyByPrimaryKeySelective(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getCheckObjVal())
                        && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()))))
                .thenThrow(new RuntimeException(UPDATE_EXCEPTION_MESSAGE));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st104Pbc.execute(input()));

        assertEquals(UPDATE_EXCEPTION_MESSAGE, ex.getMessage());
    }
}
