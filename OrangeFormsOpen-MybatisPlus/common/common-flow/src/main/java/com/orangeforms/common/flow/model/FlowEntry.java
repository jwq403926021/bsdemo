package com.orangeforms.common.flow.model;

import com.baomidou.mybatisplus.annotation.*;
import com.orangeforms.common.core.annotation.RelationOneToOne;
import lombok.Data;

import java.util.Date;

/**
 * 流程的实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@TableName(value = "zz_flow_entry")
public class FlowEntry {

    /**
     * 主键。
     */
    @TableId(value = "entry_id")
    private Long entryId;

    /**
     * 租户Id。
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 流程名称。
     */
    @TableField(value = "process_definition_name")
    private String processDefinitionName;

    /**
     * 流程标识Key。
     */
    @TableField(value = "process_definition_key")
    private String processDefinitionKey;

    /**
     * 流程分类。
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 工作流部署的发布主版本Id。
     */
    @TableField(value = "main_entry_publish_id")
    private Long mainEntryPublishId;

    /**
     * 最新发布时间。
     */
    @TableField(value = "latest_publish_time")
    private Date latestPublishTime;

    /**
     * 流程状态。
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 流程定义的xml。
     */
    @TableField(value = "bpmn_xml")
    private String bpmnXml;

    /**
     * 流程图类型。0: 普通流程图，1: 钉钉风格的流程图。
     */
    @TableField(value = "diagram_type")
    private Integer diagramType;

    /**
     * 绑定表单类型。
     */
    @TableField(value = "bind_form_type")
    private Integer bindFormType;

    /**
     * 流程类型。
     */
    @TableField(value = "flow_type")
    private Integer flowType;

    /**
     * 在线表单的页面Id。
     */
    @TableField(value = "page_id")
    private Long pageId;

    /**
     * 在线表单Id。
     */
    @TableField(value = "default_form_id")
    private Long defaultFormId;

    /**
     * 静态表单的缺省路由名称。
     */
    @TableField(value = "default_router_name")
    private String defaultRouterName;

    /**
     * 工单表编码字段的编码规则，如果为空则不计算工单编码。
     */
    @TableField(value = "encoded_rule")
    private String encodedRule;

    /**
     * 流程的自定义扩展数据(JSON格式)。
     */
    @TableField(value = "extension_data")
    private String extensionData;

    /**
     * 更新时间。
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 更新者Id。
     */
    @TableField(value = "update_user_id")
    private Long updateUserId;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建者Id。
     */
    @TableField(value = "create_user_id")
    private Long createUserId;

    @TableField(exist = false)
    private FlowEntryPublish mainFlowEntryPublish;

    @RelationOneToOne(
            masterIdField = "categoryId",
            slaveModelClass = FlowCategory.class,
            slaveIdField = "categoryId")
    @TableField(exist = false)
    private FlowCategory flowCategory;
}
