package com.orangeforms.common.flow.object;

import lombok.Data;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

/**
 * 工作流运行时常用对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class FlowRumtimeObject {

    /**
     * 运行时流程实例对象。
     */
    private ProcessInstance instance;
    /**
     * 运行时流程任务对象。
     */
    private Task task;
}
