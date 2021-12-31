package com.orangeforms.common.online.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 在线表单的数据表VO对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("在线表单的数据表VO对象")
@Data
public class OnlineTableVo {

    /**
     * 主键Id。
     */
    @ApiModelProperty(value = "主键Id")
    private Long tableId;

    /**
     * 表名称。
     */
    @ApiModelProperty(value = "表名称")
    private String tableName;

    /**
     * 实体名称。
     */
    @ApiModelProperty(value = "实体名称")
    private String modelName;

    /**
     * 数据库链接Id。
     */
    @ApiModelProperty(value = "数据库链接Id")
    private Long dblinkId;

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
}
