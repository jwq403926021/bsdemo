package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 数据源及其关联所引用的数据表实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_datasource_table")
public class OnlineDatasourceTable {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 数据源Id。
     */
    @Column(value = "datasource_id")
    private Long datasourceId;

    /**
     * 数据源关联Id。
     */
    @Column(value = "relation_id")
    private Long relationId;

    /**
     * 数据表Id。
     */
    @Column(value = "table_id")
    private Long tableId;
}
