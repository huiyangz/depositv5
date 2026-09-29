package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.TranBranch;
import com.dcits.depsit.facade.bo.ST023InputBO;
import com.dcits.depsit.facade.bo.ST023OutputBO;
import com.dcits.depsit.facade.components.IFmBranchCcyBcc;
import com.dcits.depsit.facade.eo.FmBranchCcyEO;

/**
 * ST023 检查机构币种交易权限。
 *
 * 步骤1 以输入{交易机构号}（tranBranch）为条件查询【机构币种信息】（FM_BRANCH_CCY，
 * 按归属机构号匹配，无其他筛选条件，可能返回零条或多条）得到[机构币种列表]；
 * 步骤2 判定{交易币种}（tranCcy）是否在列表范围内（存在币种等于{交易币种}的记录）：
 * 在范围内时检查结果"通过"（succeed=true，输出 ccy 为命中记录的币种，按主键
 * （归属机构号、币种）至多一条命中，值等于输入{交易币种}）；否则（列表为空或
 * 无记录币种等于{交易币种}）返回[错误码]"ER0047"（succeed=false，ccy 为空）。
 * 只读步骤，无数据写入副作用，无事务要求。
 */
@Service
public class ST023Pbc implements IST023 {

    /** 步骤2 否则分支错误码：交易币种不在机构币种交易权限范围（需求文本常量，无枚举绑定） */
    private static final String ERROR_CODE_CCY_NOT_IN_SCOPE = "ER0047";

    @Autowired
    private IFmBranchCcyBcc fmBranchCcyBcc;

    @Override
    public ST023OutputBO execute(ST023InputBO input) {
        ST023OutputBO output = new ST023OutputBO();

        // 步骤1 获取机构币种列表：以{交易机构号}为唯一查询条件
        List<FmBranchCcyEO> branchCcyList = findBranchCcyList(input.getTranBranch());

        // 步骤2 检查交易币种：在范围内→检查结果"通过"并输出命中记录的币种；否则→返回错误码 ER0047
        if (branchCcyList != null) {
            for (FmBranchCcyEO branchCcy : branchCcyList) {
                if (branchCcy.getCcy() == input.getTranCcy()) {
                    output.setCcy(branchCcy.getCcy());
                    output.setSucceed(true);
                    return output;
                }
            }
        }

        // 否则分支（列表为空，或列表非空但无记录币种等于{交易币种}）：业务失败，ccy 保持 null
        output.setErrorCode(ERROR_CODE_CCY_NOT_IN_SCOPE);
        output.setErrorMessage(ERROR_CODE_CCY_NOT_IN_SCOPE + "::交易币种不在机构币种交易权限范围内");
        return output;
    }

    /**
     * 步骤1 获取机构币种列表：以{交易机构号}（tranBranch）为条件查询 FM_BRANCH_CCY
     * （EO 仅设 branch，无其他筛选条件），可能返回零条或多条记录。
     */
    private List<FmBranchCcyEO> findBranchCcyList(TranBranch tranBranch) {
        FmBranchCcyEO condition = new FmBranchCcyEO();
        condition.setBranch(tranBranch);
        return fmBranchCcyBcc.findByEo(condition);
    }
}
