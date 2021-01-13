package com.longruan.ark.common.db.bean;

import com.longruan.ark.common.db.util.DBT;
import com.longruan.ark.common.db.util.DataBaseTools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DataBaseValue {
    /*--操作符开始--*/
    doublue_colon("::");

    /**
     * 获取操作符
     */
    public String getOperator() {
        return operator;
    }


    private DataBaseValue(String operator) {
        this.operator = operator;
    }

    /**
     * 枚举值
     */
    private String operator;

    /**
     * 判断字段中是否包含操作符
     */
    public static boolean containsOperator(String input) {
        boolean result = false;
        if (DBT.isNotNull(input)) {
            for (DataBaseValue value : DataBaseValue.values()) {
                String lowerCase = input.toLowerCase();
                if (lowerCase.contains(value.getOperator())) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * 获取字符串中包含的操作符
     *
     * @param input
     * @return
     */
    public static String getOperator(String input) {
        String result = "";
        if (DataBaseTools.isNotNull(input)) {
            for (DataBaseValue value : DataBaseValue.values()) {
                result = input.substring(indexOfOperator(input));
            }
        }
        return result;
    }

    /**
     * operator起始位置
     */
    public static int indexOfOperator(String input) {
        int result = -1;
        if (DataBaseTools.isNotNull(input)) {
            input = input.toLowerCase();
            result = input.length();
            String operator = "";
            List<DataBaseValue> dataBaseOperators = Arrays.asList(DataBaseValue.values());
            Collections.sort(dataBaseOperators);
            if (DataBaseTools.isNotNull(input)) {
                for (DataBaseValue value : dataBaseOperators) {
                    if (input.contains(value.getOperator())) {
                        operator = value.getOperator();
                    }
                }
            }
            if (DataBaseTools.isNotNull(operator)) {
                result = input.indexOf(operator);
            }
        }
        return result;
    }

    /**
     * 获取真实值
     *
     * @param input
     * @return
     */
    public static String getRealValue(String input) {
        String result = input;
        //1::integer -> 1
        if (containsOperator(input)) {
            for (DataBaseValue value : DataBaseValue.values()) {
                if (input.contains(value.getOperator())) {
                    result = input.substring(0, input.lastIndexOf(value.getOperator()));
                }
            }
        }
        return result;
    }

    /**
     * 处理值信息
     *
     * @param input
     * @return
     */
    public static ClauseValue dealValueClause(String input) {
        ClauseValue clause = null;
        if (DBT.isNotNull(input)) {
            clause = new ClauseValue();
            clause.setValue(getRealValue(input));
            clause.setOperator(getOperator(input));
            clause.setOriginal(input);
        }
        return clause;
    }

}
