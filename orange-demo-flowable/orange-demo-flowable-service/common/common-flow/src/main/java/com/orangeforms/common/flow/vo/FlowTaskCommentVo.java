package com.orangeforms.common.flow.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * FlowTaskCommentVO对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("FlowTaskCommentVO对象")
@Data
public class FlowTaskCommentVo {

    /**
     * 主键Id。
     */
    @ApiModelProperty(value = "主键Id")
    private Long id;

    /**
     * 流程实例Id。
     */
    @ApiModelProperty(value = "流程实例Id")
    private String processInstanceId;

    /**
     * 任务Id。
     */
    @ApiModelProperty(value = "任务Id")
    private String taskId;

    /**
     * 任务标识。
     */
    @ApiModelProperty(value = "任务标识")
    private String taskKey;

    /**
     * 任务名称。
     */
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    /**
     * 审批类型。
     */
    @ApiModelProperty(value = "审批类型")
    private String approvalType;

    /**
     * 批注内容。
     */
    @ApiModelProperty(value = "批注内容")
    private String comment;

    /**
     * 委托指定人，比如加签、转办等。
     */
    @ApiModelProperty(value = "委托指定人，比如加签、转办等")
    private String delegateAssginee;

    /**
     * 自定义数据。开发者可自行扩展，推荐使用JSON格式数据。
     */
    @ApiModelProperty(value = "自定义数据s")
    private String customBusinessData;

    /**
     * 创建者Id。
     */
    @ApiModelProperty(value = "创建者Id")
    private Long createUserId;

    /**
     * 创建者登录名。
     */
    @ApiModelProperty(value = "创建者登录名")
    private String createLoginName;

    /**
     * 创建者显示名。
     */
    @ApiModelProperty(value = "创建者显示名")
    private String createUsername;

    /**
     * 创建时间。
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
