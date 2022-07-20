package io.gallery.db.util;

import io.gallery.db.exception.DataBaseDataBindingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

//子类需添加注解：@ControllerAdvice
public class DataBaseExceptionHandler {
    private static final Log logger = LogFactory.getLog(DataBaseExceptionHandler.class);

    /**
     * 处理其他异常
     *
     * @param request HttpServletRequest
     * @param e       Exception
     * @return DataBaseDataBindingException
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public DataBaseDataBindingException exceptionHandler(HttpServletRequest request, Exception e) {
        DBT.clearTableCache();
        logger.error("发生异常！原因是:" + e.getMessage());
        return new DataBaseDataBindingException(e.getMessage(), e);
    }
}