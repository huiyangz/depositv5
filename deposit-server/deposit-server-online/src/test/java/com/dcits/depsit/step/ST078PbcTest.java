package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.SettleAcctClass;
import com.dcits.depsit.facade.bo.SettleAcctDTO;
import com.dcits.depsit.facade.bo.ST078InputBO;
import com.dcits.depsit.facade.bo.ST078OutputBO;

/**
 * ST078 检查利息资本化标志 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST078-TC001 ~ TC007），
 * 预期结果来自正式 Spec ST078（inputs 绑定的 ST078.md）。
 * 纯内存判定步骤，无 BCC/规则/外部调用依赖，全部用例无桩；errorMessage 需求未定义，
 * 失败用例不做等值断言。
 */
@ExtendWith(MockitoExtension.class)
public class ST078PbcTest {

    @InjectMocks
    private ST078Pbc st078;

    /** 构造输入：利息资本化标志 + 结算账户数组 */
    private ST078InputBO input(String intCapFlag, List<SettleAcctDTO> settleAcctList) {
        ST078InputBO input = new ST078InputBO();
        input.setIntCapFlag(intCapFlag);
        input.setSettleAcctList(settleAcctList);
        return input;
    }

    /** 构造结算账户元素：仅设置{结算账户类型}，其余字段需求未要求，不设置 */
    private SettleAcctDTO dto(SettleAcctClass settleAcctClass) {
        SettleAcctDTO dto = new SettleAcctDTO();
        dto.setSettleAcctClass(settleAcctClass);
        return dto;
    }

    /** 通过路径公共断言：succeed=true、错误码与错误信息为 null */
    private void assertPassed(ST078OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
    }

    // REQ-001-S01 标志为"否"且数组（PAY、INT）含利息入账账户：步骤1成立继续执行步骤2，存在性满足，检查结果"通过"
    @Test
    public void testST078T01() {
        ST078OutputBO output = st078.execute(input("N", Arrays.asList(
                dto(SettleAcctClass.PAY), dto(SettleAcctClass.INT))));

        assertPassed(output);
    }

    // REQ-001-S02 标志非"否"（示例"Y"）且数组不含"INT"账户：步骤1短路直接通过，不执行步骤2、不产生 ER0034
    @Test
    public void testST078T02() {
        ST078OutputBO output = st078.execute(input("Y", Arrays.asList(
                dto(SettleAcctClass.PAY))));

        assertPassed(output);
    }

    // REQ-002-S01 标志为"否"，数组 3 个元素（PAY、REC、INT）含 1 个利息入账账户：存在性满足，检查结果"通过"
    @Test
    public void testST078T03() {
        ST078OutputBO output = st078.execute(input("N", Arrays.asList(
                dto(SettleAcctClass.PAY), dto(SettleAcctClass.REC), dto(SettleAcctClass.INT))));

        assertPassed(output);
    }

    // REQ-002-S02 标志为"否"，数组 3 个元素含 2 个"INT"、1 个"PAY"：存在性即满足，满足数量不影响结果
    @Test
    public void testST078T04() {
        ST078OutputBO output = st078.execute(input("N", Arrays.asList(
                dto(SettleAcctClass.INT), dto(SettleAcctClass.INT), dto(SettleAcctClass.PAY))));

        assertPassed(output);
    }

    // REQ-002-S03 标志为"否"，数组非空但全部元素类型非"INT"（PAY、REC）：存在性不满足，返回错误码 ER0034
    @Test
    public void testST078T05() {
        ST078OutputBO output = st078.execute(input("N", Arrays.asList(
                dto(SettleAcctClass.PAY), dto(SettleAcctClass.REC))));

        assertFalse(output.isSucceed());
        assertEquals("ER0034", output.getErrorCode());
    }

    // REQ-002-S04 标志为"否"，结算账户数组为空（0 个元素）：空数组不存在"INT"账户，返回错误码 ER0034
    @Test
    public void testST078T06() {
        ST078OutputBO output = st078.execute(input("N", Collections.emptyList()));

        assertFalse(output.isSucceed());
        assertEquals("ER0034", output.getErrorCode());
    }

    // REQ-003-S01 标志为"否"且数组仅 1 个元素即"INT"账户：最小满足数组下的通过结果三字段
    @Test
    public void testST078T07() {
        ST078OutputBO output = st078.execute(input("N", Arrays.asList(
                dto(SettleAcctClass.INT))));

        assertPassed(output);
    }
}
