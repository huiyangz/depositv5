package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.facade.bo.ST032InputBO;
import com.dcits.depsit.facade.bo.ST032OutputBO;

/**
 * ST032 设置借记交易的借贷标志 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST032-TC001 ~ TC002），
 * 预期结果来自正式 Spec ST032（fileInputs.spec 绑定的 ST032.md）。
 * 本步骤为无条件常量赋值，无 BCC / EO / 规则 / 跨组件客户端 / 组件内步骤
 * 调用依赖，不创建 Mockito Mock，直接实例化被测类真实调用 execute。
 */
public class ST032PbcTest {

    private final ST032Pbc st032Pbc = new ST032Pbc();

    /** ST032-TC001（REQ-001-S01）正常路径：执行步骤后输出借贷标志被无条件赋值为借方（CrDrInd.D，value "D"），仅此一个输出字段 */
    @Test
    public void testST032T01() {
        ST032InputBO input = new ST032InputBO();

        ST032OutputBO output = st032Pbc.execute(input);

        assertEquals(CrDrInd.D, output.getCrDrInd());
        assertEquals("D", output.getCrDrInd().getValue());
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    /** ST032-TC002（REQ-002-S01）正常路径：步骤执行成功且无业务错误（errorCode、errorMessage 均为 null，无业务失败场景） */
    @Test
    public void testST032T02() {
        ST032InputBO input = new ST032InputBO();

        ST032OutputBO output = st032Pbc.execute(input);

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(CrDrInd.D, output.getCrDrInd());
        assertEquals("D", output.getCrDrInd().getValue());
    }
}
