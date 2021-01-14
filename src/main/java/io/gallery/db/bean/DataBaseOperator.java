package io.gallery.db.bean;

import io.gallery.db.util.DBT;
import io.gallery.db.util.DataBaseTools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SQL操作符枚举
 */
public enum DataBaseOperator {
    /*--操作符开始--*/
    _eq(" = "),
    _ne(" != "),
    _gt(" > "),
    _lt(" < "),
    _ge(" >= "),
    _le(" <= "),
    _in(" in "),
    _is_null(" is null"),
    _is_not_null(" is not null"),
    _not_in(" not in "),
    _like(" like "),//like '%*%'
    _not_like(" not like "),// not like '%*%'
    _left_like(" like "),// like '*%'
    _left_not_like(" not like "),// not like '*%'
    _right_like(" like "),// like '%*%
    _right_not_like(" not like ")// not like '%*%
    ;
    /**
     * or 运算符
     */
    public static final String _or_ = "_or_";

    /**
     * 获取操作符
     */
    public String getOperator() {
        return operator;
    }


    private DataBaseOperator(String operator) {
        this.operator = operator;
    }

    /**
     * 枚举值
     */
    private String operator;

    /**
     * 判断字段中是否包含操作符或函数
     */
    public static boolean containsOperator(String columnClause) {
        boolean result = false;
        if (DataBaseTools.isNotNull(columnClause)) {
            for (DataBaseOperator value : DataBaseOperator.values()) {
                String lowerCase = columnClause.toLowerCase();
                if (lowerCase.endsWith(value.name())) {
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
        String result = _eq.getOperator();
        if (DataBaseTools.isNotNull(input)) {
            for (DataBaseOperator value : DataBaseOperator.values()) {
                String lowerCase = input.toLowerCase();
                if (lowerCase.replace("__", "").endsWith(value.name())) {
                    result = value.getOperator();
                }
            }
        }
        return result;
    }

    /**
     * 获取字符串中包含的函数
     *
     * @param input
     * @return
     */
    public static String getFunction(String input) {
        String result = "";
        if (DBT.isNotNull(input) && input.contains(")")) {
            result = input.substring(0, input.indexOf(")") + 1);
        }
        return result;
    }

    /**
     * operator起始位置
     */
    public static int indexOfOperator(String columnClause) {
        int result = -1;
        if (DataBaseTools.isNotNull(columnClause)) {
            columnClause = columnClause.toLowerCase();
            result = columnClause.length();
            String operator = "";
            List<DataBaseOperator> dataBaseOperators = Arrays.asList(DataBaseOperator.values());
            Collections.sort(dataBaseOperators, (o1, o2) -> o1.name().length() - o2.name().length());
            if (DataBaseTools.isNotNull(columnClause)) {
                for (DataBaseOperator value : dataBaseOperators) {
                    if (columnClause.endsWith(value.name())) {
                        operator = value.name();
                    }
                }
            }
            if (DataBaseTools.isNotNull(operator)) {
                result = columnClause.indexOf(operator);
            }
        }
        return result;
    }

    /**
     * 获取真实表名.字段名
     *
     * @param input
     * @return
     */
    public static String getRealColumn(String input) {
        String result = input;
        if (DBT.isNotNull(result)) {
            //scjydwjbxx*length(hylbdm)_eq -> scjydwjbxx*length(hylbdm)
            if (containsOperator(result)) {
                result = result.substring(0, indexOfOperator(result));
            }
            //scjydwjbxx.length(hylbdm)
            result = result.replace("*", ".");
            if (result.contains("(") && result.contains(")")) {
                if (result.contains(",")) {
                    result = result.substring(result.indexOf("(") + 1, result.indexOf(","));
                } else {
                    result = result.substring(result.indexOf("(") + 1, result.indexOf(")"));
                }
            }
        }
        return result;
    }

    /**
     * 处理字段信息
     *
     * @param input
     * @return
     */
    public static ClauseColumn dealColumnClause(String input) {
        ClauseColumn clause = new ClauseColumn();
        clause.setColumn(getRealColumn(input));
        clause.setFunction(getFunction(input));
        clause.setOperator(getOperator(input));
        clause.setOriginal(input);
        return clause;
    }
}
