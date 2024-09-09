package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationConstDict;
import com.orangeforms.common.online.model.constant.PageStatus;
import com.orangeforms.common.online.model.constant.PageType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单所在页面实体对象。这里我们可以把页面理解为表单的容器。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_page")
public class OnlinePage {

    /**
     * 主键Id。
     */
    @Id(value = "page_id")
    private Long pageId;

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
     * 页面编码。
     */
    @Column(value = "page_code")
    private String pageCode;

    /**
     * 页面名称。
     */
    @Column(value = "page_name")
    private String pageName;

    /**
     * 页面类型。
     */
    @Column(value = "page_type")
    private Integer pageType;

    /**
     * 扩展数据。
     */
    @Column(value = "extra_json")
    private String extraJson;

    /**
     * 页面编辑状态。
     */
    @Column(value = "status")
    private Integer status;

    /**
     * 是否发布。
     */
    @Column(value = "published")
    private Boolean published;

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
            masterIdField = "pageType",
            constantDictClass = PageType.class)
    @Column(ignore = true)
    private Map<String, Object> pageTypeDictMap;

    @RelationConstDict(
            masterIdField = "status",
            constantDictClass = PageStatus.class)
    @Column(ignore = true)
    private Map<String, Object> statusDictMap;
}
