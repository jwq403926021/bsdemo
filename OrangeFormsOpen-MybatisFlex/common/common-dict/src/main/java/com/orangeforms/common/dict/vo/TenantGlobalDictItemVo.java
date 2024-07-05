package com.orangeforms.common.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户全局系统字典项目Vo。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "租户全局系统字典项目Vo")
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantGlobalDictItemVo extends GlobalDictItemVo {

}
