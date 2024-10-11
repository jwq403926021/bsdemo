package com.orangeforms.common.flow.service;

import com.orangeforms.common.core.base.service.IBaseService;
import com.orangeforms.common.flow.model.FlowTransProducer;

/**
 * 流程引擎审批操作的生产者流水服务接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public interface FlowTransProducerService extends IBaseService<FlowTransProducer, Long> {

    /**
     * 保存新数据。
     *
     * @param data 数据对象。
     * @return 更新后的对象。
     */
    FlowTransProducer saveNew(FlowTransProducer data);
}
