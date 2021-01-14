package io.gallery.db.service;

import java.util.Map;

/**
 * 数据库操作接口
 */
public interface IDataBase {
    /**
     * 获取排序分页信息
     *
     * @param input Map
     * @return 字符串
     */
    String getPageClause(Map input);

    /**
     * 获取SQL对象排序分页信息
     *
     * @param input Map
     * @return 字符串
     */
    String getPageClauseForSQL(Map input);

    /**
     * 获取排序信息
     *
     * @param input Map
     * @return 字符串
     */
    String getOrderClause(Map input);

    /**
     * 获取SQL对象排序信息
     *
     * @param input Map
     * @return 字符串
     */
    String getOrderClauseForSQL(Map input);

}
