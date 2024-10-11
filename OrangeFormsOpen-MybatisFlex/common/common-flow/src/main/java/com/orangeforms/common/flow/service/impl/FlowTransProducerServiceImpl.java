package com.orangeforms.common.flow.service.impl;

import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.core.util.MyCommonUtil;
import com.orangeforms.common.flow.dao.FlowTransProducerMapper;
import com.orangeforms.common.flow.model.FlowTransProducer;
import com.orangeforms.common.flow.service.FlowTransProducerService;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowTransProducerService")
public class FlowTransProducerServiceImpl extends BaseService<FlowTransProducer, Long> implements FlowTransProducerService {

    @Autowired
    private FlowTransProducerMapper flowTransProducerMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    @Override
    protected BaseDaoMapper<FlowTransProducer> mapper() {
        return flowTransProducerMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowTransProducer saveNew(FlowTransProducer data) {
        if (data.getTransId() == null) {
            data.setTransId(idGenerator.nextLongId());
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData != null) {
            data.setAppCode(tokenData.getAppCode());
            data.setCreateLoginName(tokenData.getLoginName());
            data.setCreateUsername(tokenData.getShowName());
        }
        data.setCreateTime(new Date());
        data.setTraceId(MyCommonUtil.getTraceId());
        flowTransProducerMapper.insert(data);
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateById(FlowTransProducer data) {
        return mapper().update(data, true) != 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean  removeById(Serializable transId) {
        return mapper().deleteById(transId) != 0;
    }
}
