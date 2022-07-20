package io.gallery.db.factory;

import io.gallery.db.bean.DataBaseBO;
import io.gallery.db.util.DB;
import io.gallery.db.util.DBT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据库操作
 */
public abstract class AbstractDataBase {
    public static final Log logger = LogFactory.getLog(AbstractDataBase.class);
    public static final String DB_PLATFORM_NAME = "dbPlatformName";

    /**
     * 数据库类型
     *
     * @return String
     */
    public abstract String getDbType();

    /**
     * 存放表名的表
     *
     * @return String
     */
    public abstract String getTablesTableName();

    /**
     * 存放字段名的表
     *
     * @return String
     */
    public abstract String getColumnsTableName();

    public String getTableName(Map record) {
        String result = (String) record.get("tableName");
        record.remove("tableName");
        return result;
    }

    public String getKeyName(Map record) {
        String result = (String) record.getOrDefault("keyName", "id");
        record.remove("keyName");
        return result;
    }

    public Map get(NamedParameterJdbcTemplate source, DataBaseBO dbo) {
        Map<String, Object> where = dbo.getWhere();
        Map<String, Object> values = dbo.getValues();
        if (DBT.isNull(dbo.getTableName())) {
            if (DBT.isNull(dbo.getTableName())) {
                logger.warn("表名未提供");
            }
            return new HashMap();
        }
        String sql = new SQL() {{
            SELECT("*");
            FROM(dbo.getTableName());
            if (where != null && where.keySet().size() > 0) {
                for (String key : where.keySet()) {
                    WHERE(key + " = :where_" + key);
                }
            } else {
                WHERE("1=-1");
            }
            LIMIT(1);
        }}.toString();
        try {
            return source.queryForMap(sql, dbo.getDto());
        } catch (DataAccessException e) {
            return new HashMap();
        }
    }

    public List select(NamedParameterJdbcTemplate source, DataBaseBO dbo) {
        return source.queryForList(getListSQL(dbo, false), dbo.getDto());
    }

    public Long count(NamedParameterJdbcTemplate source, DataBaseBO dbo) {
        return source.queryForObject(getListSQL(dbo, true), dbo.getDto(), Long.class);
    }

    public String getListSQL(DataBaseBO dbo, boolean count) {
        String tableName = dbo.getTableName();
        String columns = "*";
        if (count) {
            columns = "count(1)";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(columns).append(" from ").append(tableName);
        String whereClause = getWhereClause(dbo.getVo());
        String pageClause = getPageClause(dbo.getVo());
        String orderByClause = dbo.getOrderBy();
        if (DBT.isNotNull(whereClause)) {
            sql.append(" where ").append(whereClause);
        }
        if (!count) {
            if (DBT.isNotNull(orderByClause)) {
                sql.append(" order by ").append(orderByClause);
            }
            if (DBT.isNotNull(pageClause)) {
                sql.append(pageClause);
            }
        }
        return sql.toString();
    }

    public int update(NamedParameterJdbcTemplate source, DataBaseBO dbo) {
        Map<String, Object> where = dbo.getWhere();
        Map<String, Object> values = dbo.getValues();
        if (DBT.isNull(dbo.getTableName()) || where.isEmpty() || values.isEmpty() || DBT.isNull(dbo.getKeyName())) {
            if (DBT.isNull(dbo.getTableName())) {
                logger.warn("表名未提供");
            }
            if (where.isEmpty()) {
                logger.warn("条件未提供");
            }
            if (values.isEmpty()) {
                logger.warn("值未提供");
            }
            if (DBT.isNull(dbo.getKeyName())) {
                logger.warn("主键未提供");
            }
            return 0;
        }
        String sql = new SQL() {{
            UPDATE(dbo.getTableName());
            values.keySet().stream().forEach(key -> SET(key + " = :values_" + key));//值
            where.keySet().stream().forEach(key -> WHERE(key + " = :where_" + key));//条件
        }}.toString();
        return source.update(sql, dbo.getDto());
    }

    public int delete(NamedParameterJdbcTemplate source, DataBaseBO dbo) {
        Map<String, Object> where = dbo.getWhere();
        if (DBT.isNull(dbo.getTableName()) || where.isEmpty() || DBT.isNull(dbo.getKeyName())) {
            if (DBT.isNull(dbo.getTableName())) {
                logger.warn("表名未提供");
            }
            if (where.isEmpty()) {
                logger.warn("条件未提供");
            }
            if (DBT.isNull(dbo.getKeyName())) {
                logger.warn("主键未提供");
            }
            return 0;
        }
        String sql = new SQL() {{
            DELETE_FROM(dbo.getTableName());
            if (where.keySet().size() > 0) {
                for (String key : where.keySet()) {
                    WHERE(key + " = :where_" + key);
                }
            } else {
                WHERE("1=-1");
            }
        }}.toString();
        return source.update(sql, dbo.getDto());
    }

    public int insert(NamedParameterJdbcTemplate source, DataBaseBO dbo) {
        Map<String, Object> values = dbo.getValues();
        if (DBT.isNull(dbo.getTableName()) || values.isEmpty()) {
            if (DBT.isNull(dbo.getTableName())) {
                logger.warn("表名未提供");
            }
            if (values.isEmpty()) {
                logger.warn("值未提供");
            }
            return 0;
        }
        String sql = new SQL() {{
            values.keySet().stream().forEach(key -> SET(key + " = :values_" + key));//值
            INSERT_INTO(dbo.getTableName());
            for (String key : values.keySet()) {
                VALUES(key, ":values_" + key);
            }
        }}.toString();
        return source.update(sql, dbo.getDto());
    }

    public abstract List listDb(NamedParameterJdbcTemplate source);

    public abstract List listTable(NamedParameterJdbcTemplate source, String db);

    public abstract List listColumns(NamedParameterJdbcTemplate source, String db, String table);

    /**
     * 设置排序分页信息
     *
     * @param input Map
     * @return String
     */
    public String getPageClause(Map input) {
        String result = "";
        if (Optional.ofNullable(input).isPresent()) {
            Object start = input.get("start");
            Object length = input.get("length");
            if (start != null && length != null) {
                input.put("start", start instanceof String ? Integer.valueOf((String) start) : start);
                input.put("length", length instanceof String ? Integer.valueOf((String) length) : length);
                result = " limit :start,:length ";
            }
        }
        return result;
    }

    /**
     * 获取排序信息
     *
     * @param input Map
     * @return String
     */
    public String getOrderClause(Map input) {
        return null;
    }

    /**
     * 设置检索条件
     *
     * @param input Map
     * @return String
     */
    public String getWhereClause(Map<String, Object> input) {
        input = DB.removePublic(input);
        StringBuilder condition = new StringBuilder();
        for (String key : input.keySet()) {
            Optional.ofNullable(input.get(key)).ifPresent(value -> {
                if (key.contains("_like")) {
                    condition.append(" and ").append(key.replaceFirst("_like", "").replace("_", ".")).append(" like CONCAT('%',:where_").append(key).append(",'%')");
                } else {
                    condition.append(" and ").append(key).append(" = :where_").append(key);
                }
            });
        }
        return DB.removeFirstSQLKeyWord(condition.toString()).trim();
    }
}
