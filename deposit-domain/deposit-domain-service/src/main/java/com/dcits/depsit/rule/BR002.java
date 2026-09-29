package com.dcits.depsit.rule;

import java.math.BigDecimal;

/**
 * 计算账户利率
 *
 * <p>计算类规则：按“浮动百分点 → 浮动百分比 → 固定利率 → 产品利率”的优先级顺序，
 * 取第一个成立的分支计算账户执行利率 realRate。空值与任意标度的数值 0 在条件判断中
 * 均判为“等于 0”，条件不成立；任何非零数值（含负数）条件成立。</p>
 */
public class BR002 {

    private BR002() {
    }

    /**
     * 计算账户执行利率。
     *
     * <p>分支优先级：</p>
     * <ol>
     *     <li>a. acctSpreadRate 数值不等于 0（空值视同 0）：realRate = prodRate + acctSpreadRate</li>
     *     <li>b. acctPercentRate 数值不等于 0：realRate = prodRate × (1 + acctPercentRate)</li>
     *     <li>c. acctFixedRate 数值不等于 0：realRate = acctFixedRate（不与 prodRate 运算）</li>
     *     <li>d. 以上均不成立：realRate = prodRate</li>
     * </ol>
     *
     * <p>空值仅参与条件判断（视同 0），不会进入算术运算；计算仅含加法与乘法，
     * BigDecimal 下精确表示、无舍入，结果标度依循运算语义。</p>
     *
     * @param acctSpreadRate  账户利率浮动百分点，空值视同 0 参与条件判断
     * @param acctPercentRate 账户利率浮动百分比，空值视同 0 参与条件判断
     * @param acctFixedRate   账户固定利率，空值视同 0 参与条件判断
     * @param prodRate        产品利率
     * @return 执行利率 realRate，任一分支命中均返回非空结果
     */
    public static BigDecimal execute(BigDecimal acctSpreadRate, BigDecimal acctPercentRate,
                                     BigDecimal acctFixedRate, BigDecimal prodRate) {
        // a. 浮动百分点非零（空值视同 0）：按产品利率加点
        if (nonZero(acctSpreadRate)) {
            return prodRate.add(acctSpreadRate);
        }
        // b. 浮动百分比非零：按比例浮动
        if (nonZero(acctPercentRate)) {
            return prodRate.multiply(BigDecimal.ONE.add(acctPercentRate));
        }
        // c. 固定利率非零：直取账户固定利率，不与产品利率运算
        if (nonZero(acctFixedRate)) {
            return acctFixedRate;
        }
        // d. 默认：返回产品利率
        return prodRate;
    }

    /**
     * 条件判断按数值比较：空值视同 0，条件不成立；任意标度数值零（0、0.00、0.000）同样判为等于 0。
     */
    private static boolean nonZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) != 0;
    }
}
