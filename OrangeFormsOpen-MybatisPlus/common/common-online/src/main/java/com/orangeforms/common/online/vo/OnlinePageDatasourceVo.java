package com.orangeforms.common.online.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 在线表单页面和数据源多对多关联VO对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "在线表单页面和数据源多对多关联VO对象")
@Data
public class OnlinePageDatasourceVo {

    /**
     * 主键Id。
     */
    @Schema(description = "主键Id")
    private Long id;

    /**
     * 页面主键Id。
     */
    @Schema(description = "页面主键Id")
    private Long pageId;

    /**
     * 数据源主键Id。
     */
    @Schema(description = "数据源主键Id")
    private Long datasourceId;
}
