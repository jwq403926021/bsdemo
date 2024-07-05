package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationConstDict;
import com.orangeforms.common.core.annotation.RelationOneToOne;
import com.orangeforms.common.online.model.constant.FieldKind;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单数据表字段实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_column")
public class OnlineColumn {

    /**
     * 主键Id。
     */
    @Id(value = "column_id")
    private Long columnId;

    /**
     * 字段名。
     */
    @Column(value = "column_name")
    private String columnName;

    /**
     * 数据表Id。
     */
    @Column(value = "table_id")
    private Long tableId;

    /**
     * 数据表中的字段类型。
     */
    @Column(value = "column_type")
    private String columnType;

    /**
     * 数据表中的完整字段类型(包括了精度和刻度)。
     */
    @Column(value = "full_column_type")
    private String fullColumnType;

    /**
     * 是否为主键。
     */
    @Column(value = "primary_key")
    private Boolean primaryKey;

    /**
     * 是否是自增主键(0: 不是 1: 是)。
     */
    @Column(value = "auto_incr")
    private Boolean autoIncrement;

    /**
     * 是否可以为空 (0: 不可以为空 1: 可以为空)。
     */
    @Column(value = "nullable")
    private Boolean nullable;

    /**
     * 缺省值。
     */
    @Column(value = "column_default")
    private String columnDefault;

    /**
     * 字段在数据表中的显示位置。
     */
    @Column(value = "column_show_order")
    private Integer columnShowOrder;

    /**
     * 数据表中的字段注释。
     */
    @Column(value = "column_comment")
    private String columnComment;

    /**
     * 对象映射字段名称。
     */
    @Column(value = "object_field_name")
    private String objectFieldName;

    /**
     * 对象映射字段类型。
     */
    @Column(value = "object_field_type")
    private String objectFieldType;

    /**
     * 数值型字段的精度(目前仅Oracle使用)。
     */
    @Column(value = "numeric_precision")
    private Integer numericPrecision;

    /**
     * 数值型字段的刻度(小数点后位数，目前仅Oracle使用)。
     */
    @Column(value = "numeric_scale")
    private Integer numericScale;

    /**
     * 过滤字段类型。
     */
    @Column(value = "filter_type")
    private Integer filterType;

    /**
     * 是否是主键的父Id。
     */
    @Column(value = "parent_key")
    private Boolean parentKey;

    /**
     * 是否部门过滤字段。
     */
    @Column(value = "dept_filter")
    private Boolean deptFilter;

    /**
     * 是否用户过滤字段。
     */
    @Column(value = "user_filter")
    private Boolean userFilter;

    /**
     * 字段类别。
     */
    @Column(value = "field_kind")
    private Integer fieldKind;

    /**
     * 包含的文件文件数量，0表示无限制。
     */
    @Column(value = "max_file_count")
    private Integer maxFileCount;

    /**
     * 上传文件系统类型。
     */
    @Column(value = "upload_file_system_type")
    private Integer uploadFileSystemType;

    /**
     * 编码规则的JSON格式数据。
     */
    @Column(value = "encoded_rule")
    private String encodedRule;

    /**
     * 脱敏字段类型，具体值可参考MaskFieldTypeEnum枚举。
     */
    @Column(value = "mask_field_type")
    private String maskFieldType;

    /**
     * 字典Id。
     */
    @Column(value = "dict_id")
    private Long dictId;

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
     * SQL查询时候使用的别名。
     */
    @Column(ignore = true)
    private String columnAliasName;

    @RelationConstDict(
            masterIdField = "fieldKind",
            constantDictClass = FieldKind.class)
    @Column(ignore = true)
    private Map<String, Object> fieldKindDictMap;

    @RelationOneToOne(
            masterIdField = "dictId",
            slaveModelClass = OnlineDict.class,
            slaveIdField = "dictId",
            loadSlaveDict = false)
    @Column(ignore = true)
    private OnlineDict dictInfo;
}
