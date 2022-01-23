package com.orangeforms.common.flow.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.orangeforms.common.flow.base.service.BaseFlowService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流业务数据扩展帮助实现类。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@Slf4j
public class BaseBusinessDataExtHelper {

    private Map<String, BaseFlowService> serviceMap = new HashMap<>();

    /**
     * 子类要基于自身所处理的流程定义标识，把子类的this对象，注册到父类的map中。
     *
     * @param processDefinitionKey 流程定义标识。
     * @param service              流程服务实现基类。
     */
    public synchronized void doRegister(String processDefinitionKey, BaseFlowService service) {
        Assert.isTrue(StrUtil.isNotBlank(processDefinitionKey));
        Assert.notNull(service);
        serviceMap.put(processDefinitionKey, service);
    }

    /**
     *
     * 流程结束监听器(FlowFinishedListener) 会在流程结束时调用该方法。
     *
     * @param processDefinitionKey 流程定义标识。
     * @param processInstanceId    流程实例Id。
     * @param businessKey          业务主表的主键Id。
     */
    public void triggerSync(String processDefinitionKey, String processInstanceId, String businessKey) {
        BaseFlowService service = serviceMap.get(processDefinitionKey);
        if (service != null && service.supportSyncBusinessData()) {
            try {
                service.syncBusinessData(processInstanceId, businessKey);
            } catch (Exception e) {
                String errorMessage = String.format(
                        "Failed to call syncBusinessData with processDefinitionKey {%s}, businessKey {%s}",
                        processDefinitionKey, businessKey);
                log.error(errorMessage, e);
                throw e;
            }
        }
    }

    /**
     * 获取详细的业务数据，包括主表、一对一、一对多、多对多从表及其字典数据。
     *
     * @param processDefinitionKey 流程定义标识。
     * @param processInstanceId    流程实例Id。
     * @param businessKey          业务主表的主键Id。
     * @return JSON格式化后的业务数据。
     */
    public String getBusinessData(String processDefinitionKey, String processInstanceId, String businessKey) {
        BaseFlowService service = serviceMap.get(processDefinitionKey);
        return service == null ? null : service.getBusinessData(processInstanceId, businessKey);
    }
}
