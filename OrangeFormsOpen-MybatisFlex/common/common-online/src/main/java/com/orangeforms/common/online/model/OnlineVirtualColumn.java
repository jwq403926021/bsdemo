package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 在线数据表虚拟字段实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_virtual_column")
public class OnlineVirtualColumn {

    /**
     * 主键Id。
     */
    @Id(value = "virtual_column_id")
    private Long virtualColumnId;

    /**
     * 所在表Id。
     */
    @Column(value = "table_id")
    private Long tableId;

    /**
     * 字段名称。
     */
    @Column(value = "object_field_name")
    private String objectFieldName;

    /**
     * 属性类型。
     */
    @Column(value = "object_field_type")
    private String objectFieldType;

    /**
     * 字段提示名。
     */
    @Column(value = "column_prompt")
    private String columnPrompt;

    /**
     * 虚拟字段类型(0: 聚合)。
     */
    @Column(value = "virtual_type")
    private Integer virtualType;

    /**
     * 关联数据源Id。
     */
    @Column(value = "datasource_id")
    private Long datasourceId;

    /**
     * 关联Id。
     */
    @Column(value = "relation_id")
    private Long relationId;

    /**
     * 聚合字段所在关联表Id。
     */
    @Column(value = "aggregation_table_id")
    private Long aggregationTableId;

    /**
     * 关联表聚合字段Id。
     */
    @Column(value = "aggregation_column_id")
    private Long aggregationColumnId;

    /**
     * 聚合类型(0: count 1: sum 2: avg 3: max 4:min)。
     */
    @Column(value = "aggregation_type")
    private Integer aggregationType;

    /**
     * 存储过滤条件的json。
     */
    @Column(value = "where_clause_json")
    private String whereClauseJson;
}
