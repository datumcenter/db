package io.gallery.db.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 链式参数集合
 */
@SuppressWarnings({"NullPointerException"})
public class Maps extends LinkedHashMap {
    public static Maps init() {
        return new Maps();
    }

    public static Maps init(String key, Object value) {
        return new Maps() {{
            super.put(key, value);
        }};
    }

    @Override
    public Maps put(Object key, Object value) {
        super.put(key, value);
        return this;
    }

    public Maps putMap(Map map) {
        super.putAll(map);
        return this;
    }
}
