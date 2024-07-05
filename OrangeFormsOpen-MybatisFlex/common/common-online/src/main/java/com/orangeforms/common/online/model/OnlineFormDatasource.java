package com.orangeforms.common.online.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 在线表单和数据源多对多关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_online_form_datasource")
public class OnlineFormDatasource {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 表单Id。
     */
    @Column(value = "form_id")
    private Long formId;

    /**
     * 数据源Id。
     */
    @Column(value = "datasource_id")
    private Long datasourceId;
}
