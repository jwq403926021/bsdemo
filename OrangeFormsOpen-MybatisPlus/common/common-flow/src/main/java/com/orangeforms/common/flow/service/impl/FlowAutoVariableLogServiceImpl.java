package com.orangeforms.common.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.core.util.MyCommonUtil;
import com.orangeforms.common.flow.dao.FlowAutoVariableLogMapper;
import com.orangeforms.common.flow.model.FlowAutoVariableLog;
import com.orangeforms.common.flow.service.FlowAutoVariableLogService;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowAutoVariableLogService")
public class FlowAutoVariableLogServiceImpl extends BaseService<FlowAutoVariableLog, Long> implements FlowAutoVariableLogService {

    @Autowired
    private FlowAutoVariableLogMapper flowAutoVariableLogMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    @Override
    protected BaseDaoMapper<FlowAutoVariableLog> mapper() {
        return flowAutoVariableLogMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNew(FlowAutoVariableLog o) {
        o.setId(idGenerator.nextLongId());
        o.setTraceId(MyCommonUtil.getTraceId());
        o.setCreateTime(new Date());
        flowAutoVariableLogMapper.insert(o);
    }

    @Override
    public FlowAutoVariableLog getAutoVariableByProcessInstanceId(String processInstanceId) {
        LambdaQueryWrapper<FlowAutoVariableLog> qw = new LambdaQueryWrapper<>();
        qw.eq(FlowAutoVariableLog::getProcessInstanceId, processInstanceId);
        return flowAutoVariableLogMapper.selectOne(qw);
    }
}
