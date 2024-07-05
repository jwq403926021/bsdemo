package com.orangeforms.common.flow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 流程发布信息的Vo对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("流程发布信息的Vo对象")
@Data
public class FlowEntryPublishVo {

    /**
     * 主键Id。
     */
    @ApiModelProperty(value = "主键Id")
    private Long entryPublishId;

    /**
     * 发布版本。
     */
    @ApiModelProperty(value = "发布版本")
    private Integer publishVersion;

    /**
     * 流程引擎中的流程定义Id。
     */
    @ApiModelProperty(value = "流程引擎中的流程定义Id")
    private String processDefinitionId;

    /**
     * 激活状态。
     */
    @ApiModelProperty(value = "激活状态")
    private Boolean activeStatus;

    /**
     * 是否为主版本。
     */
    @ApiModelProperty(value = "是否为主版本")
    private Boolean mainVersion;

    /**
     * 创建者Id。
     */
    @ApiModelProperty(value = "创建者Id")
    private Long createUserId;

    /**
     * 发布时间。
     */
    @ApiModelProperty(value = "发布时间")
    private Date publishTime;
}
