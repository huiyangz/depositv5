package com.dcits.depsit.rule;

/**
 * BR003 检查允许转久悬标志（断言类规则）。
 *
 * 依据输入字段「允许账户转久悬标志」（allowSuspendFlag）与「是否允许转久悬」（allowDormantFlag）
 * 判定是否允许转久悬，输出二值断言结果「是」（true）或「否」（false）：
 * 条件 a：allowSuspendFlag 等于 "Y" 且 allowDormantFlag 等于 "N" 时，返回「否」；
 * 条件 b：allowSuspendFlag 等于 "N" 且 allowDormantFlag 等于空（null，未传入）时，返回「否」；
 * 条件 c：不满足条件 a 且不满足条件 b 的其余全部输入组合，返回「是」。
 * 空值口径：「空」仅指 null（未传入该字段）；空字符串 "" 是已传入取值，落入条件 c。
 * 比较语义：均按 java.lang.String 字面量精确相等。
 */
public class BR003 {

	/**
	 * 允许转久悬断言判定。
	 *
	 * @param allowSuspendFlag 允许账户转久悬标志，比较字面量为 "Y"、"N"
	 * @param allowDormantFlag 是否允许转久悬，非必填，「空」= null，比较字面量为 "N"
	 * @return 「是」（true）或「否」（false），每次判定有且仅有一个返回值
	 */
	public static boolean execute(String allowSuspendFlag, String allowDormantFlag) {
		// 条件 a：账户允许转久悬且显式不允许转久悬，返回「否」
		if ("Y".equals(allowSuspendFlag) && "N".equals(allowDormantFlag)) {
			return false;
		}
		// 条件 b：账户不允许转久悬且是否允许转久悬未传入（「空」= null），返回「否」
		if ("N".equals(allowSuspendFlag) && allowDormantFlag == null) {
			return false;
		}
		// 条件 c：其余全部输入组合，返回「是」
		return true;
	}
}
