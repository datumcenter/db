package io.gallery.db.util;

import io.gallery.db.bean.*;
import io.gallery.db.factory.AbstractDataBase;
import io.gallery.db.mapper.DataBaseMapper;
import io.gallery.db.service.IDataBaseCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.jdbc.SQL;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).map(r -> ((ServletRequestAttributes) r).getRequest()).orElse(null);
    }

    /**
     * 获得response对象
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 通过正则获取内容
     *
     * @param string  String
     * @param pattern String
     * @return String
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
     * @param string String
     * @return Boolean
     */
    public static Boolean isNull(String string) {
        return null == string || "".equals(string);
    }

    /**
     * 字符串判空
     *
     * @param string String
     * @return Boolean
     */
    public static Boolean isNotNull(String string) {
        return !isNull(string);
    }

    /**
     * 截取字符串
     *
     * @param string String
     * @param start  int
     * @param end    int
     * @return String
     */
    public static String subString(String string, int start, int end) {
        if (!isNull(string) && string.length() >= end) {
            string = string.substring(start, end);
        } else if (!isNull(string) && string.length() < end) {
            string = string.substring(start);
        } else {
            return null;
        }
        return string;

    }

    /**
     * 合并数组
     *
     * @param first T
     * @param rest  T
     * @param <T>   T
     * @return T
     */
    @SafeVarargs
    public static <T> T[] concatArrays(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
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
        Map<String, Object> map = new HashMap<>();
        if (obj instanceof Map) {
            Map<String, Object> temp = new HashMap<>((Map<String, Object>) obj);
            map = new HashMap<>((Map<String, Object>) obj);
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

    public static Map<String, Object> objectToMapWithNull(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        if (obj instanceof Map) {
            map = new HashMap<>((Map<String, Object>) obj);
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
            bean = JSON.parseObject(JSON.toJSONString(map), clazz);
            /*PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            bean = clazz.newInstance();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (map.containsKey(key)) {
                    setParamter(map, bean, key, property);
                }
            }*/
        } catch (Exception e) {
            logger.error("mapToBean error:" + e.getMessage());
        }
        return bean;
    }

    public static <T> T mapToBeanIngnoreCase(Map<String, Object> map, Class<T> clazz) {
        T bean = null;
        if (map != null && map.size() > 0 && clazz != null) {
            if (!clazz.getSimpleName().contains("Map")) {
                String key = null;
                bean = JSON.parseObject(JSON.toJSONString(map), clazz);
                /*try {
                    BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    bean = clazz.newInstance();
                    for (String mapKey : map.keySet()) {
                        for (PropertyDescriptor property : propertyDescriptors) {
                            key = property.getName().toLowerCase();
                            if (mapKey.equalsIgnoreCase(key)) {
                                setParamter(map, bean, mapKey, property);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("mapToBeanIngnoreCase key[" + key + "] error:" + e.getMessage(), e.getCause());
                }*/
            } else {
                bean = (T) map;
            }
        } else if (map == null && clazz != null) {
        }
        return bean;
    }

    private static <T> void setParamter(Map<String, Object> map, T bean, String mapKey, PropertyDescriptor property) {
        Object value = map.get(mapKey);
        String typeName = "";
        Method setter = property.getWriteMethod();
        if (setter == null) {
            return;
        }
        Class<?>[] parameterTypes = setter.getParameterTypes();
        try {
            if (parameterTypes.length > 0 && value != null) {
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
                        setter.invoke(bean, Boolean.FALSE);
                    } else if (value.toString().equalsIgnoreCase("1") || value.toString().equalsIgnoreCase("true")) {
                        setter.invoke(bean, Boolean.TRUE);
                    } else {
                        setter.invoke(bean, Boolean.FALSE);
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
        if (target != null && source != null) {
            target.putAll(objectToMap(source));
        }
        return target;
    }

    private static Map<String, Object> getParamObjectMap(DataBaseGenericPage page, HttpServletRequest request) {
        return merge(page, getParamObjectMap(request));
    }

    public static Map<String, String> getParamStringMap(DataBaseGenericPage page, HttpServletRequest request) {
        String permissionCondition = (String) request.getAttribute("permission_condition");
        Map merge = merge(page, getParamStringMap(request));
        if (DBT.isNotNull(permissionCondition)) {
            if ("all".equals(permissionCondition)) {
                // 不追加条件
            } else if ("dborgid".equals(permissionCondition)) {
                merge.put(permissionCondition, request.getAttribute("permission_organizationId"));
            } else if ("dbdepid".equals(permissionCondition)) {
                merge.put(permissionCondition, request.getAttribute("permission_organizationId"));
            } else {
                merge.put("dbuid", request.getAttribute("permission_userId"));
            }
        }
        return merge;
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
        Map<String, Object> result = new HashMap<>();
        Iterator<Map.Entry<String, String[]>> iter = properties.entrySet().iterator();
        String name = "";
        String value = "";
        while (iter.hasNext()) {
            Map.Entry<String, String[]> entry = iter.next();
            name = entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else {
                String[] values = (String[]) valueObj;
                for (String s : values) {
                    value = s + ",";
                }
                value = value.substring(0, value.length() - 1);
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
        Map<String, String> returnMap = new HashMap<>();
        String name = "";
        String value = "";
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            name = entry.getKey();
            String[] values = entry.getValue();
            if (null == values) {
                value = "";
            } else if (values.length > 1) {
                for (String s : values) {
                    value = s + ",";
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
     * @param string String
     * @return String
     */
    public static String toUpperCaseFirst(String string) {
        if (Character.isUpperCase(string.charAt(0))) {
            return string;
        } else {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1);
        }
    }

    /**
     * 首字母转小写
     *
     * @param string String
     * @return String
     */
    public static String toLowerCaseFirst(String string) {
        if (Character.isLowerCase(string.charAt(0))) {
            return string;
        } else {
            return Character.toLowerCase(string.charAt(0)) + string.substring(1);
        }
    }

    /**
     * 初始化数据库信息
     *
     * @param dataBaseConfig DataBaseConfig
     * @param platform       DataBasePlatform
     * @param tableName      String
     * @param fileName       String
     * @throws Exception Exception
     */
    public static void initTable(DataBaseConfig dataBaseConfig, DataBasePlatform platform, String tableName, String fileName) throws Exception {
        long count = 0;
        if (dataBaseConfig != null) {
            if (!DataBasePlatform.dm.name().equalsIgnoreCase(dataBaseConfig.getPlatform())) {
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
        DataBaseUtil.execute(sql.toString().replace("#{tableSchema}", dataBaseConfig.getDeafultDb()).replace("#{tableName}", tableName));
        br.close();
        isr.close();
        is.close();
        logger.debug("table [" + tableName + "] in [" + dataBaseConfig.getDeafultDb() + "] of [" + platform + "] init success");
    }


    /**
     * 获取Mac地址
     *
     * @return String
     */
    public static String getMacAddr() {
        String result = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
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
            if (real.size() > 0) {
                result = real.get(0);
            } else if (unique.size() > 0) {
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
     * @param input String
     * @return String
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
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
     * @param bytes byte[]
     * @return String
     */
    public static String unCompress(byte[] bytes) {
        return unCompress(bytes, "UTF-8");
    }

    /**
     * 解压缩字节
     *
     * @param bytes    byte[]
     * @param encoding String
     * @return String
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
     * @param string String
     * @return byte[]
     */
    public static byte[] compress(String string) {
        return compress(string, "UTF-8");
    }

    /**
     * 压缩字符串
     *
     * @param string   String
     * @param encoding String
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
     * @param columnNameWithType String
     * @return String table.id::int转成table.id
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
     * @param columnNameWithType String
     * @return String
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
        if (map != null) {
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
    }

    /**
     * Map的所有键大小写转换
     *
     * @param map       Map
     * @param lowerCase Boolean
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
     * @param column     递归字段名
     * @param treeColumn 递归父字段名
     * @param treePlain  是否是展示原始树
     * @return List
     */
    public static List dealTree(List list, String column, String treeColumn, boolean treePlain) {
        Integer level = 0;
        List firstLevel = (List) list.stream().filter(o -> {
            Object treeLevel = DBT.objectToMap(o).get("tree_level");
            if (treeLevel instanceof Integer) {
                return level.equals(treeLevel);
            } else if (treeLevel instanceof Double) {
                return (Double) treeLevel - level == 0;
            }
            return false;
        }).collect(Collectors.toList());
        List tree = new ArrayList();
        list.removeAll(firstLevel);
        for (Object o : firstLevel) {
            Map<String, Object> map = DBT.objectToMap(o);
            Object id = map.get(column);
            Map<String, Object> leaf = new HashMap<>(map);
            if (!treePlain) {//不显示原始树
                Object parentId = map.get(treeColumn);
                leaf.put("id", id);
                leaf.put("pid", parentId);
                leaf.put("data", o);
            }
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
     * @return List
     */
    private static List dealLeaf(List list, String column, String treeColumn, Object parentId, boolean treePlain) {
        if (parentId == null || list.size() == 0) {
            return null;
        }
        List result = null;
        List children = (List) list.stream().filter(o -> Optional.ofNullable(DBT.objectToMap(o).get(treeColumn)).map(pid -> (String.valueOf(pid)).equals(String.valueOf(parentId))).orElse(false)).collect(Collectors.toList());
        if (children.size() > 0) {
            list.removeAll(children);
            result = new ArrayList();
            for (Object o : children) {
                Map<String, Object> data = DBT.objectToMap(o);
                Object id = data.get(column);
                Map record = new HashMap<>(data);
                if (!treePlain) {//不显示原始树
                    record.put("id", id);
                    record.put("pid", parentId);
                    record.put("data", data);
                }
                record.put("children", dealLeaf(list, column, treeColumn, id, treePlain));
                result.add(record);
            }
        }
        return result;
    }

    /**
     * 导出Xls
     *
     * @param excelTitle   标题
     * @param excelHeaders 表头（数组中的字符串格式：字段名:字段值,...例如："name:姓名"）
     * @param list         数据
     * @param needTitle    导出内容顶部是否需要标题
     */
    public static void exportXls(String excelTitle, String[] excelHeaders, List list, boolean needTitle) {
        HttpServletResponse response = DBT.getResponse();
        try {
            excelTitle = Optional.ofNullable(excelTitle).orElse(String.valueOf(System.currentTimeMillis()));
            HSSFWorkbook workbook = new HSSFWorkbook(); // 创建工作簿对象
            HSSFCellStyle cellStyle = workbook.createCellStyle();//样式
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            HSSFSheet sheet = workbook.createSheet(); // 创建工作表
            int index = 0;
            if (needTitle) {
                HSSFRow rowTitle = sheet.createRow(index++);//标题（同文件名一致）
                HSSFCell cellTitle = rowTitle.createCell(0);
                cellTitle.setCellValue(excelTitle);
                CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, excelHeaders.length - 1);
                sheet.addMergedRegion(cellAddresses);
                cellTitle.setCellStyle(cellStyle);
                RegionUtil.setBorderBottom(BorderStyle.THIN, cellAddresses, sheet);
                RegionUtil.setBorderTop(BorderStyle.THIN, cellAddresses, sheet);
                RegionUtil.setBorderRight(BorderStyle.THIN, cellAddresses, sheet);
            }
            HSSFRow head = sheet.createRow(index);//表头
            for (int i = 0; i < excelHeaders.length; i++) {
                HSSFCell cell = head.createCell(i);
                cell.setCellType(CellType.STRING);
                String name = excelHeaders[i]; //字段列名信息
                if (name.contains(":")) {
                    name = name.split(":")[1];
                }
                HSSFRichTextString text = new HSSFRichTextString(name);
                HSSFFont font = workbook.createFont();
                font.setBold(true);
                text.applyFont(font);
                cell.setCellValue(text);
                cell.setCellStyle(cellStyle);
            }
            for (Object o : list) {//内容
                Map map = (Map) o;
                HSSFRow r = sheet.createRow(++index);
                for (int i = 0; i < excelHeaders.length; i++) {
                    String name = excelHeaders[i]; //字段列名信息
                    if (name.contains(":")) {
                        name = name.split(":")[0];
                    }
                    HSSFCell cell = r.createCell(i);
                    cell.setCellType(CellType.STRING);
                    String value = Optional.ofNullable(map.get(name)).map(String::valueOf).orElse(null);
                    if (DBT.isNotNull(value)) {
                        cell.setCellValue(value);
                    }
                    cell.setCellStyle(cellStyle);
                }
            }
            for (int i = 0; i < excelHeaders.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i) + 256 * 6;
                sheet.setColumnWidth(i, Math.min(width, 255 * 50));
            }
            OutputStream out = response.getOutputStream();
            response.setContentType("application/msexcel");
            String filename = new String(excelTitle.getBytes("gbk"), "iso8859-1") + ".xls";
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.setCharacterEncoding("utf-8");
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            logger.error("exportXls error:", e);
        }
    }

    /**
     * 导出Xlsx
     *
     * @param excelTitle   标题
     * @param excelHeaders 表头（数组中的字符串格式：字段名:字段值,...例如："name:姓名"）
     * @param list         数据
     * @param needTitle    导出内容顶部是否需要标题
     */
    public static void exportXlsx(String excelTitle, String[] excelHeaders, List list, boolean needTitle) {
        HttpServletResponse response = DBT.getResponse();
        try {
            excelTitle = Optional.ofNullable(excelTitle).orElse(String.valueOf(System.currentTimeMillis()));
            XSSFWorkbook wb = new XSSFWorkbook();// 声明一个工作簿
            CellStyle cellStyle = wb.createCellStyle();// 全局加线样式
            cellStyle.setBorderBottom(BorderStyle.THIN); //下边框
            cellStyle.setBorderLeft(BorderStyle.THIN);//左边框
            cellStyle.setBorderTop(BorderStyle.THIN);//上边框
            cellStyle.setBorderRight(BorderStyle.THIN);//右边框
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            CellStyle contentCellStyle = wb.createCellStyle();
            BeanUtils.copyProperties(cellStyle, contentCellStyle);
            XSSFSheet sheet = wb.createSheet();// 创建sheet页
            int index = 0;
            if (needTitle) {
                XSSFRow rowTitle = sheet.createRow(index++);
                Cell cellTitle = rowTitle.createCell(0); // 0列
                cellTitle.setCellValue(excelTitle);
                CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, excelHeaders.length - 1);
                sheet.addMergedRegion(cellAddresses);
                cellTitle.setCellStyle(cellStyle);
                RegionUtil.setBorderBottom(BorderStyle.THIN, cellAddresses, sheet);
                RegionUtil.setBorderTop(BorderStyle.THIN, cellAddresses, sheet);
                RegionUtil.setBorderRight(BorderStyle.THIN, cellAddresses, sheet);
            }
            XSSFRow head = sheet.createRow(index);// 表头
            for (int i = 0; i < excelHeaders.length; i++) {
                XSSFCell cell = head.createCell(i);
                cell.setCellType(CellType.STRING);
                String headName = excelHeaders[i];
                if (headName.contains(":")) {
                    headName = headName.split(":")[1];
                }
                cell.setCellValue(headName);
                cell.setCellStyle(cellStyle);
                Font font = wb.createFont();
                font.setBold(true);
                cellStyle.setFont(font);
            }
            for (Object o : list) {// 写入内容数据
                Map map = (Map) o;
                XSSFRow r = sheet.createRow(++index);
                for (int i = 0; i < excelHeaders.length; i++) {
                    String name = excelHeaders[i]; // 列名
                    if (name.contains(":")) {
                        name = name.split(":")[0];
                    }
                    XSSFCell cell = r.createCell(i);
                    cell.setCellType(CellType.STRING);
                    String value = Optional.ofNullable(map.get(name)).map(String::valueOf).orElse(null);
                    if (DBT.isNotNull(value)) {
                        cell.setCellValue(value);
                    }
                    cell.setCellStyle(contentCellStyle);
                }
            }
            for (int i = 0; i < excelHeaders.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i) + 256 * 10;
                sheet.setColumnWidth(i, Math.min(width, 255 * 50));
            }
            OutputStream output = response.getOutputStream();
            response.reset();
            String filename = new String(excelTitle.getBytes("gbk"), "iso8859-1") + ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            response.setContentType("application/msexcel");
            wb.write(output);
            output.flush();
            wb.close();
        } catch (Exception e) {
            logger.error("exportXlsx error:", e);
        }
    }

    /**
     * 导出Excel
     *
     * @param exportTitle   标题
     * @param exportHeaders 表头（数组中的字符串格式：字段名:字段值,...例如："name:姓名"）
     * @param list          数据
     * @param needTitle     是否需要标题
     * @param type          导出类型
     */
    public static void exportFile(String exportTitle, String[] exportHeaders, List list, boolean needTitle, ExportType type) {
        if (ExportType.xlsx.equals(type)) {
            exportXlsx(exportTitle, exportHeaders, list, needTitle);
        } else {
            exportXls(exportTitle, exportHeaders, list, needTitle);
        }
    }

    /**
     * 导出Excel
     *
     * @param exportTitle   标题
     * @param exportHeaders 表头（数组中的字符串格式：字段名:字段值,...例如："name:姓名"）
     * @param list          数据
     * @param needTitle     是否需要标题
     * @param type          导出类型
     */
    public static void exportFile(String exportTitle, String[] exportHeaders, List list, boolean needTitle, String type) {
        if (ExportType.xlsx.name().equals(type)) {
            exportXlsx(exportTitle, exportHeaders, list, needTitle);
        } else {
            exportXls(exportTitle, exportHeaders, list, needTitle);
        }
    }

    /**
     * 获取客户端浏览器UA
     *
     * @param request HttpServletRequest
     * @return String
     */
    public static String getUa(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HttpServletRequest
     * @return String
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 过滤非法SQL字符串
     *
     * @param input String
     * @return String
     */
    public static String filterSql(String input) {
        String regex = "execute |exec |insert |delete |update |drop |truncate |grant |use |create ";
        return Optional.ofNullable(input).map(string -> string.replaceAll("(?i)" + regex, "")).orElse(input);
    }

    /**
     * 保存文本内容
     *
     * @param path String
     * @param text String
     * @return boolean
     */
    public static boolean textToFile(String path, String text) {
        File f = new File(path);//向指定文本框内写入
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    /**
     * 获取文本内容
     *
     * @param path String
     * @return String
     * @throws Exception Exception
     */
    public static String readTextFromPath(String path) throws Exception {
        return readTextFromPath(path, null);
    }

    /**
     * 获取文本内容
     *
     * @param path      String
     * @param enconding String
     * @return String
     * @throws Exception Exception
     */
    public static String readTextFromPath(String path, String enconding) throws Exception {
        File file = new File(path);
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        FileInputStream in = new FileInputStream(file);
        in.read(filecontent);
        in.close();
        return new String(filecontent, Optional.ofNullable(enconding).orElse("UTF-8"));
    }

    public static String format(long time) {
        return format(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String format(long time, String format) {
        try {
            return DateTimeFormatter.ofPattern(format).format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDateTime() {
        return DBT.format(System.currentTimeMillis());
    }

    public static String getDateTime(long time) {
        return DBT.format(System.currentTimeMillis());
    }

    public static String getDateTime(long time, String format) {
        return DBT.format(System.currentTimeMillis(), format);
    }

    /**
     * 字符串转日期
     *
     * @param dateTime String
     * @return LocalDateTime
     */
    public static LocalDateTime String2DateTime(String dateTime) {
        return String2DateTime(dateTime, null);
    }

    /**
     * 字符串转日期
     *
     * @param dateTime String
     * @param pattern  String
     * @return LocalDateTime
     */
    public static LocalDateTime String2DateTime(String dateTime, String pattern) {
        if (isNull(dateTime)) {
            return null;
        } else {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(Optional.ofNullable(pattern).orElse("yyyy-MM-dd HH:mm:ss")));
        }
    }

    /**
     * 获得指定Cookie的值
     *
     * @param name 名字
     * @return 值
     */
    public static String getCookie(String name) {
        HttpServletRequest request = DBT.getRequest();
        String value = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    try {
                        value = URLDecoder.decode(cookie.getValue(), "utf-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return value;
    }

    /**
     * 清除表结构缓存
     */
    public static void clearTableCache() {
        Optional.ofNullable((IDataBaseCache) DataBaseSpringUtil.getBean(IDataBaseCache.class)).ifPresent(cache -> cache.clear(DataBaseMapper.CACHE_COLUMNS_NAME));
    }


    /**
     * 获取数据库实现类
     *
     * @param dbtype String
     * @return AbstractDataBase
     */
    public static AbstractDataBase getDataBase(String dbtype) {
        AbstractDataBase abstractDataBase = ServiceLoader.getAbstractDataBases().get(dbtype);
        if (abstractDataBase == null) {
            logger.error("暂不支持该数据库类型：" + dbtype);
        }
        return abstractDataBase;
    }

    /**
     * 获取数据库实现类
     *
     * @param dbtype Object
     * @return AbstractDataBase
     */
    public static AbstractDataBase getDataBase(Object dbtype) {
        return getDataBase((String) dbtype);
    }

    /**
     * 设置排序分页信息
     *
     * @param input Map
     * @return String
     */
    public static String getPageClause(Map input) {
        String result = "";
        if (input != null) {
            result = getDataBase((String) input.get(AbstractDataBase.DB_PLATFORM_NAME)).getPageClause(input);
            input.remove(AbstractDataBase.DB_PLATFORM_NAME);
        }
        return result;
    }

    /**
     * 设置检索条件
     *
     * @param input Map
     * @return String
     */
    public static String getWhereClause(Map<String, Object> input) {
        input = DB.removePublic(input);
        StringBuilder condition = new StringBuilder();
        for (String key : input.keySet()) {
            Optional.ofNullable(input.get(key)).ifPresent(value -> {
                if (key.contains("_like")) {
                    condition.append(" and ").append(key.replaceFirst("_like", "").replace("_", ".")).append(" like CONCAT('%',:").append(key).append(",'%')");
                } else {
                    condition.append(" and ").append(key.replace("_", ".")).append(" = :").append(key);
                }
            });
        }
        return DB.removeFirstSQLKeyWord(condition.toString()).trim();
    }

    /**
     * 判断是否为整数
     *
     * @param string 传入的字符串
     * @return 是整数返回true, 否则返回false
     */
    public static boolean isInteger(String string) {
        if (DBT.isNull(string)) return false;
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(string).matches();
    }

    public static String decode(String input) {
        return Optional.ofNullable(input).map(s -> new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8)).orElse(input);
    }

    public static String encode(String input) {
        return Optional.ofNullable(input).map(s -> Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8))).orElse(input);
    }

    public static DataSource getDataSource() {
        DataSource ds = null;
        try {
            ds = DataBaseSpringUtil.getBean(DataSource.class);
        } catch (Exception e) {
            logger.debug("getDataSource error:" + e.getMessage());
        }
        return ds;
    }

    public static String getPlatform() {
        String platform = "";
        try {
            DataSource dataSource = getDataSource();
            if (dataSource != null) {
                Class c = dataSource.getClass();
                String driverClassName = (String) c.getMethod("getDriverClassName", new Class[]{}).invoke(dataSource, new Object[]{});
                if (DBT.isNotNull(driverClassName)) {
                    if ("org.postgresql.Driver".equalsIgnoreCase(driverClassName)) {
                        platform = DataBasePlatform.postgres.name();
                    } else if ("com.mysql.jdbc.Driver".equalsIgnoreCase(driverClassName)) {
                        platform = DataBasePlatform.mysql.name();
                    } else if ("dm.jdbc.driver.DmDriver".equalsIgnoreCase(driverClassName)) {
                        platform = DataBasePlatform.dm.name();
                    } else if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equalsIgnoreCase(driverClassName)) {
                        platform = DataBasePlatform.sqlserver.name();
                    } else if ("org.sqlite.JDBC".equalsIgnoreCase(driverClassName)) {
                        platform = DataBasePlatform.sqlite.name();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getPlatform error:" + e.getMessage());
        }
        return platform;
    }

    /**
     * 模拟请求
     *
     * @param option AjaxOption
     * @return String
     */
    public static String ajax(AjaxOption option) {
        String result = "";
        try {
            CloseableHttpResponse res = ajaxWithResponse(option);
            result = EntityUtils.toString(res.getEntity());
        } catch (Exception e) {
            logger.error("ajax url " + option.getUrl() + " error：" + e.getMessage());
        }
        return result;
    }

    /**
     * 带返回的模拟请求
     *
     * @param option AjaxOption
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse ajaxWithResponse(AjaxOption option) {
        CloseableHttpResponse response = null;
        if (option != null) {
            try {
                Header[] hs = null;
                Map<String, String> headers = option.getHeaders();
                if (headers != null) {
                    Map<String, String> newHeaders = new HashMap<>();
                    for (String key : headers.keySet()) {//移除null值
                        if (null != headers.get(key))
                            newHeaders.put(key, headers.get(key));
                    }
                    hs = new Header[newHeaders.size()];
                    int i = 0;
                    for (String key : newHeaders.keySet()) {
                        String value = newHeaders.get(key);
                        if ("User-Agent".equalsIgnoreCase(key) && DBT.isNotNull(value) && !value.contains("DB/dbt")) {
                            value += " DB/dbt";
                        }
                        hs[i] = new BasicHeader(key, value);
                        i++;
                    }
                }
                CloseableHttpClient httpclient = HttpClients.createDefault();
                StringEntity entity = new StringEntity(JSON.toJSONString(Optional.ofNullable(option.getData()).orElse("{}")), "utf-8");
                if (DBT.isNotNull(option.getEncoding()))
                    entity.setContentEncoding(option.getEncoding());
                if (RequestMethod.POST.equals(option.getType())) {
                    entity.setContentType("application/json");
                    HttpPost post = new HttpPost(option.getUrl());
                    post.setHeaders(hs);
                    post.setEntity(entity);
                    post.releaseConnection();
                    response = httpclient.execute(post);
                } else if (RequestMethod.PUT.equals(option.getType())) {
                    entity.setContentType("application/json");
                    HttpPut put = new HttpPut(option.getUrl());
                    put.setHeaders(hs);
                    put.setEntity(entity);
                    put.releaseConnection();
                    response = httpclient.execute(put);
                } else if (RequestMethod.DELETE.equals(option.getType())) {
                    HttpDelete delete = new HttpDelete(option.getUrl());
                    delete.setHeaders(hs);
                    delete.releaseConnection();
                    response = httpclient.execute(delete);
                } else {
                    HttpGet get = new HttpGet(option.getUrl());
                    get.setHeaders(hs);
                    get.releaseConnection();
                    response = httpclient.execute(get);
                }
            } catch (Exception e) {
                logger.error("ajaxWithResponse url:" + option.getUrl() + " error：" + e.getMessage());
            }
        }
        return response;
    }
}
