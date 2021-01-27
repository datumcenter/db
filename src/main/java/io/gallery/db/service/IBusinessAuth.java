package io.gallery.db.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限操作接口
 */
public interface IBusinessAuth {
    /**
     * 业务权限信息
     *
     * @param request HttpServletRequest
     */
    void setBusinessAuth(HttpServletRequest request);
}
