package com.orangeforms.common.flow.dao;

import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.flow.model.FlowMessageIdentityOperation;
import org.apache.ibatis.annotations.Param;

/**
 * 流程任务消息所属用户的操作数据操作访问接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public interface FlowMessageIdentityOperationMapper extends BaseDaoMapper<FlowMessageIdentityOperation> {

    /**
     * 删除指定流程实例的消息关联数据。
     *
     * @param processInstanceId 流程实例Id。
     */
    void deleteByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
}
