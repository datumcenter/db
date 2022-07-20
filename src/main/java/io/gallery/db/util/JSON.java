package io.gallery.db.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JSON {
    private static final Log logger = LogFactory.getLog(JSON.class);
    public static final ObjectMapper mapper = new ObjectMapper() {{
        // 通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
        // Include.Include.ALWAYS 默认
        // Include.NON_DEFAULT 属性为默认值不序列化
        // Include.NON_EMPTY 属性为 空（""） 或者为 NULL 都不序列化，则返回的json是没有这个字段的。这样对移动端会更省流量
        // Include.NON_NULL 属性为NULL 不序列化
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许出现特殊字符和转义符
        configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 允许出现单引号
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 字段保留，将null值转为""
        getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString("");
            }
        });
    }};

    /**
     * 对象转Json字符串
     *
     * @param object Object
     * @return String
     */
    public static String toJSONString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("toJSONString error: " + e.getMessage(), e);
            return null;
        }
    }

    public static Object parse(String text) {
        return parseObject(text, Map.class);
    }

    /**
     * Json 字符串转对象
     *
     * @param text  String
     * @param clazz Class
     * @param <T>   T
     * @return T
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (DBT.isNull(text)) {
            return null;
        }
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
     * @param text  String
     * @param clazz Class
     * @param <T>   T
     * @return List
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (DBT.isNull(text)) {
            return null;
        }
        try {
            return mapper.readValue(text, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            logger.error("parseArray error：" + text, e);
            return null;
        }
    }
}
