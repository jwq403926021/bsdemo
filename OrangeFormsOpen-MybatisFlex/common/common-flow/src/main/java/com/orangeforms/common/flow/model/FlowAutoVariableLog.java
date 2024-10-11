package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 自动化流程变量实体。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_auto_variable_log")
public class FlowAutoVariableLog {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 流程实例Id。
     */
    @Column(value = "process_instance_id")
    private String processInstanceId;

    /**
     * 执行实例Id。
     */
    @Column(value = "execution_id")
    private String executionId;

    /**
     * 任务Id。
     */
    @Column(value = "task_id")
    private String taskId;

    /**
     * 任务标识。
     */
    @Column(value = "task_key")
    private String taskKey;

    /**
     * 当前请求的traceId。
     */
    @Column(value = "trace_id")
    private String traceId;

    /**
     * 变量数据。
     */
    @Column(value = "variable_data")
    private String variableData;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;
}
