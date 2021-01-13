package com.longruan.ark.common.db.mapper;

import com.longruan.ark.common.db.bean.DataBaseGenericPage;
import com.longruan.ark.common.db.util.DataBaseProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface DataBaseMapper<Entity, Query extends DataBaseGenericPage, Example extends DataBaseGenericPage> {

    /**
     * 表字段缓存名称
     */
    String CACHE_COLUMNS_NAME = "columns";

    /**
     * 新增记录
     *
     * @param record
     * @return
     */
    @Transactional
    @InsertProvider(type = DataBaseProvider.class, method = "insert")
    @Options(useGeneratedKeys = true, keyProperty = "generatedKey")
    int insert(Entity record);

    /**
     * 根据主键删除记录
     *
     * @param input
     * @return
     */
    @Transactional
    @DeleteProvider(type = DataBaseProvider.class, method = "delete")
    int delete(Map<String, Object> input);

    /**
     * 根据条件删除记录
     *
     * @param input
     * @return
     */
    @Transactional
    @DeleteProvider(type = DataBaseProvider.class, method = "deleteByParams")
    int deleteByParams(Map<String, Object> input);

    /**
     * 根据主键更新记录
     *
     * @param record
     * @return
     */
    @Transactional
    @UpdateProvider(type = DataBaseProvider.class, method = "update")
    int update(Entity record);

    /**
     * 根据主键获取记录
     *
     * @param input
     * @return 实体类
     */
    @SelectProvider(type = DataBaseProvider.class, method = "get")
    LinkedHashMap get(Map<String, Object> input);

    /**
     * 根据条件获取列表
     *
     * @return 实体类列表
     */
    @SelectProvider(type = DataBaseProvider.class, method = "select")
    List<LinkedHashMap> select(Map<String, Object> input);

    /**
     * 根据条件获取第一条记录
     *
     * @param input
     * @return 实体类
     */
    @SelectProvider(type = DataBaseProvider.class, method = "selectOne")
    LinkedHashMap selectOne(Map<String, Object> input);

    /**
     * 根据条件获取数量
     *
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "count")
    long count(Map<String, Object> input);

    /**
     * 设置列表检索条件
     *
     * @param query 检索条件入参
     * @return 检索条件出参
     */
    Example setCondition(Query query);

    /**
     * 获取字段列表
     *
     * @return 列表
     */
    @Cacheable(cacheNames = {CACHE_COLUMNS_NAME})
    @SelectProvider(type = DataBaseProvider.class, method = "columns")
    List<String> columns(@Param("table") String table, @Param("schema") String schema);

    /**
     * 执行自定义SQL
     *
     * @param sql
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "execute")
    List<HashMap> execute(String sql);

    /**
     * 执行自定义SQL
     *
     * @param sql
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "execute")
    long executeCount(String sql);

    /**
     * 执行自定义SQL
     *
     * @param sql
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "execute")
    LinkedHashMap executeOne(String sql);

    /**
     * 执行自定义SQL
     *
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "executeWithParams")
    List<LinkedHashMap> executeWithParams(Map<String, Object> params);

    /**
     * 执行自定义SQL
     *
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "executeWithParams")
    LinkedHashMap executeWithParamsOne(Map<String, Object> params);

    /**
     * 执行自定义SQL
     *
     * @return
     */
    @SelectProvider(type = DataBaseProvider.class, method = "executeWithParams")
    long executeWithParamsCount(Map<String, Object> params);

    /**
     * 执行自定义SQL
     *
     * @return
     */
    @UpdateProvider(type = DataBaseProvider.class, method = "executeWithParams")
    int executeInsertOrUpdate(Map<String, Object> params);

    /**
     * 执行自定义SQL
     *
     * @return
     */
    @UpdateProvider(type = DataBaseProvider.class, method = "execute")
    void exec(String sql);

    /**
     * 执行自定义SQL
     *
     * @return
     */
    @UpdateProvider(type = DataBaseProvider.class, method = "executeWithParams")
    void execWithParams(Map<String, Object> input);
}
