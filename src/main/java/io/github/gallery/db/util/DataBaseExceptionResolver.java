package com.longruan.ark.common.db.util;

import com.longruan.ark.common.db.exception.DataBaseDataBindingException;
import com.longruan.ark.common.db.service.impl.DataBaseGenericService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DataBindingException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Optional;

public class DataBaseExceptionResolver implements HandlerExceptionResolver {
    private static final Log logger = LogFactory.getLog(DataBaseGenericService.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        //deal(request, response, e);spring默认处理
        return null;
    }

    /**
     * 处理Ajax请求错误
     *
     * @param request
     * @param response
     * @param e
     */
    protected void dealAjax(HttpServletRequest request, HttpServletResponse response, Exception e) {
        // 判断是否Ajax请求
        if (DataBaseRequestUtil.isAjax(request)) {
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write(JSON.toJSONString(new LinkedHashMap<String, Object>() {{
                    put("success", false);
                    put("message", e.getMessage());
                    if (e instanceof DataBindingException) {
                        DataBaseDataBindingException dbe = (DataBaseDataBindingException) e;
                        Optional.ofNullable(dbe.getFieldErrors()).ifPresent(errors -> put("fieldErrors", dbe.getFieldErrors()));
                        Optional.ofNullable(dbe.getGlobalErrors()).ifPresent(errors -> put("globalErrors", dbe.getGlobalErrors()));
                    }
                }}));
                writer.flush();
                writer.close();
            } catch (Exception exception) {
                logger.error("Exception resolve error: " + exception.getMessage(), exception.getCause());
            }
        }
    }
}
