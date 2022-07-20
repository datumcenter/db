package io.gallery.db.bean;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库配置
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")//springboot 获取配置方式
public class DataBaseSpringDataSource {
    private static final Log logger = LogFactory.getLog(DataBaseSpringDataSource.class);
    @ApiModelProperty(value = "数据库连接字符串", required = true)
    @Value("${spring.datasource.url:}")//springmvc 获取配置方式
    private String url;
    @ApiModelProperty(value = "数据库类型", required = true)
    @Value("${spring.datasource.driver-class-name:}")//springmvc 获取配置方式
    private String driverClassName;
    @ApiModelProperty(value = "数据库用户名")
    @Value("${spring.datasource.username:}")//springmvc 获取配置方式
    private String username;
    @ApiModelProperty(value = "数据库密码")
    @Value("${spring.datasource.password:}")//springmvc 获取配置方式
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
