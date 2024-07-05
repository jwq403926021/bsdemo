package com.orangeforms.webadmin.upms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据权限与部门关联Dto。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "数据权限与部门关联Dto")
@Data
public class SysDataPermDeptDto {

    /**
     * 数据权限Id。
     */
    @Schema(description = "数据权限Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long dataPermId;

    /**
     * 关联部门Id。
     */
    @Schema(description = "关联部门Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptId;
}