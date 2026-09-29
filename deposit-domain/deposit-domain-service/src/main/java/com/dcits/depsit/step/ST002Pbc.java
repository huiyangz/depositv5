package com.dcits.depsit.step;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RestraintType;
import com.dcits.depsit.enums.SourceType;
import com.dcits.depsit.enums.Status;
import com.dcits.depsit.facade.bo.ST002InputBO;
import com.dcits.depsit.facade.bo.ST002OutputBO;
import com.dcits.depsit.facade.components.IFmChannelBcc;
import com.dcits.depsit.facade.components.IRbRestraintControlDetailsBcc;
import com.dcits.depsit.facade.eo.FmChannelEO;
import com.dcits.depsit.facade.eo.RbRestraintControlDetailsEO;

/**
 * ST002 检查限制豁免 步骤实现。
 *
 * 依据正式 Spec ST002：
 * 子步骤 1 按 sourceType 查 FM_CHANNEL 取渠道柜面标志（无记录按 "N"）；
 * 子步骤 2 按 restraintType + status=Status.A 查 RB_RESTRAINT_CONTROL_DETAILS 取生效明细集合；
 * 子步骤 3 渠道柜面标志与生效明细柜面标志均为 "Y" 时执行子步骤 4（柜面渠道豁免检查），否则执行子步骤 5（非柜面渠道豁免检查）；
 * 子步骤 4 / 5 按三项同时匹配规则（多交易类型包含交易代码值、摘要码相等、产品类型相等）判定，
 * 分别返回 不检查限制/需检查限制 与 豁免/不豁免，并回填匹配明细字段。
 * 本步骤无业务失败场景（REQ-006），数据服务技术异常原样向上传播。
 */
@Service
public class ST002Pbc implements IST002 {

	/** 柜面标志规范常量：是 */
	private static final String COUNTER_FLAG_YES = "Y";
	/** 柜面标志规范常量：否（FM_CHANNEL 无对应记录时按此处理） */
	private static final String COUNTER_FLAG_NO = "N";
	/** 检查结果规范常量：不检查限制（子步骤 4 三项同时匹配） */
	private static final String CHECK_RESULT_SKIP = "不检查限制";
	/** 检查结果规范常量：需检查限制（子步骤 4 无同时匹配明细） */
	private static final String CHECK_RESULT_NEED = "需检查限制";
	/** 检查结果规范常量：豁免（子步骤 5 三项同时匹配） */
	private static final String CHECK_RESULT_EXEMPT = "豁免";
	/** 检查结果规范常量：不豁免（子步骤 5 无同时匹配明细，含生效明细集合为空） */
	private static final String CHECK_RESULT_NOT_EXEMPT = "不豁免";

	private final IFmChannelBcc fmChannelBcc;
	private final IRbRestraintControlDetailsBcc rbRestraintControlDetailsBcc;

	public ST002Pbc(IFmChannelBcc fmChannelBcc, IRbRestraintControlDetailsBcc rbRestraintControlDetailsBcc) {
		this.fmChannelBcc = fmChannelBcc;
		this.rbRestraintControlDetailsBcc = rbRestraintControlDetailsBcc;
	}

	@Override
	public ST002OutputBO execute(ST002InputBO input) {
		ST002OutputBO output = new ST002OutputBO();
		// 子步骤1 获取渠道柜面标志：FM_CHANNEL 无 sourceType 对应记录时按 "N"（无记录不构成失败）
		String channelCounterFlag = queryChannelCounterFlag(input.getSourceType());
		output.setCounterFlag(channelCounterFlag);
		// 子步骤2 获取生效限制控制明细：restraintType + status=Status.A 等值过滤，空集不构成失败
		List<RbRestraintControlDetailsEO> activeDetails = queryActiveDetails(input.getRestraintType());
		RbRestraintControlDetailsEO matchedDetail;
		if (isRouteToCounterCheck(channelCounterFlag, activeDetails)) {
			// 子步骤4 柜面渠道豁免检查：三项同时匹配返回"不检查限制"并回填明细字段，否则返回"需检查限制"、七个明细字段为空
			matchedDetail = findMatchedDetail(activeDetails, input);
			output.setCheckResult(matchedDetail != null ? CHECK_RESULT_SKIP : CHECK_RESULT_NEED);
		} else {
			// 子步骤5 非柜面渠道豁免检查：三项同时匹配返回"豁免"并回填明细字段，否则（含生效明细集合为空）返回"不豁免"、七个明细字段为空
			matchedDetail = findMatchedDetail(activeDetails, input);
			output.setCheckResult(matchedDetail != null ? CHECK_RESULT_EXEMPT : CHECK_RESULT_NOT_EXEMPT);
		}
		if (matchedDetail != null) {
			fillDetailFields(output, matchedDetail);
		}
		// 本步骤无业务失败场景，完成执行的路径均置成功，错误字段保持 null；数据服务技术异常原样向上传播（REQ-006）
		output.setSucceed(true);
		return output;
	}

	/**
	 * 子步骤1：按渠道类型查询渠道类型表（FM_CHANNEL），取对应记录的柜面标志。
	 *
	 * @param sourceType 渠道类型
	 * @return 记录存在时取表内柜面标志；无对应记录时按 "N"
	 */
	private String queryChannelCounterFlag(SourceType sourceType) {
		FmChannelEO channel = fmChannelBcc.findByChannel(sourceType);
		if (channel == null) {
			return COUNTER_FLAG_NO;
		}
		return channel.getCounterFlag();
	}

	/**
	 * 子步骤2：按账户限制类型查询存款限制检查控制详情表（RB_RESTRAINT_CONTROL_DETAILS），
	 * 取状态为 Status.A 的生效明细集合；查询 EO 仅设置 restraintType 与 status=Status.A（按非空属性等值过滤）。
	 *
	 * @param restraintType 账户限制类型
	 * @return 生效明细集合，无状态为 A 的记录时为空集合
	 */
	private List<RbRestraintControlDetailsEO> queryActiveDetails(RestraintType restraintType) {
		RbRestraintControlDetailsEO condition = new RbRestraintControlDetailsEO();
		condition.setRestraintType(restraintType);
		condition.setStatus(Status.A);
		return rbRestraintControlDetailsBcc.findByEo(condition);
	}

	/**
	 * 子步骤3 路由条件：渠道柜面标志等于 "Y" 且 生效明细的柜面标志等于 "Y" 时走子步骤 4，否则走子步骤 5。
	 * 无生效明细时"生效明细的柜面标志"不存在，条件不成立（口径说明 3）；
	 * 生效明细柜面标志按单一取值口径使用，取首条生效明细的取值（口径说明 2，范围内场景取值一致）。
	 *
	 * @param channelCounterFlag 子步骤1取得的渠道柜面标志
	 * @param activeDetails 子步骤2取得的生效明细集合
	 * @return true 走子步骤 4（柜面渠道豁免检查），false 走子步骤 5（非柜面渠道豁免检查）
	 */
	private boolean isRouteToCounterCheck(String channelCounterFlag, List<RbRestraintControlDetailsEO> activeDetails) {
		if (!COUNTER_FLAG_YES.equals(channelCounterFlag) || activeDetails.isEmpty()) {
			return false;
		}
		return COUNTER_FLAG_YES.equals(activeDetails.get(0).getCounterFlag());
	}

	/**
	 * 子步骤4 / 子步骤5 共用的三项同时匹配规则：多交易类型（tranTypeLink）包含交易类型对应代码值（字符串包含）、
	 * 摘要码按字面相等、产品类型按字面相等；渠道集合等其余明细字段不参与判定。
	 *
	 * @param activeDetails 生效明细集合
	 * @param input 步骤输入
	 * @return 首条同时匹配的明细，无同时匹配明细时返回 null
	 */
	private RbRestraintControlDetailsEO findMatchedDetail(List<RbRestraintControlDetailsEO> activeDetails, ST002InputBO input) {
		String tranTypeValue = input.getTranType().getValue();
		for (RbRestraintControlDetailsEO detail : activeDetails) {
			if (detail.getTranTypeLink().contains(tranTypeValue)
					&& detail.getNarrativeCode().equals(input.getNarrativeCode())
					&& detail.getProdNo().equals(input.getProdNo())) {
				return detail;
			}
		}
		return null;
	}

	/**
	 * 子步骤4 / 子步骤5 共用的匹配明细回填：来源为 RB_RESTRAINT_CONTROL_DETAILS 的七个输出字段取匹配明细对应值。
	 *
	 * @param output 步骤输出
	 * @param detail 匹配明细
	 */
	private void fillDetailFields(ST002OutputBO output, RbRestraintControlDetailsEO detail) {
		output.setStatus(detail.getStatus());
		output.setProdNo(detail.getProdNo());
		output.setTranTypeLink(detail.getTranTypeLink());
		output.setChannelMuster(detail.getChannelMuster());
		output.setNarrativeCode(detail.getNarrativeCode());
		output.setResBranchRange(detail.getResBranchRange());
		output.setDetailCounterFlag(detail.getCounterFlag());
	}
}
