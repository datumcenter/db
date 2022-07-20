package io.gallery.db.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value = "分页")
public class DataBaseGenericPage extends DataBasePermission {
    @ApiModelProperty(value = "记录位置", example = "0")
    private Integer start;
    @ApiModelProperty(value = "页面显示数量", example = "10")
    private Integer length;
    @ApiModelProperty(value = "总记录数", hidden = true)
    private Long recordsTotal;
    @ApiModelProperty(value = "总记录数", hidden = true)
    private Long total;
    @ApiModelProperty(value = "记录", hidden = true)
    private Object data;
    @ApiModelProperty(value = "排序条件")
    private String orderBy;
    @ApiModelProperty(value = "分组条件")
    private String groupBy;
    @ApiModelProperty(value = "表示请求次数", hidden = true)
    private Integer draw;
    @ApiModelProperty(value = "过滤后的总记录数", hidden = true)
    private Long recordsFiltered;
    @ApiModelProperty(value = "是否递归")
    private boolean tree;
    @ApiModelProperty(value = "是否显示原始树")
    private boolean treePlain;
    @ApiModelProperty(value = "递归父字段名")
    private String treeColumn;
    @ApiModelProperty(value = "是否分页")
    private boolean page = true;
    @ApiModelProperty(value = "导出文件名称")
    private String exportTitle;
    @ApiModelProperty(value = "导出文件表头,逗号分开")
    private String exportHeaders;
    @ApiModelProperty(value = "导出文件格式")
    private ExportType exportType;
    @ApiModelProperty(value = "内容是否需要标题（和文件名一致）")
    private boolean needTitle;
    @ApiModelProperty(value = "是否需要从视图里获取数据")
    private boolean needView;
    @ApiModelProperty(value = "是否需要翻译数据字典")
    private boolean needDict;
    @ApiModelProperty(value = "是否需要翻译关联字段")
    private boolean needKey;

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }

    public Long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
        this.total = recordsTotal;
    }

    public Long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

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
        this.recordsTotal = total;
    }

    public boolean isTreePlain() {
        return treePlain;
    }

    public void setTreePlain(boolean treePlain) {
        this.treePlain = treePlain;
    }

    public String getExportTitle() {
        return exportTitle;
    }

    public void setExportTitle(String exportTitle) {
        this.exportTitle = exportTitle;
    }

    public String getExportHeaders() {
        return exportHeaders;
    }

    public void setExportHeaders(String exportHeaders) {
        this.exportHeaders = exportHeaders;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }

    public boolean isNeedTitle() {
        return needTitle;
    }

    public void setNeedTitle(boolean needTitle) {
        this.needTitle = needTitle;
    }

    public boolean isNeedView() {
        return needView;
    }

    public void setNeedView(boolean needView) {
        this.needView = needView;
    }

    public boolean isNeedDict() {
        return needDict;
    }

    public void setNeedDict(boolean needDict) {
        this.needDict = needDict;
    }

    public boolean isNeedKey() {
        return needKey;
    }

    public void setNeedKey(boolean needKey) {
        this.needKey = needKey;
    }
}
