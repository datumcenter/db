package io.gallery.db.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "分页")
public class DataBaseGenericPage extends DataBasePermission {
    @ApiModelProperty(value = "记录位置")
    private Integer start;
    @ApiModelProperty(value = "页面显示数量", required = true)
    private Integer length;
    @ApiModelProperty(value = "总记录数", hidden = true)
    private Long total;
    @ApiModelProperty(value = "记录", hidden = true)
    private Object data;
    @ApiModelProperty(value = "排序条件")
    private String orderBy;
    @ApiModelProperty(value = "分组条件")
    private String groupBy;
    @ApiModelProperty(value = "是否递归")
    private boolean tree;
    @ApiModelProperty(value = "是否显示原始树")
    private boolean treePlain;
    @ApiModelProperty(value = "递归父字段名")
    private String treeColumn;
    @ApiModelProperty(value = "是否分页")
    private boolean page = true;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }


    public boolean isTree() {
        return tree;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getTreeColumn() {
        return treeColumn;
    }

    public void setTreeColumn(String treeColumn) {
        this.treeColumn = treeColumn;
    }

    public boolean isPage() {
        return page;
    }

    public void setPage(boolean page) {
        this.page = page;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public boolean isTreePlain() {
        return treePlain;
    }

    public void setTreePlain(boolean treePlain) {
        this.treePlain = treePlain;
    }
}
