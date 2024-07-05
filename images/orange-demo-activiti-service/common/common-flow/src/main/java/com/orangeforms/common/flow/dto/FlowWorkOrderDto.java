package com.orangeforms.common.flow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工作流工单Dto对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("工作流工单Dto对象")
@Data
public class FlowWorkOrderDto {

    /**
     * 流程状态。参考FlowTaskStatus常量值对象。
     */
    @ApiModelProperty(value = "流程状态")
    private Integer flowStatus;

    /**
     * createTime 范围过滤起始值(>=)。
     */
    @ApiModelProperty(value = "createTime 范围过滤起始值")
    private String createTimeStart;

    /**
     * createTime 范围过滤结束值(<=)。
     */
    @ApiModelProperty(value = "createTime 范围过滤结束值")
    private String createTimeEnd;
}
