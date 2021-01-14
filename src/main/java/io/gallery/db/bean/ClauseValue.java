package io.gallery.db.bean;

import java.util.List;

/**
 * 值信息
 */
public class ClauseValue {
    /**
     * 原始值
     */
    private String original;
    /**
     * 真实值
     */
    private String value;
    /**
     * 真实值列表
     */
    private List valueList;
    /**
     * 操作符
     */
    private String operator;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List getValueList() {
        return valueList;
    }

    public void setValueList(List valueList) {
        this.valueList = valueList;
    }
}
