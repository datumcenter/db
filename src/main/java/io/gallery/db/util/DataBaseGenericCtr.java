package io.gallery.db.util;

import io.gallery.db.bean.DataBaseConfig;
import io.gallery.db.bean.DataBaseGenericPage;
import io.gallery.db.exception.DataBaseDataBindingException;
import io.gallery.db.service.IDataBaseGenericService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
    @ApiImplicitParams({@ApiImplicitParam(name = "input", value = "对象", dataType = "object", paramType = "body", required = true)})
    @ResponseBody
    public Object add(@RequestBody @Valid Entity input, BindingResult result) {
        DataBaseAjaxResultContext ajaxResult = new DataBaseAjaxResultContext();
        if (result.hasErrors()) {
            DataBaseTools.printErrors(result, logger);
            ajaxResult.setResult(new DataBaseDataBindingException(result));
        } else {
            try {
                Object param = getObject(input);
                ajaxResult.setData(param);
                int affectedRows = getService().insert(param);
                if (dataBaseConfig.isReturnInserted()) {
                    ajaxResult.setResult(param);
                } else {
                    ajaxResult.setResult(affectedRows);
                }
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
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "主键", dataType = "string", paramType = "path", required = true)})
    @ResponseBody
    public Object delete(@PathVariable String id) {
        return getService().delete(id);
    }

    @RequestMapping(value = "delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "根据主键删除记录", notes = "数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "主键", dataType = "string", paramType = "path", required = true)})
    @ResponseBody
    public Object del(@PathVariable String id) {
        return getService().delete(id);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.POST, RequestMethod.PUT})
    @ApiOperation(value = "根据主键修改记录", notes = "数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键", dataType = "string", paramType = "path", required = true),
            @ApiImplicitParam(name = "input", value = "对象", dataType = "object", paramType = "body", required = true)})
    @ResponseBody
    public Object edit(@PathVariable String id, @RequestBody @Valid Entity input, BindingResult result) {
        DataBaseAjaxResultContext ajaxResult = new DataBaseAjaxResultContext();
        if (result.hasErrors()) {
            DataBaseTools.printErrors(result, logger);
            ajaxResult.setResult(new DataBaseDataBindingException(result));
        } else {
            try {
                Object param = null;
                try {//判断主键是否在请求体里
                    Map<String, Object> values = DBT.objectToMapWithNull(input);
                    String keyName = getService().getKeyName();
                    if (DBT.isNotNull(keyName) && values != null) {
                        Object idOfInput = values.get(keyName);
                        if (idOfInput == null) {
                            values.put(keyName, id);
                            input = (Entity) getService().getEntity(values);
                        }
                    }
                    param = getObject(input);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                ajaxResult.setResult(getService().update(param));
            } catch (DuplicateKeyException e) {
                setError(result, ajaxResult, e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                ajaxResult.setSuccess(false);
            }
        }
        return ajaxResult;
    }

    @GetMapping(value = "{id}")
    @ApiOperation(value = "根据主键获取信息", notes = "数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "主键", dataType = "string", paramType = "path", required = true)})
    @ResponseBody
    public Object get(@PathVariable String id) {
        return getService().get(id);
    }

    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "综合查询列表", notes = "数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "对象", dataType = "object", paramType = "query")})
    @ResponseBody
    public Object list(Query input, @RequestBody(required = false) Map body) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            DBT.merge(body, params);
            if (body != null) {
                input.setTree(Optional.ofNullable((Boolean) body.get("tree")).orElse(false));
                input.setPage(Optional.ofNullable((Boolean) body.get("page")).orElse(true));
            }
        }
        List list = getService().select(params);
        if (!input.isPage()) {
            return list;
        }
        input.setData(list);
        Long total = getService().count(params);
        input.setRecordsTotal(total);
        input.setRecordsFiltered(total);
        return input;
    }


    @RequestMapping(value = "count", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "综合查询数量", notes = "数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "对象", dataType = "object", paramType = "query")})
    @ResponseBody
    public Object count(Query input, @RequestBody(required = false) Map body) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            DBT.merge(body, params);
            input.setTree(Optional.ofNullable((Boolean) body.get("tree")).orElse(false));
            input.setPage(Optional.ofNullable((Boolean) body.get("page")).orElse(true));
        }
        return getService().count(params);
    }

    @RequestMapping(value = "exist", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "指定条件记录是否存在", notes = "数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "对象", dataType = "object", paramType = "query")})
    @ResponseBody
    public Object exist(Query input, @RequestBody(required = false) Map body) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DataBaseTools.getParamStringMap(input, request);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            DBT.merge(body, params);
            input.setTree(Optional.ofNullable((Boolean) body.get("tree")).orElse(false));
            input.setPage(Optional.ofNullable((Boolean) body.get("page")).orElse(true));
        }
        return getService().exist(params);
    }

    @RequestMapping(value = "import", method = {RequestMethod.POST})
    @ApiOperation(value = "导入数据", notes = "数据")
    @ResponseBody
    public Object importFile(@RequestParam(value = "file") MultipartFile file) {
        DataBaseAjaxResultContext result = new DataBaseAjaxResultContext();
        if (!file.isEmpty()) {
            result = getService().importFromFile(file, null, DBT.getParamMap());
        }
        return result;
    }

    @CrossOrigin
    @RequestMapping(value = "export", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "导出数据", notes = "数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "对象", dataType = "object", paramType = "query")})
    @ResponseBody
    public void export(Query input, @RequestBody(required = false) Map body, HttpServletResponse response) {
        HttpServletRequest request = getRequest();
        Map<String, String> params = DBT.getParamStringMap();
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            DBT.merge(body, params);
            BeanUtils.copyProperties(DBT.mapToBean(body, DataBaseGenericPage.class), input);
        }
        //id:主键,name:名称->["id:主键","name:名称"]
        String[] excelHeaders = Optional.ofNullable(input.getExportHeaders()).map(s -> s.split(",")).orElse(new String[0]);
        input.setPage(false);
        long count = (long) count(input, params);
        input.setLength((int) count);
        List list = (List) list(input, params);
        getService().exportFile(input.getExportTitle(), excelHeaders, list, input.getExportType(), input.isNeedTitle());
    }

    @CrossOrigin
    @RequestMapping(value = "export/detail", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "导出数据详情", notes = "数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "对象", dataType = "object", paramType = "query")})
    @ResponseBody
    public void exportDetail(Query input, @RequestBody(required = false) Map body, HttpServletResponse response) {
        input.setPage(false);
        long count = (long) count(input, body);
        input.setLength((int) count);
        Map<String, String> params = DBT.getParamStringMap();
        if ("POST".equalsIgnoreCase(getRequest().getMethod())) {
            DBT.merge(body, params);
            BeanUtils.copyProperties(DBT.mapToBean(body, DataBaseGenericPage.class), input);
        }
        getService().exportDetail(params);
    }

    /**
     * 获取服务接口（单例）
     *
     * @return Interface
     */
    protected Interface getService() {
        Type genericSuperclass = getClass().getGenericSuperclass();//获取自己指定的泛型
        if (!(genericSuperclass instanceof ParameterizedType)) {//自己未指定泛型
            genericSuperclass = getClass().getSuperclass().getGenericSuperclass();//父类是否指定泛型
        }
        if (!(genericSuperclass instanceof ParameterizedType)) {//父类未指定泛型
            genericSuperclass = getClass().getGenericSuperclass();//获取自己指定的泛型
        }
        if (!(genericSuperclass instanceof ParameterizedType)) {
            initService();
        } else {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                Type actualTypeArgument = actualTypeArguments[0];
                if (actualTypeArgument instanceof TypeVariable) {
                    initService();
                } else {
                    service = DataBaseSpringUtil.getBean((Class) actualTypeArgument);
                    if (service == null) {
                        logger.error(actualTypeArgument.getTypeName() + "：没有实现类");
                        initService();
                    }
                }

            } else {
                initService();
            }
        }
        return service;
    }

    private void initService() {
        service = (Interface) DataBaseSpringUtil.getBean("dataBaseGenericService");
        if (DBT.isNotNull(tableName)) {
            service.setTableName(tableName);
        }
        if (DBT.isNotNull(keyName)) {
            service.setKeyName(keyName);
        }
        if (forginKeyName != null) {
            service.setForginKeyName(forginKeyName);
        }
        if (forginTableName != null) {
            service.setForginTableName(forginTableName);
        }
        if (forginTableKeyName != null) {
            service.setForginTableKeyName(forginTableKeyName);
        }
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
     * @param request HttpServletRequest
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
     * @return HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获得request对象
     *
     * @return Map
     */
    public Map getParameterMap() {
        return getRequest().getParameterMap();
    }

    private Object getObject(Entity input) {
        Object result = input;
        try {
            if (input instanceof Map) {
                Type genericSuperclass = getService().getClass().getSuperclass().getGenericSuperclass();
                if (genericSuperclass instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        Type type = actualTypeArguments[0];
                        Class clazz = (Class) type;
                        result = DBT.mapToBeanIngnoreCase((Map) input, clazz);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("类型转换失败：" + e.getMessage());
        }
        return result;
    }
}
