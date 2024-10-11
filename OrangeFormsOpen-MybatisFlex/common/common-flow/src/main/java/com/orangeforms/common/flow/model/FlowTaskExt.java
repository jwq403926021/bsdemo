package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 流程任务扩展实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_task_ext")
public class FlowTaskExt {

    /**
     * 流程引擎的定义Id。
     */
    @Column(value = "process_definition_id")
    private String processDefinitionId;

    /**
     * 流程引擎任务Id。
     */
    @Column(value = "task_id")
    private String taskId;

    /**
     * 操作列表JSON。
     */
    @Column(value = "operation_list_json")
    private String operationListJson;

    /**
     * 变量列表JSON。
     */
    @Column(value = "variable_list_json")
    private String variableListJson;

    /**
     * 存储多实例的assigneeList的JSON。
     */
    @Column(value = "assignee_list_json")
    private String assigneeListJson;

    /**
     * 分组类型。
     */
    @Column(value = "group_type")
    private String groupType;

    /**
     * 保存岗位相关的数据。
     */
    @Column(value = "dept_post_list_json")
    private String deptPostListJson;

    /**
     * 逗号分隔的角色Id。
     */
    @Column(value = "role_ids")
    private String roleIds;

    /**
     * 逗号分隔的部门Id。
     */
    @Column(value = "dept_ids")
    private String deptIds;

    /**
     * 逗号分隔候选用户名。
     */
    @Column(value = "candidate_usernames")
    private String candidateUsernames;

    /**
    * 抄送相关的数据。
    */
    @Column(value = "copy_list_json")
    private String copyListJson;

    /**
     * 用户任务的扩展属性，存储为JSON的字符串格式。
     */
    @Column(value = "extra_data_json")
    private String extraDataJson;

    /**
     * 自动化任务配置数据，存储为JSON的字符串格式。
     */
    @Column(value = "auto_config_json")
    private String autoConfigJson;
}
