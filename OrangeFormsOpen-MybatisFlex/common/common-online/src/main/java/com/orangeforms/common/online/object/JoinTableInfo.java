package com.orangeforms.common.online.object;

import lombok.Data;

/**
 * 连接表信息对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class JoinTableInfo {

    /**
     * 是否左连接。
     */
    private Boolean leftJoin;

    /**
     * 连接表表名。
     */
    private String joinTableName;

    /**
     * 连接条件。
     */
    private String joinCondition;
}
