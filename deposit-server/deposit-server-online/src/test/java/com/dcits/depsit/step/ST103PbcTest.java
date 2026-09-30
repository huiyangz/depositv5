package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST103InputBO;
import com.dcits.depsit.facade.bo.ST103OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST103 检查限额场景配置是否有效 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST103-TC001 ~ TC012），
 * 预期结果来自正式 Spec ST103（inputs 绑定的 ST103.md）。
 * 本步骤无业务失败场景，全部用例 succeed=true；"配置无效 / 未查到"是正常
 * 业务结果，仅由 5 个输出字段是否赋值表达。
 */
@ExtendWith(MockitoExtension.class)
public class ST103PbcTest {

    /** 示例限额机构编码（Spec 场景示例数据，对应 TranBranch.VALUE_351001） */
    private static final String LIMIT_BRANCH_ID = "351001";

    /** 示例限额场景编码（Spec 场景示例数据） */
    private static final String LIMIT_SCENE_NO = "SC0001";

    @Mock
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @InjectMocks
    private ST103Pbc st103;

    /** 构造公共输入：机构/场景编码取 Spec 示例值，交易日期与时间戳按用例传入 */
    private ST103InputBO input(Date tranDate, String tranTimestamp) {
        ST103InputBO input = new ST103InputBO();
        input.setLimitBranchId(LIMIT_BRANCH_ID);
        input.setLimitSceneNo(LIMIT_SCENE_NO);
        input.setTranDate(tranDate);
        input.setTranTimestamp(tranTimestamp);
        return input;
    }

    /** 构造标准命中记录：四控制区间字段齐全（2026-01-01~2026-12-31，09:00:00~17:00:00），validFlag 等其余字段不设值（本步骤不读取） */
    private RbLimitCtrlConfEO standardRecord() throws ParseException {
        RbLimitCtrlConfEO record = new RbLimitCtrlConfEO();
        record.setLimitBranchId(TranBranch.VALUE_351001);
        record.setLimitSceneNo(LIMIT_SCENE_NO);
        record.setLimitCtrlBgnDate(date("2026-01-01"));
        record.setLimitCtrlEndDate(date("2026-12-31"));
        record.setLimitCtrlBgnTime(time("09:00:00"));
        record.setLimitCtrlEndTime(time("17:00:00"));
        return record;
    }

    /** 日期测试值构造（yyyy-MM-dd，值确定可复现） */
    private static Date date(String text) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(text);
    }

    /** 时间量值测试值构造（HH:mm:ss，值确定可复现） */
    private static Date time(String text) throws ParseException {
        return new SimpleDateFormat("HH:mm:ss").parse(text);
    }

    /** 步骤1 桩：主键 ("351001","SC0001") 等值查询返回指定记录（精确参数值核对查询请求的字段映射） */
    private void stubFindByPrimaryKey(RbLimitCtrlConfEO record) {
        lenient().when(rbLimitCtrlConfBcc.findByPrimaryKey(LIMIT_BRANCH_ID, LIMIT_SCENE_NO)).thenReturn(record);
    }

    /** 无效/未查到路径公共断言：succeed=true、错误字段 null、5 个输出字段均为 null */
    private void assertEmptyOutput(ST103OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getLimitSceneNo());
        assertNull(output.getLimitCtrlBgnDate());
        assertNull(output.getLimitCtrlEndDate());
        assertNull(output.getLimitCtrlBgnTime());
        assertNull(output.getLimitCtrlEndTime());
    }

    /** 有效路径公共断言：succeed=true、错误字段 null、5 个输出字段取命中记录值（Date.equals 毫秒值相等） */
    private void assertHitOutput(ST103OutputBO output, RbLimitCtrlConfEO record) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(record.getLimitSceneNo(), output.getLimitSceneNo());
        assertEquals(record.getLimitCtrlBgnDate(), output.getLimitCtrlBgnDate());
        assertEquals(record.getLimitCtrlEndDate(), output.getLimitCtrlEndDate());
        assertEquals(record.getLimitCtrlBgnTime(), output.getLimitCtrlBgnTime());
        assertEquals(record.getLimitCtrlEndTime(), output.getLimitCtrlEndTime());
    }

    // REQ-001-S01＋REQ-003-S01 主键命中且四字段齐全，日期 2026-09-30、时间 "103000" 均严格处于区间内：判定有效，输出全部 5 个字段
    @Test
    public void testST103T01() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "103000"));

        assertHitOutput(output, record);
    }

    // REQ-001-S02 主键 ("351001","SC0001") 无记录，findByPrimaryKey 返回 null：不执行步骤2区间判定，5 个输出字段均为空
    @Test
    public void testST103T02() throws Exception {
        stubFindByPrimaryKey(null);

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-002-S01 命中记录 limitCtrlBgnTime 为空（其余三字段齐全，交易日期/时间本身落在其余字段区间内）：配置无效，不执行区间判定，5 个输出字段均为空
    @Test
    public void testST103T03() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        record.setLimitCtrlBgnTime(null);
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-002 "任一为空"分支：命中记录 limitCtrlBgnDate 为空（其余三字段齐全）：配置无效，5 个输出字段均为空
    @Test
    public void testST103T04() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        record.setLimitCtrlBgnDate(null);
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-002 "任一为空"分支：命中记录 limitCtrlEndDate 为空（其余三字段齐全）：配置无效，5 个输出字段均为空
    @Test
    public void testST103T05() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        record.setLimitCtrlEndDate(null);
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-002 "任一为空"分支：命中记录 limitCtrlEndTime 为空（其余三字段齐全）：配置无效，5 个输出字段均为空
    @Test
    public void testST103T06() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        record.setLimitCtrlEndTime(null);
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-003-S02 时间在区间内但交易日期 2025-12-31 早于 limitCtrlBgnDate=2026-01-01：日期条件不成立，判定无效，5 个输出字段均为空
    @Test
    public void testST103T07() throws Exception {
        stubFindByPrimaryKey(standardRecord());

        ST103OutputBO output = st103.execute(input(date("2025-12-31"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-003-S03 时间在区间内但交易日期 2027-01-01 晚于 limitCtrlEndDate=2026-12-31：日期条件不成立，判定无效，5 个输出字段均为空
    @Test
    public void testST103T08() throws Exception {
        stubFindByPrimaryKey(standardRecord());

        ST103OutputBO output = st103.execute(input(date("2027-01-01"), "103000"));

        assertEmptyOutput(output);
    }

    // REQ-003-S04 日期在区间内但交易时间 "085959" 的时分秒量值早于 limitCtrlBgnTime=09:00:00：时间条件不成立，判定无效，5 个输出字段均为空
    @Test
    public void testST103T09() throws Exception {
        stubFindByPrimaryKey(standardRecord());

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "085959"));

        assertEmptyOutput(output);
    }

    // REQ-003-S05 日期在区间内但交易时间 "170001" 的时分秒量值晚于 limitCtrlEndTime=17:00:00：时间条件不成立，判定无效，5 个输出字段均为空
    @Test
    public void testST103T10() throws Exception {
        stubFindByPrimaryKey(standardRecord());

        ST103OutputBO output = st103.execute(input(date("2026-09-30"), "170001"));

        assertEmptyOutput(output);
    }

    // REQ-003-S06 闭区间含端点：交易日期 2026-01-01 等于 limitCtrlBgnDate、时间 "090000" 等于 limitCtrlBgnTime 量值（均等于下界）：判定有效，输出全部 5 个字段
    @Test
    public void testST103T11() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-01-01"), "090000"));

        assertHitOutput(output, record);
    }

    // REQ-003-S07 闭区间含端点：交易日期 2026-12-31 等于 limitCtrlEndDate、时间 "170000" 等于 limitCtrlEndTime 量值（均等于上界）：判定有效，输出全部 5 个字段
    @Test
    public void testST103T12() throws Exception {
        RbLimitCtrlConfEO record = standardRecord();
        stubFindByPrimaryKey(record);

        ST103OutputBO output = st103.execute(input(date("2026-12-31"), "170000"));

        assertHitOutput(output, record);
    }
}
