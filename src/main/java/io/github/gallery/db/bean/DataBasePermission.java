package com.longruan.ark.common.db.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "权限")
public class DataBasePermission {
    @ApiModelProperty(value = "所属机构", required = true)
    private String dbOrganizationid;
    @ApiModelProperty(value = "所属部门", required = true)
    private String dbDepartmentid;
    @ApiModelProperty(value = "所属人", required = true)
    private String dbUserid;

    public String getDbOrganizationid() {
        return dbOrganizationid;
    }

    public void setDbOrganizationid(String dbOrganizationid) {
        this.dbOrganizationid = dbOrganizationid;
    }

    public String getDbDepartmentid() {
        return dbDepartmentid;
    }

    public void setDbDepartmentid(String dbDepartmentid) {
        this.dbDepartmentid = dbDepartmentid;
    }

    public String getDbUserid() {
        return dbUserid;
    }

    public void setDbUserid(String dbUserid) {
        this.dbUserid = dbUserid;
    }
}
