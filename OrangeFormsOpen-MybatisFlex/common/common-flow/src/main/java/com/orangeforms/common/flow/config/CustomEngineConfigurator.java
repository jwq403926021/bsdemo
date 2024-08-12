package com.orangeforms.common.flow.config;

import com.orangeforms.common.core.config.DataSourceContextHolder;
import com.orangeforms.common.core.constant.ApplicationConstant;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.EngineConfigurator;

/**
 * 服务启动过程中动态切换flowable引擎内置表所在的数据源。
 *
 * @author Jerry
 * @date 2024-04-15
 */
@Slf4j
public class CustomEngineConfigurator implements EngineConfigurator {

    @Override
    public void beforeInit(AbstractEngineConfiguration engineConfiguration) {
        DataSourceContextHolder.setDataSourceType(ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE);
    }

    @Override
    public void configure(AbstractEngineConfiguration engineConfiguration) {
        // 默认实现。
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
