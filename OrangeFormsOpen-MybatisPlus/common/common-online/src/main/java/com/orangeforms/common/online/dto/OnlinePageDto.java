package com.orangeforms.common.online.dto;

import com.orangeforms.common.core.validator.ConstDictRef;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.online.model.constant.PageStatus;
import com.orangeforms.common.online.model.constant.PageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 在线表单所在页面Dto对象。这里我们可以把页面理解为表单的容器。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "在线表单所在页面Dto对象")
@Data
public class OnlinePageDto {

    /**
     * 主键Id。
     */
    @Schema(description = "主键Id")
    @NotNull(message = "数据验证失败，主键Id不能为空！", groups = {UpdateGroup.class})
    private Long pageId;

    /**
     * 页面编码。
     */
    @Schema(description = "页面编码")
    private String pageCode;

    /**
     * 页面名称。
     */
    @Schema(description = "页面名称")
    @NotBlank(message = "数据验证失败，页面名称不能为空！")
    private String pageName;

    /**
     * 页面类型。
     */
    @Schema(description = "页面类型")
    @NotNull(message = "数据验证失败，页面类型不能为空！")
    @ConstDictRef(constDictClass = PageType.class, message = "数据验证失败，页面类型为无效值！")
    private Integer pageType;

    /**
     * 扩展数据。
     */
    @Schema(description = "扩展数据")
    private String extraJson;

    /**
     * 页面编辑状态。
     */
    @Schema(description = "页面编辑状态")
    @NotNull(message = "数据验证失败，状态不能为空！")
    @ConstDictRef(constDictClass = PageStatus.class, message = "数据验证失败，状态为无效值！")
    private Integer status;
}
