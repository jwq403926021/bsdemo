package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 在线表单页面和数据源多对多关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_page_datasource")
public class OnlinePageDatasource {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 页面主键Id。
     */
    @Column(value = "page_id")
    private Long pageId;

    /**
     * 数据源主键Id。
     */
    @Column(value = "datasource_id")
    private Long datasourceId;
}
