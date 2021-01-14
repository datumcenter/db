package io.gallery.db.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "权限")
public class DataBasePermission {
    @ApiModelProperty(value = "所属机构", required = true)
    private String dborgid;
    @ApiModelProperty(value = "所属部门", required = true)
    private String dbdepid;
    @ApiModelProperty(value = "所属人", required = true)
    private String dbuid;

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
}
