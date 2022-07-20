package io.gallery.db.util;

import io.gallery.db.factory.AbstractDataBase;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceLoader implements ApplicationContextAware {
    private static Map<String, AbstractDataBase> abstractDataBases = new HashMap<>();

    public static Map<String, AbstractDataBase> getAbstractDataBases() {
        return abstractDataBases;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractDataBase> beansOfType = applicationContext.getBeansOfType(AbstractDataBase.class);
        for (AbstractDataBase value : beansOfType.values()) {
            abstractDataBases.put(value.getDbType(), value);
        }
    }
}
