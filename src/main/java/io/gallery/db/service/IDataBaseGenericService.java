package io.gallery.db.service;

import io.gallery.db.bean.DataBasePermission;
import io.gallery.db.bean.ExportType;
import io.gallery.db.util.DBT;
import io.gallery.db.util.DataBaseAjaxResultContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 公共服务接口
 *
 * @param <Entity> 实体类
 * @param <Query>  检索条件入参
 */
public interface IDataBaseGenericService<Entity, Query> {

    /**
     * 新增记录
     *
     * @param record Entity
     * @return int
     */
    int insert(Entity record);

    /**
     * 根据主键删除记录
     *
     * @param id Object
     * @return int
     */
    int delete(Object id);

    /**
     * 根据主键删除记录
     *
     * @param id Object
     * @return DataBaseAjaxResultContext
     */
    DataBaseAjaxResultContext deleteWithBusiness(Object id);

    /**
     * 导入数据默认方法
     *
     * @param path       String
     * @param permission DataBasePermission
     * @return DataBaseAjaxResultContext
     */
    default DataBaseAjaxResultContext importFromFile(String path, DataBasePermission permission) {
        return new DataBaseAjaxResultContext(0);
    }

    /**
     * 导入数据默认方法
     *
     * @param file       MultipartFile
     * @param permission DataBasePermission
     * @param body       Map
     * @return DataBaseAjaxResultContext
     */
    default DataBaseAjaxResultContext importFromFile(MultipartFile file, DataBasePermission permission, Map body) {
        return new DataBaseAjaxResultContext(0);
    }

    /**
     * 导出数据默认方法
     *
     * @param exportTitle   标题
     * @param exportHeaders 表头
     * @param list          数据
     * @param exportType    ExportType
     * @param needTitle     是否需要标题
     */
    default void exportFile(String exportTitle, String[] exportHeaders, List list, ExportType exportType, boolean needTitle) {
        DBT.exportFile(exportTitle, exportHeaders, list, needTitle, exportType);
    }

    /**
     * 导出数据详情默认方法
     *
     * @param params Map
     */
    default void exportDetail(Map params) {

    }

    /**
     * 根据主键删除记录
     *
     * @param ids(逗号隔开) String
     * @return int
     */
    int deleteByIds(String ids);

    /**
     * 根据主键删除记录
     *
     * @param query Query
     * @return int
     */
    int deleteByParams(Query query);

    /**
     * 根据主键更新记录
     *
     * @param record Entity
     * @return int
     */
    int update(Entity record);

    /**
     * 根据主键获取记录
     *
     * @param id Object
     * @return Entity
     */
    Entity get(Object id);

    /**
     * 根据主键获取记录
     *
     * @param id Object
     * @return DataBaseAjaxResultContext
     */
    DataBaseAjaxResultContext getWithBusiness(Object id);

    /**
     * 根据主键获取记录
     *
     * @param id    Object
     * @param clazz Class
     * @param <T>   T
     * @return T实体类
     */
    <T> T get(Object id, Class<T> clazz);

    /**
     * 根据条件获取列表
     *
     * @param query Query
     * @return List实体类列表
     */
    List<Entity> select(Query query);

    /**
     * 根据条件获取列表
     *
     * @param query Query
     * @param clazz Class
     * @param <T>   T
     * @return List实体类列表
     */
    <T> List<T> select(Query query, Class<T> clazz);

    /**
     * 根据条件获取第一条记录
     *
     * @param query Query
     * @return Entity实体类
     */
    Entity selectOne(Query query);

    /**
     * 根据条件获取第一条记录
     *
     * @param query Query
     * @param clazz Class
     * @param <T> T
     * @return T实体类
     */
    <T> T selectOne(Query query, Class<T> clazz);

    /**
     * 根据条件获取数量
     *
     * @param query Query
     * @return long
     */
    long count(Query query);

    /**
     * 是否存在指定条件记录
     *
     * @param query Query
     * @return boolean
     */
    boolean exist(Query query);

    /**
     * 获取主键名
     *
     * @return String
     */
    String getKeyName();

    /**
     * 获取表字段名列表(columnname::columntype)
     *
     * @param needView boolean
     * @return List
     */
    List<String> getColumnList(boolean needView);

    /**
     * Map转实体
     *
     * @param map Map
     * @return Entity
     */
    Entity getEntity(Map map);

    /**
     * 获取表名
     *
     * @param needView boolean
     * @return String
     */
    String getTableName(boolean needView);


    /*
    设置表名
     */
    void setTableName(String tableName);

    void setKeyName(String keyName);

    String[] getForginKeyName();

    void setForginKeyName(String[] forginKeyName);

    String[] getForginTableName();

    void setForginTableName(String[] forginTableName);

    String[] getForginTableKeyName();

    void setForginTableKeyName(String[] forginTableKeyName);
}
