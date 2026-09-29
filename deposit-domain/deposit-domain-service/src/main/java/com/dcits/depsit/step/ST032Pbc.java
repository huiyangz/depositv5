package com.dcits.depsit.step;

import org.springframework.stereotype.Service;

import com.dcits.depsit.enums.CrDrInd;
import com.dcits.depsit.facade.bo.ST032InputBO;
import com.dcits.depsit.facade.bo.ST032OutputBO;

/**
 * ST032 设置借记交易的借贷标志。
 *
 * 步骤1 将输出数据[借贷标志]（crDrInd）无条件赋值为"D-借方"，即枚举常量
 * CrDrInd.D（value "D"），并作为步骤输出返回。纯赋值步骤：无输入字段、
 * 无数据访问、无外部调用、无组件内步骤调用、无状态变更副作用；无业务失败
 * 场景，失败仅由技术异常原样传播表达。
 */
@Service
public class ST032Pbc implements IST032 {

    @Override
    public ST032OutputBO execute(ST032InputBO input) {
        ST032OutputBO output = new ST032OutputBO();

        // 步骤1 赋值[借贷标志]="D-借方"：无条件常量赋值，不依赖任何输入或前置条件
        output.setCrDrInd(CrDrInd.D);

        output.setSucceed(true);
        return output;
    }
}
