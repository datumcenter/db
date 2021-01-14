package io.gallery.db.util;

import io.gallery.db.bean.DataBaseGenericPage;
import io.gallery.db.mapper.DataBaseMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataBaseUtil {
    private static DataBaseMapper dataBaseMapper = (DataBaseMapper) DataBaseSpringUtil.getBean("dataBaseMapper");

    /**
     * 执行拼接好的SQL
     *
     * @param sql
     * @return
     */
    public static void execute(String sql) {
        dataBaseMapper.exec(sql);
    }

    /**
     * 执行预处理的SQL
     *
     * @param sql
     * @param input
     * @return
     */
    public static void execute(String sql, Map<String, Object> input) {
        input = Optional.ofNullable(input).orElse(new HashMap<String, Object>());
        input.put("executeWithParams", sql);
        dataBaseMapper.execWithParams(input);
    }

    /**
     * 执行拼接好的SQL，返回列表
     *
     * @param sql
     * @return
     */
    public static List<HashMap> select(String sql) {
        List list = dataBaseMapper.execute(sql);
        for (Object o : list) {
            DBT.dealMegaText((Map) o);
        }
        return list;
    }

    /**
     * 执行拼接好的SQL，返回数量
     *
     * @param sql
     * @return
     */
    public static long count(String sql) {
        return dataBaseMapper.executeCount("select count(1) from (" + sql + ") t ");
    }

    /**
     * 执行预处理的SQL，返回列表
     *
     * @param sql
     * @param input
     * @return
     */
    public static List<HashMap> select(String sql, Map input) {
        input = Optional.ofNullable(input).orElse(new HashMap<String, Object>());
        input.put("executeWithParams", sql);
        List list = dataBaseMapper.executeWithParams(input);
        for (Object o : list) {
            DBT.dealMegaText((Map) o);
        }
        return list;
    }

    /**
     * 执行预处理的SQL，返回列表
     *
     * @param sql
     * @param clazz
     * @return
     */
    public static <T> List<T> select(String sql, Class<T> clazz) {
        return Optional.ofNullable(select(sql)).map(list -> list.stream().map(map -> DataBaseTools.mapToBeanIngnoreCase((Map<String, Object>) map, clazz)).collect(Collectors.toList())).orElse(Collections.emptyList());
    }


    /**
     * 执行预处理的SQL，返回列表
     *
     * @param sql
     * @param input
     * @param clazz
     * @return
     */
    public static <T> List<T> select(String sql, Map input, Class<T> clazz) {
        return Optional.ofNullable(select(sql, input)).map(list -> list.stream().map(map -> DataBaseTools.mapToBeanIngnoreCase((Map<String, Object>) map, clazz)).collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    /**
     * 执行预处理的SQL，返回列表
     *
     * @param sql
     * @param input
     * @return
     */
    public static long count(String sql, Map input) {
        input = Optional.ofNullable(input).orElse(new HashMap<String, Object>());
        input.put("executeWithParams", "select count(1) from (" + sql + ") t ");
        return dataBaseMapper.executeWithParamsCount(input);
    }

    /**
     * 执行预处理的SQL，返回影响条数
     *
     * @param sql
     * @param input
     * @return
     */
    public static long insertOrUpdate(String sql, Map input) {
        input = Optional.ofNullable(input).orElse(new HashMap<String, Object>());
        input.put("executeWithParams", sql);
        return dataBaseMapper.executeInsertOrUpdate(input);
    }

    /**
     * 执行预处理的SQL，返回影响条数
     *
     * @param sql
     * @return
     */
    public static long insertOrUpdate(String sql) {
        return insertOrUpdate(sql, null);
    }

    /**
     * 执行拼接好的SQL，返回单条记录
     *
     * @param sql
     * @return
     */
    public static HashMap selectOne(String sql) {
        LinkedHashMap map = Optional.ofNullable(dataBaseMapper.executeOne(sql)).orElse(new LinkedHashMap<>());
        DBT.dealMegaText(map);
        return map;
    }

    /**
     * 执行拼接好的SQL，返回单条记录
     *
     * @param sql
     * @param clazz
     * @return
     */
    public static <T> T selectOne(String sql, Class<T> clazz) {
        return DataBaseTools.mapToBeanIngnoreCase((Map<String, Object>) selectOne(sql), clazz);
    }

    /**
     * 执行预处理的SQL，返回单条记录
     *
     * @param sql
     * @param input
     * @return
     */
    public static HashMap selectOne(String sql, Map input) {
        input = Optional.ofNullable(input).orElse(new HashMap<String, Object>());
        input.put("executeWithParams", sql);
        LinkedHashMap map = dataBaseMapper.executeWithParamsOne(input);
        DBT.dealMegaText(map);
        return map;
    }

    /**
     * 执行预处理的SQL，返回单条记录
     *
     * @param sql
     * @param input
     * @param clazz
     * @return
     */
    public static <T> T selectOne(String sql, Map input, Class<T> clazz) {
        return DataBaseTools.mapToBeanIngnoreCase((Map<String, Object>) selectOne(sql, input), clazz);
    }

    /**
     * 设置排序信息
     *
     * @param input
     * @return
     */
    public static String getOrderClause(Map input) {
        return Optional.ofNullable(input).map(params -> Optional.ofNullable(params.get("orderClause")).map(orderClause -> " order by ${orderClause} ").orElse("")).orElse("");
    }

    /**
     * 设置排序信息
     *
     * @param input
     * @return
     */
    public static String getOrderClauseForSQL(Map input) {
        return getOrderClause(input).replace("order by", "");
    }

    /**
     * 设置排序分页信息
     *
     * @param input
     * @return
     */
    public static String getPageClause(Map input) {
        String result = "";
        if (Optional.ofNullable(input).isPresent()) {
            Object start = input.get("start");
            Object length = input.get("length");
            if (Optional.ofNullable(start).isPresent() && Optional.ofNullable(length).isPresent()) {
                input.put("start", start instanceof String ? Integer.valueOf((String) start) : start);
                input.put("length", length instanceof String ? Integer.valueOf((String) length) : length);
                result = " limit #{start},#{length} ";
            }
        }
        return result;
    }

    /**
     * 设置排序分页信息
     *
     * @param input
     * @return
     */
    public static String getPageClauseForSQL(Map input) {
        return getPageClause(input).replace("limit", "");
    }

    /**
     * 设置检索条件
     *
     * @param input
     * @return
     */
    public static String getWhereClause(Map<String, Object> input) {
        input = removePublic(input);
        StringBuilder condition = new StringBuilder();
        for (String key : input.keySet()) {
            Optional.ofNullable(input.get(key)).ifPresent(value -> {
                if (key.contains("_like")) {
                    condition.append(" and " + key.replaceFirst("_like", "").replace("_", ".") + " like CONCAT('%',#{" + key + "},'%')");
                } else {
                    condition.append(" and " + key.replace("_", ".") + " = #{" + key + "}");
                }
            });
        }
        return removeFirstSQLKeyWord(condition.toString());
    }

    /**
     * 移除多余的SQL关键字
     *
     * @param sql
     * @return
     */
    public static String removeFirstSQLKeyWord(String sql) {
        String result = "";
        if (DataBaseTools.isNotNull(sql)) {
            result = sql;
            result = result.trim();
            if (result.indexOf("and") == 0) {
                result = sql.replaceFirst("and", "");
            } else if (result.indexOf("or") == 0) {
                result = sql.replaceFirst("or", "");
            }
        }
        return " " + result.trim() + " ";
    }

    /**
     * 移除公共字段
     *
     * @param where
     * @return
     */
    public static Map<String, Object> removePublic(Map<String, Object> where) {
        if (where != null) {
            where = new ConcurrentHashMap(where);
            List<String> keys = DataBaseTools.classKeyToMap(DataBaseGenericPage.class);
            keys.add("executeWithParams");
            for (String key : keys) {
                where.remove(key);
            }
            String[] dataTables = {"columns[", "order[", "search[", "orderClause", "groupByClause"};
            //移除datatable参数
            for (String dataTable : dataTables) {
                Set<String> strings = where.keySet();
                for (String string : strings) {
                    if (string.contains(dataTable)) {
                        where.remove(string);
                    }
                }
            }
            where.remove("_");
        }
        return where;
    }


}
