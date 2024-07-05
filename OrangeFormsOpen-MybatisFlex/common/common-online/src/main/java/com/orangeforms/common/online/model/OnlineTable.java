package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationOneToMany;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线表单的数据表实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_table")
public class OnlineTable {

    /**
     * 主键Id。
     */
    @Id(value = "table_id")
    private Long tableId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 表名称。
     */
    @Column(value = "table_name")
    private String tableName;

    /**
     * 实体名称。
     */
    @Column(value = "model_name")
    private String modelName;

    /**
     * 数据库链接Id。
     */
    @Column(value = "dblink_id")
    private Long dblinkId;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 创建者。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 更新者。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    @RelationOneToMany(
            masterIdField = "tableId",
            slaveModelClass = OnlineColumn.class,
            slaveIdField = "tableId")
    @Column(ignore = true)
    private List<OnlineColumn> columnList;

    /**
     * 该字段会被缓存，因此在线表单执行操作时可以从缓存中读取该数据，并可基于columnId进行快速检索。
     */
    @Column(ignore = true)
    private Map<Long, OnlineColumn> columnMap;

    /**
     * 当前表的主键字段，该字段仅仅用于动态表单运行时的SQL拼装。
     */
    @Column(ignore = true)
    private OnlineColumn primaryKeyColumn;

    /**
     * 当前表的逻辑删除字段，该字段仅仅用于动态表单运行时的SQL拼装。
     */
    @Column(ignore = true)
    private OnlineColumn logicDeleteColumn;
}
