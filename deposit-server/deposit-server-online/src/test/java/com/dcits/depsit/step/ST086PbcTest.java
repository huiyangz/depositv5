package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.ClientType;
import com.dcits.depsit.facade.bo.ST086InputBO;
import com.dcits.depsit.facade.bo.ST086OutputBO;
import com.dcits.depsit.facade.components.IFmClientCopyBcc;
import com.dcits.depsit.facade.eo.FmClientCopyEO;

/**
 * ST086 检查客户类型 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST086-TC001 ~ TC005），
 * 预期结果来自正式 Spec ST086（inputs 绑定的 ST086.md）。
 * 失败路径 errorMessage 内容需求未约束，不作断言。
 */
@ExtendWith(MockitoExtension.class)
public class ST086PbcTest {

    @Mock
    private IFmClientCopyBcc fmClientCopyBcc;

    @InjectMocks
    private ST086Pbc st086;

    /** 构造输入：客户号 */
    private ST086InputBO input(String clientNo) {
        ST086InputBO input = new ST086InputBO();
        input.setClientNo(clientNo);
        return input;
    }

    /** 构造客户副本记录：仅设主键客户号与客户类型（本路径仅用这两个字段） */
    private FmClientCopyEO clientCopy(String clientNo, ClientType clientType) {
        FmClientCopyEO eo = new FmClientCopyEO();
        eo.setClientNo(clientNo);
        eo.setClientType(clientType);
        return eo;
    }

    /** 步骤1 桩：以精确客户号设桩并返回记录，若实现未将输入客户号原样作为主键查询条件则桩不命中 */
    private void stubClientCopy(FmClientCopyEO eo, String clientNo) {
        lenient().when(fmClientCopyBcc.findByPrimaryKey(clientNo)).thenReturn(eo);
    }

    /** 失败路径公共断言：succeed=false、errorCode="ER0042"，clientType 保持步骤1 取得的值 */
    private void assertNotCorporate(ST086OutputBO output, ClientType expectedClientType) {
        assertFalse(output.isSucceed());
        assertEquals("ER0042", output.getErrorCode());
        assertEquals(expectedClientType, output.getClientType());
    }

    // REQ-001-S01 + REQ-002-S01 客户号命中记录且客户类型为"公司"（VALUE_200）：检查通过，输出客户类型
    @Test
    public void testST086T01() {
        String clientNo = "C20260930000001";
        stubClientCopy(clientCopy(clientNo, ClientType.VALUE_200), clientNo);

        ST086OutputBO output = st086.execute(input(clientNo));

        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(ClientType.VALUE_200, output.getClientType());
    }

    // REQ-002-S02 客户类型为"个人"（VALUE_100，非"公司"）：返回 ER0042，输出 clientType 保持步骤1 取得值
    @Test
    public void testST086T02() {
        String clientNo = "C20260930000002";
        stubClientCopy(clientCopy(clientNo, ClientType.VALUE_100), clientNo);

        ST086OutputBO output = st086.execute(input(clientNo));

        assertNotCorporate(output, ClientType.VALUE_100);
    }

    // REQ-002-S02 参数化展开 客户类型为"金融机构"（VALUE_300，非"公司"）：同走 ER0042 分支
    @Test
    public void testST086T03() {
        String clientNo = "C20260930000003";
        stubClientCopy(clientCopy(clientNo, ClientType.VALUE_300), clientNo);

        ST086OutputBO output = st086.execute(input(clientNo));

        assertNotCorporate(output, ClientType.VALUE_300);
    }

    // REQ-002-S02 参数化展开 客户类型为"内部客户"（VALUE_600，非"公司"）：同走 ER0042 分支
    @Test
    public void testST086T04() {
        String clientNo = "C20260930000004";
        stubClientCopy(clientCopy(clientNo, ClientType.VALUE_600), clientNo);

        ST086OutputBO output = st086.execute(input(clientNo));

        assertNotCorporate(output, ClientType.VALUE_600);
    }

    // REQ-001-S02 + REQ-002-S03 客户号无记录：客户类型未取得（输出 null 不填充默认值），按未取得判定返回 ER0042
    @Test
    public void testST086T05() {
        String clientNo = "C20260930999999";
        stubClientCopy(null, clientNo);

        ST086OutputBO output = st086.execute(input(clientNo));

        assertFalse(output.isSucceed());
        assertEquals("ER0042", output.getErrorCode());
        assertNull(output.getClientType());
    }
}
