package com.orangeforms.common.dict.model;

import com.mybatisflex.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户全局系统字典项目实体类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "zz_tenant_global_dict_item")
public class TenantGlobalDictItem extends GlobalDictItem {

    /**
     * 租户Id。
     */
    @Column(value = "tenant_id")
    private Long tenantId;
}
