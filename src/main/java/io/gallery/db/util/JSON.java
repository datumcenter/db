package io.gallery.db.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class JSON {
    private static final Log logger = LogFactory.getLog(JSON.class);
    public static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对象转Json字符串
     *
     * @param object Object
     * @return 字符串
     */
    public static String toJSONString(Object object) {
        if (object == null)
            return null;
        if (object instanceof String)
            return (String) object;
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("jtoJSONString error: " + e.getMessage(), e);
            return null;
        }
    }

    public static Object parse(String text) {
        return parseObject(text, Map.class);
    }

    /**
     * Json 字符串转对象
     *
     * @param text  字符串
     * @param clazz 类
     * @param <T>   泛型
     * @return 泛型
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (DBT.isNull(text))
            return null;
        try {
            return mapper.readValue(text, clazz);
        } catch (Exception e) {
            logger.error("parseObject error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Json 字符串转对象列表
     *
     * @param text  字符串
     * @param clazz 类
     * @param <T>   泛型
     * @return 泛型
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (DBT.isNull(text))
            return null;
        try {
            return mapper.readValue(text, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            logger.error("parseArray error：" + text, e);
            return null;
        }
    }
}
