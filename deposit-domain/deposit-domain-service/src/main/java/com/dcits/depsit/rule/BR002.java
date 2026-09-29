package com.dcits.depsit.rule;

import java.math.BigDecimal;

/**
 * 规则：BR002-计算账户利率（计算类）
 *
 * 以{产品利率}为基准，结合账户侧三种利率浮动配置，按 a>b>c 固定优先级链式判定首个命中分支，
 * 计算并返回[执行利率]（realRate）：
 * <ul>
 *   <li>分支a：{账户利率浮动百分点}不等于0，realRate = productRate + acctSpreadRate</li>
 *   <li>分支b：否则若{账户利率浮动百分比}不等于0，realRate = productRate ×（1 + acctPercentRate）</li>
 *   <li>分支c：否则若{账户固定利率}不等于0，realRate = acctFixedRate（不使用 productRate）</li>
 * </ul>
 * 判定口径："不等于0"按 BigDecimal 数值比较（compareTo 语义），任意标度的 0 均视为等于 0，
 * 一切非 0 数值（含负数）视为不等于 0；浮动输入为 null 表示未配置该浮动方式，对应分支不命中，
 * null 不作为数值参与任何公式运算；a/b/c 均未命中时不对 realRate 赋值（返回 null），
 * 无默认分支，不返回任何默认利率（包括 productRate）。
 * 三个公式均为 BigDecimal 精确运算，不引入中间舍入。纯计算规则：无状态副作用、
 * 无数据库访问、无外部服务调用，输出仅由四个入参决定。
 */
public class BR002 {

    /**
     * 计算执行利率
     *
     * @param acctSpreadRate  账户利率浮动百分点（非必填，允许 null）
     * @param acctPercentRate 账户利率浮动百分比（非必填，允许 null）
     * @param acctFixedRate   账户固定利率（非必填，允许 null）
     * @param productRate     产品利率（必填，必填性由调用方契约保证）
     * @return 执行利率 realRate；a/b/c 均未命中时为 null
     */
    public static BigDecimal execute(BigDecimal acctSpreadRate, BigDecimal acctPercentRate,
                                     BigDecimal acctFixedRate, BigDecimal productRate) {
        // 分支a：账户利率浮动百分点不等于0（null 不满足条件），realRate = productRate + acctSpreadRate
        if (isNonZero(acctSpreadRate)) {
            return productRate.add(acctSpreadRate);
        }
        // 分支b：否则若账户利率浮动百分比不等于0，realRate = productRate ×（1 + acctPercentRate）
        if (isNonZero(acctPercentRate)) {
            return productRate.multiply(BigDecimal.ONE.add(acctPercentRate));
        }
        // 分支c：否则若账户固定利率不等于0，realRate = acctFixedRate（不使用 productRate）
        if (isNonZero(acctFixedRate)) {
            return acctFixedRate;
        }
        // a/b/c 均未命中：不对 realRate 赋值（null 与 0 在分支选择上行为一致）
        return null;
    }

    /**
     * 按数值比较口径判定"不等于0"：null 视为未配置不命中；0、0.0、0.00 等任意标度均视为等于 0
     */
    private static boolean isNonZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) != 0;
    }
}
