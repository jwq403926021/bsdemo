package com.orangeforms.common.flow.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.flow.dao.FlowMultiInstanceTransMapper;
import com.orangeforms.common.flow.model.FlowMultiInstanceTrans;
import com.orangeforms.common.flow.service.FlowMultiInstanceTransService;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 会签任务操作流水数据操作服务接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowMultiInstanceTransService")
public class FlowMultiInstanceTransServiceImpl
        extends BaseService<FlowMultiInstanceTrans, Long> implements FlowMultiInstanceTransService {

    @Autowired
    private FlowMultiInstanceTransMapper flowMultiInstanceTransMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<FlowMultiInstanceTrans> mapper() {
        return flowMultiInstanceTransMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param flowMultiInstanceTrans 新增对象。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowMultiInstanceTrans saveNew(FlowMultiInstanceTrans flowMultiInstanceTrans) {
        flowMultiInstanceTrans.setId(idGenerator.nextLongId());
        TokenData tokenData = TokenData.takeFromRequest();
        flowMultiInstanceTrans.setCreateUserId(tokenData.getUserId());
        flowMultiInstanceTrans.setCreateLoginName(tokenData.getLoginName());
        flowMultiInstanceTrans.setCreateUsername(tokenData.getShowName());
        flowMultiInstanceTrans.setCreateTime(new Date());
        flowMultiInstanceTransMapper.insert(flowMultiInstanceTrans);
        return flowMultiInstanceTrans;
    }

    @Override
    public FlowMultiInstanceTrans getByExecutionId(String executionId, String taskId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowMultiInstanceTrans::getExecutionId, executionId);
        queryWrapper.eq(FlowMultiInstanceTrans::getTaskId, taskId);
        return flowMultiInstanceTransMapper.selectOneByQuery(queryWrapper);
    }

    @Override
    public FlowMultiInstanceTrans getWithAssigneeListByMultiInstanceExecId(String multiInstanceExecId) {
        if (multiInstanceExecId == null) {
            return null;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowMultiInstanceTrans::getMultiInstanceExecId, multiInstanceExecId);
        queryWrapper.isNotNull(FlowMultiInstanceTrans::getAssigneeList);
        return flowMultiInstanceTransMapper.selectOneByQuery(queryWrapper);
    }
}
