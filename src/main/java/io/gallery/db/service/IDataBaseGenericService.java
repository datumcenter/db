package io.gallery.db.service;

import io.gallery.db.util.DataBaseAjaxResultContext;

import java.util.List;

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
     * @param record 记录
     * @return 条数
     */
    int insert(Entity record);

    /**
     * 根据主键删除记录
     *
     * @param id 主键
     * @return 生效条数
     */
    int delete(Object id);

    /**
     * 根据主键删除记录
     *
     * @param id 主键
     * @return 生效条数
     */
    DataBaseAjaxResultContext deleteWithBusiness(Object id);

    /**
     * 根据主键删除记录
     *
     * @param ids(逗号隔开) 主键
     * @return 生效条数
     */
    int deleteByIds(String ids);

    /**
     * 根据主键删除记录
     *
     * @param query 条件
     * @return 生效条数
     */
    int deleteByParams(Query query);

    /**
     * 根据主键更新记录
     *
     * @param record 记录
     * @return 生效条数
     */
    int update(Entity record);

    /**
     * 根据主键获取记录
     *
     * @param id 主键
     * @return 记录
     */
    Entity get(Object id);

    /**
     * 根据主键获取记录
     *
     * @param id 主键
     * @return 结果
     */
    DataBaseAjaxResultContext getWithBusiness(Object id);

    /**
     * 根据主键获取记录
     *
     * @param id    主键
     * @param clazz 类
     * @param <T>   泛型
     * @return 实体类
     */
    <T> T get(Object id, Class<T> clazz);

    /**
     * 根据条件获取列表
     *
     * @param query 条件
     * @return 记录列表
     */
    List<Entity> select(Query query);

    /**
     * 根据条件获取列表
     *
     * @param query 条件
     * @param clazz 类
     * @param <T>   泛型
     * @return 实体类列表
     */
    <T> List<T> select(Query query, Class<T> clazz);

    /**
     * 根据条件获取第一条记录
     *
     * @param query 条件
     * @return 记录
     */
    Entity selectOne(Query query);

    /**
     * 根据条件获取第一条记录
     *
     * @param query 条件
     * @param clazz 类
     * @param <T>   泛型
     * @return 实体类
     */
    <T> T selectOne(Query query, Class<T> clazz);

    /**
     * 根据条件获取数量
     *
     * @param query 条件
     * @return 数量
     */
    long count(Query query);

    /**
     * 是否存在指定条件记录
     *
     * @param query 条件
     * @return 是否
     */
    boolean exist(Query query);

    /**
     * 获取主键名
     *
     * @return 主键名
     */
    String getKeyName();

    /**
     * 获取表名
     *
     * @return 表名
     */
    String getTableName();


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
