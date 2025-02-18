package com.orangeforms.common.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作流通知消息Dto对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "工作流通知消息Dto对象")
@Data
public class FlowMessageDto {

    /**
     * 消息类型。
     */
    @Schema(description = "消息类型")
    private Integer messageType;

    /**
     * 工单Id。
     */
    @Schema(description = "工单Id")
    private Long workOrderId;

    /**
     * 流程名称。
     */
    @Schema(description = "流程名称")
    private String processDefinitionName;

    /**
     * 流程任务名称。
     */
    @Schema(description = "流程任务名称")
    private String taskName;

    /**
     * 更新时间范围过滤起始值(>=)。
     */
    @Schema(description = "updateTime 范围过滤起始值")
    private String updateTimeStart;

    /**
     * 更新时间范围过滤结束值(<=)。
     */
    @Schema(description = "updateTime 范围过滤结束值")
    private String updateTimeEnd;
}
