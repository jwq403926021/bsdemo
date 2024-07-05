package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.annotation.RelationConstDict;
import com.orangeforms.common.online.model.constant.RuleType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 在线表单数据表字段验证规则实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_rule")
public class OnlineRule {

    /**
     * 主键Id。
     */
    @Id(value = "rule_id")
    private Long ruleId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 规则名称。
     */
    @Column(value = "rule_name")
    private String ruleName;

    /**
     * 规则类型。
     */
    @Column(value = "rule_type")
    private Integer ruleType;

    /**
     * 内置规则标记。
     */
    @Column(value = "builtin")
    private Boolean builtin;

    /**
     * 自定义规则的正则表达式。
     */
    @Column(value = "pattern")
    private String pattern;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 创建者。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 更新者。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    /**
     * 逻辑删除标记字段(1: 正常 -1: 已删除)。
     */
    @Column(value = "deleted_flag", isLogicDelete = true)
    private Integer deletedFlag;

    /**
     * ruleId 的多对多关联表数据对象。
     */
    @Column(ignore = true)
    private OnlineColumnRule onlineColumnRule;

    @RelationConstDict(
            masterIdField = "ruleType",
            constantDictClass = RuleType.class)
    @Column(ignore = true)
    private Map<String, Object> ruleTypeDictMap;
}
