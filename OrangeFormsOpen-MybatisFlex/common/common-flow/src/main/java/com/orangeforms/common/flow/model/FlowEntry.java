package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
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
@Table(value = "zz_flow_entry")
public class FlowEntry {

    /**
     * 主键。
     */
    @Id(value = "entry_id")
    private Long entryId;

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
     * 流程名称。
     */
    @Column(value = "process_definition_name")
    private String processDefinitionName;

    /**
     * 流程标识Key。
     */
    @Column(value = "process_definition_key")
    private String processDefinitionKey;

    /**
     * 流程分类。
     */
    @Column(value = "category_id")
    private Long categoryId;

    /**
     * 工作流部署的发布主版本Id。
     */
    @Column(value = "main_entry_publish_id")
    private Long mainEntryPublishId;

    /**
     * 最新发布时间。
     */
    @Column(value = "latest_publish_time")
    private Date latestPublishTime;

    /**
     * 流程状态。
     */
    @Column(value = "status")
    private Integer status;

    /**
     * 流程定义的xml。
     */
    @Column(value = "bpmn_xml")
    private String bpmnXml;

    /**
     * 流程图类型。0: 普通流程图，1: 钉钉风格的流程图。
     */
    @Column(value = "diagram_type")
    private Integer diagramType;

    /**
     * 绑定表单类型。
     */
    @Column(value = "bind_form_type")
    private Integer bindFormType;

    /**
     * 流程类型。
     */
    @Column(value = "flow_type")
    private Integer flowType;

    /**
     * 在线表单的页面Id。
     */
    @Column(value = "page_id")
    private Long pageId;

    /**
     * 在线表单Id。
     */
    @Column(value = "default_form_id")
    private Long defaultFormId;

    /**
     * 静态表单的缺省路由名称。
     */
    @Column(value = "default_router_name")
    private String defaultRouterName;

    /**
     * 工单表编码字段的编码规则，如果为空则不计算工单编码。
     */
    @Column(value = "encoded_rule")
    private String encodedRule;

    /**
     * 流程的自定义扩展数据(JSON格式)。
     */
    @Column(value = "extension_data")
    private String extensionData;

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

    @Column(ignore = true)
    private FlowEntryPublish mainFlowEntryPublish;

    @RelationOneToOne(
            masterIdField = "categoryId",
            slaveModelClass = FlowCategory.class,
            slaveIdField = "categoryId")
    @Column(ignore = true)
    private FlowCategory flowCategory;
}
