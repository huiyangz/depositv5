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

import com.dcits.depsit.enums.Ccy;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST023InputBO;
import com.dcits.depsit.facade.bo.ST023OutputBO;
import com.dcits.depsit.facade.components.IFmBranchCcyBcc;
import com.dcits.depsit.facade.eo.FmBranchCcyEO;

/**
 * ST023 检查机构币种交易权限 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST023-TC001 ~ TC004），
 * 预期结果来自正式 Spec ST023（inputs 绑定的 ST023.md）。
 * 失败路径 errorMessage 内容需求未定义，Spec 明示不作断言依据，失败用例不断言该字段。
 */
@ExtendWith(MockitoExtension.class)
public class ST023PbcTest {

    @Mock
    private IFmBranchCcyBcc fmBranchCcyBcc;

    @InjectMocks
    private ST023Pbc st023;

    /** 构造输入：交易机构号 + 交易币种 */
    private ST023InputBO input(TranBranch tranBranch, Ccy tranCcy) {
        ST023InputBO input = new ST023InputBO();
        input.setTranBranch(tranBranch);
        input.setTranCcy(tranCcy);
        return input;
    }

    /** 构造机构币种记录：归属机构号 + 币种 */
    private FmBranchCcyEO record(TranBranch branch, Ccy ccy) {
        FmBranchCcyEO eo = new FmBranchCcyEO();
        eo.setBranch(branch);
        eo.setCcy(ccy);
        return eo;
    }

    // 匹配器 lambda 需 null 防护：注册同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 步骤1 桩：按 归属机构号（唯一查询条件，ccy 不设值）核对请求并返回机构币种记录列表 */
    private void stubBranchCcyList(TranBranch branch, List<FmBranchCcyEO> records) {
        lenient().when(fmBranchCcyBcc.findByEo(argThat(eo ->
                eo != null && eo.getBranch() == branch && eo.getCcy() == null)))
                .thenReturn(records);
    }

    // REQ-001-S01 + REQ-002-S01 机构 351001 有 CNY、USD 两条记录（他机构 351888 的 EUR 记录不混入）；
    // 交易币种 USD 在列表内且命中非首条记录：检查通过，输出 ccy=USD（等于输入 tranCcy）
    @Test
    public void testST023T01() {
        stubBranchCcyList(TranBranch.VALUE_351001, Arrays.asList(
                record(TranBranch.VALUE_351001, Ccy.CNY),
                record(TranBranch.VALUE_351001, Ccy.USD)));

        ST023OutputBO output = st023.execute(input(TranBranch.VALUE_351001, Ccy.USD));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(Ccy.USD, output.getCcy());
    }

    // REQ-002-S01 覆盖列表仅 1 条记录边界：机构 351002 仅存在 CNY 一条记录，交易币种 CNY 命中唯一记录：检查通过
    @Test
    public void testST023T02() {
        stubBranchCcyList(TranBranch.VALUE_351002, Collections.singletonList(
                record(TranBranch.VALUE_351002, Ccy.CNY)));

        ST023OutputBO output = st023.execute(input(TranBranch.VALUE_351002, Ccy.CNY));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(Ccy.CNY, output.getCcy());
    }

    // REQ-001-S01 + REQ-002-S02 机构 351001 列表非空（CNY、USD），交易币种 EUR 不在列表范围内：
    // 否则分支返回错误码 ER0047，输出 ccy 为空；errorMessage 需求未定义，不作断言
    @Test
    public void testST023T03() {
        stubBranchCcyList(TranBranch.VALUE_351001, Arrays.asList(
                record(TranBranch.VALUE_351001, Ccy.CNY),
                record(TranBranch.VALUE_351001, Ccy.USD)));

        ST023OutputBO output = st023.execute(input(TranBranch.VALUE_351001, Ccy.EUR));

        assertFalse(output.isSucceed());
        assertEquals("ER0047", output.getErrorCode());
        assertNull(output.getCcy());
    }

    // REQ-001-S02 + REQ-002-S03 机构 351155 不存在任何币种记录，查询得到空列表，
    // 交易币种 CNY 不在空列表范围内：否则分支返回错误码 ER0047，输出 ccy 为空；errorMessage 不作断言
    @Test
    public void testST023T04() {
        stubBranchCcyList(TranBranch.VALUE_351155, Collections.emptyList());

        ST023OutputBO output = st023.execute(input(TranBranch.VALUE_351155, Ccy.CNY));

        assertFalse(output.isSucceed());
        assertEquals("ER0047", output.getErrorCode());
        assertNull(output.getCcy());
    }
}
