package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 数据权限与用户关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_data_perm_user")
public class SysDataPermUser {

    /**
     * 数据权限Id。
     */
    @Column(value = "data_perm_id")
    private Long dataPermId;

    /**
     * 用户Id。
     */
    @Column(value = "user_id")
    private Long userId;
}
