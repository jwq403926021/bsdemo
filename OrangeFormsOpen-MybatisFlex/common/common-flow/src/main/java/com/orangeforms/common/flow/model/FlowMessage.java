package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 工作流通知消息实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_message")
public class FlowMessage {

    /**
     * 主键Id。
     */
    @Id(value = "message_id")
    private Long messageId;

    /**
     * 租户Id。
     */
    @Column(value = "tenant_id")
    private Long tenantId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 消息类型。
     */
    @Column(value = "message_type")
    private Integer messageType;

    /**
     * 消息内容。
     */
    @Column(value = "message_content")
    private String messageContent;

    /**
     * 催办次数。
     */
    @Column(value = "remind_count")
    private Integer remindCount;

    /**
     * 工单Id。
     */
    @Column(value = "work_order_id")
    private Long workOrderId;

    /**
     * 流程定义Id。
     */
    @Column(value = "process_definition_id")
    private String processDefinitionId;

    /**
     * 流程定义标识。
     */
    @Column(value = "process_definition_key")
    private String processDefinitionKey;

    /**
     * 流程名称。
     */
    @Column(value = "process_definition_name")
    private String processDefinitionName;

    /**
     * 流程实例Id。
     */
    @Column(value = "process_instance_id")
    private String processInstanceId;

    /**
     * 流程实例发起者。
     */
    @Column(value = "process_instance_initiator")
    private String processInstanceInitiator;

    /**
     * 流程任务Id。
     */
    @Column(value = "task_id")
    private String taskId;

    /**
     * 流程任务定义标识。
     */
    @Column(value = "task_definition_key")
    private String taskDefinitionKey;

    /**
     * 流程任务名称。
     */
    @Column(value = "task_name")
    private String taskName;

    /**
     * 创建时间。
     */
    @Column(value = "task_start_time")
    private Date taskStartTime;

    /**
     * 任务指派人登录名。
     */
    @Column(value = "task_assignee")
    private String taskAssignee;

    /**
     * 任务是否已完成。
     */
    @Column(value = "task_finished")
    private Boolean taskFinished;

    /**
     * 业务数据快照。
     */
    @Column(value = "business_data_shot")
    private String businessDataShot;

    /**
     * 是否为在线表单消息数据。
     */
    @Column(value = "online_form_data")
    private Boolean onlineFormData;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 更新者Id。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 创建者Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 创建者显示名。
     */
    @Column(value = "create_username")
    private String createUsername;
}
