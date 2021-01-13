package com.longruan.ark.common.db.exception;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.longruan.ark.common.db.util.DataBaseBindingResultUtil;
import org.springframework.validation.BindingResult;

import java.util.*;

/**
 * 自定义数据绑定异常
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public class DataBaseDataBindingException extends RuntimeException {

    private static final long serialVersionUID = 4886698281191876208L;
    /**
     * 是否校验成功
     */
    @JsonProperty
    private boolean success = true;
    /**
     * 全局错误列表
     */
    @JsonProperty
    private List<String> globalErrors = new ArrayList<String>();
    /**
     * 字段属性校验错误列表
     */
    @JsonProperty
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();

    public DataBaseDataBindingException(String errors) {
        success = false;
        globalErrors = new ArrayList<String>() {{
            add(errors);
        }};
    }

    public DataBaseDataBindingException(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            success = false;
            globalErrors = DataBaseBindingResultUtil.getGlobalErrors(bindingResult);
            fieldErrors = DataBaseBindingResultUtil.getFieldErros(bindingResult);
        }
    }

    // getter & setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getGlobalErrors() {
        return globalErrors;
    }

    public void setGlobalErrors(List<String> globalErrors) {
        this.globalErrors = globalErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    @Override
    public String getMessage() {
        return Optional.ofNullable(globalErrors).map(List::toString).orElse(super.getMessage());
    }
}
