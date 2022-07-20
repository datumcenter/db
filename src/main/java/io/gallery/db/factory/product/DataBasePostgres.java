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
public class DataBasePostgres extends AbstractDataBase {
    @Override
    public String getDbType() {
        return "org.postgresql.Driver";
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
        List<Map<String, Object>> list = source.queryForList(
                "select t.column_name as label,case when u.pk='1' then '1' else null end as pk,t.* from information_schema.columns t " +
                        " left join ( " +
                        " select pg_attribute.attname as column_name,pg_class.relname tablename,'1' as pk " +
                        " from pg_constraint join pg_class on pg_constraint.conrelid = pg_class.oid join pg_attribute on pg_attribute.attrelid = pg_class.oid and  pg_attribute.attnum = pg_constraint.conkey[1] " +
                        " where pg_constraint.contype='p' " +
                        ") u on t.column_name = u.column_name and t.table_name = u.tablename " +
                        " where table_name=:table and table_schema=:db order by ordinal_position", Maps.init("table", table).put("db", db));
        list.stream().forEach(v -> {
            String info = (String) v.get("udt_name");
            info += Optional.ofNullable(v.get("column_default")).map(cd -> " = " + (cd == "" ? "''" : cd)).orElse("");
            v.put("label_info", info);
        });
        return list;
    }

    @Override
    public String getPageClause(Map input) {
        return DBT.isNull(super.getPageClause(input)) ? "" : " limit :length offset :start";
    }
}
