package io.gallery.db.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "权限")
public class DataBaseCommon {
    @ApiModelProperty(value = "所属机构", required = true)
    private String dborgid;
    @ApiModelProperty(value = "所属部门", required = true)
    private String dbdepid;
    @ApiModelProperty(value = "所属人", required = true)
    private String dbuid;
    @ApiModelProperty(value = "创建时间")
    private String dbctime;
    @ApiModelProperty(value = "更新时间")
    private String dbutime;

    public String getDborgid() {
        return dborgid;
    }

    public void setDborgid(String dborgid) {
        this.dborgid = dborgid;
    }

    public String getDbdepid() {
        return dbdepid;
    }

    public void setDbdepid(String dbdepid) {
        this.dbdepid = dbdepid;
    }

    public String getDbuid() {
        return dbuid;
    }

    public void setDbuid(String dbuid) {
        this.dbuid = dbuid;
    }

    public String getDbctime() {
        return dbctime;
    }

    public void setDbctime(String dbctime) {
        this.dbctime = dbctime;
    }

    public String getDbutime() {
        return dbutime;
    }

    public void setDbutime(String dbutime) {
        this.dbutime = dbutime;
    }
}
