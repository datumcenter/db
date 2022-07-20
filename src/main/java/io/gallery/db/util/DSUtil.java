package io.gallery.db.util;

import io.gallery.db.bean.DataBaseConnection;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DSUtil {
    /**
     * 根据项目id获取数据源
     *
     * @param id String
     * @return NamedParameterJdbcTemplate
     */
    public static NamedParameterJdbcTemplate init(String id) {
        return DataSourceUtil.npsource(DB.selectOne("select *,dburl url,dbtype driverclassname from search_project where id=#{id}", Maps.init("id", id), DataBaseConnection.class));
    }

}
