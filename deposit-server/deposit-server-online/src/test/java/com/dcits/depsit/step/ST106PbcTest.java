package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST106InputBO;
import com.dcits.depsit.facade.bo.ST106OutputBO;
import com.dcits.depsit.facade.components.IRbRestraintTypeBcc;
import com.dcits.depsit.facade.eo.RbRestraintTypeEO;

/**
 * ST106 检查限制类型 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST106-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST106（inputs 绑定的 ST106.md）。
 * 本步骤无业务失败场景，"不通过"是业务检查结果不是失败，全部业务用例 succeed=true。
 */
@ExtendWith(MockitoExtension.class)
public class ST106PbcTest {

    @Mock
    private IRbRestraintTypeBcc rbRestraintTypeBcc;

    @InjectMocks
    private ST106Pbc st106;

    /** 构造公共输入：账户限制类型 */
    private ST106InputBO input(RestraintType restraintType) {
        ST106InputBO input = new ST106InputBO();
        input.setRestraintType(restraintType);
        return input;
    }

    /** 构造存款限制类型表记录 */
    private RbRestraintTypeEO restraintType(RestraintType restraintType, Status status) {
        RbRestraintTypeEO eo = new RbRestraintTypeEO();
        eo.setRestraintType(restraintType);
        eo.setStatus(status);
        return eo;
    }

    // REQ-001-S01→REQ-002-S02→REQ-003-S01 记录存在且状态"A-有效"（VALUE_13 挂失止付）：全路径通过，checkResult="通过"
    @Test
    public void testST106T01() {
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.A));

        ST106OutputBO output = st106.execute(input(RestraintType.VALUE_13));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("通过", output.getCheckResult());
    }

    // REQ-001-S02→REQ-002-S01 记录不存在（主键查询无匹配返回 null）：短路返回"不通过"，步骤成功结束
    @Test
    public void testST106T02() {
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(null);

        ST106OutputBO output = st106.execute(input(RestraintType.VALUE_13));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("不通过", output.getCheckResult());
    }

    // REQ-002-S02→REQ-003-S02 记录存在但状态"F-无效"（非"A-有效"）：继续状态检查后返回"不通过"
    @Test
    public void testST106T03() {
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenReturn(restraintType(RestraintType.VALUE_13, Status.F));

        ST106OutputBO output = st106.execute(input(RestraintType.VALUE_13));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("不通过", output.getCheckResult());
    }

    // REQ-001/REQ-003-S02 另一类型 VALUE_70（黑名单控制专用-停止所有业务）且状态"C-非活动状态"：查询条件按输入传递，非"A"取值返回"不通过"
    @Test
    public void testST106T04() {
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_70))
                .thenReturn(restraintType(RestraintType.VALUE_70, Status.C));

        ST106OutputBO output = st106.execute(input(RestraintType.VALUE_70));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals("不通过", output.getCheckResult());
    }

    // REQ-004-S02 第1步查询技术异常：不捕获、不转译为业务失败结果或业务错误码，异常按原样向调用方传播
    @Test
    public void testST106T05() {
        lenient().when(rbRestraintTypeBcc.findByRestraintType(RestraintType.VALUE_13))
                .thenThrow(new RuntimeException("模拟存款限制类型表数据访问异常"));

        ST106InputBO input = input(RestraintType.VALUE_13);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st106.execute(input));
        assertEquals("模拟存款限制类型表数据访问异常", ex.getMessage());
    }
}
