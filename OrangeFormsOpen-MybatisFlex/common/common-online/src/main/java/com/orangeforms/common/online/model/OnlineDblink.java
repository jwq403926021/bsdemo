package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationConstDict;
import com.orangeforms.common.dbutil.constant.DblinkType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单数据表所在数据库链接实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_dblink")
public class OnlineDblink {

    /**
     * 主键Id。
     */
    @Id(value = "dblink_id")
    private Long dblinkId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 链接中文名称。
     */
    @Column(value = "dblink_name")
    private String dblinkName;

    /**
     * 链接描述。
     */
    @Column(value = "dblink_description")
    private String dblinkDescription;

    /**
     * 配置信息。
     */
    private String configuration;

    /**
     * 数据库链接类型。
     */
    @Column(value = "dblink_type")
    private Integer dblinkType;

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
     * 修改时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 更新者。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    @RelationConstDict(
            masterIdField = "dblinkType",
            constantDictClass = DblinkType.class)
    @Column(ignore = true)
    private Map<String, Object> dblinkTypeDictMap;
}
