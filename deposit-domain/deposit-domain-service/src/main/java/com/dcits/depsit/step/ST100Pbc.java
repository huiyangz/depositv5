package com.dcits.depsit.step;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dcits.depsit.facade.bo.ST100InputBO;
import com.dcits.depsit.facade.bo.ST100OutputBO;
import com.dcits.depsit.facade.components.IRbLimitSumInfoBcc;
import com.dcits.depsit.facade.eo.RbLimitSumInfoEO;

/**
 * ST100 获取累计限额。
 *
 * 步骤1 以[限额场景编码]、{客户号}、{账号}（账号作为限额检查对象值 checkObjVal）
 * 三个条件等值查询【限额累计表】（RB_LIMIT_SUM_INFO）；查询条件覆盖表主键
 * (CHECK_OBJ_VAL, LIMIT_SCENE_NO)，命中记录至多一条。命中时输出该记录的客户号、
 * 限额场景编码、限额累计金额、限额累计笔数（原样透传存值，不计算、不填充默认值）；
 * 未命中时正常返回且四个输出字段均为 null。只读步骤：无业务失败场景，
 * 失败仅由技术异常向上传播表达（不捕获、不转换），无事务要求。
 */
@Service
public class ST100Pbc implements IST100 {

    @Autowired
    private IRbLimitSumInfoBcc rbLimitSumInfoBcc;

    @Override
    public ST100OutputBO execute(ST100InputBO input) {
        ST100OutputBO output = new ST100OutputBO();

        // 步骤1 获取累计限额：三条件（checkObjVal=账号、客户号、限额场景编码）等值查询 RB_LIMIT_SUM_INFO
        List<RbLimitSumInfoEO> records = findLimitSumInfo(input);

        // 未命中（空集合）为正常返回，输出字段保持 null；命中时查询条件覆盖主键两列，至多一条
        if (records != null && !records.isEmpty()) {
            fillHitOutput(output, records.get(0));
        }

        output.setSucceed(true);
        return output;
    }

    /**
     * 步骤1 获取累计限额：以 checkObjVal={账号}、clientNo={客户号}、
     * limitSceneNo=[限额场景编码] 构造等值查询条件（请求 EO 仅设置该三个非空属性），
     * 查询【限额累计表】RB_LIMIT_SUM_INFO。
     */
    private List<RbLimitSumInfoEO> findLimitSumInfo(ST100InputBO input) {
        RbLimitSumInfoEO condition = new RbLimitSumInfoEO();
        condition.setCheckObjVal(input.getBaseAcctNo());
        condition.setClientNo(input.getClientNo());
        condition.setLimitSceneNo(input.getLimitSceneNo());
        return rbLimitSumInfoBcc.findByEo(condition);
    }

    /**
     * 步骤1 结果映射：命中记录的客户号、限额场景编码、限额累计金额、限额累计笔数
     * 原样透传到输出；可空列存值为 NULL 时输出亦为 null，不填充默认值。
     */
    private void fillHitOutput(ST100OutputBO output, RbLimitSumInfoEO record) {
        output.setClientNo(record.getClientNo());
        output.setLimitSceneNo(record.getLimitSceneNo());
        output.setLimitSumAmt(record.getLimitSumAmt());
        output.set否(record.get否());
    }
}
