package io.gallery.db.util;

import io.gallery.db.service.IDataBaseGenericService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class DataBaseServiceLoader implements ApplicationContextAware {
    private static Map<String, DataBaseGenericCtr> dataBaseGenericCtrs;
    private static Map<String, IDataBaseGenericService> iDataBaseGenericServices;
    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        dataBaseGenericCtrs = applicationContext.getBeansOfType(DataBaseGenericCtr.class);
        iDataBaseGenericServices = applicationContext.getBeansOfType(IDataBaseGenericService.class);
        Optional.ofNullable(dataBaseGenericCtrs).ifPresent(ctrs -> logger.debug("DataBaseGenericCtr's subClass: " + ctrs.size()));
        Optional.ofNullable(iDataBaseGenericServices).ifPresent(services -> logger.debug("IDataBaseGenericService's implementation class: " + services.size()));
    }

    /**
     * 获取Ctr子类
     *
     * @return Map
     */
    public static Map<String, DataBaseGenericCtr> getDataBaseGenericCtrs() {
        return dataBaseGenericCtrs;
    }

    /**
     * 获取IDataBaseGenericService实现类
     *
     * @return Map
     */
    public static Map<String, IDataBaseGenericService> getIDataBaseGenericServices() {
        return iDataBaseGenericServices;
    }
}
