package com.orangeforms.common.online.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线表单的数据源VO对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("在线表单的数据源VO对象")
@Data
public class OnlineDatasourceVo {

    /**
     * 主键Id。
     */
    @ApiModelProperty(value = "主键Id")
    private Long datasourceId;

    /**
     * 数据源名称。
     */
    @ApiModelProperty(value = "数据源名称")
    private String datasourceName;

    /**
     * 数据源变量名，会成为数据访问url的一部分。
     */
    @ApiModelProperty(value = "数据源变量名")
    private String variableName;

    /**
     * 数据库链接Id。
     */
    @ApiModelProperty(value = "数据库链接Id")
    private Long dblinkId;

    /**
     * 主表Id。
     */
    @ApiModelProperty(value = "主表Id")
    private Long masterTableId;

    /**
     * 更新时间。
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 创建时间。
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * datasourceId 的多对多关联表数据对象，数据对应类型为OnlinePageDatasourceVo。
     */
    @ApiModelProperty(value = "datasourceId 的多对多关联表数据对象")
    private Map<String, Object> onlinePageDatasource;

    /**
     * masterTableId 字典关联数据。
     */
    @ApiModelProperty(value = "masterTableId 字典关联数据")
    private Map<String, Object> masterTableIdDictMap;

    /**
     * 当前数据源及其关联，引用的数据表对象列表。
     */
    @ApiModelProperty(value = "当前数据源及其关联，引用的数据表对象列表")
    private List<OnlineTableVo> tableList;
}
