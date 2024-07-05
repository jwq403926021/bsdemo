package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationConstDict;
import com.orangeforms.common.core.annotation.RelationDict;
import com.orangeforms.common.core.constant.DictType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单关联的字典实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_dict")
public class OnlineDict {

    /**
     * 主键Id。
     */
    @Id(value = "dict_id")
    private Long dictId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 字典名称。
     */
    @Column(value = "dict_name")
    private String dictName;

    /**
     * 字典类型。
     */
    @Column(value = "dict_type")
    private Integer dictType;

    /**
     * 数据库链接Id。
     */
    @Column(value = "dblink_id")
    private Long dblinkId;

    /**
     * 字典表名称。
     */
    @Column(value = "table_name")
    private String tableName;

    /**
     * 全局字典编码。
     */
    @Column(value = "dict_code")
    private String dictCode;

    /**
     * 字典表键字段名称。
     */
    @Column(value = "key_column_name")
    private String keyColumnName;

    /**
     * 字典表父键字段名称。
     */
    @Column(value = "parent_key_column_name")
    private String parentKeyColumnName;

    /**
     * 字典值字段名称。
     */
    @Column(value = "value_column_name")
    private String valueColumnName;

    /**
     * 逻辑删除字段。
     */
    @Column(value = "deleted_column_name")
    private String deletedColumnName;

    /**
     * 用户过滤滤字段名称。
     */
    @Column(value = "user_filter_column_name")
    private String userFilterColumnName;

    /**
     * 部门过滤字段名称。
     */
    @Column(value = "dept_filter_column_name")
    private String deptFilterColumnName;

    /**
     * 租户过滤字段名称。
     */
    @Column(value = "tenant_filter_column_name")
    private String tenantFilterColumnName;

    /**
     * 是否树形标记。
     */
    @Column(value = "tree_flag")
    private Boolean treeFlag;

    /**
     * 获取字典数据的url。
     */
    @Column(value = "dict_list_url")
    private String dictListUrl;

    /**
     * 根据主键id批量获取字典数据的url。
     */
    @Column(value = "dict_ids_url")
    private String dictIdsUrl;

    /**
     * 字典的JSON数据。
     */
    @Column(value = "dict_data_json")
    private String dictDataJson;

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

    @RelationConstDict(
            masterIdField = "dictType",
            constantDictClass = DictType.class)
    @Column(ignore = true)
    private Map<String, Object> dictTypeDictMap;

    @RelationDict(
            masterIdField = "dblinkId",
            slaveModelClass = OnlineDblink.class,
            slaveIdField = "dblinkId",
            slaveNameField = "dblinkName")
    @Column(ignore = true)
    private Map<String, Object> dblinkIdDictMap;
}
