package com.orangeforms.common.online.dto;

import com.orangeforms.common.core.validator.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 在线表单数据表字段规则和字段多对多关联Dto对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("在线表单数据表字段规则和字段多对多关联Dto对象")
@Data
public class OnlineColumnRuleDto {

    /**
     * 字段Id。
     */
    @ApiModelProperty(value = "字段Id")
    @NotNull(message = "数据验证失败，字段Id不能为空！", groups = {UpdateGroup.class})
    private Long columnId;

    /**
     * 规则Id。
     */
    @ApiModelProperty(value = "规则Id")
    @NotNull(message = "数据验证失败，规则Id不能为空！", groups = {UpdateGroup.class})
    private Long ruleId;

    /**
     * 规则属性数据。
     */
    @ApiModelProperty(value = "规则属性数据")
    private String propDataJson;
}
