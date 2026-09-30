package com.dcits.depsit.step;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * ST097 计算限额累计金额。
 *
 * 主体段 以{限额场景编码}与启用标志"有效"在【限额控制配置表】（RB_LIMIT_CTRL_CONF）获取
 * 配置记录（多条时取最后更新时间戳最新一条），按其自定义相关标志决定[限额信息]取数来源
 * （仅客户自定义 / 自定义优先回退 / 仅配置表），未取到配置记录或[限额信息]时[限额累计金额]
 * 与[限额累计笔数]返回空；子条1 [限额信息]限额控制类型为"O-单笔金额"时不计算累计、两字段
 * 返回空；子条2、3 类型为 N/A/B 且累计类型代码为 1/3/4/5 时以{限额检查对象值}查【限额累计
 * 信息表】（RB_LIMIT_SUM_INFO），按生效范围（生效日期≤系统日期 且 失效日期≥系统日期，
 * 含端点）累加或以{交易金额}初始化；子条4、5 累计类型代码为 2-滑动窗口时按选中配置记录
 * $周期值$与$期限类型$确定的有效周期查【滑动流水表】（RB_LIMIT_SUM_JNL）汇总或初始化
 * （累计类型代码、周期值、期限类型始终取自配置表选中记录）。只读计算步骤，无业务失败
 * 场景（技术异常原样传播），无事务要求。
 */
@Service
public class ST097Pbc implements IST097 {

    /** 启用标志取值：有效（需求文本常量，无枚举绑定） */
    private static final String VALID_FLAG_EFFECTIVE = "有效";

    /** 自定义相关标志取值：是（需求文本常量，无枚举绑定） */
    private static final String FLAG_YES = "是";

    @Autowired
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Autowired
    private IRbLimitCtrlCustomInfoBcc rbLimitCtrlCustomInfoBcc;

    @Autowired
    private IRbLimitSceneDefBcc rbLimitSceneDefBcc;

    @Autowired
    private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

    @Autowired
    private IRbLimitSumJnlBcc rbLimitSumJnlBcc;

    @Override
    public ST097OutputBO execute(ST097InputBO input) {
        ST097OutputBO output = new ST097OutputBO();

        // 主体段 REQ-001 获取启用有效的限额控制配置记录：多条时取最后更新时间戳最新一条
        RbLimitCtrlConfEO selectedConf = findLatestValidConf(input.getLimitSceneNo());
        if (selectedConf == null) {
            // 未取到启用有效配置记录：[限额累计金额]与[限额累计笔数]返回空
            output.setSucceed(true);
            return output;
        }

        // 主体段 REQ-002 按自定义相关标志决定[限额信息]取数来源；未取到[限额信息]时两字段返回空
        CtrlItemType ctrlItemType = resolveLimitInfoCtrlItemType(selectedConf, input);
        if (ctrlItemType == null) {
            output.setSucceed(true);
            return output;
        }

        // 子条1 REQ-003 限额控制类型为"O-单笔金额"：无需计算累计，两字段返回空
        if (ctrlItemType == CtrlItemType.O) {
            output.setSucceed(true);
            return output;
        }

        // 子条2/4 前置：读【限额场景定义表】检查对象类型，确认{限额检查对象值}的取值维度
        //（checkObjVal 已按该维度由调用方取值传入，后续查询以该值为条件，不因维度改变）
        determineCheckObjType(input.getLimitSceneNo());

        // 子条2-5 REQ-004/005 按选中配置记录累计类型代码计算累计并计入本次{交易金额}
        if (selectedConf.getSumType() == SumType.VALUE_2) {
            fillSlidingWindowSum(output, selectedConf, input);
        } else {
            // SumType 取值域为 1-5，非 2 即 1/3/4/5 的周期型累计
            fillPeriodicSum(output, input);
        }

        output.setSucceed(true);
        return output;
    }

    /**
     * 主体段 REQ-001：以{限额场景编码}与启用标志"有效"为条件查询【限额控制配置表】，命中多条时
     * 取$最后更新时间戳$最新的一条（定长 yyyy-MM-dd HH:mm:ss 文本，字典序即时间序）；
     * 无启用有效记录时返回 null。
     */
    private RbLimitCtrlConfEO findLatestValidConf(String limitSceneNo) {
        RbLimitCtrlConfEO condition = new RbLimitCtrlConfEO();
        condition.setLimitSceneNo(limitSceneNo);
        condition.setValidFlag(VALID_FLAG_EFFECTIVE);
        List<RbLimitCtrlConfEO> records = rbLimitCtrlConfBcc.findByEo(condition);
        if (records == null || records.isEmpty()) {
            return null;
        }
        RbLimitCtrlConfEO latest = records.get(0);
        for (RbLimitCtrlConfEO record : records) {
            if (record.getLastUpdTimestamp().compareTo(latest.getLastUpdTimestamp()) > 0) {
                latest = record;
            }
        }
        return latest;
    }

    /**
     * 主体段 REQ-002 按自定义相关标志决定[限额信息]来源（先判$仅检查客户自定义标志$，
     * 再判$允许自定义标识$，两标志同时为"是"时按"仅检查客户自定义"处理）：
     * a) 仅检查客户自定义标志="是"：仅取【限额控制客户自定义配置表】，未命中不回退；
     * b) 否则 允许自定义标识="是"：先取客户自定义配置，未命中回退取选中配置记录；
     * c) 其余（两标志均为"否"）：取选中配置记录。
     * 返回 null 表示按来源未取到[限额信息]。
     */
    private CtrlItemType resolveLimitInfoCtrlItemType(RbLimitCtrlConfEO selectedConf, ST097InputBO input) {
        if (FLAG_YES.equals(selectedConf.getOnlyCustom())) {
            RbLimitCtrlCustomInfoEO customInfo = findCustomInfo(input.getLimitSceneNo(), input.getClientNo());
            if (customInfo == null) {
                // 仅检查客户自定义未命中：不回退【限额控制配置表】
                return null;
            }
            return customInfo.getCtrlItemType();
        }
        if (FLAG_YES.equals(selectedConf.getAllowCustomFlag())) {
            RbLimitCtrlCustomInfoEO customInfo = findCustomInfo(input.getLimitSceneNo(), input.getClientNo());
            if (customInfo != null) {
                return customInfo.getCtrlItemType();
            }
            // 允许自定义未命中：回退取选中配置记录的[限额信息]
            return selectedConf.getCtrlItemType();
        }
        return selectedConf.getCtrlItemType();
    }

    /**
     * 主体段 客户维度定位【限额控制客户自定义配置表】：以{限额场景编码}与{客户号}为条件查询
     * （表主键为限额检查对象值+限额场景编码，客户号非主键，故使用 findByEo），未命中返回 null。
     */
    private RbLimitCtrlCustomInfoEO findCustomInfo(String limitSceneNo, String clientNo) {
        RbLimitCtrlCustomInfoEO condition = new RbLimitCtrlCustomInfoEO();
        condition.setLimitSceneNo(limitSceneNo);
        condition.setClientNo(clientNo);
        List<RbLimitCtrlCustomInfoEO> records = rbLimitCtrlCustomInfoBcc.findByEo(condition);
        if (records == null || records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    /**
     * 子条2/4 前置：读【限额场景定义表】的$检查对象类型$（条件：{限额场景编码}），
     * 确认{限额检查对象值}的取值维度（如客户号/账号/卡号）。
     */
    private CheckObjType determineCheckObjType(String limitSceneNo) {
        RbLimitSceneDefEO sceneDef = rbLimitSceneDefBcc.findByPrimaryKey(limitSceneNo);
        if (sceneDef == null) {
            return null;
        }
        return sceneDef.getCheckObjType();
    }

    /**
     * 子条2、3 REQ-004 周期型累计（累计类型代码 1/3/4/5）：以{限额检查对象值}与{限额场景编码}
     * 为条件查询【限额累计信息表】，生效范围判定通过时累计金额、笔数在该记录基础上加本次交易；
     * 无记录或不在生效范围时以{交易金额}初始化。
     */
    private void fillPeriodicSum(ST097OutputBO output, ST097InputBO input) {
        RbLimitSumInfoEO sumInfo = rbLimitSumInfoBcc.findByPrimaryKey(input.getCheckObjVal(), input.getLimitSceneNo());
        if (sumInfo != null && isInEffectRange(sumInfo)) {
            // 生效范围内存在[累计限额信息]：限额累计金额+{交易金额}、限额累计笔数+1
            output.setLimitSumAmt(sumInfo.getLimitSumAmt().add(input.getTranAmt()));
            output.setLimitSumNum(sumInfo.get否() + 1);
            return;
        }
        // 不存在（无记录，或记录均不在生效范围内）：以{交易金额}初始化
        output.setLimitSumAmt(input.getTranAmt());
        output.setLimitSumNum(1);
    }

    /**
     * 子条3 REQ-004 生效范围判定：$生效日期$ ≤ 系统日期 且 $失效日期$ ≥ 系统日期，
     * 两侧均含端点比较；系统日期为运行时系统当前日期。日期缺失的记录无法判定在生效范围内。
     */
    private boolean isInEffectRange(RbLimitSumInfoEO sumInfo) {
        LocalDate today = LocalDate.now();
        LocalDate effectDate = toLocalDate(sumInfo.getEffectDate());
        LocalDate expireDate = toLocalDate(sumInfo.getExpireDate());
        return effectDate != null && !effectDate.isAfter(today)
                && expireDate != null && !expireDate.isBefore(today);
    }

    /**
     * 子条4、5 REQ-005 滑动窗口型累计（累计类型代码 2）：以{限额场景编码}与{限额检查对象值}
     * 为条件查询【滑动流水表】（BCC 无按日期范围查询的方法，有效周期由流水交易日期在窗口内
     * 判定），按选中配置记录$周期值$与$期限类型$确定的有效周期过滤流水后汇总交易金额与笔数，
     * 再计入本次{交易金额}；窗口内无流水时等同于以{交易金额}初始化。
     */
    private void fillSlidingWindowSum(ST097OutputBO output, RbLimitCtrlConfEO selectedConf, ST097InputBO input) {
        RbLimitSumJnlEO condition = new RbLimitSumJnlEO();
        condition.setLimitSceneNo(input.getLimitSceneNo());
        condition.setCheckObjVal(input.getCheckObjVal());
        List<RbLimitSumJnlEO> jnlList = rbLimitSumJnlBcc.findByEo(condition);

        LocalDate today = LocalDate.now();
        LocalDate windowStart = slidingWindowStart(today, selectedConf.getPeriodType(), selectedConf.getPeriodValue());

        // 子条5 有效周期内流水汇总：金额求和、笔数计数（无交易日期的流水无法判定所在周期，不计入）
        BigDecimal sumAmt = BigDecimal.ZERO;
        int sumNum = 0;
        if (jnlList != null) {
            for (RbLimitSumJnlEO jnl : jnlList) {
                if (isInSlidingWindow(jnl.getTranDate(), windowStart, today)) {
                    sumAmt = sumAmt.add(jnl.getTranAmt());
                    sumNum++;
                }
            }
        }
        output.setLimitSumAmt(sumAmt.add(input.getTranAmt()));
        output.setLimitSumNum(sumNum + 1);
    }

    /**
     * 子条4 滑动窗口有效周期起点：以系统日期为基准向前推$周期值$个$期限类型$
     * （D-日 / W-周 / M-月 / Q-季按 3 个月 / H-半年按 6 个月 / Y-年；需求未约定窗口边界
     * 开闭细节与折算规则，按自然日历单位计算，窗口两侧含端点）。
     */
    private LocalDate slidingWindowStart(LocalDate today, TermType periodType, String periodValue) {
        long value = Long.parseLong(periodValue);
        LocalDate windowStart = today;
        switch (periodType) {
            case D:
                windowStart = today.minusDays(value);
                break;
            case W:
                windowStart = today.minusWeeks(value);
                break;
            case M:
                windowStart = today.minusMonths(value);
                break;
            case Q:
                windowStart = today.minusMonths(value * 3);
                break;
            case H:
                windowStart = today.minusMonths(value * 6);
                break;
            case Y:
                windowStart = today.minusYears(value);
                break;
            default:
                break;
        }
        return windowStart;
    }

    /** 子条4 流水交易日期在有效周期内（窗口起点 ≤ 交易日期 ≤ 系统日期，含端点） */
    private boolean isInSlidingWindow(Date tranDate, LocalDate windowStart, LocalDate today) {
        LocalDate date = toLocalDate(tranDate);
        return date != null && !date.isBefore(windowStart) && !date.isAfter(today);
    }

    /** java.util.Date 按系统时区转换为 LocalDate（经 epoch 毫秒转换，兼容 java.sql.Date 子类）；null 返回 null */
    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
