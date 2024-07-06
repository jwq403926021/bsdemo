package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流程变量实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_entry_variable")
public class FlowEntryVariable {

    /**
     * 主键Id。
     */
    @Id(value = "variable_id")
    private Long variableId;

    /**
     * 流程Id。
     */
    @Column(value = "entry_id")
    private Long entryId;

    /**
     * 变量名。
     */
    @Column(value = "variable_name")
    private String variableName;

    /**
     * 显示名。
     */
    @Column(value = "show_name")
    private String showName;

    /**
     * 流程变量类型。
     */
    @Column(value = "variable_type")
    private Integer variableType;

    /**
     * 绑定数据源Id。
     */
    @Column(value = "bind_datasource_id")
    private Long bindDatasourceId;

    /**
     * 绑定数据源关联Id。
     */
    @Column(value = "bind_relation_id")
    private Long bindRelationId;

    /**
     * 绑定字段Id。
     */
    @Column(value = "bind_column_id")
    private Long bindColumnId;

    /**
     * 是否内置。
     */
    @Column(value = "builtin")
    private Boolean builtin;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;
}