package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.util.MyCommonUtil;
import com.orangeforms.common.core.annotation.RelationManyToMany;
import com.orangeforms.common.core.base.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * 数据权限实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "zz_sys_data_perm")
public class SysDataPerm extends BaseModel {

    /**
     * 主键Id。
     */
    @Id(value = "data_perm_id")
    private Long dataPermId;

    /**
     * 显示名称。
     */
    @Column(value = "data_perm_name")
    private String dataPermName;

    /**
     * 数据权限规则类型(0: 全部可见 1: 只看自己 2: 只看本部门 3: 本部门及子部门 4: 多部门及子部门 5: 自定义部门列表)。
     */
    @Column(value = "rule_type")
    private Integer ruleType;

    @Column(ignore = true)
    private String deptIdListString;

    @RelationManyToMany(
            relationMasterIdField = "dataPermId",
            relationModelClass = SysDataPermDept.class)
    @Column(ignore = true)
    private List<SysDataPermDept> dataPermDeptList;

    @RelationManyToMany(
            relationMasterIdField = "dataPermId",
            relationModelClass = SysDataPermMenu.class)
    @Column(ignore = true)
    private List<SysDataPermMenu> dataPermMenuList;

    @Column(ignore = true)
    private String searchString;

    public void setSearchString(String searchString) {
        this.searchString = MyCommonUtil.replaceSqlWildcard(searchString);
    }
}
