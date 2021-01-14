package io.gallery.db.util;

import io.gallery.db.service.IDataBaseCache;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.Cache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

public abstract class DataBaseCacheCtr {

    private IDataBaseCache dataBaseCache;

    private IDataBaseCache getService() {
        return Optional.ofNullable(dataBaseCache).orElse((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class));
    }

    @GetMapping(value = {""})
    @ApiOperation(value = "缓存配置信息", notes = "数据")
    @ResponseBody
    public Cache config(String cacheName) {
        return getService().get(cacheName);
    }

    @GetMapping(value = {"/clear"})
    @ApiOperation(value = "清空缓存", notes = "数据")
    @ResponseBody
    public Cache clearByName(String cacheName) {
        getService().clear(cacheName);
        return getService().get(cacheName);
    }

    @GetMapping(value = {"/keys"})
    @ApiOperation(value = "缓存主键列表", notes = "数据")
    @ResponseBody
    public Object keys(String cacheName) {
        return getService().keys(cacheName);
    }
}
