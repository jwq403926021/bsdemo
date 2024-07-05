package com.orangeforms.common.flow.listener;

import cn.hutool.core.util.StrUtil;
import com.orangeforms.common.core.object.GlobalThreadLocal;
import com.orangeforms.common.core.util.ApplicationContextHolder;
import com.orangeforms.common.flow.model.FlowWorkOrder;
import com.orangeforms.common.flow.service.FlowWorkOrderService;
import com.orangeforms.common.flow.constant.FlowTaskStatus;
import com.orangeforms.common.flow.util.FlowCustomExtFactory;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

/**
 * 流程实例监听器，在流程实例结束的时候，需要完成一些自定义的业务行为。如：
 * 1. 更新流程工单表的审批状态字段。
 * 2. 业务数据同步。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class FlowFinishedListener implements ExecutionListener {

    private final transient FlowWorkOrderService flowWorkOrderService =
            ApplicationContextHolder.getBean(FlowWorkOrderService.class);
    private final transient FlowCustomExtFactory flowCustomExtFactory =
            ApplicationContextHolder.getBean(FlowCustomExtFactory.class);

    @Override
    public void notify(DelegateExecution execution) {
        if (!StrUtil.equals("end", execution.getEventName())) {
            return;
        }
        boolean enabled = GlobalThreadLocal.setDataFilter(false);
        try {
            String processInstanceId = execution.getProcessInstanceId();
            FlowWorkOrder workOrder = flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(processInstanceId);
            if (workOrder == null) {
                return;
            }
            int flowStatus = FlowTaskStatus.FINISHED;
            if (workOrder.getFlowStatus().equals(FlowTaskStatus.CANCELLED)
                    || workOrder.getFlowStatus().equals(FlowTaskStatus.STOPPED)) {
                flowStatus = workOrder.getFlowStatus();
            }
            workOrder.setFlowStatus(flowStatus);
            // 更新流程工单中的流程状态。
            flowWorkOrderService.updateFlowStatusByProcessInstanceId(processInstanceId, flowStatus);
            // 处理在线表单工作流的自定义状态更新。
            flowCustomExtFactory.getOnlineBusinessDataExtHelper().updateFlowStatus(workOrder);
        } finally {
            GlobalThreadLocal.setDataFilter(enabled);
        }
    }
}
