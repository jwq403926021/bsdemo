package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流程处理事务事件生产者流水实体。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_trans_producer")
public class FlowTransProducer {

    /**
     * 流水Id。
     */
    @Id(value = "trans_id")
    private Long transId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 业务数据库链接Id。
     */
    @Column(value = "dblink_id")
    private Long dblinkId;

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
     * 任务名称。
     */
    @Column(value = "task_name")
    private String taskName;

    /**
     * 审批批注。
     */
    @Column(value = "task_comment")
    private String taskComment;

    /**
     * 当前请求的url。
     */
    @Column(value = "url")
    private String url;

    /**
     * 创建该事务性事件对象的初始方法。格式为：方法名(参数类型1,参数类型2)。
     */
    @Column(value = "init_method")
    private String initMethod;

    /**
     * 当前请求的traceId。
     */
    @Column(value = "trace_id")
    private String traceId;

    /**
     * 和SQL操作相关的数据。值类型为TransactionalBusinessData.BusinessSqlData对象。
     */
    @Column(value = "sql_data")
    private String sqlData;

    /**
     * 自动化流程的任务配置。
     */
    @Column(value = "auto_task_config")
    private String autoTaskConfig;

    /**
     * 尝试次数。默认的插入值为1。
     */
    @Column(value = "try_times")
    private Integer tryTimes;

    /**
     * 提交业务数据时的错误信息。如果正常提交，该值为空。
     */
    @Column(value = "error_reason")
    private String errorReason;

    /**
     * 创建者登录名。
     */
    @Column(value = "create_login_name")
    private String createLoginName;

    /**
     * 创建者中文用户名。
     */
    @Column(value = "create_username")
    private String createUsername;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;
}
