package com.orangeforms.common.core.base.model;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.util.Date;

/**
 * 实体对象的公共基类，所有子类均必须包含基类定义的数据表字段和实体对象字段。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class BaseModel {

    /**
     * 创建者Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 更新者Id。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;
}
