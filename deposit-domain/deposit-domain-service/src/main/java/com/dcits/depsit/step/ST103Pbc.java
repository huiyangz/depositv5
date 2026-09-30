package com.dcits.depsit.step;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST103InputBO;
import com.dcits.depsit.facade.bo.ST103OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;

/**
 * ST103 检查限额场景配置是否有效。
 *
 * 步骤1「获取限额场景控制区间」以输入{限额机构编码}、[限额场景编码]主键等值
 * 查询【限额控制配置】（RB_LIMIT_CTRL_CONF，至多命中一条），取得限额控制
 * 开始/结束日期与开始/结束时间四个区间字段；步骤2「检查交易时间」先检查四个
 * 区间字段完整性（任一为空视为配置无效），再判定{交易日期}在开始/结束日期
 * 闭区间内且{交易时间}（tranTimestamp 的 HHmmss 时分秒量值）在开始/结束
 * 时间闭区间内（按当日时分秒量值比较，不含日期成分）；均满足时判定配置有效，
 * 输出场景编码及四个区间字段，否则（含未查询到、字段不全、判定不满足）全部
 * 输出为空。只读步骤，无业务失败场景，无事务要求。
 */
@Service
public class ST103Pbc implements IST103 {

    /** 一分钟对应的毫秒数 */
    private static final long MILLIS_PER_SECOND = 1000L;
    /** 一小时对应的分钟数 */
    private static final int MINUTES_PER_HOUR = 60;
    /** 一分钟的秒数 */
    private static final int SECONDS_PER_MINUTE = 60;
    /** 交易时间戳 HHmmss 的各段长度 */
    private static final int TIMESTAMP_FIELD_LENGTH = 2;

    @Autowired
    private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

    @Override
    public ST103OutputBO execute(ST103InputBO input) {
        ST103OutputBO output = new ST103OutputBO();

        // 步骤1 获取限额场景控制区间：主键等值查询至多一条；未查询到时不执行步骤2区间判定，直接返回（全部输出为空）
        RbLimitCtrlConfEO record = rbLimitCtrlConfBcc.findByPrimaryKey(input.getLimitBranchId(), input.getLimitSceneNo());
        if (record == null) {
            output.setSucceed(true);
            return output;
        }

        // 步骤2 检查交易时间——完整性检查：任一控制区间字段为空视为配置无效，不执行区间判定（全部输出为空）
        if (!hasCompleteCtrlRanges(record)) {
            output.setSucceed(true);
            return output;
        }

        // 步骤2 检查交易时间——区间判定：日期闭区间与时间闭区间两条件同时成立才判定配置有效
        if (isTranDateInCtrlRange(input.getTranDate(), record)
                && isTranTimeInCtrlRange(input.getTranTimestamp(), record)) {
            fillValidOutput(output, record);
        }

        // 未满足区间判定时走"否则"分支：不赋值，5 个输出字段保持空；均为正常业务结果
        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 结果取得：限额控制开始日期、结束日期、开始时间、结束时间四个字段均非空时配置字段齐全。
     */
    private boolean hasCompleteCtrlRanges(RbLimitCtrlConfEO record) {
        return record.getLimitCtrlBgnDate() != null
                && record.getLimitCtrlEndDate() != null
                && record.getLimitCtrlBgnTime() != null
                && record.getLimitCtrlEndTime() != null;
    }

    /**
     * 步骤2 区间判定（日期）：limitCtrlBgnDate ≤ tranDate ≤ limitCtrlEndDate，闭区间含两端。
     */
    private boolean isTranDateInCtrlRange(Date tranDate, RbLimitCtrlConfEO record) {
        return !tranDate.before(record.getLimitCtrlBgnDate())
                && !tranDate.after(record.getLimitCtrlEndDate());
    }

    /**
     * 步骤2 区间判定（时间）：limitCtrlBgnTime ≤ 交易时间 ≤ limitCtrlEndTime，闭区间含两端。
     * tranTimestamp 为 HHmmss 字符串，按当日时分秒量值比较，不含日期成分。
     */
    private boolean isTranTimeInCtrlRange(String tranTimestamp, RbLimitCtrlConfEO record) {
        long tranTimeOfDay = timeOfDayMillis(tranTimestamp);
        return tranTimeOfDay >= timeOfDayMillis(record.getLimitCtrlBgnTime())
                && tranTimeOfDay <= timeOfDayMillis(record.getLimitCtrlEndTime());
    }

    /**
     * 步骤2 结果输出：配置有效时 5 个输出字段取命中记录值（limitSceneNo 与查询所用输入值相同，主键同值）。
     */
    private void fillValidOutput(ST103OutputBO output, RbLimitCtrlConfEO record) {
        output.setLimitSceneNo(record.getLimitSceneNo());
        output.setLimitCtrlBgnDate(record.getLimitCtrlBgnDate());
        output.setLimitCtrlEndDate(record.getLimitCtrlEndDate());
        output.setLimitCtrlBgnTime(record.getLimitCtrlBgnTime());
        output.setLimitCtrlEndTime(record.getLimitCtrlEndTime());
    }

    /**
     * 取 HHmmss 交易时间戳的当日时分秒量值（毫秒）。格式非法时由数值解析异常按技术异常传播。
     */
    private long timeOfDayMillis(String tranTimestamp) {
        int hour = Integer.parseInt(tranTimestamp.substring(0, TIMESTAMP_FIELD_LENGTH));
        int minute = Integer.parseInt(tranTimestamp.substring(TIMESTAMP_FIELD_LENGTH, TIMESTAMP_FIELD_LENGTH * 2));
        int second = Integer.parseInt(tranTimestamp.substring(TIMESTAMP_FIELD_LENGTH * 2, TIMESTAMP_FIELD_LENGTH * 3));
        return timeOfDayMillis(hour, minute, second, 0);
    }

    /**
     * 取控制时间字段的当日时分秒量值（毫秒），剔除日期成分。
     */
    private long timeOfDayMillis(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return timeOfDayMillis(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
    }

    /**
     * 按时、分、秒、毫秒计算当日时分秒量值（毫秒）。
     */
    private long timeOfDayMillis(int hour, int minute, int second, int millis) {
        return ((hour * (long) MINUTES_PER_HOUR + minute) * SECONDS_PER_MINUTE + second) * MILLIS_PER_SECOND + millis;
    }
}
