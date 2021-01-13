package com.longruan.ark.common.db.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataBaseSpringUtil implements ApplicationContextAware {
    private static final Log logger = LogFactory.getLog(DataBaseSpringUtil.class);
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return DataBaseSpringUtil.applicationContext;
    }

    public static Object getBean(String name) {
        if (DBT.isNull(name)) return null;
        try {
            return Optional.ofNullable(applicationContext).map(context -> context.getBean(name)).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> Object getBean(Class clazz) {
        try {
            return Optional.ofNullable(applicationContext).map(context -> context.getBean(clazz)).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DataBaseSpringUtil.applicationContext = applicationContext;
    }

    public static Object getBean(String name, Class<?> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getAliases(name);
    }
}