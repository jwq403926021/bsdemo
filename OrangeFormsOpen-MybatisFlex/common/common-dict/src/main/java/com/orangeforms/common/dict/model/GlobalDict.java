package com.orangeforms.common.dict.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 全局系统字典实体类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_global_dict")
public class GlobalDict {

    /**
     * 主键Id。
     */
    @Id(value = "dict_id")
    private Long dictId;

    /**
     * 字典编码。
     */
    @Column(value = "dict_code")
    private String dictCode;

    /**
     * 字典中文名称。
     */
    @Column(value = "dict_name")
    private String dictName;

    /**
     * 更新用户名。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 创建用户Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 逻辑删除字段。
     */
    @Column(value = "deleted_flag", isLogicDelete = true)
    private Integer deletedFlag;
}
