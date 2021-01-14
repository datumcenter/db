package io.gallery.db.util;

/**
 * Ajax异步请求结果封装类
 */
public class DataBaseAjaxResult {
    /**
     * 结果状态success/error，分别标识成功和逻辑错误
     */
    private DataBaseAjaxStatus status = DataBaseAjaxStatus.success;
    /**
     * 错误内容，发生错误时的消息提示
     */
    private String error;
    /**
     * 具体业务内容
     */
    private Object data;

    public DataBaseAjaxStatus getStatus() {
        return status;
    }

    public void setStatus(DataBaseAjaxStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
