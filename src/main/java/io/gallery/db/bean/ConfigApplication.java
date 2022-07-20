package io.gallery.db.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigApplication {
    private static final Log logger = LogFactory.getLog(ConfigApplication.class);
    @Value("${server.port:0}")
    private int serverPort;
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
