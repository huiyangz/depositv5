package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST007InputBO;
import com.dcits.depsit.facade.bo.ST007OutputBO;
import com.dcits.depsit.facade.components.IRbBusRestraintsBcc;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbBusRestraintsEO;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST007 检查质押类限制 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST007-TC001 ~ TC007），
 * 预期结果来自正式 Spec ST007（inputs 绑定的 ST007.md）。
 * 本步骤无业务失败场景：空结果为正常返回（succeed=true）；
 * 失败仅由技术异常传播表达（TC006/TC007 断言传播契约，非业务失败结果）。
 */
@ExtendWith(MockitoExtension.class)
public class ST007PbcTest {

    /** 示例账号（Spec 场景示例数据） */
    private static final String BASE_ACCT_NO = "1002003004005006";

    @Mock
    private IRbBusRestraintsBcc rbBusRestraintsBcc;

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST007Pbc st007;

    /** 构造公共输入：账号 */
    private ST007InputBO input() {
        ST007InputBO input = new ST007InputBO();
        input.setBaseAcctNo(BASE_ACCT_NO);
        return input;
    }

    /** 构造账户限制记录：限制状态均为"生效"（RestraintsStatus.A） */
    private RbBusRestraintsEO restraint(String resSeqNo, RestraintType restraintType) {
        RbBusRestraintsEO eo = new RbBusRestraintsEO();
        eo.setBaseAcctNo(BASE_ACCT_NO);
        eo.setResSeqNo(resSeqNo);
        eo.setRestraintType(restraintType);
        eo.setRestraintsStatus(RestraintsStatus.A);
        return eo;
    }

    /** 构造限制类型生效记录：状态均为 Status.A，质押标志为示例测试数据（取值含义需求未定义） */
    private RbRestraintTypeEO restraintType(RestraintType restraintType, String pledgedFlag) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(Status.A);
        eo.setPledgedFlag(pledgedFlag);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 账号+限制状态=RestraintsStatus.A 组合条件核对请求并返回限制记录列表 */
    private void stubRestraints(List<RbBusRestraintsEO> records) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()) && eo.getRestraintsStatus() == RestraintsStatus.A)))
                .thenReturn(records);
    }

    /** 步骤1 桩（技术异常）：按同组合条件核对请求并抛出指定异常 */
    private void stubRestraintsFailure(RuntimeException failure) {
        lenient().when(rbBusRestraintsBcc.findByEo(argThat(eo ->
                eo != null && BASE_ACCT_NO.equals(eo.getBaseAcctNo()) && eo.getRestraintsStatus() == RestraintsStatus.A)))
                .thenThrow(failure);
    }

    /** 步骤2 桩：按 账户限制类型+状态=Status.A 组合条件核对请求并返回类型记录列表 */
    private void stubRestraintType(RestraintType restraintType, List<RbRestraintTypeEO> records) {
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && eo.getRestraintType() == restraintType && eo.getStatus() == Status.A)))
                .thenReturn(records);
    }

    /** 步骤2 桩（技术异常）：按同组合条件核对请求并抛出指定异常 */
    private void stubRestraintTypeFailure(RestraintType restraintType, RuntimeException failure) {
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo ->
                eo != null && eo.getRestraintType() == restraintType && eo.getStatus() == Status.A)))
                .thenThrow(failure);
    }

    /** 空结果路径公共断言：succeed=true、错误字段 null、5 个输出字段均为 null（无生效记录不填充默认值） */
    private void assertEmptyOutput(ST007OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getResSeqNo());
        assertNull(output.getRestraintType());
        assertNull(output.getRestraintsStatus());
        assertNull(output.getPledgedFlag());
        assertNull(output.getStatus());
    }

    // REQ-001-S01 账号无任何限制记录：空列表无记录可遍历、不执行步骤2，正常返回且 5 个输出字段均为空
    @Test
    public void testST007T01() {
        stubRestraints(Collections.emptyList());

        ST007OutputBO output = st007.execute(input());

        assertEmptyOutput(output);
    }

    // REQ-001-S02 限制记录存在但限制状态为"E-已终止"（业务背景），被步骤1 组合查询条件排除（状态过滤在查询请求内），等同无记录
    @Test
    public void testST007T02() {
        stubRestraints(Collections.emptyList());

        ST007OutputBO output = st007.execute(input());

        assertEmptyOutput(output);
    }

    // REQ-002-S01 + REQ-003-S01 单条生效记录且类型存在生效记录：全 5 个输出字段赋值返回，质押标志原样透传
    @Test
    public void testST007T03() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_12)));
        stubRestraintType(RestraintType.VALUE_12, Collections.singletonList(
                restraintType(RestraintType.VALUE_12, "1")));

        ST007OutputBO output = st007.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("RES20260930000001", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_12, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals("1", output.getPledgedFlag());
        assertEquals(Status.A, output.getStatus());
    }

    // REQ-002-S02 + REQ-003-S02 单条生效记录但类型无生效记录：记录来源三字段有值，pledgedFlag/status 为空，不产生业务失败
    @Test
    public void testST007T04() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000002", RestraintType.VALUE_66)));
        stubRestraintType(RestraintType.VALUE_66, Collections.emptyList());

        ST007OutputBO output = st007.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("RES20260930000002", output.getResSeqNo());
        assertEquals(RestraintType.VALUE_66, output.getRestraintType());
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertNull(output.getPledgedFlag());
        assertNull(output.getStatus());
    }

    // REQ-001-S03 + REQ-003-S03 两条生效记录（12/66）逐条执行步骤2且无短路：输出取自同一条记录及其生效类型记录，选取不依赖处理顺序
    @Test
    public void testST007T05() {
        stubRestraints(Arrays.asList(
                restraint("RES20260930000001", RestraintType.VALUE_12),
                restraint("RES20260930000002", RestraintType.VALUE_66)));
        // 步骤2 桩：核对请求状态恒为 Status.A，记录收到的账户限制类型并按请求类型返回对应生效记录
        // （在桩内记录收到的请求字段并断言业务数据，验证逐条处理无短路，非调用次数验证）
        Set<RestraintType> queriedTypes = new HashSet<>();
        lenient().when(rbRestraintTypeBcc.findByEo(argThat(eo -> eo != null && eo.getStatus() == Status.A)))
                .thenAnswer(invocation -> {
                    RbRestraintTypeEO request = invocation.getArgument(0);
                    queriedTypes.add(request.getRestraintType());
                    String pledgedFlag = request.getRestraintType() == RestraintType.VALUE_12 ? "1" : "0";
                    return Collections.singletonList(restraintType(request.getRestraintType(), pledgedFlag));
                });

        ST007OutputBO output = st007.execute(input());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        // 组合断言：5 字段全取甲组（12/"1"）或全取乙组（66/"0"），组内字段自洽，不依赖处理顺序
        boolean firstGroup = "RES20260930000001".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_12
                && "1".equals(output.getPledgedFlag());
        boolean secondGroup = "RES20260930000002".equals(output.getResSeqNo())
                && output.getRestraintType() == RestraintType.VALUE_66
                && "0".equals(output.getPledgedFlag());
        assertTrue(firstGroup || secondGroup);
        assertEquals(RestraintsStatus.A, output.getRestraintsStatus());
        assertEquals(Status.A, output.getStatus());
        // 两类账户限制类型的步骤2 查询均出现（逐条处理无短路）
        assertTrue(queriedTypes.contains(RestraintType.VALUE_12));
        assertTrue(queriedTypes.contains(RestraintType.VALUE_66));
    }

    // REQ-004-S01 步骤1 查询底层抛技术异常（如数据源不可用）：同一实例原样向上传播，不捕获、不转换、不组装输出
    @Test
    public void testST007T06() {
        RuntimeException dataSourceFailure = new RuntimeException("数据源不可用");
        stubRestraintsFailure(dataSourceFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> st007.execute(input()));

        assertSame(dataSourceFailure, thrown);
    }

    // REQ-004-S01 步骤2 查询底层抛技术异常（GIVEN"或【限制类型表】"分支）：同样同一实例原样向上传播
    @Test
    public void testST007T07() {
        stubRestraints(Collections.singletonList(
                restraint("RES20260930000001", RestraintType.VALUE_12)));
        RuntimeException dataSourceFailure = new RuntimeException("数据源不可用");
        stubRestraintTypeFailure(RestraintType.VALUE_12, dataSourceFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> st007.execute(input()));

        assertSame(dataSourceFailure, thrown);
    }
}
