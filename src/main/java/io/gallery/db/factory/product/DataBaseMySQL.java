package io.gallery.db.factory.product;

import io.gallery.db.factory.AbstractDataBase;
import io.gallery.db.util.DBT;
import io.gallery.db.util.Maps;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DataBaseMySQL extends AbstractDataBase {

    @Override
    public String getDbType() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public String getTablesTableName() {
        return "information_schema.tables";
    }

    @Override
    public String getColumnsTableName() {
        return "information_schema.columns";
    }

    @Override
    public List listDb(NamedParameterJdbcTemplate source) {
        return source.queryForList("select *,schema_name as label from information_schema.schemata order by SCHEMA_NAME", Maps.init());
    }

    @Override
    public List listTable(NamedParameterJdbcTemplate source, String db) {
        return source.queryForList("select *,table_name as label from information_schema.tables where table_schema=:db order by table_type,table_name", Maps.init("db", db));
    }

    @Override
    public List listColumns(NamedParameterJdbcTemplate source, String db, String table) {
        List<Map<String, Object>> list = source.queryForList("select column_name as label,case when column_key='PRI' then '1' else '' end as pk,t.* from information_schema.columns t where table_name=:table and table_schema=:db order by ordinal_position", Maps.init("table", table).put("db", db));
        list.stream().forEach(v -> {
            String info = (String) v.get("COLUMN_TYPE");
            info += Optional.ofNullable(v.get("COLUMN_COMMENT")).map(cc -> (DBT.isNull((String) cc) ? "" : (" " + cc + " "))).orElse("");
            info += Optional.ofNullable(v.get("COLUMN_DEFAULT")).map(cd -> " = " + (cd == "" ? "''" : cd)).orElse("");
            v.put("label_info", info);
        });
        return list;
    }

    @Override
    public String getPageClause(Map input) {
        return DBT.isNull(super.getPageClause(input)) ? "" : " limit :start,:length ";
    }
}
