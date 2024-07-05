package com.orangeforms.webadmin.upms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 岗位VO对象。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@ApiModel("岗位VO")
@Data
public class SysPostVo {

    /**
     * 岗位Id。
     */
    @ApiModelProperty(value = "岗位Id")
    private Long postId;

    /**
     * 岗位名称。
     */
    @ApiModelProperty(value = "岗位名称")
    private String postName;

    /**
     * 岗位层级，数值越小级别越高。
     */
    @ApiModelProperty(value = "岗位层级，数值越小级别越高")
    private Integer level;

    /**
     * 是否领导岗位。
     */
    @ApiModelProperty(value = "是否领导岗位")
    private Boolean leaderPost;

    /**
     * 创建者Id。
     */
    @ApiModelProperty(value = "创建者Id")
    private Long createUserId;

    /**
     * 创建时间。
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新者Id。
     */
    @ApiModelProperty(value = "更新者Id")
    private Long updateUserId;

    /**
     * 更新时间。
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * postId 的多对多关联表数据对象，数据对应类型为SysDeptPostVo。
     */
    @ApiModelProperty(value = "postId 的多对多关联表数据对象")
    private Map<String, Object> sysDeptPost;
}
