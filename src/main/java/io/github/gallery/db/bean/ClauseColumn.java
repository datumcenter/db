package com.longruan.ark.common.db.bean;

/**
 * 字段信息
 */
public class ClauseColumn {
    /**
     * 原始值
     */
    private String original;
    /**
     * 真实字段
     */
    private String column;
    /**
     * 操作符
     */
    private String operator;
    /**
     * 函数
     */
    private String function;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
