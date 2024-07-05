package com.orangeforms.common.flow.config;

import com.orangeforms.common.core.config.DynamicDataSource;
import com.orangeforms.common.core.constant.ApplicationConstant;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.EngineConfigurator;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 服务启动过程中动态切换flowable引擎内置表所在的数据源。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class CustomEngineConfigurator implements EngineConfigurator {

    @Override
    public void beforeInit(AbstractEngineConfiguration engineConfiguration) {
        DataSource dataSource = engineConfiguration.getDataSource();
        if (dataSource instanceof TransactionAwareDataSourceProxy) {
            TransactionAwareDataSourceProxy proxy = (TransactionAwareDataSourceProxy) dataSource;
            DataSource targetDataSource = proxy.getTargetDataSource();
            if (targetDataSource instanceof DynamicDataSource) {
                DynamicDataSource dynamicDataSource = (DynamicDataSource) targetDataSource;
                Map<Object, DataSource> dynamicDataSourceMap = dynamicDataSource.getResolvedDataSources();
                DataSource flowDataSource = dynamicDataSourceMap.get(ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE);
                if (flowDataSource != null) {
                    engineConfiguration.setDataSource(flowDataSource);
                }
            }
        }
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
