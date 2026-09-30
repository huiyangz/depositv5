package com.dcits.depsit.step;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.DocClass;
import com.dcits.depsit.enums.RcBlackStatus;
import com.dcits.depsit.enums.ResBranchRange;
import com.dcits.depsit.enums.ResOperateFlag;
import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST105InputBO;
import com.dcits.depsit.facade.bo.ST105OutputBO;
import com.dcits.depsit.facade.components.IFmBranchBcc;
import com.dcits.depsit.facade.components.IFmServiceDefineBcc;
import com.dcits.depsit.facade.components.IRbBusAcctBcc;
import com.dcits.depsit.facade.components.IRcAllListBcc;
import com.dcits.depsit.facade.components.IRcListCheckRangeBcc;
import com.dcits.depsit.facade.components.IRcListNotCheckRangeBcc;
import com.dcits.depsit.facade.components.IRcListTypeBcc;
import com.dcits.depsit.facade.components.IRcRuleTypeBcc;
import com.dcits.depsit.facade.eo.FmBranchEO;
import com.dcits.depsit.facade.eo.FmServiceDefineEO;
import com.dcits.depsit.facade.eo.RbBusAcctEO;
import com.dcits.depsit.facade.eo.RcAllListEO;
import com.dcits.depsit.facade.eo.RcListCheckRangeEO;
import com.dcits.depsit.facade.eo.RcListNotCheckRangeEO;
import com.dcits.depsit.facade.eo.RcListTypeEO;
import com.dcits.depsit.facade.eo.RcRuleTypeEO;

/**
 * ST105 检查黑名单。
 *
 * 子步骤 1–16 逐层判定，任一层未命中即返回检查结果"通过"（dealFlow=null）：
 * 子步骤 1 以{接口服务代码}+{接口服务类型}查【核心服务定义表】取[服务信息列表]；
 * 子步骤 2 存在 服务状态="A" 且 黑名单检查标志="Y" 的记录（同一记录同时满足）时继续，
 * 否则"通过"（判定以查询结果为准，输入字段不参与，契约注记 2）；
 * 子步骤 3 以{客户号}查【名单信息表】取黑名单状态"生效"记录的名单类型代码（RC_ALL_LIST
 * 无证件号码、账号列，{证件号码}/{账号}两条件无表列可映射，按已接受结论仅以{客户号}
 * 构造查询条件）；子步骤 4 [黑名单信息]为空时"通过"；
 * 子步骤 5 以名单类型代码查【名单类型表】取黑名单检查规则编号，子步骤 6 编号为空时"通过"；
 * 子步骤 7 以规则编号查【名单限制规则表】取[名单限制规则信息]（黑名单限制操作标识、介质、
 * 限制机构范围），子步骤 8 标识不等于"E"（含未查询到规则信息）时"通过"；
 * 子步骤 9 以七条件查【名单检查范围表】取所有匹配记录的事件类型构成[事件类型]，子步骤 10
 * 以六条件查【名单不检查范围表】且匹配记录的事件类型属于[事件类型]，子步骤 11 命中时"通过"；
 * 子步骤 12 {凭证种类}为空或不在介质范围内时"通过"（介质范围承载格式需求未定义，按凭证
 * 种类代码是否包含于介质范围字符串判定）；
 * 子步骤 13 以{账号}查【账户信息】取[账户开立行行号]（以查询结果为判定依据，契约注记 3），
 * 子步骤 14 限制机构范围等于"B"且账户开立行等于当前{交易机构}或属其下级机构（经机构信息表
 * 上级机构逐级上溯）时继续，否则"通过"（范围仅 B 有定义，其余取值一律"否则"，已接受结论 3）；
 * 子步骤 15 再以规则编号查【名单限制规则表】取处理方式，子步骤 16 拒绝/授权/提醒分别输出
 * dealFlow=DealFlow.B/A/D。
 *
 * 当前{交易机构}需求未声明数据来源（契约注记 1、不覆盖事项 6），本实现取输入 acctBranch
 * 字段值；机构层级关系经机构信息表（FM_BRANCH）上级机构字段取得。
 * 只读步骤：无数据写入副作用、无业务失败场景（失败仅由技术异常传播），无事务要求。
 */
@Service
public class ST105Pbc implements IST105 {

    /** 服务状态取值："A"-生效（需求文本常量，无枚举绑定） */
    private static final String SERVICE_STATUS_ACTIVE = "A";

    /** 黑名单检查标志取值："Y"-是（需求文本常量，无枚举绑定） */
    private static final String BLACKLIST_CHECK_FLAG_YES = "Y";

    @Autowired
    private IFmServiceDefineBcc fmServiceDefineBcc;

    @Autowired
    private IRcAllListBcc rcAllListBcc;

    @Autowired
    private IRcListTypeBcc rcListTypeBcc;

    @Autowired
    private IRcRuleTypeBcc rcRuleTypeBcc;

    @Autowired
    private IRcListCheckRangeBcc rcListCheckRangeBcc;

    @Autowired
    private IRcListNotCheckRangeBcc rcListNotCheckRangeBcc;

    @Autowired
    private IRbBusAcctBcc rbBusAcctBcc;

    @Autowired
    private IFmBranchBcc fmBranchBcc;

    @Override
    public ST105OutputBO execute(ST105InputBO input) {
        ST105OutputBO output = new ST105OutputBO();

        // 子步骤1 获取黑名单检查标志与服务状态：以{接口服务代码}+{接口服务类型}查【核心服务定义表】
        List<FmServiceDefineEO> serviceDefines =
                findServiceDefines(input.getMessageCode(), input.getMessageType());
        // 子步骤2 检查接口是否允许检查黑名单：存在同一记录 服务状态="A" 且 黑名单检查标志="Y" 时跳转获取黑名单信息，否则"通过"
        if (!hasBlacklistCheckEnabled(serviceDefines)) {
            return pass(output);
        }

        // 子步骤3 获取黑名单信息：查【名单信息表】取黑名单状态"生效"记录的名单类型代码
        String listType = findEffectiveListType(input.getClientNo());
        // 子步骤4 检查是否存在黑名单：[黑名单信息]为空时"通过"，子步骤5–16不执行
        if (listType == null || listType.isEmpty()) {
            return pass(output);
        }

        // 子步骤5 获取黑名单检查规则编号：以名单类型代码查【名单类型表】
        String ruleId = findRuleId(listType);
        // 子步骤6 检查黑名单检查规则编号：编号为空（无记录或记录编号为空）时"通过"，子步骤7–16不执行
        if (ruleId == null || ruleId.isEmpty()) {
            return pass(output);
        }

        // 子步骤7 获取黑名单限制操作标识：以规则编号查【名单限制规则表】取[名单限制规则信息]
        RcRuleTypeEO ruleType = findRuleType(ruleId);
        // 子步骤8 检查黑名单是否是检查类：黑名单限制操作标识等于"E"时跳转获取事件类型，否则（含未取得规则信息）"通过"
        if (ruleType == null || ruleType.getResOperateFlag() != ResOperateFlag.E) {
            return pass(output);
        }

        // 子步骤9 获取事件类型：以七条件查【名单检查范围表】，所有匹配记录的事件类型构成[事件类型]
        Set<String> eventTypes = findCheckRangeEventTypes(input);
        // 子步骤10 获取名单不检查范围列表：以六条件查【名单不检查范围表】，匹配记录的事件类型须属于[事件类型]
        List<RcListNotCheckRangeEO> notCheckRanges = findNotCheckRanges(input, eventTypes);
        // 子步骤11 检查名单不检查范围：[名单不检查范围信息]不为空时"通过"（子步骤12–16不执行），否则继续
        if (!notCheckRanges.isEmpty()) {
            return pass(output);
        }

        // 子步骤12 检查介质：{凭证种类}为空或不在介质范围内时"通过"（子步骤13–16不执行），否则继续
        if (!isDocClassInMediumRange(input.getDocClass(), ruleType.getCardMedium())) {
            return pass(output);
        }

        // 子步骤13 获取账户开立行行号：以{账号}查【账户信息】
        TranBranch acctBranch = findAcctBranch(input.getBaseAcctNo());
        // 子步骤14 检查机构：限制机构范围="B"且账户开立行等于当前{交易机构}或属其下级机构时继续，否则"通过"
        if (!isBranchInRange(ruleType.getResBranchRange(), acctBranch, input.getAcctBranch())) {
            return pass(output);
        }

        // 子步骤15 获取黑名单处理方式：与子步骤7同一查询条件，再以规则编号查【名单限制规则表】
        RcRuleTypeEO dealRuleType = findRuleType(ruleId);
        // 子步骤16 检查处理方式：拒绝/授权/提醒分别输出 dealFlow=DealFlow.B/A/D（记录取值即枚举，映射为同一取值）
        if (dealRuleType != null) {
            output.setDealFlow(dealRuleType.getDealFlow());
        }
        output.setSucceed(true);
        return output;
    }

    /**
     * 检查结果"通过"：dealFlow 保持空（null，不赋值），正常提前结束。
     */
    private ST105OutputBO pass(ST105OutputBO output) {
        output.setSucceed(true);
        return output;
    }

    /**
     * 子步骤1 获取黑名单检查标志与服务状态：以{接口服务代码}+{接口服务类型}为条件查询
     * FM_SERVICE_DEFINE，[服务信息列表]可能多条。
     */
    private List<FmServiceDefineEO> findServiceDefines(String messageCode, String messageType) {
        FmServiceDefineEO condition = new FmServiceDefineEO();
        condition.setMessageCode(messageCode);
        condition.setMessageType(messageType);
        return fmServiceDefineBcc.findByEo(condition);
    }

    /**
     * 子步骤2 检查接口是否允许检查黑名单：[服务信息列表]中存在同一记录
     * 服务状态="A-生效" 且 黑名单检查标志="Y-是" 时判定成立。
     */
    private boolean hasBlacklistCheckEnabled(List<FmServiceDefineEO> serviceDefines) {
        if (serviceDefines == null) {
            return false;
        }
        for (FmServiceDefineEO serviceDefine : serviceDefines) {
            if (SERVICE_STATUS_ACTIVE.equals(serviceDefine.getServiceStatus())
                    && BLACKLIST_CHECK_FLAG_YES.equals(serviceDefine.getBlacklistCheckFlag())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 子步骤3、4 获取黑名单信息：以{客户号}为条件查询 RC_ALL_LIST（{证件号码}、{账号}两条件
     * 无表列可映射，按已接受结论仅以{客户号}构造条件），取黑名单状态等于"生效"（RcBlackStatus.A）
     * 记录的名单类型代码；按正文单条书写口径取首条生效记录，无生效记录或名单类型代码为空时
     * [黑名单信息]为空。
     */
    private String findEffectiveListType(String clientNo) {
        RcAllListEO condition = new RcAllListEO();
        condition.setClientNo(clientNo);
        List<RcAllListEO> blackLists = rcAllListBcc.findByEo(condition);
        if (blackLists == null) {
            return null;
        }
        for (RcAllListEO blackList : blackLists) {
            if (blackList.getRcBlackStatus() == RcBlackStatus.A
                    && blackList.getListType() != null && !blackList.getListType().isEmpty()) {
                return blackList.getListType();
            }
        }
        return null;
    }

    /**
     * 子步骤5 获取黑名单检查规则编号：以[黑名单信息]的名单类型代码查询 RC_LIST_TYPE，
     * 取记录的黑名单检查规则编号；按单条书写口径取首条，无记录时为空。
     */
    private String findRuleId(String listType) {
        RcListTypeEO condition = new RcListTypeEO();
        condition.setListType(listType);
        List<RcListTypeEO> listTypes = rcListTypeBcc.findByEo(condition);
        if (listTypes == null || listTypes.isEmpty()) {
            return null;
        }
        return listTypes.get(0).getRuleId();
    }

    /**
     * 子步骤7、15 获取名单限制规则信息：以黑名单检查规则编号查询 RC_RULE_TYPE，
     * 按单条书写口径取首条，无记录时返回 null。
     */
    private RcRuleTypeEO findRuleType(String ruleId) {
        RcRuleTypeEO condition = new RcRuleTypeEO();
        condition.setRuleId(ruleId);
        List<RcRuleTypeEO> ruleTypes = rcRuleTypeBcc.findByEo(condition);
        if (ruleTypes == null || ruleTypes.isEmpty()) {
            return null;
        }
        return ruleTypes.get(0);
    }

    /**
     * 子步骤9 获取事件类型：以 {事件类型}+{交易类型}+{渠道类型}+{交易码}+{服务代码}+
     * {接口服务类型}+{接口服务代码} 七条件查询 RC_LIST_CHECK_RANGE，
     * 所有匹配记录的事件类型构成[事件类型]；无匹配记录时为空集合。
     */
    private Set<String> findCheckRangeEventTypes(ST105InputBO input) {
        RcListCheckRangeEO condition = new RcListCheckRangeEO();
        condition.setEventType(input.getEventType());
        condition.setTranType(input.getTranType());
        condition.setSourceType(input.getSourceType());
        condition.setProgramId(input.getProgramId());
        condition.setServiceCode(input.getServiceCode());
        condition.setMessageType(input.getMessageType());
        condition.setMessageCode(input.getMessageCode());
        List<RcListCheckRangeEO> checkRanges = rcListCheckRangeBcc.findByEo(condition);

        Set<String> eventTypes = new LinkedHashSet<>();
        if (checkRanges != null) {
            for (RcListCheckRangeEO checkRange : checkRanges) {
                if (checkRange.getEventType() != null) {
                    eventTypes.add(checkRange.getEventType());
                }
            }
        }
        return eventTypes;
    }

    /**
     * 子步骤10 获取名单不检查范围列表：以 {交易类型}+{渠道类型}+{交易码}+{服务代码}+
     * {接口服务类型}+{接口服务代码} 六条件查询 RC_LIST_NOT_CHECK_RANGE，
     * 匹配记录的事件类型须属于子步骤 9 的[事件类型]；[事件类型]为空集合时无可匹配记录，
     * [名单不检查范围信息]为空。
     */
    private List<RcListNotCheckRangeEO> findNotCheckRanges(ST105InputBO input, Set<String> eventTypes) {
        RcListNotCheckRangeEO condition = new RcListNotCheckRangeEO();
        condition.setTranType(input.getTranType());
        condition.setSourceType(input.getSourceType());
        condition.setProgramId(input.getProgramId());
        condition.setServiceCode(input.getServiceCode());
        condition.setMessageType(input.getMessageType());
        condition.setMessageCode(input.getMessageCode());
        List<RcListNotCheckRangeEO> notCheckRanges = rcListNotCheckRangeBcc.findByEo(condition);

        List<RcListNotCheckRangeEO> matched = new ArrayList<>();
        if (notCheckRanges != null) {
            for (RcListNotCheckRangeEO notCheckRange : notCheckRanges) {
                if (notCheckRange.getEventType() != null && eventTypes.contains(notCheckRange.getEventType())) {
                    matched.add(notCheckRange);
                }
            }
        }
        return matched;
    }

    /**
     * 子步骤12 检查介质：{凭证种类}非空且属于[名单限制规则信息]的介质范围时通过。
     * 介质范围的物理承载格式需求未定义（不覆盖事项 7），以凭证种类代码是否包含于
     * 介质范围字符串作为"是否属于介质范围"的业务判定。
     */
    private boolean isDocClassInMediumRange(DocClass docClass, String cardMedium) {
        if (docClass == null || cardMedium == null || cardMedium.isEmpty()) {
            return false;
        }
        return cardMedium.contains(docClass.getValue());
    }

    /**
     * 子步骤13 获取账户开立行行号：以{账号}为条件查询 RB_BUS_ACCT，
     * 取记录的账户开立行行号；按单条书写口径取首条，无记录时为 null。
     */
    private TranBranch findAcctBranch(String baseAcctNo) {
        RbBusAcctEO condition = new RbBusAcctEO();
        condition.setBaseAcctNo(baseAcctNo);
        List<RbBusAcctEO> accounts = rbBusAcctBcc.findByEo(condition);
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        return accounts.get(0).getAcctBranch();
    }

    /**
     * 子步骤14 检查机构：限制机构范围等于"B-下级机构"（ResBranchRange.B，仅 B 有定义，
     * 其余取值一律落入"否则"）且[账户开立行行号]等于当前{交易机构}或属于其下级机构时通过。
     * 下级机构关系经机构信息表（FM_BRANCH）上级机构字段逐级上溯判定：等于本机构直接命中，
     * 否则沿上级机构链上溯至{交易机构}即属于下级机构，上溯到链尾（无机构信息或无上级机构）
     * 仍未命中则不属于。
     */
    private boolean isBranchInRange(ResBranchRange resBranchRange, TranBranch acctBranch, TranBranch tranBranch) {
        if (resBranchRange != ResBranchRange.B) {
            return false;
        }
        TranBranch current = acctBranch;
        while (current != null) {
            if (current == tranBranch) {
                return true;
            }
            FmBranchEO branch = fmBranchBcc.findByBranch(current);
            current = branch == null ? null : branch.getAttachedTo();
        }
        return false;
    }
}
