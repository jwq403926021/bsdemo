package com.orangeforms.common.online.dto;

import com.orangeforms.common.core.validator.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 在线表单数据表字段规则和字段多对多关联Dto对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "在线表单数据表字段规则和字段多对多关联Dto对象")
@Data
public class OnlineColumnRuleDto {

    /**
     * 字段Id。
     */
    @Schema(description = "字段Id")
    @NotNull(message = "数据验证失败，字段Id不能为空！", groups = {UpdateGroup.class})
    private Long columnId;

    /**
     * 规则Id。
     */
    @Schema(description = "规则Id")
    @NotNull(message = "数据验证失败，规则Id不能为空！", groups = {UpdateGroup.class})
    private Long ruleId;

    /**
     * 规则属性数据。
     */
    @Schema(description = "规则属性数据")
    private String propDataJson;
}
