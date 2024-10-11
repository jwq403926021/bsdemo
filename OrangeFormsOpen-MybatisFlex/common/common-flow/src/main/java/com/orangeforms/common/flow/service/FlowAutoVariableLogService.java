package com.orangeforms.common.flow.service;

import com.orangeforms.common.core.base.service.IBaseService;
import com.orangeforms.common.flow.model.FlowAutoVariableLog;

/**
 * 自动化流程变量操作服务接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public interface FlowAutoVariableLogService extends IBaseService<FlowAutoVariableLog, Long> {

    /**
     * 保存数据对象。
     *
     * @param flowAutoVariableLog 数据对象。
     */
    void saveNew(FlowAutoVariableLog flowAutoVariableLog);

    /**
     * 获取指定自动化流程实例的最新变量对象。
     *
     * @param processInstanceId 流程实例Id。
     * @return 自动化流程实例的最新变量对象。
     */
    FlowAutoVariableLog getAutoVariableByProcessInstanceId(String processInstanceId);
}
