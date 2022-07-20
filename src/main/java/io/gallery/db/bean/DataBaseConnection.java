package io.gallery.db.bean;

import io.gallery.db.util.DBT;
import io.swagger.annotations.ApiModelProperty;

/**
 * 数据源信息
 */
public class DataBaseConnection {
    @ApiModelProperty(value = "主键唯一标志", required = true)
    private String id;
    @ApiModelProperty(value = "自定义名称")
    private String name;
    @ApiModelProperty(value = "数据库连接字符串", required = true)
    private String url;
    @ApiModelProperty(value = "数据库类型", required = true)
    private String driverClassName;
    @ApiModelProperty(value = "数据库用户名")
    private String username;
    @ApiModelProperty(value = "数据库密码")
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        this.password = DBT.decode(password);
    }
}
