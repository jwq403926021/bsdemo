package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationConstDict;
import com.orangeforms.common.core.annotation.RelationDict;
import com.orangeforms.common.core.annotation.RelationOneToOne;
import com.orangeforms.common.online.model.constant.RelationType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单的数据源关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_datasource_relation")
public class OnlineDatasourceRelation {

    /**
     * 主键Id。
     */
    @Id(value = "relation_id")
    private Long relationId;

    /**
     * 应用Id。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 关联名称。
     */
    @Column(value = "relation_name")
    private String relationName;

    /**
     * 变量名。
     */
    @Column(value = "variable_name")
    private String variableName;

    /**
     * 主数据源Id。
     */
    @Column(value = "datasource_id")
    private Long datasourceId;

    /**
     * 关联类型。
     */
    @Column(value = "relation_type")
    private Integer relationType;

    /**
     * 主表关联字段Id。
     */
    @Column(value = "master_column_id")
    private Long masterColumnId;

    /**
     * 从表Id。
     */
    @Column(value = "slave_table_id")
    private Long slaveTableId;

    /**
     * 从表关联字段Id。
     */
    @Column(value = "slave_column_id")
    private Long slaveColumnId;

    /**
     * 删除主表的时候是否级联删除一对一和一对多的从表数据，多对多只是删除关联，不受到这个标记的影响。。
     */
    @Column(value = "cascade_delete")
    private Boolean cascadeDelete;

    /**
     * 是否左连接。
     */
    @Column(value = "left_join")
    private Boolean leftJoin;

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

    @RelationOneToOne(
            masterIdField = "masterColumnId",
            slaveModelClass = OnlineColumn.class,
            slaveIdField = "columnId")
    @Column(ignore = true)
    private OnlineColumn masterColumn;

    @RelationOneToOne(
            masterIdField = "slaveTableId",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId")
    @Column(ignore = true)
    private OnlineTable slaveTable;

    @RelationOneToOne(
            masterIdField = "slaveColumnId",
            slaveModelClass = OnlineColumn.class,
            slaveIdField = "columnId")
    @Column(ignore = true)
    private OnlineColumn slaveColumn;

    @RelationDict(
            masterIdField = "masterColumnId",
            equalOneToOneRelationField = "onlineColumn",
            slaveModelClass = OnlineColumn.class,
            slaveIdField = "columnId",
            slaveNameField = "columnName")
    @Column(ignore = true)
    private Map<String, Object> masterColumnIdDictMap;

    @RelationDict(
            masterIdField = "slaveTableId",
            equalOneToOneRelationField = "onlineTable",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId",
            slaveNameField = "modelName")
    @Column(ignore = true)
    private Map<String, Object> slaveTableIdDictMap;

    @RelationDict(
            masterIdField = "slaveColumnId",
            equalOneToOneRelationField = "onlineColumn",
            slaveModelClass = OnlineColumn.class,
            slaveIdField = "columnId",
            slaveNameField = "columnName")
    @Column(ignore = true)
    private Map<String, Object> slaveColumnIdDictMap;

    @RelationConstDict(
            masterIdField = "relationType",
            constantDictClass = RelationType.class)
    @Column(ignore = true)
    private Map<String, Object> relationTypeDictMap;
}
