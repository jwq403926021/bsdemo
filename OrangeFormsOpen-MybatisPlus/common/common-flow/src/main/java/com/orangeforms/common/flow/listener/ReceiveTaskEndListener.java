package com.orangeforms.common.flow.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.orangeforms.common.core.util.ApplicationContextHolder;
import com.orangeforms.common.flow.constant.FlowApprovalType;
import com.orangeforms.common.flow.constant.FlowAutoActionType;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.model.FlowTaskComment;
import com.orangeforms.common.flow.model.FlowTransProducer;
import com.orangeforms.common.flow.object.AutoTaskConfig;
import com.orangeforms.common.flow.service.FlowApiService;
import com.orangeforms.common.flow.service.FlowTaskCommentService;
import com.orangeforms.common.flow.service.FlowTransProducerService;
import com.orangeforms.common.flow.util.AutoFlowHelper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空审批人审批人检测监听器。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class ReceiveTaskEndListener implements ExecutionListener {

    private final transient FlowApiService flowApiService =
            ApplicationContextHolder.getBean(FlowApiService.class);
    private final transient FlowTransProducerService flowTransProducerService =
            ApplicationContextHolder.getBean(FlowTransProducerService.class);
    private final transient FlowTaskCommentService flowTaskCommentService =
            ApplicationContextHolder.getBean(FlowTaskCommentService.class);
    private final transient AutoFlowHelper autoFlowHelper =
            ApplicationContextHolder.getBean(AutoFlowHelper.class);

    @Override
    public void notify(DelegateExecution d) {
        //先从内存中的临时变量中获取，以便提升正常运行情况下的运行时效率。
        FlowTransProducer transProducer =
                (FlowTransProducer) d.getTransientVariable(FlowConstant.AUTO_FLOW_TRANS_PRODUCER_VAR);
        //如果临时变量中没有存在，则从流程执行实例中查询该变量。
        if (transProducer == null) {
            transProducer = (FlowTransProducer)
                    flowApiService.getExecutionVariable(d.getId(), FlowConstant.AUTO_FLOW_TRANS_PRODUCER_VAR);
        }
        if (transProducer == null) {
            FlowTransProducer filter = new FlowTransProducer();
            filter.setProcessInstanceId(d.getProcessInstanceId());
            filter.setExecutionId(d.getId());
            filter.setTaskKey(d.getCurrentActivityId());
            List<FlowTransProducer> transProducers = flowTransProducerService.getListByFilter(filter);
            if (CollUtil.isNotEmpty(transProducers)) {
                transProducers = transProducers.stream()
                        .sorted(Comparator.comparing(FlowTransProducer::getTransId, Comparator.reverseOrder()))
                        .collect(Collectors.toList());
                transProducer = transProducers.get(0);
            }
        }
        if (transProducer == null) {
            return;
        }
        if (StrUtil.isNotBlank(transProducer.getAutoTaskConfig())) {
            AutoTaskConfig taskConfig = JSON.parseObject(transProducer.getAutoTaskConfig(), AutoTaskConfig.class);
            autoFlowHelper.executeTask(transProducer.getTransId(), taskConfig, d);
            FlowTaskComment comment = new FlowTaskComment();
            comment.setTaskKey(d.getCurrentActivityId());
            comment.setTaskName(taskConfig.getTaskName());
            comment.setProcessInstanceId(d.getProcessInstanceId());
            comment.setExecutionId(d.getId());
            comment.setApprovalType(FlowApprovalType.AUTO_FLOW_TASK);
            String s = StrFormatter.format("执行任务 [{}] 成功，任务类型 [{}]",
                    taskConfig.getTaskName(), FlowAutoActionType.getShowNname(taskConfig.getActionType()));
            comment.setTaskComment(s);
            flowTaskCommentService.saveNew(comment);
        }
        flowTransProducerService.removeById(transProducer.getTransId());
    }
}
