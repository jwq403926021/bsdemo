package com.orangeforms.common.online.dto;

import com.orangeforms.common.core.validator.ConstDictRef;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.online.model.constant.RuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 在线表单数据表字段验证规则Dto对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "在线表单数据表字段验证规则Dto对象")
@Data
public class OnlineRuleDto {

    /**
     * 主键Id。
     */
    @Schema(description = "主键Id")
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long ruleId;

    /**
     * 规则名称。
     */
    @Schema(description = "规则名称")
    @NotBlank(message = "数据验证失败，规则名称不能为空！")
    private String ruleName;

    /**
     * 规则类型。
     */
    @Schema(description = "规则类型")
    @NotNull(message = "数据验证失败，规则类型不能为空！")
    @ConstDictRef(constDictClass = RuleType.class, message = "数据验证失败，规则类型为无效值！")
    private Integer ruleType;

    /**
     * 内置规则标记。
     */
    @Schema(description = "内置规则标记")
    @NotNull(message = "数据验证失败，内置规则标记不能为空！")
    private Boolean builtin;

    /**
     * 自定义规则的正则表达式。
     */
    @Schema(description = "自定义规则的正则表达式")
    private String pattern;
}
