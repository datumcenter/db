package io.gallery.db.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Configuration
public class DataBaseBalanceInterceptor implements HandlerInterceptor {
    @Autowired
    private Environment environment;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String id = environment.getProperty("eureka.instance.instance-id");
        if (DBT.isNull(id)) {
            id = UUID.randomUUID().toString();
        }
        Cookie cookie = new Cookie("slb_id", id);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
