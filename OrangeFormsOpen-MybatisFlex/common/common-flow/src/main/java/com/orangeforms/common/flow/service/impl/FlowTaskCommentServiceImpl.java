package com.orangeforms.common.flow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.orangeforms.common.flow.service.*;
import com.orangeforms.common.flow.dao.*;
import com.orangeforms.common.flow.model.*;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 流程任务批注数据操作服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowTaskCommentService")
public class FlowTaskCommentServiceImpl extends BaseService<FlowTaskComment, Long> implements FlowTaskCommentService {

    @Autowired
    private FlowTaskCommentMapper flowTaskCommentMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<FlowTaskComment> mapper() {
        return flowTaskCommentMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param flowTaskComment 新增对象。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowTaskComment saveNew(FlowTaskComment flowTaskComment) {
        flowTaskComment.setId(idGenerator.nextLongId());
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData != null) {
            flowTaskComment.setHeadImageUrl(tokenData.getHeadImageUrl());
            flowTaskComment.setCreateUserId(tokenData.getUserId());
            flowTaskComment.setCreateLoginName(tokenData.getLoginName());
            flowTaskComment.setCreateUsername(tokenData.getShowName());
        }
        flowTaskComment.setCreateTime(new Date());
        flowTaskCommentMapper.insert(flowTaskComment);
        FlowTaskComment.setToRequest(flowTaskComment);
        return flowTaskComment;
    }

    /**
     * 查询指定流程实例Id下的所有审批任务的批注。
     *
     * @param processInstanceId 流程实例Id。
     * @return 查询结果集。
     */
    @Override
    public List<FlowTaskComment> getFlowTaskCommentList(String processInstanceId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowTaskComment::getProcessInstanceId, processInstanceId);
        queryWrapper.orderBy(FlowTaskComment::getId, true);
        return flowTaskCommentMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public List<FlowTaskComment> getFlowTaskCommentListByTaskIds(Set<String> taskIdSet) {
        QueryWrapper queryWrapper = new QueryWrapper().in(FlowTaskComment::getTaskId, taskIdSet);
        queryWrapper.orderBy(FlowTaskComment::getId, false);
        return flowTaskCommentMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public FlowTaskComment getLatestFlowTaskComment(String processInstanceId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowTaskComment::getProcessInstanceId, processInstanceId);
        queryWrapper.orderBy(FlowTaskComment::getId, false);
        Page<FlowTaskComment> pageData = flowTaskCommentMapper.paginate(new Page<>(1, 1), queryWrapper);
        return CollUtil.isEmpty(pageData.getRecords()) ? null : pageData.getRecords().get(0);
    }

    @Override
    public FlowTaskComment getLatestFlowTaskComment(String processInstanceId, String taskDefinitionKey) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowTaskComment::getProcessInstanceId, processInstanceId);
        queryWrapper.eq(FlowTaskComment::getTaskKey, taskDefinitionKey);
        queryWrapper.orderBy(FlowTaskComment::getId, false);
        Page<FlowTaskComment> pageData = flowTaskCommentMapper.paginate(new Page<>(1, 1), queryWrapper);
        return CollUtil.isEmpty(pageData.getRecords()) ? null : pageData.getRecords().get(0);
    }

    @Override
    public FlowTaskComment getFirstFlowTaskComment(String processInstanceId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowTaskComment::getProcessInstanceId, processInstanceId);
        queryWrapper.orderBy(FlowTaskComment::getId, true);
        Page<FlowTaskComment> pageData = flowTaskCommentMapper.paginate(new Page<>(1, 1), queryWrapper);
        return CollUtil.isEmpty(pageData.getRecords()) ? null : pageData.getRecords().get(0);
    }

    @Override
    public List<FlowTaskComment> getFlowTaskCommentListByExecutionId(
            String processInstanceId, String taskId, String executionId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowTaskComment::getProcessInstanceId, processInstanceId);
        queryWrapper.eq(FlowTaskComment::getTaskId, taskId);
        queryWrapper.eq(FlowTaskComment::getExecutionId, executionId);
        queryWrapper.orderBy(FlowTaskComment::getCreateTime, true);
        return flowTaskCommentMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public List<FlowTaskComment> getFlowTaskCommentListByMultiInstanceExecId(String multiInstanceExecId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowTaskComment::getMultiInstanceExecId, multiInstanceExecId);
        return flowTaskCommentMapper.selectListByQuery(queryWrapper);
    }
}
