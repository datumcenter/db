package com.longruan.ark.common.db.util;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataBaseApiCtr extends DataBaseGenericCtr {

    protected String defaultDb;

    @Override
    public void setAttribute(HttpServletRequest request) {
        NativeWebRequest webRequest = new ServletWebRequest(request);
        Map<String, String> map = (Map<String, String>) webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String table = map.get("table");
        String forginKeyName = map.get("forginKeyName");
        String keyName = map.get("keyName");
        String forginTableName = map.get("forginTableName");
        String forginTableKeyName = map.get("forginTableKeyName");
        request.setAttribute("tableName", dealTableName(table));
        request.setAttribute("defaultDb", defaultDb);
        request.setAttribute("keyName", Optional.ofNullable(keyName).orElse("id"));
        request.setAttribute("forginKeyName", Optional.ofNullable(forginKeyName).map(v -> v.split(",")).orElse(null));
        request.setAttribute("forginTableName", Optional.ofNullable(dealTableNames(forginTableName)).map(v -> v.split(",")).orElse(null));
        request.setAttribute("forginTableKeyName", Optional.ofNullable(forginTableKeyName).map(v -> v.split(",")).orElse(null));
    }

    private String dealTableName(String tableName) {
        if (DataBaseTools.isNotNull(tableName) && !tableName.contains(".")) {
            if (DataBaseTools.isNotNull(defaultDb)) {
                tableName = defaultDb + "." + tableName;
            }
        }
        return tableName;
    }

    private String dealTableNames(String tableNames) {
        if (DataBaseTools.isNotNull(tableNames)) {
            List<String> result = new ArrayList<>();
            for (String name : tableNames.split(",")) {
                result.add(dealTableName(name));
            }
            tableNames = result.stream().collect(Collectors.joining(","));
        }
        return tableNames;
    }

}
