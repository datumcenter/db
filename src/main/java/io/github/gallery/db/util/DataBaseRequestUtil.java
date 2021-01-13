package com.longruan.ark.common.db.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DataBaseRequestUtil {
    private static final Log logger = LogFactory.getLog(DataBaseRequestUtil.class);

    /**
     * 设置排序条件
     *
     * @param request
     * @return
     */
    public static String getOrderByClause(HttpServletRequest request) {
        String setOrderClause = null;
        if (request != null) {
            try {
                String orderNum = request.getParameter("order[0][column]");// 排序的列号
                String orderDir = request.getParameter("order[0][dir]");// 排序的顺序
                String order = request.getParameter("columns[" + orderNum + "][data]");// 排序的列
                if (DataBaseTools.isNotNull(order) && DataBaseTools.isNotNull(orderDir)) {
                    setOrderClause = order + " " + orderDir;
                }
            } catch (Exception e) {
                logger.error("getOrderByClause error: " + e.getMessage(), e.getCause());
            }
        }
        return setOrderClause;
    }

    /**
     * 设置排序条件
     *
     * @param request
     * @param additionnal 追加排序
     * @return
     */
    public static String getOrderByClause(HttpServletRequest request, String additionnal) {
        String setOrderClause = getOrderByClause(request);
        if (DataBaseTools.isNotNull(setOrderClause)) {
            setOrderClause += (" " + additionnal + " ");
        } else {
            setOrderClause = (" " + additionnal + " ");
        }
        return setOrderClause;
    }

    /**
     * 获取请求参数
     */
    public static Optional<String> get(HttpServletRequest request, String name) {
        return Optional.ofNullable(request).map(r -> {
            Map<String, String> map = new LinkedHashMap(r.getParameterMap());
            Object result = map.getOrDefault(name, null);
            if (result != null) {
                return ((String[]) result)[0];
            } else {
                return null;
            }
        });
    }

    /**
     * 是否是ajax请求
     *
     * @param request
     * @return
     */
    public static boolean isAjax(HttpServletRequest request) {
        return (request.getHeader("X-Requested-With") != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With").toString()));
    }

}
