package com.dcits.depsit.step;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST065InputBO;
import com.dcits.depsit.facade.bo.ST065OutputBO;

/**
 * ST065 设置账户开户日期 步骤实现。
 *
 * 业务定义来源：docs/specs/ST065.md。
 * 子步骤 1（获取系统日期）：[系统日期] 取输入 runDate 的赋值，
 * 不另行取运行环境时钟，不访问 FM_DATE 或任何其他数据实体。
 * 子步骤 2（设置账户开户日期）：[账户开户日期] 等于[系统日期]，
 * 作为步骤输出 acctOpenDate 返回；赋值为值传递语义，不做格式转换、
 * 时区处理或精度截断。
 * 本步骤无业务失败场景，唯一业务路径以成功结束；技术异常原样向上传播，
 * 不捕获、不转换。
 */
@Service
public class ST065Pbc implements IST065 {

	@Override
	public ST065OutputBO execute(ST065InputBO input) {
		ST065OutputBO output = new ST065OutputBO();

		// 子步骤1 获取系统日期：[系统日期] 取输入 runDate（核心运行日期）的赋值
		Date systemDate = input.getRunDate();

		// 子步骤2 设置账户开户日期：[账户开户日期] 等于[系统日期]，值传递，作为步骤输出返回
		output.setAcctOpenDate(systemDate);

		// 唯一业务路径以成功结束，不设置业务错误码与错误信息
		output.setSucceed(true);
		return output;
	}
}
