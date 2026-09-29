package com.dcits.depsit.facade.bo;

import com.dcits.depsit.enums.Ccy;

/**
 * ST046 检查币种 输入BO。
 *
 * 字段定义来自正式 Spec ST046「输入」表：ccy（币种，必填）、prodNo（产品编号，必填）、
 * attrKey（参数KEY值，必填）、attrValue（属性值，必填）。
 * attrValue 正式需求声明为必填输入但正文未引用，本 Spec 按原样保留声明、不规定用途；
 * 需求未定义各输入缺失或为空时的处理，本 BO 不做校验。
 */
public class ST046InputBO {

    /** 币种（取值来源于代码[货币币种]） */
    private Ccy ccy;
    /** 产品编号 */
    private String prodNo;
    /** 参数KEY值 */
    private String attrKey;
    /** 属性值（必填输入，正文未引用，不规定用途） */
    private String attrValue;

    public Ccy getCcy() {
        return ccy;
    }

    public void setCcy(Ccy ccy) {
        this.ccy = ccy;
    }

    public String getProdNo() {
        return prodNo;
    }

    public void setProdNo(String prodNo) {
        this.prodNo = prodNo;
    }

    public String getAttrKey() {
        return attrKey;
    }

    public void setAttrKey(String attrKey) {
        this.attrKey = attrKey;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }
}
