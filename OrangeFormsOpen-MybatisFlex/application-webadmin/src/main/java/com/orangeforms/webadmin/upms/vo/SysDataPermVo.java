package com.orangeforms.webadmin.upms.vo;

import com.orangeforms.common.core.base.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 数据权限VO。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "数据权限VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDataPermVo extends BaseVo {

    /**
     * 数据权限Id。
     */
    @Schema(description = "数据权限Id")
    private Long dataPermId;

    /**
     * 显示名称。
     */
    @Schema(description = "显示名称")
    private String dataPermName;

    /**
     * 数据权限规则类型(0: 全部可见 1: 只看自己 2: 只看本部门 3: 本部门及子部门 4: 多部门及子部门 5: 自定义部门列表)。
     */
    @Schema(description = "数据权限规则类型")
    private Integer ruleType;

    /**
     * 部门Id列表(逗号分隔)。
     */
    @Schema(description = "部门Id列表")
    private String deptIdListString;

    /**
     * 数据权限与部门关联对象列表。
     */
    @Schema(description = "数据权限与部门关联对象列表")
    private List<Map<String, Object>> dataPermDeptList;

    /**
     * 数据权限与菜单关联对象列表。
     */
    @Schema(description = "数据权限与菜单关联对象列表")
    private List<Map<String, Object>> dataPermMenuList;
}