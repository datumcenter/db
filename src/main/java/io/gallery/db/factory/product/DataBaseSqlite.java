package io.gallery.db.factory.product;

import io.gallery.db.factory.AbstractDataBase;
import io.gallery.db.util.DBT;
import io.gallery.db.util.Maps;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataBaseSqlite extends AbstractDataBase {

    @Override
    public String getDbType() {
        return "org.sqlite.JDBC";
    }

    @Override
    public String getTablesTableName() {
        return "sqlite_master";
    }

    @Override
    public String getColumnsTableName() {
        return "db_tablecolumns";
    }

    @Override
    public List listDb(NamedParameterJdbcTemplate source) {
        return new ArrayList<Map>() {{
            add(Maps.init("label", "main"));
        }};
    }

    @Override
    public List listTable(NamedParameterJdbcTemplate source, String db) {
        return source.queryForList("select *,name label from " + getTablesTableName() + " order by type,name", Maps.init());
    }

    @Override
    public List listColumns(NamedParameterJdbcTemplate source, String db, String table) {
        return source.queryForList("select *,name label,type label_info from " + getColumnsTableName() + " where tablename=:table", Maps.init("table", table));
    }

    @Override
    public String getPageClause(Map input) {
        return DBT.isNull(super.getPageClause(input)) ? "" : " limit :start,:length ";
    }


}
