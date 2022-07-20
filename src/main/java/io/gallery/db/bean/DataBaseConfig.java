package io.gallery.db.bean;

import io.gallery.db.util.DBT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库配置
 */
@Component
@ConfigurationProperties(prefix = "db.info")//springboot 获取配置方式
public class DataBaseConfig {

    /**
     * 数据库平台（默认mysql）
     */
    @Value("${db.info.platform:}")//springmvc 获取配置方式
    private String platform;

    /**
     * 默认数据库
     */
    @Value("${db.info.deafultDb:}")//springmvc 获取配置方式
    private String deafultDb;

    /**
     * 分组别名（默认count）
     */
    @Value("${db.info.groupByAlia:groupCount}")//springmvc 获取配置方式
    private String groupByAlia;

    /**
     * 树查询父节点字段名
     */
    @Value("${db.info.treeColumn:id_at_me__parent}")//springmvc 获取配置方式
    private String treeColumn;

    /**
     * 函数dict
     */
    @Value("${db.info.funcDict:dict}")//springmvc 获取配置方式
    private String funcDict;

    /**
     * 函数dicts
     */
    @Value("${db.info.funcDicts:dicts}")//springmvc 获取配置方式
    private String funcDicts;

    /**
     * 树查询是否从顶点查起
     */
    @Value("${db.info.treeFromRoot:true}")//springmvc 获取配置方式
    private boolean treeFromRoot;

    /**
     * where条件错误展示
     */
    @Value("${db.info.error-where:false}")//springmvc 获取配置方式
    private boolean errorWhere;

    /**
     * 列表展示所有字段
     */
    @Value("${db.info.column-all:true}")//springmvc 获取配置方式
    private boolean columnAll;

    /**
     * 字段大小写
     */
    @Value("${db.info.columnCase:}")//springmvc 获取配置方式
    private String columnCase;

    /**
     * 是否返回插入的信息
     */
    @Value("${db.info.return-inserted:false}")//springmvc 获取配置方式
    private boolean returnInserted;

    /**
     * @Cacheable缓存默认有效期，默认3600秒（1小时）
     */
    @Value("${db.info.ttl:3600}")//springmvc 获取配置方式
    private long ttl;

    /**
     * 默认项目
     */
    @Value("${db.info.app-id:api}")//springmvc 获取配置方式
    private String appId;

    /**
     * 代理端口号
     */
    @Value("${db.info.proxyPort:808}")//springmvc 获取配置方式
    private int proxyPort;

    public String getPlatform() {
        if (DBT.isNull(platform)) {
            platform = DBT.getPlatform();
            if (DBT.isNull(platform)) {
                platform = "mysql";
            }
        }
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDeafultDb() {
        return deafultDb;
    }

    public void setDeafultDb(String deafultDb) {
        this.deafultDb = deafultDb;
    }

    public boolean isErrorWhere() {
        return errorWhere;
    }

    public void setErrorWhere(boolean errorWhere) {
        this.errorWhere = errorWhere;
    }

    public String getGroupByAlia() {
        return groupByAlia;
    }

    public void setGroupByAlia(String groupByAlia) {
        this.groupByAlia = groupByAlia;
    }

    public String getTreeColumn() {
        return treeColumn;
    }

    public void setTreeColumn(String treeColumn) {
        this.treeColumn = treeColumn;
    }

    public boolean isTreeFromRoot() {
        return treeFromRoot;
    }

    public void setTreeFromRoot(boolean treeFromRoot) {
        this.treeFromRoot = treeFromRoot;
    }

    public boolean isColumnAll() {
        return columnAll;
    }

    public void setColumnAll(boolean columnAll) {
        this.columnAll = columnAll;
    }

    public boolean isReturnInserted() {
        return returnInserted;
    }

    public void setReturnInserted(boolean returnInserted) {
        this.returnInserted = returnInserted;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public String getFuncDict() {
        return funcDict;
    }

    public void setFuncDict(String funcDict) {
        this.funcDict = funcDict;
    }

    public String getFuncDicts() {
        return funcDicts;
    }

    public void setFuncDicts(String funcDicts) {
        this.funcDicts = funcDicts;
    }

    public String getColumnCase() {
        return columnCase;
    }

    public void setColumnCase(String columnCase) {
        this.columnCase = columnCase;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
