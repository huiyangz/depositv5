package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;
import com.dcits.depsit.facade.bo.ST091InputBO;
import com.dcits.depsit.facade.bo.ST091OutputBO;

/**
 * ST091 检查客户限制 步骤实现。
 *
 * 业务定义来源：docs/specs/ST091.md。
 * 以输入{客户号}调用本组件内步骤 ST001「检查客户是否存在限制」执行客户限制检查，
 * 获取[客户限制信息]；若该信息为空（未命中生效限制，三字段均为空），检查结果为"通过"，
 * 步骤成功返回，输出字段均为空；否则将[客户限制信息]中的限制编号、账户限制类型、
 * 限制状态赋值到对应输出字段后正常返回，由调用方按输出字段处理限制。
 * 本步骤无业务失败场景，技术异常原样向上传播，不捕获、不转换。
 */
@Service
public class ST091Pbc implements IST091 {

	private final IST001 st001;

	public ST091Pbc(IST001 st001) {
		this.st001 = st001;
	}

	@Override
	public ST091OutputBO execute(ST091InputBO input) {
		ST091OutputBO output = new ST091OutputBO();

		// 步骤描述 第1条：以{客户号}调用组件内步骤 ST001 执行客户限制检查，获取[客户限制信息]
		ST001InputBO st001Input = new ST001InputBO();
		st001Input.setClientNo(input.getClientNo());
		ST001OutputBO restraintInfo = st001.execute(st001Input);

		// 步骤描述 第2条：[客户限制信息]为空（三字段均为空）时检查通过，输出保持空不填默认值；
		// 非空时将限制编号、账户限制类型、限制状态赋值到对应输出字段，枚举常量原样透传
		if (restraintInfo.getResSeqNo() != null || restraintInfo.getRestraintType() != null
				|| restraintInfo.getRestraintsStatus() != null) {
			output.setResSeqNo(restraintInfo.getResSeqNo());
			output.setRestraintType(restraintInfo.getRestraintType());
			output.setRestraintsStatus(restraintInfo.getRestraintsStatus());
		}

		// 空与非空分支均为正常返回，不产生业务失败；命中后的限制处置由调用方按输出字段处理
		output.setSucceed(true);
		return output;
	}
}
