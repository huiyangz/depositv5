package com.dcits.depsit.step;

import com.dcits.depsit.facade.bo.ST065InputBO;
import com.dcits.depsit.facade.bo.ST065OutputBO;

/**
 * ST065 设置账户开户日期 步骤接口。
 *
 * 将输入的核心运行日期（runDate，值来源于系统日期表 FM_DATE）赋值为[系统日期]，
 * 再将[账户开户日期]赋值为[系统日期]，作为步骤输出 acctOpenDate 返回。
 * 本步骤为纯赋值步骤：无数据库读写、无外部服务调用、无状态变更，
 * 无业务失败场景；技术异常向上传播，由上层统一处理，无事务要求。
 */
public interface IST065 {

	/**
	 * 执行 ST065 设置账户开户日期。
	 *
	 * @param input 步骤输入，runDate 必填（必填性由上送方保证）
	 * @return 步骤输出：acctOpenDate 与输入 runDate 值相同（值传递，无格式转换），succeed=true
	 */
	ST065OutputBO execute(ST065InputBO input);
}
