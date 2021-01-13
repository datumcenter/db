package com.longruan.ark.common.db.util;

import com.longruan.ark.common.db.bean.DataBaseConfig;
import com.longruan.ark.common.db.bean.DataBaseGenericPage;
import com.longruan.ark.common.db.exception.DataBaseDataBindingException;
import com.longruan.ark.common.db.service.IDataBaseGenericService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 公共CRUD Ctr
 *
 * @param <Interface> 服务接口
 * @param <Entity>    实体类
 * @param <Query>     检索条件入参
 */
@SuppressWarnings("rawtypes")
public abstract class DataBaseGenericCtr<Interface extends IDataBaseGenericService, Entity, Query extends DataBaseGenericPage> {
    private final Log logger = LogFactory.getLog(getClass());
    private Interface service;
    //表信息
    public String tableName;//表名
    public String keyName;//主键列名
    protected String[] forginKeyName;//外键列名
    //需要关联的表信息
    protected String[] forginTableName;//关联表名列表
    protected String[] forginTableKeyName;//关联表主键列名列表
    @Autowired
    DataBaseConfig dataBaseConfig;

    public DataBaseGenericCtr() {
    }

    @ModelAttribute
    public void setAttribute(HttpServletRequest request) {
        //请求每个URL方法前执行
        init(request);
    }

    @PostMapping(value = "")
    @ApiOperation(value = "新增记录", notes = "数据")
    @ResponseBody
    public Object add(@RequestBody @Valid Entity input, BindingResult result) {
        DataBaseAjaxResultContext ajaxResult = new DataBaseAjaxResultContext();
        if (result.hasErrors()) {
            DataBaseTools.printErrors(result, logger);
            ajaxResult.setResult(new DataBaseDataBindingException(result));
        } else {
            try {
                int affectedRows = getService().insert(input);
                if (dataBaseConfig.isReturnInserted()) ajaxResult.setResult(input);
                else ajaxResult.setResult(affectedRows);
            } catch (DuplicateKeyException e) {
                setError(result, ajaxResult, e);
            } catch (Exception e) {
                logger.error(e.getMessage());
                ajaxResult.setSuccess(false);
            }
        }
        return ajaxResult;
    }

    @DeleteMapping(value = "{id}")
    @ApiOperation(value = "根据主键删除记录", notes = "数据")
    @ResponseBody
    public Object delete(@ApiParam(value = "主键", required = true) @PathVariable String id) {
        return getService().delete(id);
    }

    @PutMapping(value = "{id}")
    @ApiOperation(value = "根据主键修改记录", notes = "数据")
    @ResponseBody
    public Object edit(@ApiParam(value = "主键", required = true) @PathVariable String id, @RequestBody @Valid Entity input, BindingResult result) {
        DataBaseAjaxResultContext ajaxResult = new DataBaseAjaxResultContext();
        if (result.hasErrors()) {
            DataBaseTools.printErrors(result, logger);
            ajaxResult.setResult(new DataBaseDataBindingException(result));
        } else {
            try {
                try {//判断主键是否在请求体里
                    Map<String, Object> values = DBT.objectToMap(input);
                    String keyName = getService().getKeyName();
                    if (DBT.isNotNull(keyName) && values != null) {
                        Object idOfInput = values.get(keyName);
                        if (idOfInput == null) {
                            values.put(keyName, id);
                            input = (Entity) values;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                ajaxResult.setResult(getService().update(input));
            } catch (DuplicateKeyException e) {
                setError(result, ajaxResult, e);
            } catch (Exception e) {
                logger.error(e.getMessage());
                ajaxResult.setSuccess(false);
            }
        }
        return ajaxResult;
    }

    @GetMapping(value = "{id}")
    @ApiOperation(value = "根据主键获取信息", notes = "数据")
    @ResponseBody
    public Object get(@ApiParam(value = "主键", required = true) @PathVariable String id) {
        return getService().get(id);
    }

    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "综合查询列表", notes = "数据")
    @ResponseBody
    public Object list(Query input) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        List list = getService().select(params);
        if (input.isTree())//是否显示树结构
            list = DBT.dealTree(list, getService().getKeyName(), Optional.ofNullable(input.getTreeColumn()).orElse(dataBaseConfig.getTreeColumn()), input.isTreePlain());
        if (!input.isPage())
            return list;
        input.setData(list);
        Long total = getService().count(params);
        input.setRecordsTotal(total);
        input.setRecordsFiltered(total);
        return input;
    }


    @RequestMapping(value = "count", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "综合查询数量", notes = "数据")
    @ResponseBody
    public Object count(Query input) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        return getService().count(params);
    }

    @RequestMapping(value = "exist", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "指定条件记录是否存在", notes = "数据")
    @ResponseBody
    public Object exist(Query input) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        return getService().exist(params);
    }

    /**
     * 获取服务接口（单例）
     *
     * @return
     */
    protected Interface getService() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType))
            initService();
        else {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                service = (Interface) DataBaseSpringUtil.getBean((Class) actualTypeArguments[0]);
                if (service == null)
                    initService();
            } else
                initService();
        }
        return service;
    }

    private void initService() {
        service = (Interface) DataBaseSpringUtil.getBean("dataBaseGenericService");
        if (DBT.isNotNull(tableName))
            service.setTableName(tableName);
        if (DBT.isNotNull(keyName))
            service.setKeyName(keyName);
        if (forginKeyName != null)
            service.setForginKeyName(forginKeyName);
        if (forginTableName != null)
            service.setForginTableName(forginTableName);
        if (forginTableKeyName != null)
            service.setForginTableKeyName(forginTableKeyName);
    }

    /**
     * 封装异常
     *
     * @param result
     * @param ajaxResult
     * @param e
     */
    private void setError(BindingResult result, DataBaseAjaxResultContext ajaxResult, DuplicateKeyException e) {
        logger.error(e.getMessage());
        String message = e.getCause().getMessage();
        ObjectError error = new ObjectError("customError", message);
        result.addError(error);
        ajaxResult.setResult(new DataBaseDataBindingException(result));
        ajaxResult.setSuccess(false);
    }

    /**
     * 初始化表信息
     *
     * @param request
     */
    protected void init(HttpServletRequest request) {
        if (request != null) {
            request.setAttribute("tableName", tableName);
            keyName = Optional.ofNullable(keyName).orElse("id");
            request.setAttribute("keyName", keyName);
            request.setAttribute("forginKeyName", forginKeyName);
            request.setAttribute("forginTableName", forginTableName);
            request.setAttribute("forginTableKeyName", forginTableKeyName);
        }
    }

    /**
     * 获得request对象
     *
     * @return
     */
    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获得request对象
     *
     * @return
     */
    public Map getParameterMap() {
        return getRequest().getParameterMap();
    }
}
