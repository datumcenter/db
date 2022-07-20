package io.gallery.db.util;

import io.gallery.db.bean.DataBaseConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源工具
 */
public class DataSourceUtil {
    private static Log logger = LogFactory.getLog(DS.class);
    private static Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
    private static Map<String, NamedParameterJdbcTemplate> namedParameterJdbcTemplate = new ConcurrentHashMap<>();

    /**
     * 根据数据源conn,获取JdbcTemplate
     *
     * @param conn 数据源
     * @return JdbcTemplate
     */
    public static JdbcTemplate source(DataBaseConnection conn) {
        JdbcTemplate result = null;
        if (conn != null) {
            final String id = conn.getId();
            if (id != null) {
                result = jdbcTemplates.get(id);
                if (result == null) {
                    result = new JdbcTemplate() {{
                        setDataSource(new DriverManagerDataSource() {{
                            setDriverClassName(conn.getDriverClassName());
                            setUrl(conn.getUrl());
                            setUsername(conn.getUsername());
                            setPassword(conn.getPassword());
                        }});
                        logger.debug("Put [" + conn.getUrl() + "] of [" + id + "] in datasource cache");
                    }};
                    jdbcTemplates.put(id, result);
                }
            }
        }
        return result;
    }

    /**
     * 根据数据源conn,获取NamedParameterJdbcTemplate
     *
     * @param conn DataBaseConnection
     * @return NamedParameterJdbcTemplate
     */
    public static NamedParameterJdbcTemplate npsource(DataBaseConnection conn) {
        NamedParameterJdbcTemplate result = null;
        if (conn != null) {
            String id = conn.getId();
            if (id != null) {
                result = namedParameterJdbcTemplate.get(id);
                if (result == null) {
                    result = new NamedParameterJdbcTemplate(source(conn));
                    namedParameterJdbcTemplate.put(id, result);
                    logger.debug("Put [" + conn.getUrl() + "] of [" + id + "] in datasource cache");
                }
            }
        }
        return result;
    }

    public static void initSqliteColumns(DataBaseConnection conn) {
        try {
            NamedParameterJdbcTemplate source = DataSourceUtil.npsource(conn);
            Optional.ofNullable((Long) source.queryForObject("SELECT count(1) FROM sqlite_master where name='db_tablecolumns'", Maps.init(), Long.class)).ifPresent(t -> {
                if (t == 0L) {//表不存在
                    logger.info("create table [db_tablecolumns] in sqlite");
                    String sql = "create table 'db_tablecolumns' ('tablename' TEXT, 'name' TEXT, 'type' TEXT, 'pk' TEXT)";
                    source.update(sql, Maps.init());
                }
            });
            logger.info("init columns of sqlite in project [" + conn.getName() + "]");
            source.queryForList("select * from sqlite_master where type in ('table','view')", new HashMap<>()).stream().forEach(row -> {
                String tableName = (String) row.get("name");
                source.update("delete from db_tablecolumns where tablename=:tablename", new HashMap<String, Object>() {{
                    put("tablename", tableName);
                }});
                source.queryForList("PRAGMA table_info([" + tableName + "])", new HashMap<>()).stream().forEach(columns -> {
                    String type = (String) columns.get("type");
                    if (DBT.isNotNull(type) && type.contains("(")) {
                        columns.put("type", type.substring(0, type.indexOf("(")));
                    }
                    source.update("insert into db_tablecolumns(tablename,name,type,pk) values('" + tableName + "',:name,:type,:pk)", columns);
                });
            });
        } catch (Exception e) {
            logger.error("initSqlite error:" + e.getMessage());
        }
    }
}
