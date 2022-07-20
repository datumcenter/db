package io.gallery.db.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@ApiModel(value = "ajax参数")
public class AjaxOption {
    @ApiModelProperty(value = "请求地址")
    private String url;
    @ApiModelProperty(value = "请求参数（对象或Map）")
    private Object data;
    @ApiModelProperty(value = "请求类型")
    private RequestMethod type;
    @ApiModelProperty(value = "请求头")
    private Map<String, String> headers;
    @ApiModelProperty(value = "编码")
    private String encoding;

    public AjaxOption() {

    }

    public AjaxOption(String url) {
        this.url = url;
    }

    public AjaxOption(String url, Object data) {
        this.url = url;
        this.data = data;
    }

    public AjaxOption(String url, Map headers) {
        this.url = url;
        this.headers = headers;
    }

    public AjaxOption(String url, RequestMethod type) {
        this.url = url;
        this.type = type;
    }

    public AjaxOption(String url, Object data, RequestMethod type) {
        this.url = url;
        this.data = data;
        this.type = type;
    }

    public AjaxOption(String url, Object data, RequestMethod type, Map headers) {
        this.url = url;
        this.data = data;
        this.type = type;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public RequestMethod getType() {
        return type;
    }

    public void setType(RequestMethod type) {
        this.type = type;
    }

    public Map<String,String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
