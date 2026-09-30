package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.dcits.depsit.enums.AcctCcy;
import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.facade.bo.ST080InputBO;
import com.dcits.depsit.facade.bo.ST080OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST080 检查交易币种 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST080-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST080（inputs 绑定的 ST080.md）。
 * 数据异常路径（TC003 ~ TC005）按技术异常传播断言，异常类型不作具体限定（Spec 明确不覆盖）。
 */
@ExtendWith(MockitoExtension.class)
public class ST080PbcTest {

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @InjectMocks
    private ST080Pbc st080;

    /** 构造输入：账号 + 交易币种 */
    private ST080InputBO input(String baseAcctNo, Ccy tranCcy) {
        ST080InputBO input = new ST080InputBO();
        input.setBaseAcctNo(baseAcctNo);
        input.setTranCcy(tranCcy);
        return input;
    }

    /** 构造对公存款账户主表记录 */
    private RbBusAcctEO acct(Integer internalKey, String baseAcctNo, AcctCcy acctCcy) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setInternalKey(internalKey);
        eo.setBaseAcctNo(baseAcctNo);
        eo.setAcctCcy(acctCcy);
        return eo;
    }

    // 匹配器 lambda 需 null 防护（同 ST004PbcTest 约定）：注册桩时 argThat 以占位 null 真实调用 mock，
    // 不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 账号 等值条件核对请求并返回账户记录列表 */
    private void stubFindByEo(String baseAcctNo, List<RbBusAcctEO> records) {
        lenient().when(rbBusAcctBcc.findByEo(argThat(eo ->
                eo != null && baseAcctNo.equals(eo.getBaseAcctNo()))))
                .thenReturn(records);
    }

    // REQ-001-S01 + REQ-002-S01 账号唯一命中 1 条 ACCT_CCY=CNY 记录，交易币种 CNY 按币种代码比较相等：检查结果"通过"，acctCcy=CNY
    @Test
    public void testST080T01() {
        stubFindByEo("1002003004005006",
                Collections.singletonList(acct(1001, "1002003004005006", AcctCcy.CNY)));

        ST080OutputBO output = st080.execute(input("1002003004005006", Ccy.CNY));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(AcctCcy.CNY, output.getAcctCcy());
    }

    // REQ-002-S02 唯一命中 ACCT_CCY=CNY 记录，交易币种 USD 按币种代码比较不相等：业务失败 ER0051，acctCcy 照常赋值 CNY
    @Test
    public void testST080T02() {
        stubFindByEo("1002003004005006",
                Collections.singletonList(acct(1001, "1002003004005006", AcctCcy.CNY)));

        ST080OutputBO output = st080.execute(input("1002003004005006", Ccy.USD));

        assertFalse(output.isSucceed());
        assertEquals("ER0051", output.getErrorCode());
        assertEquals(AcctCcy.CNY, output.getAcctCcy());
    }

    // REQ-001-S02 账号查无任何记录：数据异常按技术异常处理，异常传播，不比较、不生成步骤结果、不设业务错误码
    @Test
    public void testST080T03() {
        stubFindByEo("1002003004004007", Collections.emptyList());

        assertThrows(Exception.class, () -> st080.execute(input("1002003004004007", Ccy.CNY)));
    }

    // REQ-001-S03 同账号命中 2 条记录（INTERNAL_KEY 不同）：数据异常按技术异常处理，异常传播，不比较、不生成步骤结果
    @Test
    public void testST080T04() {
        stubFindByEo("1002003004005008", Arrays.asList(
                acct(1001, "1002003004005008", AcctCcy.CNY),
                acct(1002, "1002003004005008", AcctCcy.CNY)));

        assertThrows(Exception.class, () -> st080.execute(input("1002003004005008", Ccy.CNY)));
    }

    // REQ-001-S04 记录唯一但账户币种为空：数据异常按技术异常处理，异常传播，不比较、不生成步骤结果
    @Test
    public void testST080T05() {
        stubFindByEo("1002003004005009",
                Collections.singletonList(acct(1001, "1002003004005009", null)));

        assertThrows(Exception.class, () -> st080.execute(input("1002003004005009", Ccy.CNY)));
    }
}
