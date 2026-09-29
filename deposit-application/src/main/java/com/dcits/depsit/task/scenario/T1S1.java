package com.dcits.depsit.task.scenario;

import org.springframework.stereotype.Component;

import com.dcits.common.task.RespHeader;
import com.dcits.depsit.facade.bo.ST001InputBO;
import com.dcits.depsit.facade.bo.ST001OutputBO;
import com.dcits.depsit.step.IST001;
import com.dcits.depsit.task.dto.T1S1InputDTO;
import com.dcits.depsit.task.dto.T1S1OutputDTO;

/**
 * T1S1 检查客户限制 交易场景。
 *
 * 业务契约来源：docs/specs/T1S1.md。按交易输入 clientNo 编排组件内步骤
 * ST001《检查客户是否存在限制》，将限制编号、账户限制类型、限制状态映射为
 * 交易输出。本交易无业务失败场景（ST001 亦无业务失败），失败仅由技术异常
 * 向上传播表达，本场景不捕获、不转换；ST001 仅只读查询、无事务要求。
 */
@Component
public class T1S1 {

	private final IST001 st001;

	public T1S1(IST001 st001) {
		this.st001 = st001;
	}

	/**
	 * 执行 T1S1 检查客户限制。
	 *
	 * @param header 调用方传入的响应头，成功时显式置成功并清理旧错误字段
	 * @param input 交易输入，clientNo 必填（必填性由上送方保证）
	 * @return 交易输出：命中生效记录时为选中记录三字段；未命中时三字段均为 null
	 */
	public T1S1OutputDTO execute(RespHeader header, T1S1InputDTO input) {
		T1S1OutputDTO output = new T1S1OutputDTO();

		// 执行步骤序号 1（唯一步骤）：ST001 检查客户是否存在限制，clientNo 原样传递
		ST001InputBO st001Input = new ST001InputBO();
		st001Input.setClientNo(input.getClientNo());
		ST001OutputBO st001Output = st001.execute(st001Input);
		if (!st001Output.isSucceed()) {
			handleError(header, st001Output.getErrorCode(), st001Output.getErrorMessage());
			return output;
		}

		output.setResSeqNo(st001Output.getResSeqNo());
		if (st001Output.getRestraintType() != null) {
			output.setRestraintType(st001Output.getRestraintType().getValue());
		}
		if (st001Output.getRestraintsStatus() != null) {
			output.setRestraintsStatus(st001Output.getRestraintsStatus().getValue());
		}

		header.setSucceed(true);
		header.setErrorCode(null);
		header.setErrorMessage(null);
		return output;
	}

	/**
	 * 设置失败响应头：原样传递步骤输出携带的错误码与错误信息。
	 * 本交易无已定义业务失败场景与错误码键，不查询错误资源，不自造文案。
	 */
	private void handleError(RespHeader header, String errorCode, String errorMessage) {
		header.setSucceed(false);
		header.setErrorCode(errorCode);
		header.setErrorMessage(errorMessage);
	}
}
