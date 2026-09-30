package com.dcits.depsit.step;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcits.depsit.enums.CheckObjType;
import com.dcits.depsit.enums.TermType;
import com.dcits.depsit.facade.bo.ST102InputBO;
import com.dcits.depsit.facade.bo.ST102OutputBO;
import com.dcits.depsit.facade.components.IRbLimitCtrlConfBcc;
import com.dcits.depsit.facade.components.IRbLimitSceneDefBcc;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.eo.RbLimitCtrlConfEO;
import com.dcits.depsit.facade.eo.RbLimitSceneDefEO;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;

/**
 * ST102 登记累计限额 步骤实现。
 *
 * 业务定义来源：docs/specs/ST102.md。执行路径：
 * REQ-001 登记条件判定（纯输入判定）——[限额检查结果]为"未超限"且（[限额累计金额]
 * 数值上等于 0 或[限额累计笔数]等于 0）时登记，任一不满足时不写入任何记录、
 * 正常返回且六输出均为 null；
 * REQ-002 以[限额场景编码]主键查询【限额场景定义】取得限额检查对象类型，
 * ACCT→$限额检查对象值$={账号}、CUST→{客户号}（CARD/CDGROUP 取值需求未写明，
 * 已按「已接受的需求处理结论」豁免，不为其发明取值）；
 * REQ-004 以[限额场景编码]等值查询【限额控制配置】取得周期类型与周期值，
 * $失效日期$＝{核心运行日期}＋周期值×周期单位（D→日、W→周（7 日）、M→月、
 * Q→季（3 月）、H→半年（6 月）、Y→年，按日历日期计算）；
 * REQ-003 向【限额累计信息表】新增一条记录（仅新增，不更新、不删除）：
 * $限额场景编码$=[限额场景编码]、$限额检查对象值$按 REQ-002、$限额累计金额$={交易金额}
 * 原样赋值、$限额累计笔数$=1、$生效日期$={核心运行日期}、$失效日期$按 REQ-004；
 * 其余字段按系统规则自动生成（需求未定义具体规则，不在此补默认值），输出回显六项登记值。
 * REQ-005 本步骤无业务失败场景，技术异常原样向上传播，不捕获、不转换。
 */
@Service
public class ST102Pbc implements IST102 {

	/** 限额检查结果常量："未超限"（需求正文常量，无枚举绑定） */
	private static final String LIMIT_CHECK_RESULT_PASS = "未超限";

	/** 登记记录的限额累计笔数恒为 1（需求正文常量） */
	private static final Integer REGISTER_SUM_CNT = Integer.valueOf(1);

	/** 限额场景定义数据服务 */
	@Autowired
	private IRbLimitSceneDefBcc rbLimitSceneDefBcc;

	/** 限额控制配置数据服务 */
	@Autowired
	private IRbLimitCtrlConfBcc rbLimitCtrlConfBcc;

	/** 限额累计信息表数据服务 */
	@Autowired
	private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

	/**
	 * 涉及本地数据库新增（RB_LIMIT_SUM_INFO 登记写入），方法须在事务中执行；
	 * 事务边界与提交管理由调用方与运行平台事务管理决定（Spec「明确不覆盖」第 6 项）。
	 */
	@Override
	@Transactional
	public ST102OutputBO execute(ST102InputBO input) {
		ST102OutputBO output = new ST102OutputBO();

		// REQ-001 登记条件判定：[限额检查结果]为"未超限"，且（[限额累计金额]等于 0 或[限额累计笔数]等于 0）
		if (!shouldRegister(input)) {
			// 条件任一不满足：不写入任何记录（无新增、无更新、无删除），正常返回，六输出均为 null
			output.setSucceed(true);
			return output;
		}

		// REQ-002 限额检查对象值取值：按[限额场景编码]查【限额场景定义】取得的检查对象类型映射
		String checkObjVal = resolveCheckObjVal(input);

		// REQ-004 失效日期计算：按[限额场景编码]查【限额控制配置】取得的周期类型与周期值加算
		Date expireDate = calcExpireDate(input.getLimitSceneNo(), input.getRunDate());

		// REQ-003 登记写入：向【限额累计信息表】新增一条记录（仅新增，不更新、不删除已有记录）
		RbLimitSumInfoEO registerEo = buildRegisterEo(input, checkObjVal, expireDate);
		rbLimitSumInfoBcc.create(registerEo);

		// 输出回显本次登记写入的六个字段值；无业务失败场景，成功结束时错误字段保持 null
		fillRegisteredOutput(output, registerEo);
		output.setSucceed(true);
		return output;
	}

	/**
	 * REQ-001 登记条件判定（纯输入判定）：[限额检查结果]与"未超限"做 String 等值比较，
	 * 且[限额累计金额]数值上等于 0（BigDecimal.compareTo，0 与 0.00 均视为等于 0）
	 * 或[限额累计笔数]等于 0（Integer 等值），两者满足其一即登记。
	 */
	private boolean shouldRegister(ST102InputBO input) {
		return LIMIT_CHECK_RESULT_PASS.equals(input.getLimitCheckResult())
				&& (isZeroAmount(input.getLimitSumAmt())
						|| Integer.valueOf(0).equals(input.getLimitSumCnt()));
	}

	/**
	 * [限额累计金额]等于 0 的判定口径：数值上等于 0（compareTo==0），
	 * BigDecimal 的 0 与 0.00 标度差异不影响判定结果。
	 */
	private boolean isZeroAmount(BigDecimal amount) {
		return amount != null && amount.compareTo(BigDecimal.ZERO) == 0;
	}

	/**
	 * REQ-002 限额检查对象值取值：以输入[限额场景编码]按主键等值查询【限额场景定义】
	 * （至多一条）取得$限额检查对象类型$，为"账户级别（ACCT）"时取输入{账号}，
	 * 为"客户级别（CUST）"时取输入{客户号}。对象类型为 CARD、CDGROUP（或定义表
	 * 无记录、类型为空）时的取值需求未写明（已按「已接受的需求处理结论」豁免，
	 * Spec 不纳入验收），本实现不为其发明取值，保持 null。
	 */
	private String resolveCheckObjVal(ST102InputBO input) {
		RbLimitSceneDefEO sceneDef = rbLimitSceneDefBcc.findByPrimaryKey(input.getLimitSceneNo());
		CheckObjType checkObjType = sceneDef == null ? null : sceneDef.getCheckObjType();
		if (checkObjType == CheckObjType.ACCT) {
			return input.getBaseAcctNo();
		}
		if (checkObjType == CheckObjType.CUST) {
			return input.getClientNo();
		}
		return null;
	}

	/**
	 * REQ-004 失效日期计算：以输入[限额场景编码]等值查询【限额控制配置】取得
	 * $周期类型$与$周期值$，$失效日期$＝{核心运行日期}＋周期值×周期单位，
	 * 周期单位由 TermType 定义（D→日、W→周（7 日）、M→月、Q→季（3 月）、
	 * H→半年（6 月）、Y→年），按日历日期加算。同场景编码多条配置的选取规则
	 * 需求未定义（Spec「明确不覆盖」第 3 项），验收以单条有效配置为前提，
	 * 按单条书写口径取首条；无配置或周期字段无效时的处理需求未定义，
	 * 由此产生的技术异常按 REQ-005 原样传播。
	 */
	private Date calcExpireDate(String limitSceneNo, Date runDate) {
		RbLimitCtrlConfEO condition = new RbLimitCtrlConfEO();
		condition.setLimitSceneNo(limitSceneNo);
		List<RbLimitCtrlConfEO> confs = rbLimitCtrlConfBcc.findByEo(condition);
		RbLimitCtrlConfEO conf = confs.get(0);

		int periodValue = Integer.parseInt(conf.getPeriodValue());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(runDate);
		TermType periodType = conf.getPeriodType();
		switch (periodType) {
		case D:
			calendar.add(Calendar.DAY_OF_MONTH, periodValue);
			break;
		case W:
			calendar.add(Calendar.DAY_OF_MONTH, periodValue * 7);
			break;
		case M:
			calendar.add(Calendar.MONTH, periodValue);
			break;
		case Q:
			calendar.add(Calendar.MONTH, periodValue * 3);
			break;
		case H:
			calendar.add(Calendar.MONTH, periodValue * 6);
			break;
		case Y:
			calendar.add(Calendar.YEAR, periodValue);
			break;
		default:
			break;
		}
		return calendar.getTime();
	}

	/**
	 * REQ-003 构造登记记录（仅新增）：六项显式赋值为 $限额场景编码$＝输入[限额场景编码]、
	 * $限额检查对象值$按 REQ-002、$限额累计金额$＝输入{交易金额}（原样赋值，不计算、
	 * 不舍入）、$限额累计笔数$＝1、$生效日期$＝输入{核心运行日期}、$失效日期$按 REQ-004。
	 * 其余字段（限额累计描述、客户号、原交易参考号、交易币种、交易参考号、创建/修改
	 * 时间戳）按系统规则自动生成：需求未定义具体规则（Spec 技术约束、不作断言），
	 * 不在此补默认值。
	 */
	private RbLimitSumInfoEO buildRegisterEo(ST102InputBO input, String checkObjVal, Date expireDate) {
		RbLimitSumInfoEO eo = new RbLimitSumInfoEO();
		eo.setLimitSceneNo(input.getLimitSceneNo());
		eo.setCheckObjVal(checkObjVal);
		eo.setLimitSumAmt(input.getTranAmt());
		eo.set否(REGISTER_SUM_CNT);
		eo.setEffectDate(input.getRunDate());
		eo.setExpireDate(expireDate);
		return eo;
	}

	/**
	 * REQ-003 输出回显：六个输出字段分别等于本次登记写入记录的对应取值。
	 */
	private void fillRegisteredOutput(ST102OutputBO output, RbLimitSumInfoEO registerEo) {
		output.setLimitSceneNo(registerEo.getLimitSceneNo());
		output.setCheckObjVal(registerEo.getCheckObjVal());
		output.setLimitSumAmt(registerEo.getLimitSumAmt());
		output.setLimitSumCnt(registerEo.get否());
		output.setEffectDate(registerEo.getEffectDate());
		output.setExpireDate(registerEo.getExpireDate());
	}
}
