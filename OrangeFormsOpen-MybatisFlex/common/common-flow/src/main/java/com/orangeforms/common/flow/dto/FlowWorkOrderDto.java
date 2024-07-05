package com.orangeforms.common.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作流工单Dto对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "工作流工单Dto对象")
@Data
public class FlowWorkOrderDto {

    /**
     * 工单编码。
     */
    @Schema(description = "工单编码")
    private String workOrderCode;

    /**
     * 流程状态。参考FlowTaskStatus常量值对象。
     */
    @Schema(description = "流程状态")
    private Integer flowStatus;

    /**
     * createTime 范围过滤起始值(>=)。
     */
    @Schema(description = "createTime 范围过滤起始值")
    private String createTimeStart;

    /**
     * createTime 范围过滤结束值(<=)。
     */
    @Schema(description = "createTime 范围过滤结束值")
    private String createTimeEnd;
}
