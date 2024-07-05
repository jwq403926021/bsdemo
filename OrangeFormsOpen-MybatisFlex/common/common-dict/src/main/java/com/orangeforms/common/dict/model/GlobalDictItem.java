package com.orangeforms.common.dict.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 全局系统字典项目实体类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_global_dict_item")
public class GlobalDictItem {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 字典编码。
     */
    @Column(value = "dict_code")
    private String dictCode;

    /**
     * 字典数据项Id。
     */
    @Column(value = "item_id")
    private String itemId;

    /**
     * 字典数据项名称。
     */
    @Column(value = "item_name")
    private String itemName;

    /**
     * 显示顺序(数值越小越靠前)。
     */
    @Column(value = "show_order")
    private Integer showOrder;

    /**
     * 字典状态。具体值引用DictItemStatus常量类。
     */
    private Integer status;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 创建用户Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 更新用户名。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 逻辑删除字段。
     */
    @Column(value = "deleted_flag", isLogicDelete = true)
    private Integer deletedFlag;
}
