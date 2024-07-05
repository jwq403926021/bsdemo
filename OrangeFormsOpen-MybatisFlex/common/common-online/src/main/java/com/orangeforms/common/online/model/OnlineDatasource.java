package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationDict;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 在线表单的数据源实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_datasource")
public class OnlineDatasource {

    /**
     * 主键Id。
     */
    @Id(value = "datasource_id")
    private Long datasourceId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 数据源名称。
     */
    @Column(value = "datasource_name")
    private String datasourceName;

    /**
     * 数据源变量名，会成为数据访问url的一部分。
     */
    @Column(value = "variable_name")
    private String variableName;

    /**
     * 数据库链接Id。
     */
    @Column(value = "dblink_id")
    private Long dblinkId;

    /**
     * 主表Id。
     */
    @Column(value = "master_table_id")
    private Long masterTableId;

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

    /**
     * datasourceId 的多对多关联的数据对象。
     */
    @Column(ignore = true)
    private OnlinePageDatasource onlinePageDatasource;

    /**
     * datasourceId 的多对多关联的数据对象。
     */
    @Column(ignore = true)
    private List<OnlineFormDatasource> onlineFormDatasourceList;

    @RelationDict(
            masterIdField = "masterTableId",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId",
            slaveNameField = "tableName")
    @Column(ignore = true)
    private Map<String, Object> masterTableIdDictMap;

    @Column(ignore = true)
    private OnlineTable masterTable;
}
