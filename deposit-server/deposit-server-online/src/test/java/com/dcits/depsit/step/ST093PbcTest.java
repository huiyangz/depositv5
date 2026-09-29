package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.AcctNatureNo;
import com.dcits.depsit.facade.bo.ST093InputBO;
import com.dcits.depsit.facade.bo.ST093OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST093 检查存入账户账户属性 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST093-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST093（inputs 绑定的 ST093.md）。
 * 本步骤无业务失败场景，业务路径全部 succeed=true；检查"通过"由不输出账户属性表达。
 */
@ExtendWith(MockitoExtension.class)
public class ST093PbcTest {

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @InjectMocks
    private ST093Pbc st093;

    /** 构造输入：指定账号 */
    private ST093InputBO input(String baseAcctNo) {
        ST093InputBO input = new ST093InputBO();
        input.setBaseAcctNo(baseAcctNo);
        return input;
    }

    /** 构造账户信息记录：账号 + 账户属性 */
    private RbBusAcctEO acctRecord(String baseAcctNo, AcctNatureNo acctNatureNo) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setAcctNatureNo(acctNatureNo);
        return eo;
    }

    // 匹配器 lambda 需 null 防护（沿用既有 ST004PbcTest 惯例）：argThat 以占位 null 真实调用 mock，
    // 不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 baseAcctNo 等值条件核对请求（REQ-001 字段映射）并返回账户信息记录列表 */
    private void stubAcct(RbBusAcctEO record) {
        final String acctNo = record.getBaseAcctNo();
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && acctNo.equals(eo.getBaseAcctNo()))))
                .thenReturn(Collections.singletonList(record));
    }

    // REQ-002-S01 账户属性为"验资户"（VALUE_17，库存值 '17'）：输出账户属性并正常返回
    @Test
    public void testST093T01() {
        stubAcct(acctRecord("ACCT2002", AcctNatureNo.VALUE_17));

        ST093OutputBO output = st093.execute(input("ACCT2002"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(AcctNatureNo.VALUE_17, output.getAcctNatureNo());
    }

    // REQ-001-S01 + REQ-002-S02 按 'ACCT1001' 查得账户属性为"临时存款账户"（VALUE_11003，库存值 '11003' 按枚举 value 映射）：输出账户属性并正常返回
    @Test
    public void testST093T02() {
        stubAcct(acctRecord("ACCT1001", AcctNatureNo.VALUE_11003));

        ST093OutputBO output = st093.execute(input("ACCT1001"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(AcctNatureNo.VALUE_11003, output.getAcctNatureNo());
    }

    // REQ-003-S01 账户属性为其他取值（VALUE_11001 基本存款账户，库存值 '11001'）：检查结果"通过"，不输出账户属性
    @Test
    public void testST093T03() {
        stubAcct(acctRecord("ACCT3003", AcctNatureNo.VALUE_11001));

        ST093OutputBO output = st093.execute(input("ACCT3003"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getAcctNatureNo());
    }

    // REQ-004-S01 查询【账户信息】底层技术异常（数据源不可用）：异常原样向上传播，不捕获、不转换、不生成业务失败应答
    @Test
    public void testST093T04() {
        RuntimeException ex = new RuntimeException("数据源不可用");
        when(rbBusAcctBcc.findByEo(any(RbBusAcctEO.class))).thenThrow(ex);

        RuntimeException caught = assertThrows(RuntimeException.class, () -> st093.execute(input("ACCT1001")));

        assertSame(ex, caught);
    }
}
