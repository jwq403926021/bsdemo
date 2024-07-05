package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流程发布数据的实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_entry_publish")
public class FlowEntryPublish {

    /**
     * 主键Id。
     */
    @Id(value = "entry_publish_id")
    private Long entryPublishId;

    /**
     * 流程Id。
     */
    @Column(value = "entry_id")
    private Long entryId;

    /**
     * 流程引擎的部署Id。
     */
    @Column(value = "deploy_id")
    private String deployId;

    /**
     * 流程引擎中的流程定义Id。
     */
    @Column(value = "process_definition_id")
    private String processDefinitionId;

    /**
     * 发布版本。
     */
    @Column(value = "publish_version")
    private Integer publishVersion;

    /**
     * 激活状态。
     */
    @Column(value = "active_status")
    private Boolean activeStatus;

    /**
     * 是否为主版本。
     */
    @Column(value = "main_version")
    private Boolean mainVersion;

    /**
     * 创建者Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 发布时间。
     */
    @Column(value = "publish_time")
    private Date publishTime;

    /**
     * 第一个非开始节点任务的附加信息。
     */
    @Column(value = "init_task_info")
    private String initTaskInfo;

    /**
     * 分析后的节点JSON信息。
     */
    @Column(value = "analyzed_node_json")
    private String analyzedNodeJson;

    /**
     * 流程的自定义扩展数据(JSON格式)。
     */
    @Column(value = "extension_data")
    private String extensionData;
}
