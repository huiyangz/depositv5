package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.facade.bo.ST099InputBO;
import com.dcits.depsit.facade.bo.ST099OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST099 处理限额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST099-TC001 ~ TC006），
 * 预期结果来自正式 Spec ST099（fileInputs.spec 绑定的 ST099.md）。
 * 本步骤无业务失败场景，dealFlow=null 是正常输出不是失败；
 * 技术异常按 REQ-003 原样向调用方传播。
 * 每例仅注册一个精确参数桩且必被调用，键字段映射由精确参数核对（传错键时桩不命中、断言失败）。
 */
@ExtendWith(MockitoExtension.class)
public class ST099PbcTest {

    @Mock
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @InjectMocks
    private ST099Pbc st099;

    /** 构造输入：限额机构编码 + 限额场景编码（RB_LIMIT_CTRL_CONF 两主键字段） */
    private ST099InputBO input(String limitBranchId, String limitSceneNo) {
        ST099InputBO input = new ST099InputBO();
        input.setLimitBranchId(limitBranchId);
        input.setLimitSceneNo(limitSceneNo);
        return input;
    }

    /** 构造命中配置记录：本步骤仅消费处理方式字段，其余字段不设值 */
    private RbLimitCtrlConfEO conf(DealFlow dealFlow) {
        RbLimitCtrlConfEO eo = new RbLimitCtrlConfEO();
        eo.setDealFlow(dealFlow);
        return eo;
    }

    // REQ-001-S01、REQ-002-S01 命中配置记录（键 "0301"/"ACCT_OPEN"）且处理方式为拒绝：succeed=true，dealFlow=DealFlow.B
    @Test
    public void testST099T01() {
        when(rbLimitCtrlConfBcc.findByPrimaryKey("0301", "ACCT_OPEN")).thenReturn(conf(DealFlow.B));

        ST099OutputBO output = st099.execute(input("0301", "ACCT_OPEN"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(DealFlow.B, output.getDealFlow());
    }

    // REQ-001-S01、REQ-002-S02 命中配置记录（键 "0302"/"CASH_WITHDRAW"，与 T01 键值可区分）且处理方式为提醒：dealFlow=DealFlow.D
    @Test
    public void testST099T02() {
        when(rbLimitCtrlConfBcc.findByPrimaryKey("0302", "CASH_WITHDRAW")).thenReturn(conf(DealFlow.D));

        ST099OutputBO output = st099.execute(input("0302", "CASH_WITHDRAW"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(DealFlow.D, output.getDealFlow());
    }

    // REQ-001-S01、REQ-002-S03 命中配置记录（键 "0303"/"TRANSFER_OUT"）且处理方式为授权：dealFlow=DealFlow.A
    @Test
    public void testST099T03() {
        when(rbLimitCtrlConfBcc.findByPrimaryKey("0303", "TRANSFER_OUT")).thenReturn(conf(DealFlow.A));

        ST099OutputBO output = st099.execute(input("0303", "TRANSFER_OUT"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(DealFlow.A, output.getDealFlow());
    }

    // REQ-001-S02 未命中配置记录（键 "9999"/"ACCT_OPEN" 无匹配记录）：dealFlow=null 且步骤正常结束、不产生业务失败
    @Test
    public void testST099T04() {
        when(rbLimitCtrlConfBcc.findByPrimaryKey("9999", "ACCT_OPEN")).thenReturn(null);

        ST099OutputBO output = st099.execute(input("9999", "ACCT_OPEN"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getDealFlow());
    }

    // REQ-002-S04 命中记录但处理方式为空：不命中"拒绝/提醒/授权"任何返回分支，dealFlow=null（"非三种取值"分支按用例依赖说明并入本例）
    @Test
    public void testST099T05() {
        when(rbLimitCtrlConfBcc.findByPrimaryKey("0301", "ACCT_OPEN")).thenReturn(conf(null));

        ST099OutputBO output = st099.execute(input("0301", "ACCT_OPEN"));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getDealFlow());
    }

    // REQ-003-S01 查询发生技术异常（数据库连接失败）：异常原样向调用方传播，不捕获、不吞没、不转换为业务失败结果
    @Test
    public void testST099T06() {
        when(rbLimitCtrlConfBcc.findByPrimaryKey("0301", "ACCT_OPEN"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        ST099InputBO input = input("0301", "ACCT_OPEN");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st099.execute(input));
        assertEquals("数据库连接失败", ex.getMessage());
    }
}
