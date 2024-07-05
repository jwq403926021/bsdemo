package com.orangeforms.common.log.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.TenantFilterColumn;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志记录表
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table("zz_sys_operation_log")
public class SysOperationLog {

    /**
     * 主键Id。
     */
    @Id(value = "log_id")
    private Long logId;

    /**
     * 日志描述。
     */
    @Column(value = "description")
    private String description;

    /**
     * 操作类型。
     * 常量值定义可参考SysOperationLogType对象。
     */
    @Column(value = "operation_type")
    private Integer operationType;

    /**
     * 接口所在服务名称。
     * 通常为spring.application.name配置项的值。
     */
    @Column(value = "service_name")
    private String serviceName;

    /**
     * 调用的controller全类名。
     * 之所以为独立字段，是为了便于查询和统计接口的调用频度。
     */
    @Column(value = "api_class")
    private String apiClass;

    /**
     * 调用的controller中的方法。
     * 格式为：接口类名 + "." + 方法名。
     */
    @Column(value = "api_method")
    private String apiMethod;

    /**
     * 用户会话sessionId。
     * 主要是为了便于统计，以及跟踪查询定位问题。
     */
    @Column(value = "session_id")
    private String sessionId;

    /**
     * 每次请求的Id。
     * 对于微服务之间的调用，在同一个请求的调用链中，该值是相同的。
     */
    @Column(value = "trace_id")
    private String traceId;

    /**
     * 调用时长。
     */
    @Column(value = "elapse")
    private Long elapse;

    /**
     * HTTP 请求方法，如GET。
     */
    @Column(value = "request_method")
    private String requestMethod;

    /**
     * HTTP 请求地址。
     */
    @Column(value = "request_url")
    private String requestUrl;

    /**
     * controller接口参数。
     */
    @Column(value = "request_arguments")
    private String requestArguments;

    /**
     * controller应答结果。
     */
    @Column(value = "response_result")
    private String responseResult;

    /**
     * 请求IP。
     */
    @Column(value = "request_ip")
    private String requestIp;

    /**
     * 应答状态。
     */
    @Column(value = "success")
    private Boolean success;

    /**
     * 错误信息。
     */
    @Column(value = "error_msg")
    private String errorMsg;

    /**
     * 租户Id。
     * 仅用于多租户系统，是便于进行对租户的操作查询和统计分析。
     */
    @TenantFilterColumn
    @Column(value = "tenant_id")
    private Long tenantId;

    /**
     * 操作员Id。
     */
    @Column(value = "operator_id")
    private Long operatorId;

    /**
     * 操作员名称。
     */
    @Column(value = "operator_name")
    private String operatorName;

    /**
     * 操作时间。
     */
    @Column(value = "operation_time")
    private Date operationTime;

    /**
     * 调用时长最小值。
     */
    @Column(ignore = true)
    private Long elapseMin;

    /**
     * 调用时长最大值。
     */
    @Column(ignore = true)
    private Long elapseMax;

    /**
     * 操作开始时间。
     */
    @Column(ignore = true)
    private String operationTimeStart;

    /**
     * 操作结束时间。
     */
    @Column(ignore = true)
    private String operationTimeEnd;
}
