package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 用户角色实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_user_role")
public class SysUserRole {

    /**
     * 用户Id。
     */
    @Column(value = "user_id")
    private Long userId;

    /**
     * 角色Id。
     */
    @Column(value = "role_id")
    private Long roleId;
}
