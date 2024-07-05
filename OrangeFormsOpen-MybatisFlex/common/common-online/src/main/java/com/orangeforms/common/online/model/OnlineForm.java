package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.orangeforms.common.core.annotation.*;
import com.orangeforms.common.online.model.constant.FormType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_form")
public class OnlineForm {

    /**
     * 主键Id。
     */
    @Id(value = "form_id")
    private Long formId;

    /**
     * 租户Id。
     */
    @Column(value = "tenant_id")
    private Long tenantId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 页面id。
     */
    @Column(value = "page_id")
    private Long pageId;

    /**
     * 表单编码。
     */
    @Column(value = "form_code")
    private String formCode;

    /**
     * 表单名称。
     */
    @Column(value = "form_name")
    private String formName;

    /**
     * 表单类别。
     */
    @Column(value = "form_kind")
    private Integer formKind;

    /**
     * 表单类型。
     */
    @Column(value = "form_type")
    private Integer formType;

    /**
     * 表单主表id。
     */
    @Column(value = "master_table_id")
    private Long masterTableId;

    /**
     * 表单组件JSON。
     */
    @Column(value = "widget_json")
    private String widgetJson;

    /**
     * 表单参数JSON。
     */
    @Column(value = "params_json")
    private String paramsJson;

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
            masterIdField = "masterTableId",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId")
    @Column(ignore = true)
    private OnlineTable onlineTable;

    @RelationDict(
            masterIdField = "masterTableId",
            equalOneToOneRelationField = "onlineTable",
            slaveModelClass = OnlineTable.class,
            slaveIdField = "tableId",
            slaveNameField = "modelName")
    @Column(ignore = true)
    private Map<String, Object> masterTableIdDictMap;

    @RelationConstDict(
            masterIdField = "formType",
            constantDictClass = FormType.class)
    @Column(ignore = true)
    private Map<String, Object> formTypeDictMap;
}
