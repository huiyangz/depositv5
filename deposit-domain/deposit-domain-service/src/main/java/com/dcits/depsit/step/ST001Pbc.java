package com.dcits.depsit.step;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.RestraintsStatus;
import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;
import com.dcits.depsit.facade.components.IRbClientRestraintsBcc;
import com.dcits.depsit.facade.eo.RbClientRestraintsEO;

/**
 * ST001 检查客户是否存在限制 步骤实现。
 *
 * 业务定义来源：docs/specs/ST001.md。
 * 按输入客户号等值查询【客户限制表】（RB_CLIENT_RESTRAINTS），
 * 仅选取限制状态为 "A-生效" 的记录；命中多条时按创建时间戳从新到旧取第一条，
 * 将限制编号、账户限制类型、限制状态赋值到步骤输出返回；
 * 未命中生效记录时正常返回空输出（三字段为 null，不填充默认值）。
 * 本步骤无业务失败场景，技术异常原样向上传播，不捕获、不转换。
 */
@Service
public class ST001Pbc implements IST001 {

	private final IRbClientRestraintsBcc rbClientRestraintsBcc;

	public ST001Pbc(IRbClientRestraintsBcc rbClientRestraintsBcc) {
		this.rbClientRestraintsBcc = rbClientRestraintsBcc;
	}

	@Override
	public ST001OutputBO execute(ST001InputBO input) {
		ST001OutputBO output = new ST001OutputBO();

		// 步骤描述 第1条：按{客户号}等值查询【客户限制表】
		RbClientRestraintsEO queryEo = new RbClientRestraintsEO();
		queryEo.setClientNo(input.getClientNo());
		List<RbClientRestraintsEO> records = rbClientRestraintsBcc.findByEo(queryEo);

		// 步骤描述 第1条：仅选取$限制状态$为 "A-生效" 的记录；命中多条时按$创建时间戳$从新到旧取第一条
		RbClientRestraintsEO selected = null;
		if (records != null && !records.isEmpty()) {
			selected = records.stream()
					.filter(record -> RestraintsStatus.A.equals(record.getRestraintsStatus()))
					.max(Comparator.comparing(RbClientRestraintsEO::getCreateTimestamp))
					.orElse(null);
		}

		// 步骤描述 第2条：将选中记录的$限制编号$、$账户限制类型$、$限制状态$赋值到步骤输出；未命中时三字段保持 null
		if (selected != null) {
			output.setResSeqNo(selected.getResSeqNo());
			output.setRestraintType(selected.getRestraintType());
			output.setRestraintsStatus(selected.getRestraintsStatus());
		}

		// 未命中不属于业务失败：正常返回空输出
		output.setSucceed(true);
		return output;
	}
}
