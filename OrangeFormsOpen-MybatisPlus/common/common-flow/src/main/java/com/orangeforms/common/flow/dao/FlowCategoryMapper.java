package com.orangeforms.common.flow.dao;

import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.flow.model.FlowCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * FlowCategory数据操作访问接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public interface FlowCategoryMapper extends BaseDaoMapper<FlowCategory> {

    /**
     * 获取过滤后的对象列表。
     *
     * @param flowCategoryFilter 主表过滤对象。
     * @param orderBy 排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<FlowCategory> getFlowCategoryList(
            @Param("flowCategoryFilter") FlowCategory flowCategoryFilter, @Param("orderBy") String orderBy);
}
