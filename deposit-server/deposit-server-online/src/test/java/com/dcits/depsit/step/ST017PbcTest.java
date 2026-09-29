package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.facade.bo.ST017InputBO;
import com.dcits.depsit.facade.bo.ST017OutputBO;

/**
 * ST017 设置贷记交易的借贷标志 单元测试
 *
 * <p>依据正式 Spec ST017 与测试用例 ST017-TC001～TC002：执行时无条件将借贷标志
 * 赋值为 "C-贷方"（CrDrInd.C），成功返回且无副作用。本步骤无 BCC、规则、
 * 组件接口或跨组件客户端依赖（Spec「依赖」为无），不存在可设桩协作者，
 * 无业务失败场景；被测步骤真实执行。</p>
 */
@ExtendWith(MockitoExtension.class)
class ST017PbcTest {

    @InjectMocks
    private ST017Pbc st017Pbc;

    // REQ-001-S01 执行赋值：步骤被调用后无条件将借贷标志赋为 "C-贷方"，成功返回且无副作用
    @Test
    void testST017T01() {
        ST017OutputBO output = st017Pbc.execute(new ST017InputBO());

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertSame(CrDrInd.C, output.getCrDrInd());
    }

    // REQ-002-S01 输出取值属于代码[借贷标志]取值域：取值为 "C-贷方"（value "C"），不出现 "D-借方" 或取值域外的值
    @Test
    void testST017T02() {
        ST017OutputBO output = st017Pbc.execute(new ST017InputBO());

        assertSame(CrDrInd.C, output.getCrDrInd());
        assertEquals("C", output.getCrDrInd().getValue());
        // 不出现 "D-借方"
        assertNotSame(CrDrInd.D, output.getCrDrInd());
        assertTrue(output.isSucceed());
    }
}
