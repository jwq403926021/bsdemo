package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.task.api.TaskInfo;

import java.util.Date;

/**
 * 流程多实例任务执行流水对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@NoArgsConstructor
@Table(value = "zz_flow_multi_instance_trans")
public class FlowMultiInstanceTrans {

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
     * 会签任务的执行Id。
     */
    @Column(value = "multi_instance_exec_id")
    private String multiInstanceExecId;

    /**
     * 任务的执行Id。
     */
    @Column(value = "execution_id")
    private String executionId;

    /**
     * 会签指派人列表。
     */
    @Column(value = "assignee_list")
    private String assigneeList;

    /**
     * 创建者Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 创建者登录名。
     */
    @Column(value = "create_login_name")
    private String createLoginName;

    /**
     * 创建者显示名。
     */
    @Column(value = "create_username")
    private String createUsername;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    public FlowMultiInstanceTrans(TaskInfo task) {
        this.fillWith(task);
    }

    public void fillWith(TaskInfo task) {
        this.taskId = task.getId();
        this.taskKey = task.getTaskDefinitionKey();
        this.processInstanceId = task.getProcessInstanceId();
        this.executionId = task.getExecutionId();
    }
}
