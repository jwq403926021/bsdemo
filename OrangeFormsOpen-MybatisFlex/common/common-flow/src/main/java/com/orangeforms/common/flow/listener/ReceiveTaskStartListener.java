package com.orangeforms.common.flow.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.orangeforms.common.core.util.ApplicationContextHolder;
import com.orangeforms.common.flow.model.FlowTransProducer;
import com.orangeforms.common.flow.object.AutoTaskConfig;
import com.orangeforms.common.flow.service.FlowTransProducerService;
import com.orangeforms.common.flow.util.AutoFlowHelper;
import com.orangeforms.common.flow.util.ListenerEventPublishHelper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

/**
 * 空审批人审批人检测监听器。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class ReceiveTaskStartListener implements ExecutionListener {

    private final transient AutoFlowHelper autoFlowHelper =
            ApplicationContextHolder.getBean(AutoFlowHelper.class);
    private final transient FlowTransProducerService flowTransProducerService =
            ApplicationContextHolder.getBean(FlowTransProducerService.class);
    private final transient ListenerEventPublishHelper eventPublishHelper =
            ApplicationContextHolder.getBean(ListenerEventPublishHelper.class);

    @Override
    public void notify(DelegateExecution d) {
        //TODO 在目标表所在数据库也要创建业务执行的流水表，基于TransProduer的TransId做唯一性校验。
        AutoTaskConfig taskConfig = autoFlowHelper.parseAutoTaskConfig(d.getProcessDefinitionId(), d.getCurrentActivityId());
        FlowTransProducer producerData = new FlowTransProducer();
        producerData.setProcessInstanceId(d.getProcessInstanceId());
        producerData.setExecutionId(d.getId());
        producerData.setTaskKey(d.getCurrentActivityId());
        producerData.setDblinkId(taskConfig.getDestDblinkId());
        producerData.setInitMethod("ReceiveTaskStartListener.notify");
        producerData.setTryTimes(1);
        producerData.setAutoTaskConfig(JSON.toJSONString(taskConfig, SerializerFeature.WriteDateUseDateFormat));
        flowTransProducerService.saveNew(producerData);
        eventPublishHelper.publishEvent(producerData);
    }
}
