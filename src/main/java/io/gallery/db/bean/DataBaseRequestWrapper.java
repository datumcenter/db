package io.gallery.db.bean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

/**
 * 拓展HttpServletRequest
 */
public class DataBaseRequestWrapper extends HttpServletRequestWrapper {
    private Map<String, String[]> params;

    public DataBaseRequestWrapper(HttpServletRequest request, Map<String, String[]> newParams) {
        super(request);
        this.params = newParams;
        renewParameterMap(request);
    }

    @Override
    public String getParameter(String name) {
        String result = "";
        Object v = params.get(name);
        if (v == null) {
            result = null;
        } else {
            String[] strArr = (String[]) v;
            if (strArr.length > 0) {
                result = strArr[0];
            } else {
                result = null;
            }
        }

        return result;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector<>(params.keySet()).elements();
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] result = null;
        Object v = params.get(name);
        if (v == null) {
        } else {
            result = (String[]) v;
        }
        return result;
    }

    private void renewParameterMap(HttpServletRequest req) {
        String queryString = req.getQueryString();
        if (queryString != null && queryString.trim().length() > 0) {
            String[] params = queryString.split("&");
            for (String param : params) {
                int splitIndex = param.indexOf("=");
                if (splitIndex == -1) {
                    continue;
                }
                String key = param.substring(0, splitIndex);
                if (!this.params.containsKey(key)) {
                    if (splitIndex < param.length()) {
                        String value = param.substring(splitIndex + 1);
                        this.params.put(key, new String[]{value});
                    }
                }
            }
        }
    }
}
