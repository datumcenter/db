package io.gallery.db.service.impl;

import io.gallery.db.bean.*;
import io.gallery.db.exception.DataBaseDataBindingException;
import io.gallery.db.mapper.DataBaseMapper;
import io.gallery.db.service.IDataBaseCache;
import io.gallery.db.service.IDataBaseGenericService;
import io.gallery.db.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公共服务接口默认实现
 *
 * @param <Entity> Entity
 * @param <Query>  Query
 */
@Service
public class DataBaseGenericService<Entity, Query> implements IDataBaseGenericService<Entity, Query>, Cloneable {
    private static final Log logger = LogFactory.getLog(DataBaseGenericService.class);

    @Autowired
    DataBaseConfig dataBaseConfig;

    private DataBaseMapper dataBaseMapper;
    protected String tableName = "";
    protected String keyName = "";
    protected String[] forginKeyName;

    protected String[] forginTableName;
    protected String[] forginTableKeyName;

    private DataBaseMapper getMapper() {
        if (dataBaseMapper == null) {
            dataBaseMapper = (DataBaseMapper) DataBaseSpringUtil.getBean("dataBaseMapper");
        }
        return dataBaseMapper;
    }

    @Transactional
    @Override
    public int insert(Entity record) {
        int insert = 0;
        if (record != null) {
            Map<String, Object> input = new HashMap<>();
            input.put("table", getTableName(false));
            Map<String, Object> values = removeRedundantCode(DBT.objectToMap(record));
            input.put("values", values);
            if (values != null && values.size() > 0) {
                insert = getMapper().insert(input);
                if (record instanceof Map) {
                    setGeneratedMapKey((Map) record, input);
                } else {
                    Type genericSuperclass = getClass().getGenericSuperclass();
                    if (Object.class.getName().equalsIgnoreCase(genericSuperclass.getTypeName())) {
                        if (Object.class.getName().equalsIgnoreCase(record.getClass().getGenericSuperclass().getTypeName())) {
                            setGeneratedMapKey(DBT.objectToMap(record), input);
                        } else {
                            setGeneratedClazzKey(record, input, (Class<Entity>) record.getClass().getGenericSuperclass());
                        }
                    } else {
                        if (genericSuperclass instanceof ParameterizedType) {
                            Type type = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
                            if (type instanceof Class) {
                                Class<Entity> clazz = (Class<Entity>) type;
                                setGeneratedClazzKey(record, input, clazz);
                            } else if (type instanceof TypeVariable) {
                                Class<Entity> clazz = (Class<Entity>) ((TypeVariable) type).getBounds()[0];
                                setGeneratedClazzKey(record, input, clazz);
                            } else {
                                setGeneratedMapKey(DBT.objectToMap(record), input);
                            }
                        } else {
                            setGeneratedClazzKey(record, input, (Class<Entity>) genericSuperclass);
                        }
                    }
                }
            }
        }
        return insert;
    }


    @Transactional
    @Override
    public int delete(Object id) {
        Map<String, Object> input = new HashMap<>();
        String tableName = getTableName(false);
        input.put("table", tableName);
        Map<String, Object> where = new HashMap<>();
        where.put(dealColumnName(getKeyName(), tableName, getColumns(tableName)), id);
        input.put("where", where);
        if (id instanceof String && ((String) id).contains(",")) {
            return deleteByIds((String) id);
        } else {
            return getMapper().delete(input);
        }
    }

    @Override
    public DataBaseAjaxResultContext deleteWithBusiness(Object id) {
        return new DataBaseAjaxResultContext(delete(id));
    }

    @Transactional
    @Override
    public int deleteByIds(String ids) {
        if (DBT.isNotNull(ids)) {
            String tableName = getTableName(false);
            Map<String, Object> input = new HashMap<>();
            input.put("table", tableName);
            Map<String, Object> where = new HashMap<>();
            where.put(dealColumnName(getKeyName(), tableName, getColumns(tableName)), ids);
            input.put("where", where);
            return getMapper().delete(input);
        } else {
            return 0;
        }
    }

    @Transactional
    @Override
    public int deleteByParams(Query condition) {
        Map<String, Object> input = new HashMap<>();
        input.put("table", getTableName(false));
        Map<String, Object> where = DBT.objectToMap(condition);
        if (where != null && where.keySet().size() > 0) {
            Set<String> columnFullNames = getColumns(getTableName(false));
            input.put("where", dealWhere(where, columnFullNames));
            return getMapper().deleteByParams(input);
        } else {
            return 0;
        }
    }

    @Transactional
    @Override
    public int update(Entity record) {
        Map<String, Object> values = removeRedundantCode(DBT.objectToMapWithNull(record));
        values.remove("dbuid");
        values.remove("dbctime");
        values.remove("dbcuid");
        values.remove("dborgid");
        values.remove("dbdepid");
        Map<String, Object> input = new HashMap<>();
        String tableName = getTableName(false);
        input.put("values", values);
        input.put("table", tableName);
        String keyName = dealColumnName(getKeyName(), tableName, getColumns(tableName));
        Object id = values.get(keyName);
        values.remove(keyName);
        Map<String, Object> where = new HashMap<>();
        where.put(keyName, id);
        input.put("where", where);
        return getMapper().update(input);
    }

    @Override
    public Entity get(Object id) {
        return get(id, null);
    }

    @Override
    public DataBaseAjaxResultContext getWithBusiness(Object id) {
        return new DataBaseAjaxResultContext(get(id));
    }

    @Override
    public <T> T get(Object id, Class<T> clazz) {
        if (id == null) {
            return null;
        }
        HttpServletRequest request = DBT.getRequest();
        boolean needView = false;
        if (request != null) {
            needView = "true".equalsIgnoreCase(request.getParameter("needView"));
        }
        String tableName = getTableName(false);
        if (needView) {
            getTableName(true);
        }
        if (!containsColumn(getKeyName(), getColumns(tableName))) {
            String info = "默认主键字段[" + getKeyName() + "]不存在（请传入主键名）";
            throw new DataBaseDataBindingException(info, null);
        }
        List<T> list = selectCommon(Maps.init(tableName + "." + getKeyName(), id).put("needView", needView).put("length", 1), clazz);
        return list.stream().findFirst().orElse(null);
    }

    /**
     * 分页查询
     *
     * @param condition Query
     * @return List
     */
    @Override
    public List<Entity> select(Query condition) {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type type = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
            if (type instanceof Class) {
                return select(condition, (Class<Entity>) type);
            } else if (type instanceof TypeVariable) {
                return select(condition, (Class<Entity>) ((TypeVariable) type).getBounds()[0]);
            }
        }
        return select(condition, null);
    }

    /**
     * 分页查询
     *
     * @param condition Map
     * @return List
     */
    private <T> List<T> selectCommon(Map condition, Class<T> clazz) {
        HttpServletRequest request = getRequest();
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> where = Optional.ofNullable(condition).orElse(Maps.init());
        Boolean needView = (Boolean) where.get("needView");
        input.put("needDict", where.get("needDict"));
        input.put("needDicts", where.get("needDicts"));
        input.put("needKey", where.get("needKey"));
        input.put("treePlain", where.get("treePlain"));
        Boolean tree = (Boolean) where.get("tree");
        String tableName = getTableName(false);
        if (needView != null && needView) {
            tableName = getTableName(true);
        }
        input.put("table", tableName);
        if (tree != null && tree) {
            input.put("keyName", dealColumnName(getKeyName(), tableName, getColumns(tableName)));
            input.put("tree", true);
            input.put("treeColumn", dealColumnName((String) Optional.ofNullable(where.get("treeColumn")).orElse(dataBaseConfig.getTreeColumn()), tableName, getColumns(tableName)));
        }
        Set<String> columnFullNames = getColumns(tableName);
        setForgien(input, columnFullNames, tableName);
        setLimit(input, where);
        setGroupBy(input, where, columnFullNames);
        setOrder(input, where, columnFullNames);
        input.put("where", dealWhere(where, columnFullNames));
        input.put("dataBaseConfig", dataBaseConfig);
        if (request != null) {
            input.put("defaultDb", request.getAttribute("defaultDb"));
        }
        List list = null;
        try {
            list = getMapper().select(input);
            String columnCase = dataBaseConfig.getColumnCase();
            List result = new ArrayList();
            for (Object o : list) {
                if (o instanceof Map) {
                    DBT.dealMegaText((Map) o);
                    if (DBT.isNotNull(columnCase)) {
                        if (columnCase.contains("l")) {
                            o = DBT.mapKeyCase((Map) o, true);
                        } else {
                            o = DBT.mapKeyCase((Map) o, false);
                        }
                    }
                }
                result.add(o);
            }
            list = result;
        } catch (Exception e) {
            Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
            throw new DataBaseDataBindingException("执行失败，请确认入参是否合法：" + where, e);
        }
        Object groupByClause = input.get("groupByClause");
        if (groupByClause == null || DBT.isNull(groupByClause.toString())) {//分组不需要所有字段
            if (dataBaseConfig.isColumnAll()) {
                if (columnFullNames.size() > 0) {//回填空值字段
                    for (String name : columnFullNames) {
                        name = DBT.getColumnName(name);
                        String key;
                        if (name.contains(".")) {
                            String[] names = name.split("\\.");
                            if (tableName.contains(".")) {
                                tableName = tableName.split("\\.")[1];
                            }
                            if (names[0].equalsIgnoreCase(tableName)) {
                                key = names[1];
                            } else {
                                key = names[0] + "_" + names[1];
                            }
                        } else {
                            key = name;
                        }
                        for (Object o : list) {
                            Map map = (Map) o;
                            map.putIfAbsent(key, null);
                        }
                    }
                }
            }
        }
        if (tree != null && tree) {//是否显示树结构
            list = DBT.dealTree(list, getKeyName(), (String) input.get("treeColumn"), input.get("treePlain") != null && (boolean) input.get("treePlain"));
        }
        List<T> result = new ArrayList<>();
        if (clazz != null && Map.class.getName().equalsIgnoreCase(clazz.getName())) {//传入了要转换的类型,并且类型是Map
            return list;
        } else if (clazz == null) {//为空，但泛型可能定义了Bean类型
            for (Object o : list) {
                result.add((T) getEntity((Map) o));
            }
            return result;
        }
        for (Object o : list) {//传入了要转换的类型
            Map map = (Map) o;
            result.add((T) DBT.mapToBeanIngnoreCase(map, clazz));
        }
        return result;
    }

    /**
     * 分页查询
     *
     * @param condition Query
     * @return List
     */
    @Override
    public <T> List<T> select(Query condition, Class<T> clazz) {
        return selectCommon(DBT.objectToMap(condition), clazz);
    }

    @Override
    public Entity selectOne(Query condition) {
        return select(condition).stream().findFirst().orElse(null);
    }

    @Override
    public <T> T selectOne(Query condition, Class<T> clazz) {
        return select(condition, clazz).stream().findFirst().orElse(null);
    }

    /**
     * 数量
     *
     * @param condition List
     * @return long
     */
    @Override
    public long count(Query condition) {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> where = Optional.ofNullable(DBT.objectToMap(condition)).orElse(Maps.init());
        Boolean needView = (Boolean) where.get("needView");
        String tableName = getTableName(false);
        if (needView != null && needView) {
            tableName = getTableName(true);
        }
        input.put("table", tableName);
        Boolean tree = (Boolean) where.get("tree");
        if (tree != null && tree) {
            input.put("keyName", dealColumnName(getKeyName(), tableName, getColumns(tableName)));
            input.put("tree", true);
            input.put("treeColumn", dealColumnName((String) Optional.ofNullable(where.get("treeColumn")).orElse(dataBaseConfig.getTreeColumn()), tableName, getColumns(tableName)));
        }
        Set<String> columnFullNames = getColumns(tableName);
        input.put("dataBaseConfig", dataBaseConfig);
        setForgien(input, columnFullNames, tableName);
        setGroupBy(input, where, columnFullNames);
        input.put("where", dealWhere(where, columnFullNames));
        return getMapper().count(input);
    }

    @Override
    public boolean exist(Query query) {
        return count(query) > 0;
    }

    /**
     * Map转实体
     *
     * @param map Map
     * @return Entity
     */
    @Override
    public Entity getEntity(Map map) {
        if (map != null) {
            Type genericSuperclass = getClass().getGenericSuperclass();
            if (Object.class.getName().equalsIgnoreCase(genericSuperclass.getTypeName())) {
                return (Entity) map;
            }
            if (genericSuperclass instanceof ParameterizedType) {
                Type type = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
                if (type instanceof Class) {
                    Class<Entity> clazz = (Class<Entity>) type;
                    if (Map.class.getName().equalsIgnoreCase(clazz.getName())) {
                        return (Entity) map;
                    }
                    return (Entity) DBT.mapToBeanIngnoreCase(map, clazz);
                } else if (type instanceof TypeVariable) {
                    Class<Entity> clazz = (Class<Entity>) ((TypeVariable) type).getBounds()[0];
                    if (Map.class.getName().equalsIgnoreCase(clazz.getName())) {
                        return (Entity) map;
                    } else {
                        return (Entity) DBT.mapToBeanIngnoreCase(map, clazz);
                    }
                } else {
                    return (Entity) map;
                }
            } else {
                return (Entity) map;
            }
        } else {
            return null;
        }
    }

    /**
     * 是否是子类
     *
     * @return boolean
     */
    protected boolean isSonClass() {
        boolean result = false;
        if (this.getClass().getSuperclass().getName().equalsIgnoreCase(DataBaseGenericService.class.getName())) {
            result = true;
        }
        return result;
    }


    /**
     * 获取默认表名或视图名
     *
     * @param needView 是否需要返回视图名
     * @return table|db.table
     */
    @Override
    public String getTableName(boolean needView) {
        String result = "";
        HttpServletRequest request = getRequest();
        if (!isSonClass() && request != null) {
            result = (String) request.getAttribute("tableName");
        }
        if (DBT.isNull(result) && DBT.isNotNull(this.tableName)) {
            result = this.tableName;
        } else {
            if (DBT.isNull(result)) {
                String simpleName = this.getClass().getSimpleName();
                if (simpleName.contains("ServiceImpl")) {
                    result = DBT.subString(simpleName, 0, simpleName.lastIndexOf("ServiceImpl"));
                }
                if (simpleName.contains("Impl")) {
                    result = DBT.subString(simpleName, 0, simpleName.lastIndexOf("Impl"));
                }
            }
        }
        if (DBT.isNotNull(result)) {
            result = dealTableName(result);
        } else {
            logger.error("common-db error: tableName is not config!! ");
            result = "";
        }
        //table->v_table|db.table->db.v_table
        return needView ? result.contains(".") ? result.replace(".", ".v_") : "v_" + result : result;
    }


    /**
     * 设置表名
     *
     * @param tableName String
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取默认主键名
     *
     * @return String
     */
    @Override
    public String getKeyName() {
        String result = "id";
        String key = "";
        HttpServletRequest request = getRequest();
        if (!isSonClass() && request != null) {
            key = (String) request.getAttribute("keyName");
            if (DBT.isNotNull(key)) {
                result = key;
            }
        }
        if (DBT.isNull(key) && DBT.isNotNull(this.keyName)) {
            result = this.keyName;
        }
        return result;
    }

    /**
     * 设置排序
     *
     * @param input           Map
     * @param where           Map
     * @param columnFullNames Set
     */
    private void setOrder(Map<String, Object> input, Map<String, Object> where, Set<String> columnFullNames) {
        String orderBy = "";
        String groupByClause = getGroupBy(input);
        List<String> result = new ArrayList<>();
        boolean hasGroupByClause = false;
        if (DBT.isNotNull(groupByClause)) {
            hasGroupByClause = true;
            Set temp = new HashSet<>();
            for (String groupColumn : groupByClause.split(",")) {
                if (DBT.isNotNull(groupColumn)) {
                    if (columnFullNames != null && columnFullNames.size() > 0) {
                        for (String name : columnFullNames) {
                            if (groupColumn.contains(".")) {
                                if (groupColumn.equalsIgnoreCase(name)) {
                                    temp.add(name);
                                }
                            } else {
                                if (name.toLowerCase().contains("." + groupColumn.toLowerCase())) {
                                    temp.add(name);
                                }
                            }
                        }
                    }
                }
            }
            columnFullNames = temp;
        }
        if (where != null) {
            Object orderClause = Optional.ofNullable(where.get("orderClause")).orElse(where.get("orderBy"));
            if (orderClause != null) {
                orderBy = (String) orderClause;
            } else {
                orderBy = DataBaseRequestUtil.getOrderByClause(getRequest());
            }
        } else {
            orderBy = DataBaseRequestUtil.getOrderByClause(getRequest());
        }
        if (DBT.isNotNull(orderBy)) {
            for (String columnAndOrder : orderBy.split(",")) {
                if (DBT.isNotNull(columnAndOrder)) {
                    columnAndOrder = columnAndOrder.trim();
                    if (columnAndOrder.contains(" ")) {//有字段名和排序
                        String[] columnAndOrderArray = columnAndOrder.split(" ");
                        String column = columnAndOrderArray[0];
                        String order = columnAndOrderArray[1];
                        String nullinfo = "";
                        if (order.equalsIgnoreCase("asc")
                                || columnAndOrderArray[1].equalsIgnoreCase("desc")) {
                            if ((hasGroupByClause && dataBaseConfig.getGroupByAlia().equalsIgnoreCase(column))
                                    || orderHasColumn(column, columnFullNames, hasGroupByClause)) {
                                if (columnAndOrderArray.length == 4) {
                                    if (DataBasePlatform.postgres.name().equals(dataBaseConfig.getPlatform())
                                            && "nulls".equalsIgnoreCase(columnAndOrderArray[2])
                                            && ("last".equalsIgnoreCase(columnAndOrderArray[3]) || "first".equalsIgnoreCase(columnAndOrderArray[3]))) {
                                        nullinfo = columnAndOrderArray[2] + " " + columnAndOrderArray[3];
                                    } else if (DataBasePlatform.mysql.name().equals(dataBaseConfig.getPlatform()) && "is".equalsIgnoreCase(columnAndOrderArray[2])
                                            && ("null".equalsIgnoreCase(columnAndOrderArray[3]))) {
                                        nullinfo = columnAndOrderArray[2] + " " + columnAndOrderArray[3];
                                    }
                                    result.add(column + " " + order + " " + nullinfo);
                                } else {
                                    result.add(column + " " + order);
                                }
                            }
                        }
                    } else //只有字段名
                        if ((hasGroupByClause && dataBaseConfig.getGroupByAlia().equalsIgnoreCase(columnAndOrder))
                                || orderHasColumn(columnAndOrder, columnFullNames, hasGroupByClause)) {
                            result.add(columnAndOrder);
                        }
                }
            }
        }
        if (where != null) {//随机检索功能
            String randomClause = "";
            Object randomValue = where.get("random");
            boolean random = false;
            if (randomValue instanceof Boolean) {
                random = Optional.of((Boolean) randomValue).orElse(false);
            }
            if (randomValue instanceof String) {
                random = Optional.of(Boolean.valueOf((String) where.get("random"))).orElse(false);
            }
            if (DataBasePlatform.postgres.name().equals(dataBaseConfig.getPlatform()) && random) {
                randomClause = "random()";
            } else if (DataBasePlatform.mysql.name().equals(dataBaseConfig.getPlatform()) && random) {
                randomClause = "rand()";
            }
            where.remove("random");
            if (result.size() > 0) {
                String orderClause = result.stream().collect(Collectors.joining(","));
                if (DBT.isNotNull(randomClause)) {
                    orderClause += ("," + randomClause);
                }
                input.put("orderClause", orderClause);
            } else if (DBT.isNotNull(randomClause)) {
                input.put("orderClause", randomClause);
            }
        }
    }

    /**
     * 排序语句是否包含字段
     *
     * @param column           String
     * @param columns          Set
     * @param hasGroupByClause boolean
     * @return boolean
     */
    private boolean orderHasColumn(String column, Set<String> columns, boolean hasGroupByClause) {
        boolean result = false;
        if (DBT.isNotNull(column)) {
            if (columns != null && columns.size() > 0) {
                for (String name : columns) {
                    if (column.contains(".")) {
                        if (column.equalsIgnoreCase(name)) {
                            result = true;
                        }
                    } else {
                        if (name.toLowerCase().contains("." + column.toLowerCase())) {
                            result = true;
                        }
                    }
                }
            }
            if (!result) {
                Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
                String info;
                if (hasGroupByClause) {
                    info = "排序字段[" + column + "]在分组条件中不存在（请确认）";
                } else {
                    info = "排序字段[" + column + "]不存在（请检查表名或字段名是否存在）";
                }
                if (dataBaseConfig.isErrorWhere()) {
                    throw new DataBaseDataBindingException(info, null);
                } else {
                    logger.debug(info);
                }
            }
        }
        return result;
    }

    /**
     * 设置分组
     *
     * @param input           Map
     * @param where           Map
     * @param columnFullNames Set
     */
    private void setGroupBy(Map<String, Object> input, Map<String, Object> where, Set<String> columnFullNames) {
        if (where != null) {
            Object groupByClause = Optional.ofNullable(where.get("groupByClause")).orElse(where.get("groupBy"));
            if (groupByClause != null) {
                String columns = Arrays.asList(groupByClause.toString().split(",")).stream().filter(column -> containsColumn(column, columnFullNames)).collect(Collectors.joining(","));
                if (DBT.isNotNull(columns)) {//设置被分组字段
                    input.put("columns", columns + ", count(1) " + dataBaseConfig.getGroupByAlia());
                    input.put("groupByClause", columns);
                }
            }
        }
    }

    /**
     * 获取分组
     *
     * @param input Map
     * @return String
     */
    private String getGroupBy(Map<String, Object> input) {
        return Optional.ofNullable(input.get("groupByClause")).map(Object::toString).map(String::trim).orElse(null);
    }

    /**
     * 设置分页
     *
     * @param input Map
     * @param where Map
     */
    private void setLimit(Map<String, Object> input, Map<String, Object> where) {
        if (where != null) {
            Map<String, Object> limit = new HashMap<>();
            Object start = where.get("start");
            Object length = where.get("length");
            if (length != null) {
                if (start == null) {
                    start = 0;
                }
                if (start instanceof String) {
                    String string = (String) start;
                    if (DBT.isNotNull(string)) {
                        try {
                            limit.put("start", Integer.valueOf(string));
                        } catch (NumberFormatException e) {
                            throw new DataBaseDataBindingException("分页字段[start]格式应为数字，请确认", e);
                        }
                    }
                } else if (start instanceof Number) {
                    limit.put("start", start);
                }
                if (length instanceof String) {
                    String string = (String) length;
                    if (DBT.isNotNull(string)) {
                        try {
                            limit.put("length", Integer.valueOf((String) length));
                        } catch (NumberFormatException e) {
                            throw new DataBaseDataBindingException("分页字段[length]格式应为数字，请确认", e);
                        }
                    }
                } else if (length instanceof Number) {
                    limit.put("length", length);
                }
                input.put("limit", limit);
            }
        }
    }

    /**
     * 设置外键
     *
     * @param input           Map
     * @param columnFullNames Set
     * @param tableName       String
     */
    private void setForgien(Map<String, Object> input, Set<String> columnFullNames, String tableName) {
        StringBuilder columns = new StringBuilder();
        String deafultPlatform = Optional.ofNullable(dataBaseConfig).map(DataBaseConfig::getPlatform).orElse("");
        if (getForginTableName() != null && getForginKeyName() != null && getForginTableKeyName() != null) {
            input.put("forginKeyName", dealColumnNames(getForginKeyName(), null, columnFullNames, tableName));
            input.put("forginTableName", dealTableNames(getForginTableName()));
            input.put("forginTableKeyName", dealColumnNames(getForginTableKeyName(), getForginTableName(), columnFullNames, tableName));
        }
        columns.append(Optional.ofNullable(columnFullNames).map(list -> list.stream().map(columName -> {//处理字段名
            String result = columName;
            columName = DBT.getColumnName(columName);
            String[] split = columName.split("\\.");
            if (split.length == 1) {//没有.
                split = ("." + columName).split("\\.");
            }
            String[] splitTableName = tableName.split("\\.");
            if (splitTableName.length == 2 && splitTableName[1].equalsIgnoreCase(split[0])
                    || splitTableName.length == 1 && tableName.equalsIgnoreCase(split[0])) {
                if (DataBasePlatform.sqlserver.name().equals(deafultPlatform)) {
                    result = "[" + split[0] + "]" + "." + split[1];//[表名].字段名
                } else {
                    result = columName;
                }
            }
            String[] forginTableNames = getForginTableName();
            if (forginTableNames != null) {
                for (String forginTableName : forginTableNames) {
                    String[] splitForginTableName = forginTableName.split("\\.");
                    if (splitForginTableName.length == 2 && splitForginTableName[1].equalsIgnoreCase(split[0])
                            || splitForginTableName.length == 1 && forginTableName.equalsIgnoreCase(split[0])) {
                        if (DataBasePlatform.sqlserver.name().equals(deafultPlatform)) {
                            return "[" + split[0] + "]" + "." + split[1] + " as " + split[0] + "_" + split[1];//[表名].字段名 as 表名_字段名
                        } else {
                            return columName + " as " + split[0] + "_" + split[1]; //字段名 as 表名_字段名
                        }
                    }
                }
            }
            return result;
        }).collect(Collectors.joining(","))).orElse(""));
        input.put("columns", columns.toString());
        input.put("columnWithTypes", columnFullNames);
    }

    @Override
    public List<String> getColumnList(boolean needView) {
        return getColumnsFromDB(getTableName(needView));
    }

    /**
     * 获取字段列表
     *
     * @param tableName String
     * @return Set {'table.id::int',''table.name::varchar''}
     */
    private Set<String> getColumns(String tableName) {
        Set<String> columnFullNames = new HashSet<>();
        String[] tableNameInfo = tableName.split("\\.");
        if (tableNameInfo.length != 2) {
            tableNameInfo = ("." + tableName).split("\\.");
        }
        //获取主表字段
        if (tableNameInfo.length == 2) {
            List<String> columnsFromDB = getColumnsFromDB(tableName);
            if (columnsFromDB != null) {
                for (String columName : columnsFromDB) {
                    columnFullNames.add(tableNameInfo[1] + "." + columName);
                }
            }
        }
        if (getForginTableName() != null && getForginKeyName() != null && getForginTableKeyName() != null) {
            //获取关联表字段
            for (String forginTableNameInfo : getForginTableName()) {
                String[] tableInfo = forginTableNameInfo.split("\\.");
                if (tableInfo.length != 2) {
                    tableInfo = ("." + forginTableNameInfo).split("\\.");
                }
                if (tableInfo.length == 2) {
                    List<String> columnsFromDB = getColumnsFromDB(forginTableNameInfo);
                    if (columnsFromDB != null) {
                        for (String columName : columnsFromDB) {
                            columnFullNames.add(tableInfo[1] + "." + columName);
                        }
                    }
                }
            }
        }
        return columnFullNames;
    }

    /**
     * 移除非法字段
     *
     * @param where           Map
     * @param columnFullNames Set
     * @return Map
     */
    private Map<String, Object> dealWhere(Map<String, Object> where, Set<String> columnFullNames) {
        Map<String, Object> result = new HashMap<>();
        if (where != null) {
            if (columnFullNames != null && columnFullNames.size() > 0) {
                where = DataBaseUtil.removePublic(where);
                for (String whereClumnName : where.keySet()) {
                    Object value = where.get(whereClumnName);
                    if (DataBasePlatform.postgres.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {//需要转型
                        if (value instanceof String && DBT.isNotNull(value.toString()) && DataBaseValue.containsOperator(value.toString())) {//前端传入转型语句
                            ClauseValue clauseValue = DataBaseValue.dealValueClause(value.toString());
                            if (clauseValue != null) {
                                value = clauseValue;
                            }
                        } else if (value instanceof String && DBT.isNotNull(value.toString())) {//前端没传入转型语句
                            String columnType = getColumnType(whereClumnName, columnFullNames);
                            if (DBT.isNotNull(columnType)) {
                                String finalValue = value.toString();
                                value = new ClauseValue() {{
                                    setOriginal(finalValue);
                                    setValue(finalValue);
                                    setOperator(DataBaseValue.doublue_colon.getOperator() + columnType);
                                    if (DataBaseOperator._not_in.getOperator().equalsIgnoreCase(DataBaseOperator.getOperator(whereClumnName))
                                            || DataBaseOperator._in.getOperator().equalsIgnoreCase(DataBaseOperator.getOperator(whereClumnName))) {
                                        setValueList(Arrays.asList(finalValue.split(",")));
                                    }
                                }};
                            }
                        }
                    } else {
                        String columnType = getColumnType(whereClumnName, columnFullNames);
                        if (DBT.isNotNull(columnType)) {
                            String finalValue = value.toString();
                            value = new ClauseValue() {{
                                setOriginal(finalValue);
                                setValue(finalValue);
                                setOperator("");
                                if (DataBaseOperator._not_in.getOperator().equalsIgnoreCase(DataBaseOperator.getOperator(whereClumnName))
                                        || DataBaseOperator._in.getOperator().equalsIgnoreCase(DataBaseOperator.getOperator(whereClumnName))) {
                                    setValueList(Arrays.asList(finalValue.split(",")));
                                }
                            }};
                        }
                    }
                    if (value instanceof List) {//防止注入转成ClauseValue
                        List finalValue = (List) value;
                        value = new ClauseValue() {{
                            if (DataBaseOperator._not_in.getOperator().equalsIgnoreCase(DataBaseOperator.getOperator(whereClumnName))
                                    || DataBaseOperator._in.getOperator().equalsIgnoreCase(DataBaseOperator.getOperator(whereClumnName))) {
                                setValueList(finalValue);
                            }
                        }};
                    }
                    if (whereClumnName.equalsIgnoreCase("alias") ||
                            whereClumnName.equalsIgnoreCase("userFullNames") ||
                            whereClumnName.equalsIgnoreCase("dict") ||
                            whereClumnName.equalsIgnoreCase("dicts") ||
                            containsColumn(whereClumnName, columnFullNames)) {
                        //","为关键字,table.column -> table*column
                        result.put(whereClumnName.replace(".", "*").replace(",", "|"), value);
                    }
                }
            }
        }
        //分解逗号隔开的字段
        for (String key : result.keySet()) {
            if (DBT.isNotNull(key)) {
                if (key.toLowerCase().contains(DataBaseOperator._left_like.name())
                        || key.toLowerCase().contains(DataBaseOperator._left_not_like.name())
                        || key.toLowerCase().contains(DataBaseOperator._right_like.name())
                        || key.toLowerCase().contains(DataBaseOperator._right_not_like.name())
                    // || key.toLowerCase().contains(DataBaseOperator._like.name())
                ) {
                    Object object = result.get(key);
                    if (object != null) {
                        if (object instanceof String) {
                            String values = (String) object;
                            if (values.contains(",")) {
                                Map<String, Object> orWhere = new LinkedHashMap<>();
                                String[] valueArray = values.split(",", -1);
                                for (int i = 0; i < valueArray.length; i++) {
                                    orWhere.put(String.valueOf(i), valueArray[i]);
                                }
                                result.put(key, orWhere);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 是否包含字段
     *
     * @param column  String
     * @param columns Set
     * @return boolean
     */
    private boolean containsColumn(String column, Set<String> columns) {
        boolean result = false;
        int columnNum = 0;
        if (DBT.isNotNull(column)) {
            String[] realColumns = DataBaseOperator.getRealColumn(column).split(DataBaseOperator._or_);
            for (String realColumn : realColumns) {
                realColumn = realColumn.toLowerCase();
                int num = 0;
                for (String clumnName : columns) {
                    clumnName = DBT.getColumnName(clumnName);
                    if (DBT.isNotNull(clumnName)) {
                        clumnName = clumnName.toLowerCase();
                    }
                    boolean flag = false;
                    if (realColumn.contains(".")) {//table.column|db.table.column
                        if (realColumn.endsWith(clumnName)) {
                            flag = true;
                        }
                    } else {
                        if (clumnName.substring(!clumnName.contains(".") ? 0 : clumnName.indexOf(".")).equalsIgnoreCase("." + realColumn.toLowerCase())) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        num++;
                    }
                }
                if (num >= 2) {//字段重复
                    Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
                    throw new DataBaseDataBindingException("条件字段[" + realColumn + "]重复（尝试添加表名作为前缀）", null);
                } else if (num == 1) {
                    columnNum++;
                } else {
                    Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
                    String info = "条件字段[" + realColumn + "]不存在（请检查表名或字段名是否存在）";
                    if (dataBaseConfig.isErrorWhere()) {
                        throw new DataBaseDataBindingException(info, null);
                    } else {
                        logger.debug(info);
                    }
                }
            }
            if (columnNum == realColumns.length) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 获取字段类型
     *
     * @param input   String
     * @param columns Set
     * @return String
     */
    private String getColumnType(String input, Set<String> columns) {
        String result = "";
        if (DBT.isNotNull(input)) {
            String realColumn = DataBaseOperator.getRealColumn(input);
            for (String clumnName : columns) {
                String clumnType = DBT.getColumnType(clumnName);
                if ("character varying".equalsIgnoreCase(clumnType)) {
                    //continue;
                }
                clumnName = DBT.getColumnName(clumnName);
                if (DBT.isNotNull(clumnName)) {
                    clumnName = clumnName.toLowerCase();
                }
                if (realColumn.contains(".")) {//table.column
                    if (clumnName.equalsIgnoreCase(realColumn)) {
                        result = clumnType;
                    }
                } else {
                    if (clumnName.substring(!clumnName.contains(".") ? 0 : clumnName.indexOf(".")).equalsIgnoreCase("." + realColumn.toLowerCase())) {
                        result = clumnType;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获得request对象
     *
     * @return HttpServletRequest
     */
    protected HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 向Map设置自动生成的主键
     *
     * @param record Map
     * @param input  Map
     */
    private void setGeneratedMapKey(Map record, Map<String, Object> input) {
        record.put(getKeyName(), input.get("generatedKey"));
    }

    /**
     * 向Bean设置自动生成的主键
     *
     * @param record Entity
     * @param input  Map
     * @param clazz  Class
     */
    private void setGeneratedClazzKey(Entity record, Map<String, Object> input, Class<Entity> clazz) {
        if (Map.class.getName().equalsIgnoreCase(clazz.getName())) {
            setGeneratedMapKey((Map) record, input);
        } else {
            Map<String, Object> stringObjectMap = DBT.objectToMap(record);
            setGeneratedMapKey(stringObjectMap, input);
            BeanUtils.copyProperties(DBT.mapToBeanIngnoreCase(stringObjectMap, clazz), record);
        }
    }

    /**
     * 移除不存在的列对应的Key
     *
     * @param values Map
     * @return Map
     */
    private Map<String, Object> removeRedundantCode(Map<String, Object> values) {
        if (values == null) {
            return null;
        }
        String tableName = getTableName(false);
        Map<String, Object> valuesToDeal = new HashMap<>();
        List<String> columns = getColumnsFromDB(tableName);
        for (String column : columns) {
            column = DBT.getColumnName(column);
            Set<String> keys = values.keySet();
            String columnKey = null;
            for (String key : keys) {
                if (key != null && key.equalsIgnoreCase(column)) {
                    columnKey = key;
                }
            }
            if (DBT.isNotNull(columnKey)) {
                Object value = values.get(columnKey);
                if (DataBasePlatform.postgres.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {//需要转型
                    if (value instanceof String && DBT.isNotNull(value.toString())) {
                        String columnType = getColumnType(column, getColumns(tableName));
                        if (DBT.isNotNull(columnType)) {
                            String finalValue = value.toString();
                            value = new ClauseValue() {{
                                setOriginal(finalValue);
                                setValue(finalValue);
                                setOperator(DataBaseValue.doublue_colon.getOperator() + columnType);
                            }};
                        }
                    }
                }
                valuesToDeal.put(columnKey, value);
            }
        }
        return valuesToDeal;
    }

    /**
     * 从数据库获取表字段信息(columnname::columntype)
     *
     * @param tableInfo String
     * @return List
     */
    protected List<String> getColumnsFromDB(String tableInfo) {
        List<String> result = new ArrayList<>();
        if (DBT.isNotNull(tableInfo)) {
            String[] tableNameInfo = tableInfo.split("\\.");
            if (tableNameInfo.length != 2) {
                tableNameInfo = ("." + tableInfo).split("\\.");
            }
            String table = tableNameInfo[1];
            String schema = tableNameInfo[0].contains(":") ? tableNameInfo[0].split(":")[1] : tableNameInfo[0];
            result = getMapper().columns(table, schema);
            if (result == null || result.size() <= 0) {
                Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
                throw new DataBaseDataBindingException("[" + schema + "].[" + table + "]表不存在（或者表中不存在字段）", null);
            }
        }
        String columnCase = dataBaseConfig.getColumnCase();
        if (DBT.isNotNull(columnCase)) {
            if (columnCase.contains("l")) {
                return result.stream().map(String::toLowerCase).collect(Collectors.toList());
            } else {
                return result.stream().map(String::toUpperCase).collect(Collectors.toList());
            }
        } else {
            return result;
        }
    }

    /**
     * 处理表名和库名
     *
     * @param tableName String
     * @return String
     */
    private String dealTableName(String tableName) {
        if (DBT.isNotNull(tableName)) {
            if (!tableName.contains(".")) {
                if (DataBasePlatform.mysql.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
                    //mysql默认加上库前缀
                    if (DBT.isNotNull(dataBaseConfig.getDeafultDb()))
                        tableName = dataBaseConfig.getDeafultDb() + "." + tableName;
                } else if (DataBasePlatform.postgres.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
                    //postgres默认不加
                } else if (DataBasePlatform.sqlserver.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
                    //sqlserver默认不加
                } else if (DataBasePlatform.sqlite.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
                    //sqlite默认不加
                } else {
                    if (DBT.isNotNull(dataBaseConfig.getDeafultDb()))
                        tableName = dataBaseConfig.getDeafultDb() + "." + tableName;
                }
            }
        }
        return tableName;
    }

    /**
     * 批量处理表名和库名
     *
     * @param tableNames String[]
     * @return String[]
     */
    private String[] dealTableNames(String[] tableNames) {
        List<String> result = new ArrayList<>();
        for (String name : tableNames) {
            result.add(dealTableName(name));
        }
        return result.toArray(new String[0]);
    }

    /**
     * 处理字段名，不存在则抛异常
     *
     * @param columnName      String
     * @param tableInfo       String
     * @param columnFullNames Set
     * @return 入参columnName
     */
    private String dealColumnName(String columnName, String tableInfo, Set<String> columnFullNames) {
        boolean flag = false;
        if (DBT.isNotNull(columnName)) {
            for (String name : columnFullNames) {
                name = DBT.getColumnName(name);
                if (!name.contains(".")) {
                    if (columnName.equalsIgnoreCase(name)) {
                        flag = true;
                    }
                } else {
                    String[] columnNameInfo = name.split("\\.");
                    String column = columnNameInfo[1];
                    if (columnName.equalsIgnoreCase(column)) {
                        flag = true;
                    }
                }
            }
        }
        if (!flag) {
            Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
            throw new DataBaseDataBindingException("表[" + tableInfo + "]中字段[" + columnName + "]不存在", null);
        }
        return columnName;
    }

    /**
     * 处理字段名
     *
     * @param columnNames     String[]
     * @param tableInfo       String
     * @param columnFullNames Set
     * @return 入参columnNames
     */
    private String[] dealColumnNames(String[] columnNames, String[] tableInfo, Set<String> columnFullNames, String tableName) {
        List<String> result = new ArrayList<>();
        if (columnNames != null) {
            for (int i = 0; i < columnNames.length; i++) {
                if (tableInfo == null) {
                    result.add(dealColumnName(columnNames[i], tableName, columnFullNames));
                } else {
                    result.add(dealColumnName(columnNames[i], tableInfo[i], columnFullNames));
                }
            }
        }
        return result.toArray(new String[0]);
    }


    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String[] getForginKeyName() {
        HttpServletRequest request = getRequest();
        if (!isSonClass() && request != null && request.getAttribute("tableName") != null) {
            return (String[]) request.getAttribute("forginKeyName");
        } else {
            return forginKeyName;
        }
    }

    public void setForginKeyName(String[] forginKeyName) {
        this.forginKeyName = forginKeyName;
    }

    public String[] getForginTableName() {
        HttpServletRequest request = getRequest();
        String[] result = forginTableName;
        if (!isSonClass() && request != null && request.getAttribute("tableName") != null) {
            result = (String[]) request.getAttribute("forginTableName");
        }
        if (result != null) {
            result = Arrays.stream(result).map(this::dealTableName).collect(Collectors.toList()).toArray(result);
        }
        return result;
    }

    public void setForginTableName(String[] forginTableName) {
        this.forginTableName = forginTableName;
    }

    public String[] getForginTableKeyName() {
        HttpServletRequest request = getRequest();
        if (!isSonClass() && request != null && request.getAttribute("tableName") != null) {
            return (String[]) request.getAttribute("forginTableKeyName");
        } else {
            return forginTableKeyName;
        }
    }

    public void setForginTableKeyName(String[] forginTableKeyName) {
        this.forginTableKeyName = forginTableKeyName;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
