package io.gallery.db.bean;

import io.gallery.db.util.DB;
import io.gallery.db.util.DBT;
import io.gallery.db.util.Maps;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


@ApiModel(value = "业务入参")
public class DataBaseBO {
    @ApiModelProperty(value = "数据源id")
    private String id;
    @ApiModelProperty(value = "表名")
    private String tableName;
    @ApiModelProperty(value = "主键")
    private String keyName;
    @ApiModelProperty(value = "主键值")
    private String keyValue;
    @ApiModelProperty(value = "界面参数")
    private Map<String, Object> vo;
    @ApiModelProperty(value = "数据传输参数")
    private Maps dto = Maps.init();
    @ApiModelProperty(value = "条件参数")
    private Map<String, Object> where = new LinkedHashMap<>();
    @ApiModelProperty(value = "值参数")
    private Map<String, Object> values = new LinkedHashMap<>();
    @ApiModelProperty(value = "排序条件")
    private String orderBy;
    @ApiModelProperty(value = "分组条件")
    private String groupBy;

    public DataBaseBO buildForGet() {
        where.put(keyName, keyValue);
        where.keySet().stream().forEach(key -> dto.put("where_" + key, where.get(key)));//dto放入条件
        return this;
    }

    public DataBaseBO buildForDelete() {
        where.put(keyName, keyValue);
        where.keySet().stream().forEach(key -> dto.put("where_" + key, where.get(key)));//dto放入条件
        return this;
    }

    public DataBaseBO buildForUpdate() {
        values = vo;
        where.put(keyName, keyValue);
        values.keySet().stream().forEach(key -> dto.put("values_" + key, values.get(key)));//dto放入值
        where.keySet().stream().forEach(key -> dto.put("where_" + key, where.get(key)));//dto放入条件
        return this;
    }

    public DataBaseBO buildForInsert() {
        values = vo;
        values.keySet().stream().forEach(key -> dto.put("values_" + key, values.get(key)));//dto放入值
        return this;
    }

    public DataBaseBO buildForSelect() {
        Optional.ofNullable((String) vo.get("orderBy")).map(ob -> orderBy = ob);
        where = DB.removePublic(vo);
        where.keySet().stream().forEach(key -> dto.put("where_" + key, where.get(key)));//dto放入条件
        dto.put("start", vo.get("start")).put("length", vo.get("length"));//dto放入分页
        return this;
    }

    public static DataBaseBO init() {
        return new DataBaseBO();
    }

    public String getId() {
        return id;
    }

    public DataBaseBO setId(String id) {
        this.id = id;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public DataBaseBO setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getKeyName() {
        return keyName;
    }

    public DataBaseBO setKeyName(String keyName) {
        this.keyName = DBT.isNotNull(keyName) ? keyName : "id";
        return this;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public DataBaseBO setKeyValue(String keyValue) {
        this.keyValue = keyValue;
        return this;
    }

    public Map<String, Object> getVo() {
        return vo;
    }

    public DataBaseBO setVo(Map<String, Object> vo) {
        this.vo = vo;
        return this;
    }

    public Map<String, Object> getDto() {
        return dto;
    }

    public DataBaseBO setDto(Maps dto) {
        this.dto = dto;
        return this;
    }

    public Map<String, Object> getWhere() {
        return where;
    }

    public DataBaseBO setWhere(Map<String, Object> where) {
        this.where = where;
        return this;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public DataBaseBO setValues(Map<String, Object> values) {
        this.values = values;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public DataBaseBO setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public DataBaseBO setGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }


}
