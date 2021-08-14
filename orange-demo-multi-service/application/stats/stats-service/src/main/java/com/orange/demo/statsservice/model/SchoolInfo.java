package com.orange.demo.statsservice.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * SchoolInfo实体对象。
 *
 * @author Jerry
 * @date 2020-08-08
 */
@Data
@TableName(value = "zz_school_info")
public class SchoolInfo {

    /**
     * 学校Id。
     */
    @TableId(value = "school_id")
    private Long schoolId;

    /**
     * 学校名称。
     */
    @TableField(value = "school_name")
    private String schoolName;

    /**
     * 所在省Id。
     */
    @TableField(value = "province_id")
    private Long provinceId;

    /**
     * 所在城市Id。
     */
    @TableField(value = "city_id")
    private Long cityId;
}
