package com.longruan.ark.common.db.service;

import java.util.Map;

/**
 * 数据库操作接口
 */
public interface IDataBase {
    /**
     * 获取排序分页信息
     *
     * @param input
     * @return
     */
    String getPageClause(Map input);

    /**
     * 获取SQL对象排序分页信息
     *
     * @param input
     * @return
     */
    String getPageClauseForSQL(Map input);

    /**
     * 获取排序信息
     *
     * @param input
     * @return
     */
    String getOrderClause(Map input);

    /**
     * 获取SQL对象排序信息
     *
     * @param input
     * @return
     */
    String getOrderClauseForSQL(Map input);

}
