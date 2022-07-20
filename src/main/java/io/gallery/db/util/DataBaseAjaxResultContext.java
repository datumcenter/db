package io.gallery.db.util;

import javax.xml.bind.DataBindingException;

/**
 * Ajax异步请求结果封装类
 */
public class DataBaseAjaxResultContext {
    /**
     * 执行成功的标志
     */
    private Boolean success = true;
    /**
     * 返回结果
     */
    private Object result;
    /**
     * 附加信息
     */
    private Object data;

    /**
     * 返回对象
     *
     * @param result Object
     */
    public DataBaseAjaxResultContext(Object result) {
        if (result instanceof DataBindingException) {
            // 有异常则执行失败
            setSuccess(false);
        }
        setResult(result);
    }

    /**
     * 返回文本
     *
     * @param result String
     */
    public DataBaseAjaxResultContext(String result) {
        setResult(result);
    }

    public DataBaseAjaxResultContext() {
    }

    public static DataBaseAjaxResultContext init() {
        return new DataBaseAjaxResultContext();
    }

    public static DataBaseAjaxResultContext init(boolean success) {
        return DataBaseAjaxResultContext.init().setSuccess(success);
    }

    public Object getResult() {
        return result;
    }

    /**
     * result：-100代表组织机构代码重复
     *
     * @param result Object
     * @return DataBaseAjaxResultContext
     */
    public DataBaseAjaxResultContext setResult(Object result) {
        if (result instanceof DataBindingException) {
            // 有异常则执行失败
            setSuccess(false);
        }
        this.result = result;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public DataBaseAjaxResultContext setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
