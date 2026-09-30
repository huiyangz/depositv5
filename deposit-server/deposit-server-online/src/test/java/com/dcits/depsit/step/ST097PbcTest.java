package com.dcits.depsit.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dcits.depsit.enums.CheckObjType;
import com.dcits.depsit.enums.CtrlItemType;
import com.dcits.depsit.enums.SumType;
import com.dcits.depsit.enums.TermType;
import com.dcits.depsit.facade.bo.ST097InputBO;
import com.dcits.depsit.facade.bo.ST097OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.components.IRbLimitCtrlCustomInfoBcc;
import com.dcits.depsit.facade.components.IRbLimitSceneDefBcc;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.components.IRbLimitSumJnlBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;
import com.dcits.depsit.facade.eo.RbLimitCtrlCustomInfoEO;
import com.dcits.depsit.facade.eo.RbLimitSceneDefEO;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;
import com.dcits.depsit.facade.eo.RbLimitSumJnlEO;

/**
 * ST097 计算限额累计金额 单元测试。
 *
 * 用例来源：outputs/测试用例.md / 测试用例.json（ST097-TC001 ~ TC017），
 * 预期结果来自正式 Spec ST097（fileInputs.spec 绑定的 ST097.md）；
 * testST097T18 为本轮审核反馈 R1 补充（滑动窗口排除有效周期外流水）。
 * 本步骤无业务失败场景，全部用例 succeed=true；两输出字段为 null 是业务结果不是失败。
 * 日期按运行时系统当前日期构造（当年1月1日/当年12月31日/当前日期/当前日期-1日），
 * 与 Spec 场景示例字面值在 2026-09-30 运行时一致，任意执行日期下判定语义不变。
 */
@ExtendWith(MockitoExtension.class)
public class ST097PbcTest {

    /** 限额场景编码（Spec 默认示例输入） */
    private static final String LIMIT_SCENE_NO = "LS001";

    /** 客户号（Spec 默认示例输入） */
    private static final String CLIENT_NO = "C0001";

    /** 限额检查对象值（场景 LS001 检查对象类型为 CUST-客户级别，取客户号） */
    private static final String CHECK_OBJ_VAL = "C0001";

    /** 交易金额（Spec 默认示例输入） */
    private static final BigDecimal TRAN_AMT = new BigDecimal("1000.00");

    /** 启用标志取值：有效 */
    private static final String VALID_FLAG = "有效";

    /** 自定义相关标志取值：是 / 否 */
    private static final String FLAG_YES = "是";
    private static final String FLAG_NO = "否";

    @Mock
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Mock
    private IRbLimitCtrlCustomInfoBcc rbLimitCtrlCustomInfoBcc;

    @Mock
    private IRbLimitSceneDefBcc rbLimitSceneDefBcc;

    @Mock
    private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

    @Mock
    private IRbLimitSumJnlBcc rbLimitSumJnlBcc;

    @InjectMocks
    private ST097Pbc st097;

    /** 构造公共输入：限额场景编码、交易金额、客户号、限额检查对象值 */
    private ST097InputBO input() {
        ST097InputBO input = new ST097InputBO();
        input.setLimitSceneNo(LIMIT_SCENE_NO);
        input.setTranAmt(TRAN_AMT);
        input.setClientNo(CLIENT_NO);
        input.setCheckObjVal(CHECK_OBJ_VAL);
        return input;
    }

    /** 构造启用有效配置记录（公共赋值：场景 LS001、启用标志 有效、最后更新时间戳按存储约定示例定长文本） */
    private RbLimitCtrlConfEO conf(String onlyCustom, String allowCustomFlag, CtrlItemType ctrlItemType, SumType sumType) {
        RbLimitCtrlConfEO eo = new RbLimitCtrlConfEO();
        eo.setLimitSceneNo(LIMIT_SCENE_NO);
        eo.setValidFlag(VALID_FLAG);
        eo.setLastUpdTimestamp("2026-09-20 08:00:00");
        eo.setOnlyCustom(onlyCustom);
        eo.setAllowCustomFlag(allowCustomFlag);
        eo.setCtrlItemType(ctrlItemType);
        eo.setSumType(sumType);
        return eo;
    }

    /** 构造客户维度自定义配置记录（场景+客户号定位，checkObjVal 取客户号） */
    private RbLimitCtrlCustomInfoEO customInfo(CtrlItemType ctrlItemType) {
        RbLimitCtrlCustomInfoEO eo = new RbLimitCtrlCustomInfoEO();
        eo.setLimitSceneNo(LIMIT_SCENE_NO);
        eo.setClientNo(CLIENT_NO);
        eo.setCheckObjVal(CHECK_OBJ_VAL);
        eo.setCtrlItemType(ctrlItemType);
        return eo;
    }

    /** 构造累计信息记录（公共赋值：限额累计金额 2000.00、限额累计笔数 2；EO 笔数字段命名为"否"的缺陷以真实访问器书写） */
    private RbLimitSumInfoEO sumInfo(Date effectDate, Date expireDate) {
        RbLimitSumInfoEO eo = new RbLimitSumInfoEO();
        eo.setCheckObjVal(CHECK_OBJ_VAL);
        eo.setLimitSceneNo(LIMIT_SCENE_NO);
        eo.setClientNo(CLIENT_NO);
        eo.setEffectDate(effectDate);
        eo.setExpireDate(expireDate);
        eo.setLimitSumAmt(new BigDecimal("2000.00"));
        eo.set否(2);
        return eo;
    }

    /** 构造滑动流水记录（交易日期取当前日期，位于有效周期内） */
    private RbLimitSumJnlEO jnl(String tranAmt) {
        return jnl(tranAmt, today());
    }

    /** 构造滑动流水记录（指定交易日期） */
    private RbLimitSumJnlEO jnl(String tranAmt, Date tranDate) {
        RbLimitSumJnlEO eo = new RbLimitSumJnlEO();
        eo.setLimitSceneNo(LIMIT_SCENE_NO);
        eo.setCheckObjVal(CHECK_OBJ_VAL);
        eo.setClientNo(CLIENT_NO);
        eo.setTranDate(tranDate);
        eo.setTranAmt(new BigDecimal(tranAmt));
        return eo;
    }

    /** 当年1月1日（运行期系统当前日期所在年份） */
    private Date firstDayOfYear() {
        return date(LocalDate.now().withDayOfYear(1));
    }

    /** 当年12月31日 */
    private Date lastDayOfYear() {
        return date(LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1));
    }

    /** 系统当前日期（当日零点） */
    private Date today() {
        return date(LocalDate.now());
    }

    /** 系统当前日期-1日（早于系统日期，不在生效范围内） */
    private Date yesterday() {
        return date(LocalDate.now().minusDays(1));
    }

    /** 系统当前日期-10月（早于周期值 3+期限类型 M 的窗口起点 当前日期-3月，在有效周期外） */
    private Date beforeSlidingWindow() {
        return date(LocalDate.now().minusMonths(10));
    }

    /** LocalDate 按当日零点转换为 java.util.Date */
    private Date date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // 匹配器 lambda 需 null 防护：注册第二个同方法桩时 argThat 以占位 null 真实调用 mock，
    // Mockito 会先用已注册桩的匹配器匹配该 null，不防护会 NPE；生产调用恒为构造好的 EO，断言语义不变
    /** 主体段桩：按 场景编码+启用标志=有效 组合条件核对请求并返回配置记录列表 */
    private void stubConf(List<RbLimitCtrlConfEO> records) {
        lenient().when(rbLimitCtrlConfBcc.findByEo(argThat(eo ->
                eo != null && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()) && VALID_FLAG.equals(eo.getValidFlag()))))
                .thenReturn(records);
    }

    /** 自定义配置桩：按 场景编码+客户号 组合条件核对请求并返回客户维度记录列表 */
    private void stubCustom(List<RbLimitCtrlCustomInfoEO> records) {
        lenient().when(rbLimitCtrlCustomInfoBcc.findByEo(argThat(eo ->
                eo != null && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()) && CLIENT_NO.equals(eo.getClientNo()))))
                .thenReturn(records);
    }

    /** 场景定义桩：场景 LS001 的检查对象类型为 CUST-客户级别 */
    private void stubSceneDefCust() {
        RbLimitSceneDefEO eo = new RbLimitSceneDefEO();
        eo.setLimitSceneNo(LIMIT_SCENE_NO);
        eo.setCheckObjType(CheckObjType.CUST);
        lenient().when(rbLimitSceneDefBcc.findByPrimaryKey(LIMIT_SCENE_NO)).thenReturn(eo);
    }

    /** 累计信息桩：按主键 检查对象值+场景编码（注意参数顺序）返回累计记录 */
    private void stubSumInfo(RbLimitSumInfoEO record) {
        lenient().when(rbLimitSumInfoBcc.findByPrimaryKey(CHECK_OBJ_VAL, LIMIT_SCENE_NO)).thenReturn(record);
    }

    /** 滑动流水桩：按 场景编码+检查对象值 组合条件核对请求并返回流水记录列表 */
    private void stubJnl(List<RbLimitSumJnlEO> records) {
        lenient().when(rbLimitSumJnlBcc.findByEo(argThat(eo ->
                eo != null && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()) && CHECK_OBJ_VAL.equals(eo.getCheckObjVal()))))
                .thenReturn(records);
    }

    /** 成功且有累计值公共断言：succeed=true、错误字段 null、金额含标度等值、笔数等值 */
    private void assertSum(ST097OutputBO output, String expectedAmt, int expectedNum) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertEquals(new BigDecimal(expectedAmt), output.getLimitSumAmt());
        assertEquals(Integer.valueOf(expectedNum), output.getLimitSumNum());
    }

    /** 成功且两输出字段为空公共断言（未取到配置记录/限额信息或 O 型短路，非业务失败） */
    private void assertEmptySum(ST097OutputBO output) {
        assertTrue(output.isSucceed());
        assertNull(output.getErrorCode());
        assertNull(output.getErrorMessage());
        assertNull(output.getLimitSumAmt());
        assertNull(output.getLimitSumNum());
    }

    // REQ-001-S01 唯一启用记录被选用：来源取配置表（两标志否），周期型累加 2000.00+1000.00 / 2+1
    @Test
    public void testST097T01() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.N, SumType.VALUE_1)));
        stubSceneDefCust();
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "3000.00", 3);
    }

    // REQ-001-S02 多条启用记录取最后更新时间戳最新（列表旧记录甲在前，验证按时间戳选取而非取首条）：
    // 按乙执行来源选择与计算；守卫桩：自定义表返回空（误选甲将因自定义未命中得 null/null，由输出值区分）
    @Test
    public void testST097T02() {
        RbLimitCtrlConfEO older = conf(FLAG_YES, FLAG_NO, CtrlItemType.N, SumType.VALUE_1);
        older.setLastUpdTimestamp("2026-09-01 08:00:00");
        RbLimitCtrlConfEO latest = conf(FLAG_NO, FLAG_NO, CtrlItemType.N, SumType.VALUE_1);
        latest.setLastUpdTimestamp("2026-09-20 08:00:00");
        stubConf(Arrays.asList(older, latest));
        stubCustom(Collections.emptyList());
        stubSceneDefCust();
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "3000.00", 3);
    }

    // REQ-001-S03 无启用有效配置记录：两输出字段返回空（succeed=true，非业务失败），其余 BCC 不设桩
    @Test
    public void testST097T03() {
        stubConf(Collections.emptyList());

        ST097OutputBO output = st097.execute(input());

        assertEmptySum(output);
    }

    // REQ-002-S01 仅检查客户自定义-命中自定义配置且其限额控制类型为 O：按自定义[限额信息]短路返回空；
    // 守卫桩：生效范围内累计记录（误取配置表 N 型限额信息将得 3000.00/3，由输出值区分）
    @Test
    public void testST097T04() {
        stubConf(Collections.singletonList(conf(FLAG_YES, FLAG_NO, CtrlItemType.N, SumType.VALUE_1)));
        stubCustom(Collections.singletonList(customInfo(CtrlItemType.O)));
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertEmptySum(output);
    }

    // REQ-002-S02 仅检查客户自定义-未命中不回退返回空；守卫桩：生效范围内累计记录（误回退将得 3000.00/3）
    @Test
    public void testST097T05() {
        stubConf(Collections.singletonList(conf(FLAG_YES, FLAG_NO, CtrlItemType.N, SumType.VALUE_1)));
        stubCustom(Collections.emptyList());
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertEmptySum(output);
    }

    // REQ-002-S03 允许自定义-命中自定义配置且其限额控制类型为 O：使用自定义[限额信息]短路返回空；
    // 若误取配置表 N 型，未设桩的累计信息查询返回 null 按无记录得 1000.00/1，仍可区分
    @Test
    public void testST097T06() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_YES, CtrlItemType.N, SumType.VALUE_1)));
        stubCustom(Collections.singletonList(customInfo(CtrlItemType.O)));

        ST097OutputBO output = st097.execute(input());

        assertEmptySum(output);
    }

    // REQ-002-S04 允许自定义-未命中回退取选中配置记录[限额信息]并完成周期累计
    @Test
    public void testST097T07() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_YES, CtrlItemType.N, SumType.VALUE_1)));
        stubCustom(Collections.emptyList());
        stubSceneDefCust();
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "3000.00", 3);
    }

    // REQ-002-S05 两标志均为否-取配置表[限额信息]（A 型）并完成周期累计；
    // 守卫桩：自定义表存在 O 型记录（误取自定义将短路得 null/null）
    @Test
    public void testST097T08() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.A, SumType.VALUE_1)));
        stubCustom(Collections.singletonList(customInfo(CtrlItemType.O)));
        stubSceneDefCust();
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "3000.00", 3);
    }

    // REQ-002-S06 仅检查客户自定义标志优先于允许自定义标识（两标志同为是按仅自定义处理，未命中不回退）；
    // 守卫桩：生效范围内累计记录（误回退将得 3000.00/3）
    @Test
    public void testST097T09() {
        stubConf(Collections.singletonList(conf(FLAG_YES, FLAG_YES, CtrlItemType.N, SumType.VALUE_1)));
        stubCustom(Collections.emptyList());
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertEmptySum(output);
    }

    // REQ-003-S01 限额控制类型为 O-单笔金额：不执行累计计算，两输出字段返回空；
    // 守卫桩：生效范围内累计记录（未短路将得 3000.00/3）
    @Test
    public void testST097T10() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.O, SumType.VALUE_1)));
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertEmptySum(output);
    }

    // REQ-004-S01 周期型累计（累计类型 1-自然周期）：生效范围内存在累计记录，累加 2000.00+1000.00 / 2+1
    @Test
    public void testST097T11() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.N, SumType.VALUE_1)));
        stubSceneDefCust();
        stubSumInfo(sumInfo(firstDayOfYear(), lastDayOfYear()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "3000.00", 3);
    }

    // REQ-004-S02 累计记录不在生效范围（失效日期早于系统日期）：以交易金额初始化 1000.00 / 1
    @Test
    public void testST097T12() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.N, SumType.VALUE_1)));
        stubSceneDefCust();
        stubSumInfo(sumInfo(firstDayOfYear(), yesterday()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "1000.00", 1);
    }

    // REQ-004-S03 无累计记录（累计类型 3-指定日期范围示例）：以交易金额初始化 1000.00 / 1
    @Test
    public void testST097T13() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.A, SumType.VALUE_3)));
        stubSceneDefCust();
        stubSumInfo(null);

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "1000.00", 1);
    }

    // REQ-004-S04 生效边界等于系统日期（生效日期=失效日期=系统日期）：含端点视为在范围并累加 3000.00 / 3
    @Test
    public void testST097T14() {
        stubConf(Collections.singletonList(conf(FLAG_NO, FLAG_NO, CtrlItemType.N, SumType.VALUE_1)));
        stubSceneDefCust();
        stubSumInfo(sumInfo(today(), today()));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "3000.00", 3);
    }

    // REQ-005-S01 滑动窗口型累计（累计类型 2，周期值 3+期限类型 M）：有效周期内 2 笔流水
    // 500.00+300.00，加本次交易金额 1800.00 / 2+1
    @Test
    public void testST097T15() {
        RbLimitCtrlConfEO conf = conf(FLAG_NO, FLAG_NO, CtrlItemType.B, SumType.VALUE_2);
        conf.setPeriodValue("3");
        conf.setPeriodType(TermType.M);
        stubConf(Collections.singletonList(conf));
        stubSceneDefCust();
        stubJnl(Arrays.asList(jnl("500.00"), jnl("300.00")));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "1800.00", 3);
    }

    // REQ-005-S02 有效周期内无流水：以交易金额初始化 1000.00 / 1
    @Test
    public void testST097T16() {
        RbLimitCtrlConfEO conf = conf(FLAG_NO, FLAG_NO, CtrlItemType.B, SumType.VALUE_2);
        conf.setPeriodValue("3");
        conf.setPeriodType(TermType.M);
        stubConf(Collections.singletonList(conf));
        stubSceneDefCust();
        stubJnl(Collections.emptyList());

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "1000.00", 1);
    }

    // REQ-005 补充（本轮审核反馈 R1）：有效周期外流水被排除不计入汇总——周期值 3+期限类型 M 的
    // 窗口起点为 当前日期-3月，流水 700.00 交易日期=当前日期-10月 在窗口外，窗口内 500.00+300.00
    // 照常汇总，仍为 1800.00 / 3（丢失窗口过滤将得 2800.00/4，反向过滤将得 1700.00/2，均可区分）
    @Test
    public void testST097T18() {
        RbLimitCtrlConfEO conf = conf(FLAG_NO, FLAG_NO, CtrlItemType.B, SumType.VALUE_2);
        conf.setPeriodValue("3");
        conf.setPeriodType(TermType.M);
        stubConf(Collections.singletonList(conf));
        stubSceneDefCust();
        stubJnl(Arrays.asList(
                jnl("500.00"),
                jnl("300.00"),
                jnl("700.00", beforeSlidingWindow())));

        ST097OutputBO output = st097.execute(input());

        assertSum(output, "1800.00", 3);
    }

    // REQ-006-S01 配置表数据访问技术异常：原样向调用方传播，无业务失败码、无部分结果
    @Test
    public void testST097T17() {
        lenient().when(rbLimitCtrlConfBcc.findByEo(argThat(eo ->
                eo != null && LIMIT_SCENE_NO.equals(eo.getLimitSceneNo()) && VALID_FLAG.equals(eo.getValidFlag()))))
                .thenThrow(new RuntimeException("数据源不可用"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> st097.execute(input()));

        assertEquals("数据源不可用", ex.getMessage());
    }
}
