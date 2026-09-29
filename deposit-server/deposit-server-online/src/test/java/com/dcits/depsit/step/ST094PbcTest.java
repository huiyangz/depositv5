package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.AllDepInd;
import com.dcits.depsit.facade.bo.ST094InputBO;
import com.dcits.depsit.facade.bo.ST094OutputBO;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.eo.RbBusAcctEO;

/**
 * ST094 检查存入账户通存标志 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST094-TC001 ~ TC006），
 * 预期结果来自正式 Spec ST094（fileInputs.spec 绑定的 ST094.md）。
 * 失败路径 errorMessage 内容需求未定义，不作断言依据。
 */
@ExtendWith(MockitoExtension.class)
public class ST094PbcTest {

    @Mock
    private IRbBusAcctBcc rbBusAcctBcc;

    @InjectMocks
    private ST094Pbc st094;

    /** 构造输入：账号 */
    private ST094InputBO input(String baseAcctNo) {
        ST094InputBO input = new ST094InputBO();
        input.setBaseAcctNo(baseAcctNo);
        return input;
    }

    /** 构造账户信息记录：通存标识可空（对应表字段 ALL_DEP_IND 允许为空） */
    private RbBusAcctEO acct(String baseAcctNo, AllDepInd allDepInd) {
        RbBusAcctEO eo = new RbBusAcctEO();
        eo.setBaseAcctNo(baseAcctNo);
        eo.setAllDepInd(allDepInd);
        return eo;
    }

    /** 步骤1 桩：findByEo 经 thenAnswer 将入参查询账号记录到返回数组后返回给定记录列表 */
    private String[] stubFindByEo(List<RbBusAcctEO> records) {
        String[] queriedAcctNo = new String[1];
        lenient().when(rbBusAcctBcc.findByEo(any(RbBusAcctEO.class))).thenAnswer(invocation -> {
            RbBusAcctEO condition = invocation.getArgument(0);
            queriedAcctNo[0] = condition.getBaseAcctNo();
            return records;
        });
        return queriedAcctNo;
    }

    // ST094-TC001 账号存在且通存标识为 N001-允许全行存入：步骤1 取得并赋值输出、查询条件为输入账号，步骤2 判定通过，正常结束继续执行
    @Test
    public void testST094T01() {
        String[] queriedAcctNo = stubFindByEo(Collections.singletonList(
                acct("6100230010001", AllDepInd.N001)));

        ST094OutputBO output = st094.execute(input("6100230010001"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(AllDepInd.N001, output.getAllDepInd());
        assertEquals("6100230010001", queriedAcctNo[0]);
    }

    // ST094-TC002 账号存在且通存标识为 N002-允许分行存入：判定不等于 N001-允许全行存入，返回 ER0055，输出保持取得值
    @Test
    public void testST094T02() {
        stubFindByEo(Collections.singletonList(acct("6100230010003", AllDepInd.N002)));

        ST094OutputBO output = st094.execute(input("6100230010003"));

        assertFalse(output.isSucceed());
        assertEquals("ER0055", output.getErrorCode());
        assertEquals(AllDepInd.N002, output.getAllDepInd());
    }

    // ST094-TC003 账号存在且通存标识为 N003-允许同城跨行存入：同"不等于"分支，返回 ER0055
    @Test
    public void testST094T03() {
        stubFindByEo(Collections.singletonList(acct("6100230010004", AllDepInd.N003)));

        ST094OutputBO output = st094.execute(input("6100230010004"));

        assertFalse(output.isSucceed());
        assertEquals("ER0055", output.getErrorCode());
        assertEquals(AllDepInd.N003, output.getAllDepInd());
    }

    // ST094-TC004 账号存在且通存标识为 N004-不允许跨行存入：同"不等于"分支，返回 ER0055
    @Test
    public void testST094T04() {
        stubFindByEo(Collections.singletonList(acct("6100230010005", AllDepInd.N004)));

        ST094OutputBO output = st094.execute(input("6100230010005"));

        assertFalse(output.isSucceed());
        assertEquals("ER0055", output.getErrorCode());
        assertEquals(AllDepInd.N004, output.getAllDepInd());
    }

    // ST094-TC005 账号无匹配记录：步骤1 未取得、输出 allDepInd 为 null（不填充默认值）、不构成失败，步骤2 null 判定返回 ER0055
    @Test
    public void testST094T05() {
        stubFindByEo(new ArrayList<RbBusAcctEO>());

        ST094OutputBO output = st094.execute(input("6100230010002"));

        assertFalse(output.isSucceed());
        assertEquals("ER0055", output.getErrorCode());
        assertNull(output.getAllDepInd());
    }

    // ST094-TC006 账号存在但记录 ALL_DEP_IND 为空（null）：步骤1 未取得非空通存标识，步骤2 null 判定返回 ER0055
    @Test
    public void testST094T06() {
        stubFindByEo(Collections.singletonList(acct("6100230010001", null)));

        ST094OutputBO output = st094.execute(input("6100230010001"));

        assertFalse(output.isSucceed());
        assertEquals("ER0055", output.getErrorCode());
        assertNull(output.getAllDepInd());
    }
}
