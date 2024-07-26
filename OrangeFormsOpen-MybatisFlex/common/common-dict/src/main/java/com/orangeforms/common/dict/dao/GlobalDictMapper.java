package com.orangeforms.common.dict.dao;

import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.dict.model.GlobalDict;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 全局字典数据操作访问接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public interface GlobalDictMapper extends BaseDaoMapper<GlobalDict> {

    /**
     * 获取全局编码字典。
     * @param filter  过滤对象。
     * @param orderBy 排序字符串。
     * @return 全局编码字典。
     */
    @Select("<script>"
            + "SELECT * FROM zz_global_dict "
            + "WHERE deleted_flag = 1 "
            + "<if test=\"filter != null\">"
            + "   <if test=\"filter.dictCode != null and filter.dictCode != ''\"> AND dict_code = #{filter.dictCode} </if>"
            + "   <if test=\"filter.dictName != null and filter.dictName != ''\"> AND dict_name = #{filter.dictName} </if>"
            + "</if>"
            + "<if test=\"orderBy != null and orderBy != ''\"> ORDER BY ${orderBy} </if>"
            + "</script>")
    List<Map<String, Object>> getGlobalDictList(@Param("filter") GlobalDict filter, @Param("orderBy") String orderBy);
}
