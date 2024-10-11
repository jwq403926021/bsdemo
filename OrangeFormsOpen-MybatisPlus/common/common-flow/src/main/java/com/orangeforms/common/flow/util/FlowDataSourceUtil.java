package com.orangeforms.common.flow.util;

import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.dbutil.provider.DataSourceProvider;
import com.orangeforms.common.dbutil.util.DataSourceUtil;
import com.orangeforms.common.flow.model.FlowDblink;
import com.orangeforms.common.flow.service.FlowDblinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工作流模块动态加载的数据源工具类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Component
public class FlowDataSourceUtil extends DataSourceUtil {

    @Autowired
    private FlowDblinkService dblinkService;

    @Override
    protected int getDblinkTypeByDblinkId(Long dblinkId) {
        DataSourceProvider provider = this.dblinkProviderMap.get(dblinkId);
        if (provider != null) {
            return provider.getDblinkType();
        }
        FlowDblink dblink = dblinkService.getById(dblinkId);
        if (dblink == null) {
            throw new MyRuntimeException("Flow DblinkId [" + dblinkId + "] doesn't exist!");
        }
        this.dblinkProviderMap.put(dblinkId, this.getProvider(dblink.getDblinkType()));
        return dblink.getDblinkType();
    }

    @Override
    protected String getDblinkConfigurationByDblinkId(Long dblinkId) {
        FlowDblink dblink = dblinkService.getById(dblinkId);
        if (dblink == null) {
            throw new MyRuntimeException("Flow DblinkId [" + dblinkId + "] doesn't exist!");
        }
        return dblink.getConfiguration();
    }
}
