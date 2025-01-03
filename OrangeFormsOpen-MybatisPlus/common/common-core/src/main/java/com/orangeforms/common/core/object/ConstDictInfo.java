package com.orangeforms.common.core.object;

import lombok.Data;

import java.util.List;

/**
 * 常量字典的数据结构。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class ConstDictInfo {

    private List<ConstDictData> dictData;

    @Data
    public static class ConstDictData {
        private String type;
        private Object id;
        private String name;
    }
}
