package com.longruan.ark.common.db.util;


import com.longruan.ark.common.db.bean.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DataBaseProvider {

    public static String insert(Map<String, Object> input) {
        Map<String, Object> values = (Map<String, Object>) input.get("values");
        String table = (String) input.get("table");
        return new SQL() {{
            INSERT_INTO(table);
            if (values != null) {
                for (String key : values.keySet()) {
                    Object value = values.get(key);//值
                    if (value instanceof ClauseValue)
                        Optional.ofNullable((ClauseValue) value).ifPresent(clauseValue -> VALUES(key, "#{values." + key + ".value}" + clauseValue.getOperator()));
                    else VALUES(key, "#{values." + key + "}");
                }
            }
        }}.toString();
    }

    public static String delete(Map<String, Object> input) {
        Map<String, Object> where = (Map<String, Object>) input.get("where");
        String table = (String) input.get("table");
        return new SQL() {{
            DELETE_FROM(table);
            if (where != null && where.keySet().size() > 0) {
                for (String key : where.keySet()) {
                    String ids = String.valueOf(where.get(key));
                    String[] strings = ids.split(",");
                    if (strings.length > 1) {
                        where.put(key, Stream.of(strings).collect(Collectors.joining("','", "'", "'")));
                        WHERE(key + " in (${where." + key + "})");
                    } else {
                        WHERE(key + " = #{where." + key + "}");
                    }
                }
            } else {
                WHERE("1=-1");
            }
        }}.toString();
    }


    public static String deleteByParams(Map<String, Object> input) {
        Map<String, Object> where = (Map<String, Object>) input.get("where");
        String table = (String) input.get("table");
        SQL sql = new SQL();
        sql.DELETE_FROM(table);
        if (where != null && where.keySet().size() > 0) {
            setWhereClause(where, sql);
        } else {
            sql.WHERE("1=-1");
        }
        return sql.toString();
    }

    public static String update(Map<String, Object> input) {
        Map<String, Object> values = (Map<String, Object>) input.get("values");
        Map<String, Object> where = (Map<String, Object>) input.get("where");
        String table = (String) input.get("table");
        return new SQL() {{
            UPDATE(table);
            if (values != null) {
                for (String key : values.keySet()) {
                    Object value = values.get(key);//值
                    if (value instanceof ClauseValue) {
                        Optional.ofNullable((ClauseValue) value).ifPresent(clauseValue -> SET(key + " = #{values." + key + ".value}" + clauseValue.getOperator()));
                    } else SET(key + " = #{values." + key + "}");
                }
                for (String key : where.keySet()) {
                    Object value = where.get(key);//值
                    if (value instanceof ClauseValue) {
                        Optional.ofNullable((ClauseValue) value).ifPresent(clauseValue -> WHERE(key + " = #{where." + key + ".value}" + clauseValue.getOperator()));
                    } else WHERE(key + " = #{where." + key + "}");
                }
            }
        }}.toString();
    }

    public static String get(Map<String, Object> input) {
        Map<String, Object> where = (Map<String, Object>) input.get("where");
        String table = (String) input.get("table");
        String columns = (String) input.get("columns");
        return new SQL() {{
            SELECT(DBT.isNotNull(columns) ? columns : "*");
            FROM(table);
            if (where != null) {
                for (String key : where.keySet()) {
                    WHERE(key + " = #{where." + key + "}");
                }
            }
            LIMIT(1);
        }}.toString();
    }

    public static String select(Map<String, Object> input) {
        return getListSQL(input);
    }

    public static String selectOne(Map<String, Object> input) {
        return getListSQL(input) + " limit 1 ";
    }

    public static String count(Map<String, Object> input) {
        return "select count(1) from (" + getListSQL(input, true) + ") t";
    }

    public static String execute(String sql) {
        return sql;
    }

    public static String executeWithParams(Map<String, Object> params) {
        return (String) params.get("executeWithParams");
    }


    public static String columns(@Param("table") final String table, @Param("schema") final String schema) {
        DataBaseConfig dataBaseConfig = (DataBaseConfig) DataBaseSpringUtil.getBean("dataBaseConfig");
        if (dataBaseConfig == null || !DataBasePlatform.dm.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
            return new SQL() {//mysql,postgre,sqlserver
                {
                    SELECT("concat(column_name,'::',data_type) COLUMN_NAME");
                    FROM("information_schema.COLUMNS");
                    WHERE("table_name=#{table}");
                    if (DataBaseTools.isNotNull(schema))
                        WHERE("table_schema=#{schema}");
                }
            }.toString();
        } else {
            return new SQL() {//dm
                {
                    SELECT("concat(column_name,'::',data_type) COLUMN_NAME");
                    FROM("all_tab_columns");
                    WHERE("table_name=#{table}");
                    if (DataBaseTools.isNotNull(schema))
                        WHERE("owner=#{schema}");
                }
            }.toString();
        }
    }

    public static String getListSQL(Map<String, Object> input, boolean count) {
        String sqlToExcute = "";
        ConcurrentHashMap<String, Object> where = new ConcurrentHashMap((Map<String, Object>) input.get("where"));
        String defaultDb = (String) input.get("defaultDb");
        DataBaseConfig dataBaseConfig = (DataBaseConfig) input.get("dataBaseConfig");
        String deafultPlatform = Optional.ofNullable(dataBaseConfig).map(DataBaseConfig::getPlatform).orElse("");
        String table = (String) input.get("table");
        String keyName = (String) input.get("keyName");
        String treeColumn = (String) Optional.ofNullable(input.get("treeColumn")).orElse(dataBaseConfig.getTreeColumn());
        Boolean tree = (Boolean) input.get("tree");
        if (tree == null)
            tree = false;
        String columns = "*";
        if (DataBaseTools.isNotNull((String) input.get("columns")))
            columns = (String) input.get("columns");
        if (DataBasePlatform.sqlserver.name().equals(deafultPlatform))
            table = "[" + table + "]";
        SQL sql = new SQL();
        Map<String, Object> limit = (Map<String, Object>) input.get("limit");
        Object orderClause = input.get("orderClause");
        Object groupByClause = input.get("groupByClause");
        String columnToShow = columns;
        String functionStr = "";
        if (count) {
            if (groupByClause == null)
                columnToShow = table + ".*";
            sql.SELECT(columnToShow);
        } else {
            if (groupByClause == null) {
                String dict = (String) where.get("dict");
                String dicts = (String) where.get("dicts");
                String userFullNames = (String) where.get("userFullNames");
                String alias = (String) where.get("alias");
                if (DBT.isNull(defaultDb)) {
                    defaultDb = Optional.ofNullable(dataBaseConfig).map(DataBaseConfig::getDeafultDb).orElse("");
                }
                String split = "_";
                String funcDict = dataBaseConfig.getFuncDict();
                String funcDicts = dataBaseConfig.getFuncDicts();
                if (DataBaseTools.isNotNull(dict)) {
                    if (dict.contains(":")) split = ":";
                    for (String dictInfo : dict.split(",")) {
                        String[] dictNo = dictInfo.split(split);
                        if (dictNo.length == 2)
                            functionStr += "," + defaultDb + "." + funcDict + "('" + dictNo[1] + "'," + dictNo[0] + ") " + dictNo[0].replace(".", "_") + "Str";
                    }
                }
                if (DataBaseTools.isNotNull(dicts)) {
                    if (dicts.contains(":")) split = ":";
                    for (String dictInfo : dicts.split(",")) {
                        String[] dictsNo = dictInfo.split(split);
                        if (dictsNo.length == 2) {
                            functionStr += "," + defaultDb + "." + funcDicts + "('" + dictsNo[1] + "'," + dictsNo[0] + ") " + dictsNo[0].replace(".", "_") + "AllStr";
                        }
                    }
                }
                if (DataBaseTools.isNotNull(userFullNames)) {
                    String[] arrays = userFullNames.split(",");
                    for (String array : arrays) {
                        functionStr += "," + defaultDb + ".userFullNames(" + array + ") " + array + "Name";
                    }
                }
                String asSplit = "_as_";
                if (DataBaseTools.isNotNull(alias) && alias.contains(":")) asSplit = ":";
                if (DataBaseTools.isNotNull(alias) && alias.contains(asSplit)) {
                    for (String array : alias.split(",")) {
                        functionStr += "," + array.replace(asSplit, " as ");
                    }
                }
                columnToShow = columns + functionStr;
            }
            if (tree && DataBasePlatform.postgres.name().equals(deafultPlatform))
                sql.SELECT(columnToShow + ",0 as tree_level");
            else
                sql.SELECT(columnToShow);
            if (limit != null && limit.size() > 0) {
                if (DataBasePlatform.mysql.name().equals(deafultPlatform) || DataBasePlatform.dm.name().equals(deafultPlatform)) {
                    sql.LIMIT("#{limit.start},#{limit.length}");
                } else if (DataBasePlatform.postgres.name().equals(deafultPlatform)) {
                    sql.LIMIT("#{limit.length}");
                    sql.OFFSET("#{limit.start}");
                } else if (DataBasePlatform.sqlserver.name().equals(deafultPlatform)) {
                    sql.LIMIT("#{limit.length}");
                    sql.OFFSET("#{limit.start}");
                    sql.OFFSET_ROWS("#{limit.start}");
                    sql.FETCH_FIRST_ROWS_ONLY("#{limit.length}");
                } else {
                    sql.LIMIT("#{limit.start},#{limit.length}");
                }
            }
        }
        where.remove("dict");
        where.remove("dicts");
        where.remove("userFullNames");
        where.remove("alias");
        sql.FROM(table);
        setJoinClause(input, where, table, sql);
        setWhereClause(where, sql);
        if (tree && dataBaseConfig.isTreeFromRoot()) {
            String treeColumnClause = table + "." + treeColumn;
            if (where.size() == 0)
                sql.WHERE("(" + treeColumnClause + " is null or trim(" + treeColumnClause + ") = '')");
        }
        String orderBy = "";
        if (orderClause != null)
            orderBy = orderClause.toString();
        else {
            if (!count && DataBasePlatform.sqlserver.name().equals(deafultPlatform) && limit != null)
                orderBy = "1";
        }
        if (DBT.isNotNull(orderBy))
            sql.ORDER_BY(orderBy);
        if (groupByClause != null)
            sql.GROUP_BY(groupByClause.toString());
        if (!count && tree) {
            if (DataBasePlatform.postgres.name().equals(deafultPlatform)) {
                SQL treeSql = new SQL();
                treeSql.SELECT(columnToShow + ",tree_level + 1 as tree_level").FROM(table).JOIN("t on " + table + "." + treeColumn + " = t." + keyName);
                setJoinClause(input, where, table, treeSql);
                SQL select = new SQL();
                select.SELECT("*");
                select.FROM("t");
                sqlToExcute = "with recursive t as ((" + sql.toString() + ") union all " + treeSql.toString() + ") " + select.toString();
                if (DBT.isNotNull(orderBy))
                    sqlToExcute += (" order by " + orderBy);
            } else if (DataBasePlatform.mysql.name().equals(deafultPlatform)) {
                SQL treeSql = new SQL();
                String finalTable = table;
                treeSql.SELECT("t.*,temp.tree_level").
                        FROM(finalTable + " t,(" + new SQL() {{
                            SELECT("@ids as _cids");
                            SELECT("(select @ids:=group_concat(" + keyName + ") from " + finalTable + " where find_in_set(" + treeColumn + ",@ids)) as _ids");
                            SELECT("@level :=@level+1 as tree_level");
                            FROM(finalTable + ",(select @ids:=group_concat(" + keyName + "),@level :=-1 from (" + sql.toString() + " group by " + keyName + ") t) business");
                            WHERE("@ids is not null");
                        }} + ") temp").
                        WHERE("find_in_set(t." + keyName + ",temp._cids)");
                sqlToExcute = treeSql.toString();
            } else
                sqlToExcute = sql.toString();
        } else
            sqlToExcute = sql.toString();
        return sqlToExcute;
    }

    private static void setJoinClause(Map<String, Object> input, ConcurrentHashMap<String, Object> where, String table, SQL sql) {
        String[] forginTableName = (String[]) input.get("forginTableName");
        if (forginTableName != null) {
            String[] forginKeyName = (String[]) input.get("forginKeyName");
            String[] forginTableKeyName = (String[]) input.get("forginTableKeyName");
            for (int i = 0; i < forginTableName.length; i++) {
                String name = forginTableName[i];
                String[] split = name.split(":");
                if (split.length > 1 && split[0].contains("right")) {
                    String joinCondition = split[1] + " on " + table + "." + forginKeyName[i] + "=" + split[1] + "." + forginTableKeyName[i];
                    String rightCondition = DataBaseTools.getByPattern(split[0], DataBaseConstant.PATTERN_BRACKETS);
                    if (DataBaseTools.isNotNull(rightCondition)) {
                        String[] rightConditions = rightCondition.split(",");
                        for (String condition : rightConditions) {
                            if (where.get(condition) != null) {
                                //拼接至right后
                                Object value = where.get(condition);
                                if (value != null) {
                                    joinCondition += (" and " + condition + "=" + value + " ");
                                }
                                //移除条件，防止拼接到where后
                                where.keySet().remove(condition);
                            }
                        }
                    }
                    sql.RIGHT_OUTER_JOIN(joinCondition);
                } else {
                    sql.LEFT_OUTER_JOIN(name + " on " + table + "." + forginKeyName[i] + "=" + name + "." + forginTableKeyName[i]);
                }
            }
        }
    }

    private static void setWhereClause(Map<String, Object> where, SQL sql) {
        for (String key : where.keySet()) {
            Object value = where.get(key);//值
            String operator = DataBaseOperator.getOperator(key);//操作符
            ClauseColumn clauseColumn = DataBaseOperator.dealColumnClause(key);
            if (value != null) {
                String valueClause = "#{where." + key + "}";
                if (!DataBaseOperator._not_in.getOperator().equalsIgnoreCase(clauseColumn.getOperator())
                        && !DataBaseOperator._in.getOperator().equalsIgnoreCase(clauseColumn.getOperator())) {
                    if (value instanceof ClauseValue) {
                        ClauseValue clauseValue = (ClauseValue) value;
                        if (clauseValue != null) {
                            valueClause = "#{where." + key + ".value}" + clauseValue.getOperator();
                        }
                    }
                }
                if (key.contains(DataBaseOperator._left_not_like.name())) {
                    String column = getRealColumn(key, DataBaseOperator._left_not_like);
                    if (value instanceof Map) {
                        LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                        StringBuilder likeClause = new StringBuilder();
                        for (String likeKey : valueMap.keySet()) {
                            String likeValueClause = "#{where." + key + "." + likeKey + "}";
                            likeClause.append(" and " + column + operator + " CONCAT(" + likeValueClause + ",'%')");
                        }
                        sql.WHERE(DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()));
                    } else {
                        sql.WHERE(column + operator + " CONCAT(" + valueClause + ",'%')");
                    }
                } else if (key.contains(DataBaseOperator._right_not_like.name())) {
                    String column = getRealColumn(key, DataBaseOperator._right_not_like);
                    if (value instanceof Map) {
                        LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                        StringBuilder likeClause = new StringBuilder();
                        for (String likeKey : valueMap.keySet()) {
                            String likeValueClause = "#{where." + key + "." + likeKey + "}";
                            likeClause.append(" and " + column + operator + " CONCAT('%'," + likeValueClause + ")");
                        }
                        sql.WHERE(DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()));
                    } else {
                        sql.WHERE(column + operator + " CONCAT('%'," + valueClause + ")");
                    }
                } else if (key.contains(DataBaseOperator._left_like.name())) {
                    String column = getRealColumn(key, DataBaseOperator._left_like);
                    if (value instanceof Map) {
                        LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                        StringBuilder likeClause = new StringBuilder();
                        for (String likeKey : valueMap.keySet()) {
                            String likeValueClause = "#{where." + key + "." + likeKey + "}";
                            likeClause.append(" or " + column + operator + " CONCAT(" + likeValueClause + ",'%')");
                        }
                        sql.WHERE(DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()));
                    } else {
                        sql.WHERE(column + operator + " CONCAT(" + valueClause + ",'%')");
                    }
                } else if (key.contains(DataBaseOperator._right_like.name())) {
                    String column = getRealColumn(key, DataBaseOperator._right_like);
                    if (value instanceof Map) {
                        LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                        StringBuilder likeClause = new StringBuilder();
                        for (String likeKey : valueMap.keySet()) {
                            String likeValueClause = "#{where." + key + "." + likeKey + "}";
                            likeClause.append(" or " + column + operator + " CONCAT('%'," + likeValueClause + ")");
                        }
                        sql.WHERE(DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()));
                    } else {
                        sql.WHERE(column + operator + " CONCAT('%'," + valueClause + ")");
                    }
                } else if (key.contains(DataBaseOperator._not_like.name())) {
                    String column = getRealColumn(key, DataBaseOperator._not_like);
                    if (value instanceof Map) {
                        LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                        StringBuilder likeClause = new StringBuilder();
                        for (String likeKey : valueMap.keySet()) {
                            String likeValueClause = "#{where." + key + "." + likeKey + "}";
                            likeClause.append(" and " + column + operator + " CONCAT('%'," + likeValueClause + ",'%')");
                        }
                        sql.WHERE(DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()));
                    } else {
                        sql.WHERE(column + operator + " CONCAT('%'," + valueClause + ",'%')");
                    }
                } else if (key.contains(DataBaseOperator._like.name())) {
                    String column = getRealColumn(key, DataBaseOperator._like);
                    String[] columns = getRealColumn(key, DataBaseOperator._like).split(DataBaseOperator._or_);
                    if (value instanceof Map) {
                        LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                        StringBuilder likeClause = new StringBuilder();
                        for (String likeKey : valueMap.keySet()) {
                            String likeValueClause = "#{where." + key + "." + likeKey + "}";
                            likeClause.append(" or " + column + operator + " CONCAT('%'," + likeValueClause + ",'%')");
                        }
                        sql.WHERE(DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()));
                    } else {
                        StringBuilder likeClause = new StringBuilder();
                        for (String _column : columns) {
                            String likeColumnClause = _column + operator + " CONCAT('%'," + valueClause + ",'%')";
                            likeClause.append(" or " + likeColumnClause);
                        }
                        sql.WHERE("(" + DataBaseUtil.removeFirstSQLKeyWord(likeClause.toString()) + ")");
                    }
                } else if (key.contains(DataBaseOperator._not_in.name()) || key.contains(DataBaseOperator._in.name())) {
                    //多个值用逗号分开
                    if (value instanceof ClauseValue) {
                        ClauseValue clauseValue = (ClauseValue) value;
                        if (clauseValue != null) {
                            List valueList = clauseValue.getValueList();
                            StringBuilder inClause = new StringBuilder();
                            for (int i = 0; i < valueList.size(); i++) {
                                String orClause = DataBaseOperator.getRealColumn(key) + (key.contains(DataBaseOperator._not_in.name()) ? "!=" : "=") + "  #{where." + key + ".valueList[" + i + "]}";
                                inClause.append((key.contains(DataBaseOperator._not_in.name()) ? "and " : " or ") + orClause);
                            }
                            sql.WHERE("(" + DataBaseUtil.removeFirstSQLKeyWord(inClause.toString()) + ")");
                        }
                    }
                } else if (key.contains(DataBaseOperator._ge.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._ge) + operator + valueClause);
                } else if (key.contains(DataBaseOperator._le.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._le) + operator + valueClause);
                } else if (key.contains(DataBaseOperator._gt.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._gt) + operator + valueClause);
                } else if (key.contains(DataBaseOperator._lt.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._lt) + operator + valueClause);
                } else if (key.contains(DataBaseOperator._is_null.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._is_null) + operator);
                } else if (key.contains(DataBaseOperator._is_not_null.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._is_not_null) + operator);
                } else if (key.contains(DataBaseOperator._ne.name())) {
                    sql.WHERE(getRealColumn(key, DataBaseOperator._ne) + operator + valueClause);
                } else {
                    sql.WHERE(key.replace("*", ".").replace("|", ",") + operator + valueClause);
                }
            }
        }
    }

    private static String getListSQL(Map<String, Object> input) {
        return getListSQL(input, false);
    }

    private static String getRealColumn(String key, DataBaseOperator operator) {
        return key.replaceFirst(operator.name(), "").replace("*", ".").replace("|", ",");
    }


}
