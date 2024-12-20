package com.orangeforms.common.online.object;

import com.orangeforms.common.online.model.OnlineColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表字段数据对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnData {

    /**
     * 在线表字段对象。
     */
    private OnlineColumn column;

    /**
     * 字段值。
     */
    private Object columnValue;
}
