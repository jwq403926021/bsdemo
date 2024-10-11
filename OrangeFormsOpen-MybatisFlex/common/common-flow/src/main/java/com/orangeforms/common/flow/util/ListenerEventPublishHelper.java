package com.orangeforms.common.flow.util;

import com.alibaba.fastjson.JSON;
import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.model.FlowTransProducer;
import com.orangeforms.common.flow.service.FlowTransProducerService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 流程监听器发送spring事件的帮助类。
 * 注意：流程的监听器不是bean对象，不能发送和捕捉事件，因此需要借助该类完成。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Component
public class ListenerEventPublishHelper {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private FlowTransProducerService flowTransProducerService;

    public <T> void publishEvent(T data) {
        eventPublisher.publishEvent(data);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void doHandle(FlowTransProducer producerData) {
        Map<String, Object> transVariableMap = new HashMap<>(1);
        transVariableMap.put(FlowConstant.AUTO_FLOW_TRANS_PRODUCER_VAR, producerData);
        Executors.newSingleThreadExecutor().submit(() -> this.triggerReceiveTask(producerData, transVariableMap));
    }

    private void triggerReceiveTask(FlowTransProducer producerData, Map<String, Object> transVariableMap) {
        try {
            runtimeService.trigger(producerData.getExecutionId(), null, transVariableMap);
        } catch (Exception e) {
            log.error("Failed to commit automatic business data [** " + JSON.toJSONString(producerData) + " **]", e);
            producerData.setErrorReason(e.getMessage());
            flowTransProducerService.updateById(producerData);
            throw new MyRuntimeException(e.getMessage());
        }
    }
}
