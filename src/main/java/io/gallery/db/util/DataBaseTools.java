package io.gallery.db.util;

import io.gallery.db.bean.DataBaseConfig;
import io.gallery.db.bean.DataBaseGenericPage;
import io.gallery.db.bean.DataBasePlatform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataBaseTools {
    private static final Log logger = LogFactory.getLog(DataBaseTools.class);

    /**
     * 获得request对象
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 通过正则获取内容
     *
     * @param string  入参
     * @param pattern 正则
     * @return 字符串
     */
    public static String getByPattern(String string, String pattern) {
        String result = "";
        Matcher matcher = Pattern.compile(pattern).matcher(string);
        while (matcher.find()) {
            result = matcher.group(0);
            break;
        }
        return result;
    }

    /**
     * 字符串判空
     *
     * @param string 入参
     * @return 是否
     */
    public static Boolean isNull(String string) {
        return null == string || "".equals(string);
    }

    /**
     * 字符串判空
     *
     * @param string 入参
     * @return 是否
     */
    public static Boolean isNotNull(String string) {
        return !isNull(string);
    }

    /**
     * 截取字符串
     *
     * @param string 入参
     * @param start  开始
     * @param end    结束
     * @return 字符串
     */
    public static String subString(String string, int start, int end) {
        if (!isNull(string) && string.length() >= end) {
            string = string.substring(start, end);
        } else if (!isNull(string) && string.length() < end) {
            string = string.substring(start, string.length());
        } else {
            return null;
        }
        return string;

    }

    /**
     * 打印BindingResult的错误信息
     *
     * @param result BindingResult
     * @param logger Log
     */
    public static void printErrors(BindingResult result, Log logger) {
        List<ObjectError> errorList = result.getAllErrors();
        for (ObjectError error : errorList) {
            logger.error("操作失败：" + error.getDefaultMessage());
        }
    }

    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        if (obj instanceof Map) {
            Map<String, Object> temp = new HashMap<String, Object>((Map<String, Object>) obj);
            map = new HashMap<String, Object>((Map<String, Object>) obj);
            for (String s : temp.keySet()) {
                Object o = temp.get(s);
                if (o == null) {
                    map.remove(s);
                }
            }
        } else {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor property : propertyDescriptors) {
                    String key = property.getName();
                    if (key.compareToIgnoreCase("class") == 0) {
                        continue;
                    }
                    Method getter = property.getReadMethod();
                    Object value = getter != null ? getter.invoke(obj) : null;
                    if (value != null) {
                        map.put(key, value);
                    }
                }
            } catch (Exception e) {
                logger.error("convert error:" + e.getMessage());
            }
        }
        return map;
    }

    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        T bean = null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            bean = clazz.newInstance();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (map.containsKey(key)) {
                    setParamter(map, bean, key, property);
                }
            }
        } catch (Exception e) {
            logger.error("mapToBean error:" + e.getMessage());
        }
        return bean;
    }

    public static <T> T mapToBeanIngnoreCase(Map<String, Object> map, Class<T> clazz) {
        T bean = null;
        if (map != null && map.size() > 0 && clazz != null) {
            if (!clazz.getSimpleName().contains("Map")) {
                try {
                    BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    bean = clazz.newInstance();
                    for (String mapKey : map.keySet()) {
                        for (PropertyDescriptor property : propertyDescriptors) {
                            String key = property.getName().toLowerCase();
                            if (mapKey.equalsIgnoreCase(key)) {
                                setParamter(map, bean, mapKey, property);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("mapToBeanIngnoreCase error:" + e.getMessage(), e.getCause());
                }
            } else {
                bean = (T) map;
            }
        } else if (map == null && clazz != null) {
            bean = null;
        }
        return bean;
    }

    private static <T> void setParamter(Map<String, Object> map, T bean, String mapKey, PropertyDescriptor property) {
        Object value = map.get(mapKey);
        String typeName = "";
        Method setter = property.getWriteMethod();
        Class<?>[] parameterTypes = setter.getParameterTypes();
        try {
            if (parameterTypes != null && parameterTypes.length > 0 && value != null) {
                Class<?> parameterType = parameterTypes[0];
                typeName = parameterType.getName();
                if (typeName.equalsIgnoreCase(Integer.class.getName())) {
                    setter.invoke(bean, new Integer(value.toString()));
                } else if (typeName.equalsIgnoreCase(BigDecimal.class.getName())) {
                    setter.invoke(bean, new BigDecimal(value.toString()));
                } else if (typeName.equalsIgnoreCase(Long.class.getName())) {
                    setter.invoke(bean, new Long(value.toString()));
                } else if (typeName.equalsIgnoreCase(Double.class.getName())) {
                    setter.invoke(bean, new Double(value.toString()));
                } else if (typeName.equalsIgnoreCase(Float.class.getName())) {
                    setter.invoke(bean, new Float(value.toString()));
                } else if (typeName.equalsIgnoreCase(Short.class.getName())) {
                    setter.invoke(bean, new Short(value.toString()));
                } else if (typeName.equalsIgnoreCase(Byte.class.getName())) {
                    setter.invoke(bean, new Byte(value.toString()));
                } else if (typeName.equalsIgnoreCase(Boolean.class.getName()) || typeName.equalsIgnoreCase(boolean.class.getName())) {
                    if (value.toString().equalsIgnoreCase("0") || value.toString().equalsIgnoreCase("false")) {
                        setter.invoke(bean, new Boolean(false));
                    } else if (value.toString().equalsIgnoreCase("1") || value.toString().equalsIgnoreCase("true")) {
                        setter.invoke(bean, new Boolean(true));
                    } else {
                        setter.invoke(bean, new Boolean(false));
                    }
                } else if (parameterType.isEnum()) {
                    Object[] enumConstants = parameterType.getEnumConstants();
                    for (Object o : enumConstants) {
                        if (o.toString().equalsIgnoreCase(value.toString())) {
                            setter.invoke(bean, o);
                        }
                    }
                } else {
                    setter.invoke(bean, value);
                }
            }
        } catch (Exception e) {
            try {
                setter.invoke(bean, value.toString());
            } catch (Exception exception) {
                logger.error("typeName: [" + typeName + "] value: [" + value + "] set error:" + e.getMessage(), e.getCause());
            }
        }
    }

    public static List<String> classKeyToMap(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            list.add(field.getName());
        }
        return list;
    }

    public static Map merge(Object source, Map target) {
        if (target != null) {
            target.putAll(objectToMap(source));
        }
        return target;
    }

    private static Map<String, Object> getParamObjectMap(DataBaseGenericPage page, HttpServletRequest request) {
        return merge(page, getParamObjectMap(request));
    }

    public static Map<String, String> getParamStringMap(DataBaseGenericPage page, HttpServletRequest request) {
        return merge(page, getParamStringMap(request));
    }

    public static Map<String, Object> getParamMap() {
        return getParamObjectMap(getRequest());
    }

    private static void dealPageInfo(Map map) {
        try {
            Object start = map.get("start");
            Object length = map.get("length");
            if (start instanceof String) {
                map.put("start", Integer.valueOf((String) start));
            }
            if (length instanceof String) {
                map.put("length", Integer.valueOf((String) length));
            }
        } catch (Exception e) {
            logger.error("分页信息转换失败");
        }
    }

    private static Map<String, Object> getParamObjectMap(HttpServletRequest request) {
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, Object> result = new HashMap<String, Object>();
        Iterator<Map.Entry<String, String[]>> iter = properties.entrySet().iterator();
        String name = "";
        String value = "";
        while (iter.hasNext()) {
            Map.Entry<String, String[]> entry = iter.next();
            name = entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            result.put(name, value);
        }
        dealPageInfo(result);
        return result;
    }

    public static Map<String, String> getParamStringMap() {
        return getParamStringMap(getRequest());
    }

    public static Map<String, String> getParamStringMap(HttpServletRequest request) {
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<String, String>();
        String name = "";
        String value = "";
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            name = entry.getKey();
            String[] values = entry.getValue();
            if (null == values) {
                value = "";
            } else if (values.length > 1) {
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = values[0];
            }
            returnMap.put(name, value);

        }
        return returnMap;
    }

    /**
     * 首字母转大写
     *
     * @param string 入参
     * @return 字符串
     */
    public static String toUpperCaseFirst(String string) {
        if (Character.isUpperCase(string.charAt(0))) {
            return string;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(string.charAt(0))).append(string.substring(1)).toString();
        }
    }

    /**
     * 首字母转小写
     *
     * @param string 入参
     * @return 字符串
     */
    public static String toLowerCaseFirst(String string) {
        if (Character.isLowerCase(string.charAt(0))) {
            return string;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(string.charAt(0))).append(string.substring(1)).toString();
        }
    }

    /**
     * 初始化数据库信息
     *
     * @param dataBaseConfig 数据库配置
     * @param platform       平台
     * @param tableName      表名
     * @param fileName       文件名
     * @throws Exception 异常
     */
    public static void initTable(DataBaseConfig dataBaseConfig, DataBasePlatform platform, String tableName, String fileName) throws Exception {
        long count = 0;
        if (dataBaseConfig == null || !DataBasePlatform.dm.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
            count = DataBaseUtil.count(new SQL() {{
                SELECT("*");
                FROM("information_schema.TABLES");
                WHERE("TABLE_SCHEMA =#{tableSchema}");
                WHERE("TABLE_NAME =#{tableName}");
            }}.toString(), new HashMap<String, Object>() {{
                put("tableSchema", dataBaseConfig.getDeafultDb());
                put("tableName", tableName);
            }});
        } else {//dm
            count = DataBaseUtil.count(new SQL() {{
                SELECT("*");
                FROM("all_tab_columns");
                WHERE("owner =#{tableSchema}");
                WHERE("table_name =#{tableName}");
            }}.toString(), new HashMap<String, Object>() {{
                put("tableSchema", dataBaseConfig.getDeafultDb());
                put("tableName", tableName);
            }});
        }

        if (count > 0) {
            logger.debug("table [" + tableName + "] in [" + dataBaseConfig.getDeafultDb() + "] of [" + platform + "] exist");
        } else {
            creat(dataBaseConfig, platform, tableName, fileName);
        }
    }

    private static void creat(DataBaseConfig dataBaseConfig, DataBasePlatform platform, String tableName, String fileName) throws Exception {
        logger.warn("table [" + tableName + "] in [" + dataBaseConfig.getDeafultDb() + "] of [" + platform + "] not exist, init table...");
        Resource resource = new ClassPathResource("sql/" + fileName + "_" + platform + ".sql");
        InputStream is = resource.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sql = new StringBuilder();
        String data = null;
        while ((data = br.readLine()) != null) {
            sql.append(data);
        }
        DataBaseUtil.execute(sql.toString()
                .replace("#{tableSchema}", dataBaseConfig.getDeafultDb())
                .replace("#{tableName}", tableName)
        );
        br.close();
        isr.close();
        is.close();
        logger.debug("table [" + tableName + "] in [" + dataBaseConfig.getDeafultDb() + "] of [" + platform + "] init success");
    }


    /**
     * 获取Mac地址
     *
     * @return 字符串
     */
    public static String getMacAddr() {
        String result = "";
        try {
            java.util.Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            StringBuilder sb = new StringBuilder();
            ArrayList<String> tmpMacList = new ArrayList<>();
            while (en.hasMoreElements()) {
                NetworkInterface iface = en.nextElement();
                List<InterfaceAddress> addrs = iface.getInterfaceAddresses();
                for (InterfaceAddress addr : addrs) {
                    InetAddress ip = addr.getAddress();
                    NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                    if (network == null) {
                        continue;
                    }
                    byte[] mac = network.getHardwareAddress();
                    if (mac == null) {
                        continue;
                    }
                    sb.delete(0, sb.length());
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    tmpMacList.add(sb.toString());
                }
            }
            if (tmpMacList.size() <= 0) {
                return result;
            }
            List<String> unique = tmpMacList.stream().distinct().collect(Collectors.toList());
            List<String> real = unique.stream().filter(s -> s.length() == 17).collect(Collectors.toList());
            if (real != null && real.size() > 0) {
                result = real.get(0);
            } else if (unique != null && unique.size() > 0) {
                result = unique.get(0);
            }
        } catch (Exception e) {
            logger.error("getMacAddr fail: " + e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 字符串md5加密
     *
     * @param input 入参
     * @return 字符串
     * @throws NoSuchAlgorithmException 异常
     */
    public static String getMD5(String input) throws NoSuchAlgorithmException {
        if (input != null) {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(input.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } else {
            return null;
        }
    }

    /**
     * 解压缩字节
     *
     * @param bytes 字节
     * @return 字符串
     */
    public static String unCompress(byte[] bytes) {
        return unCompress(bytes, "UTF-8");
    }

    /**
     * 解压缩字节
     *
     * @param bytes    字节
     * @param encoding 编码
     * @return 字符串
     */
    public static String unCompress(byte[] bytes, String encoding) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(encoding);
        } catch (IOException e) {
            logger.error("unCompress失败：" + e.getMessage(), e.getCause());
        }
        return null;
    }

    /**
     * 压缩字符串
     *
     * @param string 字符串
     * @return byte[]
     */
    public static byte[] compress(String string) {
        return compress(string, "UTF-8");
    }

    /**
     * 压缩字符串
     *
     * @param string   字符串
     * @param encoding 编码
     * @return byte[]
     */
    public static byte[] compress(String string, String encoding) {
        if (string == null || string.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(string.getBytes(encoding));
            gzip.close();
        } catch (Exception e) {
            logger.error("compress失败：" + e.getMessage(), e.getCause());
        }
        return out.toByteArray();
    }

    /**
     * 从字段名和字段类型例表中获取字段名
     *
     * @param columnNameWithType 字段
     * @return 字符串
     */
    public static String getColumnName(String columnNameWithType) {
        if (DBT.isNotNull(columnNameWithType) && columnNameWithType.contains("::")) {
            return columnNameWithType.split("::")[0];
        }
        return columnNameWithType;
    }

    /**
     * 从字段名和字段类型例表中获取字段类型
     *
     * @param columnNameWithType 字段
     * @return 字符串
     */
    public static String getColumnType(String columnNameWithType) {
        if (DBT.isNotNull(columnNameWithType) && columnNameWithType.contains("::")) {
            return columnNameWithType.split("::")[1];
        }
        return columnNameWithType;
    }

    /**
     * 处理大文本数据
     *
     * @param map Map
     */
    public static void dealMegaText(Map map) {
        if (map != null)
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
                if (entry.getValue() instanceof Clob) {
                    Clob clob = (Clob) entry.getValue();
                    try {
                        map.put(entry.getKey(), clob.getSubString(1, (int) clob.length()));
                    } catch (SQLException e) {
                        logger.error("clob convert error: " + e.getMessage(), e);
                    }
                }
            }
    }

    /**
     * Map的所有键大小写转换
     *
     * @param map       Map
     * @param lowerCase 是否
     * @return Map
     */
    public static Map mapKeyCase(Map map, Boolean lowerCase) {
        Map<String, Object> result = new HashMap<>();
        if (map != null) {
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                if (Boolean.FALSE.equals(lowerCase)) {
                    result.put(key.toUpperCase(), map.get(key));
                } else if (Boolean.TRUE.equals(lowerCase)) {
                    result.put(key.toLowerCase(), map.get(key));
                }
            }
            map = result;
        }
        return map;
    }

    /**
     * 构造树结构
     *
     * @param list       原始数据
     * @param column 递归字段名
     * @param treeColumn 递归父字段名
     * @param treePlain  是否是展示原始树
     * @return 列表
     */
    public static List dealTree(List list, String column, String treeColumn, boolean treePlain) {
        Integer level = 0;
        List firstLevel = (List) list.stream().filter(o -> {
            Object treeLevel = DBT.objectToMap(o).get("tree_level");
            if (treeLevel instanceof Number)
                return Integer.parseInt(treeLevel.toString()) - level == 0;
            return false;
        }).collect(Collectors.toList());
        List tree = new ArrayList();
        list.removeAll(firstLevel);
        for (Object o : firstLevel) {
            Map<String, Object> map = DBT.objectToMap(o);
            Object id = map.get(column);
            Map<String, Object> leaf = new HashMap<>();
            if (!treePlain) {//不显示原始树
                Object parentId = map.get(treeColumn);
                leaf.put("id", id);
                leaf.put("pid", parentId);
                leaf.put("data", o);
            } else
                leaf = map;
            leaf.put("children", dealLeaf(list, column, treeColumn, id, treePlain));
            tree.add(leaf);
        }
        return tree;
    }

    /**
     * 构造叶子节点
     *
     * @param list       原始数据
     * @param treeColumn 递归字段名
     * @param treeColumn 递归父字段名
     * @param parentId   父级节点ID
     * @return 列表
     */
    private static List dealLeaf(List list, String column, String treeColumn, Object parentId, boolean treePlain) {
        if (parentId == null || list.size() == 0) {
            return null;
        }
        List result = null;
        List children = (List) list.stream().filter(o -> Optional.ofNullable(DBT.objectToMap(o).get(treeColumn)).map(pid -> (String.valueOf(pid)).equals(String.valueOf(parentId))).orElse(false)).collect(Collectors.toList());
        if (children != null && children.size() > 0) {
            list.removeAll(children);
            result = new ArrayList();
            for (Object o : children) {
                Map<String, Object> data = DBT.objectToMap(o);
                Object id = data.get(column);
                Map record = new HashMap<String, Object>();
                if (!treePlain) {//不显示原始树
                    record.put("id", id);
                    record.put("pid", parentId);
                    record.put("data", data);
                } else
                    record = data;
                record.put("children", dealLeaf(list, column, treeColumn, id, treePlain));
                result.add(record);
            }
        }
        return result;
    }
}