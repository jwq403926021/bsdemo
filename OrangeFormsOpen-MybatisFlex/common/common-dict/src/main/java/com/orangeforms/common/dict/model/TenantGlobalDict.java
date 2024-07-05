package com.orangeforms.common.dict.model;

import com.mybatisflex.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户全局系统字典实体类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "zz_tenant_global_dict")
public class TenantGlobalDict extends GlobalDict {

    /**
     * 是否为所有租户的通用字典。
     */
    @Column(value = "tenant_common")
    private Boolean tenantCommon;

    /**
     * 租户的非公用字典的初始化字典数据。
     */
    @Column(value = "initial_data")
    private String initialData;
}
