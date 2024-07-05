package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 在线表单数据表字段规则和字段多对多关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_column_rule")
public class OnlineColumnRule {

    /**
     * 字段Id。
     */
    @Column(value = "column_id")
    private Long columnId;

    /**
     * 规则Id。
     */
    @Column(value = "rule_id")
    private Long ruleId;

    /**
     * 规则属性数据。
     */
    @Column(value = "prop_data_json")
    private String propDataJson;

    @Column(ignore = true)
    private OnlineRule onlineRule;
}
