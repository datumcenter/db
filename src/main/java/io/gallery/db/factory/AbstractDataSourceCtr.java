package io.gallery.db.factory;

import io.gallery.db.bean.DataBaseBO;
import io.gallery.db.bean.DataBaseConnection;
import io.gallery.db.bean.DataBaseGenericPage;
import io.gallery.db.util.DBT;
import io.gallery.db.util.DataBaseAjaxResultContext;
import io.gallery.db.util.DataBaseTools;
import io.gallery.db.util.DataSourceUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * 数据源
 */
public abstract class AbstractDataSourceCtr {
    private final Log logger = LogFactory.getLog(getClass());

    @PostMapping(value = "{dsid}/{table}")
    @ApiOperation(value = "新增记录", notes = "数据")
    @ResponseBody
    public Object add(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                      @ApiParam(value = "表名", required = true) @PathVariable String table,
                      @RequestBody Map vo,
                      DataBaseAjaxResultContext result) {
        DataBaseConnection conn = getDataBaseConnection(dsid);
        AbstractDataBase adb = DBT.getDataBase(conn.getDriverClassName());
        NamedParameterJdbcTemplate source = DataSourceUtil.npsource(conn);
        DataBaseBO dbo = DataBaseBO.init().setId(dsid).setTableName(table).setVo(vo).buildForInsert();
        try {
            result.setSuccess(adb.insert(source, dbo) > 0);
            result.setResult(vo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result.setSuccess(false);
        }
        return result;
    }

    @DeleteMapping(value = "{dsid}/{table}/{key}/{id}")
    @ApiOperation(value = "根据主键删除记录", notes = "数据")
    @ResponseBody
    public Object delete(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                         @ApiParam(value = "表名", required = true) @PathVariable String table,
                         @ApiParam(value = "主键字段名", required = true) @PathVariable String key,
                         @ApiParam(value = "主键", required = true) @PathVariable String id,
                         DataBaseAjaxResultContext result) {
        DataBaseConnection conn = getDataBaseConnection(dsid);
        AbstractDataBase adb = DBT.getDataBase(conn.getDriverClassName());
        NamedParameterJdbcTemplate source = DataSourceUtil.npsource(conn);
        DataBaseBO dbo = DataBaseBO.init().setId(dsid).setTableName(table).setKeyName(key).setKeyValue(id).buildForDelete();
        try {
            result.setResult(adb.delete(source, dbo));
        } catch (Exception e) {
            result.setSuccess(false);
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @DeleteMapping(value = "{dsid}/{table}/{id}")
    @ApiOperation(value = "根据主键删除记录", notes = "数据")
    @ResponseBody
    public Object delete(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                         @ApiParam(value = "表名", required = true) @PathVariable String table,
                         @ApiParam(value = "主键", required = true) @PathVariable String id,
                         DataBaseAjaxResultContext result) {
        return delete(dsid, table, "id", id, result);
    }

    @PutMapping(value = "{dsid}/{table}/{key}/{id}")
    @ApiOperation(value = "根据主键修改记录", notes = "数据")
    @ResponseBody
    public Object edit(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                       @ApiParam(value = "表名", required = true) @PathVariable String table,
                       @ApiParam(value = "主键字段名", required = true) @PathVariable String key,
                       @ApiParam(value = "主键", required = true) @PathVariable String id,
                       @RequestBody Map vo,
                       DataBaseAjaxResultContext result) {
        DataBaseBO dbo = DataBaseBO.init().setId(dsid).setTableName(table).setKeyName(key).setKeyValue(id).setVo(vo).buildForUpdate();
        DataBaseConnection conn = getDataBaseConnection(dsid);
        AbstractDataBase adb = DBT.getDataBase(conn.getDriverClassName());
        NamedParameterJdbcTemplate source = DataSourceUtil.npsource(conn);
        try {
            result.setResult(adb.update(source, dbo));
        } catch (Exception e) {
            result.setSuccess(false);
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @PutMapping(value = "{dsid}/{table}/{id}")
    @ApiOperation(value = "根据主键修改记录", notes = "数据")
    @ResponseBody
    public Object edit(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                       @ApiParam(value = "表名", required = true) @PathVariable String table,
                       @ApiParam(value = "主键", required = true) @PathVariable String id,
                       @RequestBody Map vo,
                       DataBaseAjaxResultContext result) {
        return edit(dsid, table, "id", id, vo, result);
    }

    @GetMapping(value = "{dsid}/{table}/{key}/{id}")
    @ApiOperation(value = "根据主键获取信息", notes = "数据")
    @ResponseBody
    public Object get(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                      @ApiParam(value = "表名", required = true) @PathVariable String table,
                      @ApiParam(value = "主键字段名", required = true) @PathVariable String key,
                      @ApiParam(value = "主键", required = true) @PathVariable String id) {
        DataBaseConnection conn = getDataBaseConnection(dsid);
        AbstractDataBase adb = DBT.getDataBase(conn.getDriverClassName());
        NamedParameterJdbcTemplate source = DataSourceUtil.npsource(conn);
        DataBaseBO dbo = DataBaseBO.init().setId(dsid).setTableName(table).setKeyName(key).setKeyValue(id).buildForGet();
        return adb.get(source, dbo);
    }

    @GetMapping(value = "{dsid}/{table}/{id}")
    @ApiOperation(value = "根据主键获取信息", notes = "数据")
    @ResponseBody
    public Object get(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                      @ApiParam(value = "表名", required = true) @PathVariable String table,
                      @ApiParam(value = "主键", required = true) @PathVariable String id) {
        return get(dsid, table, "id", id);
    }


    @RequestMapping(value = "{dsid}/{table}/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "根据数据源id、表名获取记录列表", notes = "数据")
    @ResponseBody
    public Object list(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                       @ApiParam(value = "表名", required = true) @PathVariable String table,
                       DataBaseGenericPage input,
                       @RequestBody(required = false) Map body) {
        HttpServletRequest request = DBT.getRequest();
        Map<String, Object> params = DBT.getParamMap();
        DBT.merge(input, params);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            DBT.merge(body, params);
            input.setTree(Optional.ofNullable((Boolean) body.get("tree")).orElse(false));
            input.setPage(Optional.ofNullable((Boolean) body.get("page")).orElse(true));
        }
        DataBaseConnection conn = getDataBaseConnection(dsid);
        AbstractDataBase adb = DBT.getDataBase(conn.getDriverClassName());
        NamedParameterJdbcTemplate source = DataSourceUtil.npsource(conn);
        DataBaseBO dbo = DataBaseBO.init().setId(dsid).setTableName(table).setVo(params).buildForSelect();
        input.setData(adb.select(source, dbo));
        input.setTotal(adb.count(source, dbo));
        input.setRecordsFiltered(input.getTotal());
        return input;
    }

    @RequestMapping(value = "{dsid}/execute", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "根据数据源id、表名获取记录列表", notes = "数据")
    @ResponseBody
    public Object execute(@ApiParam(value = "数据源id", required = true) @PathVariable String dsid,
                          DataBaseGenericPage input,
                          @RequestBody(required = false) Map body,
                          DataBaseAjaxResultContext result) {
        HttpServletRequest request = DBT.getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        DBT.merge(input, params);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            DBT.merge(body, params);
        }
        DataBaseConnection conn = getDataBaseConnection(dsid);
        JdbcTemplate source = DataSourceUtil.source(conn);
        String sql = Optional.ofNullable(params.get("sql")).orElse(params.get("test"));
        List<List<LinkedHashMap<String, Object>>> list = null;
        try {
            if (DBT.isNotNull(sql)) {
                list = source.execute(sql, (PreparedStatementCallback<List<List<LinkedHashMap<String, Object>>>>) ps -> {
                    boolean resultsAvailable = ps.execute();
                    List<List<LinkedHashMap<String, Object>>> listResult = new ArrayList<>();
                    //遍历结果集
                    while (resultsAvailable) {
                        ResultSet resultSet = ps.getResultSet();
                        List<LinkedHashMap<String, Object>> subList = new ArrayList<>();
                        while (resultSet.next()) {
                            ResultSetMetaData meta = resultSet.getMetaData();
                            int colcount = meta.getColumnCount();
                            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                            for (int i = 1; i <= colcount; i++) {
                                String name = meta.getColumnLabel(i);
                                map.put(name, resultSet.getObject(i));
                            }
                            subList.add(map);
                        }
                        listResult.add(subList);
                        resultsAvailable = ps.getMoreResults();
                    }
                    return listResult;
                });
            } else {
                result.setSuccess(false);
            }
        } catch (Exception e) {
            if (conn != null && conn.getId() != null) {
                logger.error("[" + conn.getDriverClassName() + "] does not support：" + e.getMessage());
            } else {
                logger.debug("connection is not exist");
            }
            result.setResult(e.getMessage());
            result.setSuccess(false);
        }
        if (DBT.isNotNull(sql) && result.getSuccess()) {
            result.setResult(list);
        }
        return result;
    }

    /*--------------------------------------------------------------------------------------------------*/

    @GetMapping("db/{id}/list")
    @ApiOperation(value = "根据数据源id获取所有库信息", notes = "数据")
    @ResponseBody
    public Object dblist(@ApiParam(value = "数据源id", required = true) @PathVariable String id) {
        DataBaseConnection conn = getDataBaseConnection(id);
        return DBT.getDataBase(conn.getDriverClassName()).listDb(DataSourceUtil.npsource(conn));
    }

    @GetMapping("table/{id}/{db}/list")
    @ApiOperation(value = "根据数据源id、数据库名获取所有表信息", notes = "数据")
    @ResponseBody
    public Object tablelist(@ApiParam(value = "数据源id", required = true) @PathVariable String id,
                            @ApiParam(value = "数据库名") @PathVariable String db) {
        DataBaseConnection conn = getDataBaseConnection(id);
        return DBT.getDataBase(conn.getDriverClassName()).listTable(DataSourceUtil.npsource(conn), db);
    }

    @GetMapping("column/{id}/{db}/{table}/list")
    @ApiOperation(value = "根据数据源id、数据库名、表名获取所有字段信息", notes = "数据")
    @ResponseBody
    public Object columnlist(@ApiParam(value = "数据源id", required = true) @PathVariable String id,
                             @ApiParam(value = "数据库名") @PathVariable String db,
                             @ApiParam(value = "表名", required = true) @PathVariable String table) {
        DataBaseConnection conn = getDataBaseConnection(id);
        return DBT.getDataBase(conn.getDriverClassName()).listColumns(DataSourceUtil.npsource(conn), db, table);
    }

    @ApiOperation(value = "根据数据源id获取数据源信息", notes = "数据")
    public abstract DataBaseConnection getDataBaseConnection(String id);
}
