package io.gallery.db.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限操作接口
 */
public interface IBusinessAuth {
    /**
     * 业务权限信息
     */
    void setBusinessAuth(HttpServletRequest request);
}
