package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DealFlow;
import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.facade.bo.ST089InputBO;
import com.dcits.depsit.facade.bo.ST089OutputBO;
import com.dcits.depsit.facade.bo.ST105InputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;
import com.dcits.depsit.facade.components.ITbVoucherDefBcc;
import com.dcits.depsit.facade.eo.TbVoucherDefEO;

/**
 * ST089 检查存入账户黑名单。
 *
 * 业务定义来源：docs/specs/ST089.md。
 * 子步骤 1 以{凭证类型}查询凭证类型定义表（TB_VOUCHER_DEF，正文称【凭证信息】）取得
 * [凭证种类]；子步骤 2 赋值[事件类型]为常量 "CRET"；子步骤 3 以{账号}、{交易类型}、
 * [事件类型]、{服务代码}、{接口服务代码}、{接口服务类型}、{渠道类型}、[凭证种类]
 * 8 项映射调用组件内步骤 ST105《检查黑名单》取得[执行结果]，取业务结果 dealFlow、
 * 不取调用成功标志；{交易机构}无 ST105InputBO 承接字段，documentId、documentType、
 * programId、blacklistCheckFlag、serviceStatus、acctBranch、clientNo 本步骤无来源，
 * 均不传入（保持 null）。子步骤 4 按[执行结果]四值分支处置：通过（dealFlow=null）、
 * 提醒（DealFlow.D）继续执行正常完成；授权（DealFlow.A）返回授权标志"是"并等待授权
 * 结果后执行后续步骤（授权流程本身不在本步骤范围）；拒绝（DealFlow.B）返回错误码
 * "ER0057" 非成功返回。子步骤 1、2 先于分支判定无条件执行，四个分支 eventType 均为
 * "CRET"、docClass 均为查询所得值。
 * 只读检查步骤：无数据写入副作用、无事务要求；【凭证信息】无匹配记录的分支需求未
 * 定义，不发明处理；技术异常原样向上传播，不捕获、不转换。
 */
@Service
public class ST089Pbc implements IST089 {

    /** 事件类型常量："CRET"（需求文本常量，无枚举绑定） */
    private static final String EVENT_TYPE_CRET = "CRET";

    /** 授权标志常量："是"（需求文本常量，无枚举绑定） */
    private static final String AUTH_FLAG_YES = "是";

    /** 拒绝分支错误码："ER0057"（需求文本常量，无枚举绑定；错误文案需求未定义） */
    private static final String ERROR_CODE_BLACKLIST_REJECT = "ER0057";

    private final ITbVoucherDefBcc tbVoucherDefBcc;

    private final IST105 st105;

    public ST089Pbc(ITbVoucherDefBcc tbVoucherDefBcc, IST105 st105) {
        this.tbVoucherDefBcc = tbVoucherDefBcc;
        this.st105 = st105;
    }

    @Override
    public ST089OutputBO execute(ST089InputBO input) {
        ST089OutputBO output = new ST089OutputBO();

        // 子步骤1：按{凭证类型}查询【凭证信息】（凭证类型定义表 TB_VOUCHER_DEF），取得 $凭证种类$
        TbVoucherDefEO voucherInfo = tbVoucherDefBcc.findByDocType(input.getDocType());
        DocClass docClass = voucherInfo.getDocClass();

        // 子步骤2：赋值[事件类型]为常量 "CRET"（无条件执行，不依赖其他子步骤结果）
        String eventType = EVENT_TYPE_CRET;

        // 子步骤3：组件内步骤调用 ST105《检查黑名单》，按 8 项映射传入参数；
        // {交易机构}及 documentId 等 7 项无承接字段或无来源，不传入（对应字段保持 null）
        ST105InputBO st105Input = new ST105InputBO();
        st105Input.setBaseAcctNo(input.getBaseAcctNo());
        st105Input.setTranType(input.getTranType());
        st105Input.setEventType(eventType);
        st105Input.setServiceCode(input.getServiceCode());
        st105Input.setMessageCode(input.getMessageCode());
        st105Input.setMessageType(input.getMessageType());
        st105Input.setSourceType(input.getSourceType());
        st105Input.setDocClass(docClass);
        ST105OutputBO st105Result = st105.execute(st105Input);

        // 子步骤1、2 产出先于分支判定无条件输出：四个分支 eventType 均为 "CRET"、docClass 均为查询所得值
        output.setEventType(eventType);
        output.setDocClass(docClass);

        // 子步骤4：按[执行结果]（ST105 返回的业务结果 dealFlow，非调用成功标志）分支处置
        DealFlow dealFlow = st105Result.getDealFlow();
        if (dealFlow == DealFlow.A) {
            // 子步骤4b 授权：返回授权标志"是"并等待授权结果后再执行后续步骤，无错误码
            output.setDealFlow(DealFlow.A);
            output.setAuthFlag(AUTH_FLAG_YES);
            output.setSucceed(true);
        } else if (dealFlow == DealFlow.B) {
            // 子步骤4c 拒绝：返回错误码 "ER0057"，非成功返回；authFlag 不赋值
            output.setDealFlow(DealFlow.B);
            output.setSucceed(false);
            output.setErrorCode(ERROR_CODE_BLACKLIST_REJECT);
            output.setErrorMessage(ERROR_CODE_BLACKLIST_REJECT + "::执行结果为拒绝");
        } else if (dealFlow == DealFlow.D) {
            // 子步骤4a 提醒：继续执行，本步骤正常完成；无授权标志、无错误码
            output.setDealFlow(DealFlow.D);
            output.setSucceed(true);
        } else {
            // 子步骤4a 通过（dealFlow=null）：继续执行，本步骤正常完成；dealFlow 不赋值
            output.setSucceed(true);
        }
        return output;
    }
}
