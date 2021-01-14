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
     * @param result 字符串
     */
    public DataBaseAjaxResultContext(String result) {
        setResult(result);
    }

    public DataBaseAjaxResultContext() {
    }

    public Object getResult() {
        return result;
    }

    /**
     * result
     *
     * @param result 结果
     */
    public void setResult(Object result) {
        if (result instanceof DataBindingException) {
            // 有异常则执行失败
            setSuccess(false);
        }
        this.result = result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
