package com.orangeforms.common.flow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mybatisflex.core.query.QueryWrapper;
import com.github.pagehelper.page.PageMethod;
import com.orangeforms.common.core.annotation.DisableDataFilter;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.constant.GlobalDeletedFlag;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.core.util.MyPageUtil;
import com.orangeforms.common.flow.constant.FlowTaskStatus;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.dao.FlowWorkOrderExtMapper;
import com.orangeforms.common.flow.dao.FlowWorkOrderMapper;
import com.orangeforms.common.flow.dto.FlowWorkOrderDto;
import com.orangeforms.common.flow.model.FlowEntry;
import com.orangeforms.common.flow.model.FlowWorkOrder;
import com.orangeforms.common.flow.model.FlowWorkOrderExt;
import com.orangeforms.common.flow.util.FlowOperationHelper;
import com.orangeforms.common.flow.vo.FlowWorkOrderVo;
import com.orangeforms.common.flow.service.FlowApiService;
import com.orangeforms.common.flow.service.FlowEntryService;
import com.orangeforms.common.flow.service.FlowWorkOrderService;
import com.orangeforms.common.flow.util.BaseFlowIdentityExtHelper;
import com.orangeforms.common.flow.util.FlowCustomExtFactory;
import com.orangeforms.common.redis.util.CommonRedisUtil;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowWorkOrderService")
public class FlowWorkOrderServiceImpl extends BaseService<FlowWorkOrder, Long> implements FlowWorkOrderService {

    @Autowired
    private FlowWorkOrderMapper flowWorkOrderMapper;
    @Autowired
    private FlowWorkOrderExtMapper flowWorkOrderExtMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowEntryService flowEntryService;
    @Autowired
    private CommonRedisUtil commonRedisUtil;
    @Autowired
    private FlowOperationHelper flowOperationHelper;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<FlowWorkOrder> mapper() {
        return flowWorkOrderMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param instance      流程实例对象。
     * @param dataId        流程实例的BusinessKey。
     * @param onlineTableId 在线数据表的主键Id。
     * @param tableName     面向静态表单所使用的表名。
     * @return 新增的工作流工单对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowWorkOrder saveNew(ProcessInstance instance, Object dataId, Long onlineTableId, String tableName) {
        // 正常插入流程工单数据。
        FlowWorkOrder flowWorkOrder = this.createWith(instance);
        flowWorkOrder.setWorkOrderCode(this.generateWorkOrderCode(instance.getProcessDefinitionKey()));
        flowWorkOrder.setBusinessKey(dataId.toString());
        flowWorkOrder.setOnlineTableId(onlineTableId);
        flowWorkOrder.setTableName(tableName);
        flowWorkOrder.setFlowStatus(FlowTaskStatus.SUBMITTED);
        flowWorkOrderMapper.insert(flowWorkOrder);
        return flowWorkOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowWorkOrder saveNewWithDraft(
            ProcessInstance instance, Long onlineTableId, String tableName, String masterData, String slaveData) {
        FlowWorkOrder flowWorkOrder = this.createWith(instance);
        flowWorkOrder.setWorkOrderCode(this.generateWorkOrderCode(instance.getProcessDefinitionKey()));
        flowWorkOrder.setOnlineTableId(onlineTableId);
        flowWorkOrder.setTableName(tableName);
        flowWorkOrder.setFlowStatus(FlowTaskStatus.DRAFT);
        JSONObject draftData = new JSONObject();
        if (masterData != null) {
            draftData.put(FlowConstant.MASTER_DATA_KEY, masterData);
        }
        if (slaveData != null) {
            draftData.put(FlowConstant.SLAVE_DATA_KEY, slaveData);
        }
        FlowWorkOrderExt flowWorkOrderExt =
                BeanUtil.copyProperties(flowWorkOrder, FlowWorkOrderExt.class);
        flowWorkOrderExt.setId(idGenerator.nextLongId());
        flowWorkOrderExt.setDraftData(JSON.toJSONString(draftData));
        flowWorkOrderExtMapper.insert(flowWorkOrderExt);
        flowWorkOrderMapper.insert(flowWorkOrder);
        return flowWorkOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDraft(Long workOrderId, String masterData, String slaveData) {
        JSONObject draftData = new JSONObject();
        if (masterData != null) {
            draftData.put(FlowConstant.MASTER_DATA_KEY, masterData);
        }
        if (slaveData != null) {
            draftData.put(FlowConstant.SLAVE_DATA_KEY, slaveData);
        }
        FlowWorkOrderExt flowWorkOrderExt = new FlowWorkOrderExt();
        flowWorkOrderExt.setDraftData(JSON.toJSONString(draftData));
        flowWorkOrderExt.setUpdateTime(new Date());
        flowWorkOrderExtMapper.updateByQuery(flowWorkOrderExt,
                new QueryWrapper().eq(FlowWorkOrderExt::getWorkOrderId, workOrderId));
    }

    /**
     * 删除指定数据。
     *
     * @param workOrderId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long workOrderId) {
        return flowWorkOrderMapper.deleteById(workOrderId) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByProcessInstanceId(String processInstanceId) {
        FlowWorkOrder filter = new FlowWorkOrder();
        filter.setProcessInstanceId(processInstanceId);
        super.removeBy(filter);
    }

    @Override
    public List<FlowWorkOrder> getFlowWorkOrderList(FlowWorkOrder filter, String orderBy) {
        if (filter == null) {
            filter = new FlowWorkOrder();
        }
        TokenData tokenData = TokenData.takeFromRequest();
        filter.setTenantId(tokenData.getTenantId());
        filter.setAppCode(tokenData.getAppCode());
        return flowWorkOrderMapper.getFlowWorkOrderList(filter, orderBy);
    }

    @Override
    public List<FlowWorkOrder> getFlowWorkOrderListWithRelation(FlowWorkOrder filter, String orderBy) {
        List<FlowWorkOrder> resultList = this.getFlowWorkOrderList(filter, orderBy);
        this.buildRelationForDataList(resultList, MyRelationParam.dictOnly());
        return resultList;
    }

    @Override
    public FlowWorkOrder getFlowWorkOrderByProcessInstanceId(String processInstanceId) {
        FlowWorkOrder filter = new FlowWorkOrder();
        filter.setProcessInstanceId(processInstanceId);
        return flowWorkOrderMapper.selectOneByQuery(QueryWrapper.create(filter));
    }

    @Override
    public boolean existByBusinessKey(String tableName, Object businessKey, boolean unfinished) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowWorkOrder::getBusinessKey, businessKey.toString());
        queryWrapper.eq(FlowWorkOrder::getTableName, tableName);
        if (unfinished) {
            queryWrapper.notIn(FlowWorkOrder::getFlowStatus,
                    FlowTaskStatus.FINISHED, FlowTaskStatus.CANCELLED, FlowTaskStatus.STOPPED);
        }
        return flowWorkOrderMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public boolean existUnfinished(String processDefinitionKey, Object businessKey) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowWorkOrder::getBusinessKey, businessKey.toString());
        queryWrapper.eq(FlowWorkOrder::getProcessDefinitionKey, processDefinitionKey);
        queryWrapper.notIn(FlowWorkOrder::getFlowStatus,
                FlowTaskStatus.FINISHED, FlowTaskStatus.CANCELLED, FlowTaskStatus.STOPPED);
        return flowWorkOrderMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @DisableDataFilter
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlowStatusByProcessInstanceId(String processInstanceId, Integer flowStatus) {
        if (flowStatus == null) {
            return;
        }
        FlowWorkOrder flowWorkOrder = new FlowWorkOrder();
        flowWorkOrder.setFlowStatus(flowStatus);
        if (FlowTaskStatus.FINISHED != flowStatus) {
            flowWorkOrder.setUpdateTime(new Date());
            flowWorkOrder.setUpdateUserId(TokenData.takeFromRequest().getUserId());
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(FlowWorkOrder::getProcessInstanceId, processInstanceId);
        flowWorkOrderMapper.updateByQuery(flowWorkOrder, queryWrapper);
    }

    @DisableDataFilter
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateLatestApprovalStatusByProcessInstanceId(String processInstanceId, Integer approvalStatus) {
        if (approvalStatus == null) {
            return;
        }
        FlowWorkOrder flowWorkOrder = this.getFlowWorkOrderByProcessInstanceId(processInstanceId);
        flowWorkOrder.setLatestApprovalStatus(approvalStatus);
        flowWorkOrder.setUpdateTime(new Date());
        flowWorkOrder.setUpdateUserId(TokenData.takeFromRequest().getUserId());
        flowWorkOrderMapper.update(flowWorkOrder);
        // 处理在线表单工作流的自定义状态更新。
        flowCustomExtFactory.getOnlineBusinessDataExtHelper().updateFlowStatus(flowWorkOrder);
    }

    @Override
    public boolean hasDataPermOnFlowWorkOrder(String processInstanceId) {
        // 开启数据权限，并进行验证。
        boolean originalFlag = GlobalThreadLocal.setDataFilter(true);
        long count;
        try {
            FlowWorkOrder filter = new FlowWorkOrder();
            filter.setProcessInstanceId(processInstanceId);
            count = flowWorkOrderMapper.selectCountByQuery(QueryWrapper.create(filter));
        } finally {
            // 恢复之前的数据权限标记
            GlobalThreadLocal.setDataFilter(originalFlag);
        }
        return count > 0;
    }

    @Override
    public void fillUserShowNameByLoginName(List<FlowWorkOrderVo> dataList) {
        BaseFlowIdentityExtHelper identityExtHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        Set<String> loginNameSet = dataList.stream()
                .map(FlowWorkOrderVo::getSubmitUsername).collect(Collectors.toSet());
        if (CollUtil.isEmpty(loginNameSet)) {
            return;
        }
        Map<String, String> userNameMap = identityExtHelper.mapUserShowNameByLoginName(loginNameSet);
        dataList.forEach(workOrder -> {
            if (StrUtil.isNotBlank(workOrder.getSubmitUsername())) {
                workOrder.setUserShowName(userNameMap.get(workOrder.getSubmitUsername()));
            }
        });
    }

    @Override
    public FlowWorkOrderExt getFlowWorkOrderExtByWorkOrderId(Long workOrderId) {
        return flowWorkOrderExtMapper.selectOneByQuery(
                new QueryWrapper().eq(FlowWorkOrderExt::getWorkOrderId, workOrderId));
    }

    @Override
    public List<FlowWorkOrderExt> getFlowWorkOrderExtByWorkOrderIds(Set<Long> workOrderIds) {
        return flowWorkOrderExtMapper.selectListByQuery(
                new QueryWrapper().in(FlowWorkOrderExt::getWorkOrderId, workOrderIds));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult removeDraft(FlowWorkOrder flowWorkOrder) {
        CallResult r = flowApiService.stopProcessInstance(flowWorkOrder.getProcessInstanceId(), "撤销草稿", true);
        if (!r.isSuccess()) {
            return r;
        }
        flowWorkOrderMapper.deleteById(flowWorkOrder.getWorkOrderId());
        return CallResult.ok();
    }

    @Override
    public MyPageData<FlowWorkOrderVo> getPagedWorkOrderListAndBuildData(
            FlowWorkOrderDto flowWorkOrderDtoFilter, MyPageParam pageParam, MyOrderParam orderParam, String processDefinitionKey) {
        PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        String orderBy = MyOrderParam.buildOrderBy(orderParam, FlowWorkOrder.class);
        FlowWorkOrder filter = flowOperationHelper.makeWorkOrderFilter(flowWorkOrderDtoFilter, processDefinitionKey);
        List<FlowWorkOrder> flowWorkOrderList = this.getFlowWorkOrderList(filter, orderBy);
        MyPageData<FlowWorkOrderVo> resultData =
                MyPageUtil.makeResponseData(flowWorkOrderList, FlowWorkOrderVo.class);
        if (CollUtil.isEmpty(resultData.getDataList())) {
            return resultData;
        }
        flowOperationHelper.buildWorkOrderApprovalStatus(processDefinitionKey, resultData.getDataList());
        // 根据工单的提交用户名获取用户的显示名称，便于前端显示。
        // 同时这也是一个如何通过插件方法，将loginName映射到showName的示例，
        this.fillUserShowNameByLoginName(resultData.getDataList());
        // 组装工单中需要返回给前端的流程任务数据。
        flowOperationHelper.buildWorkOrderTaskInfo(resultData.getDataList());
        return resultData;
    }

    private FlowWorkOrder createWith(ProcessInstance instance) {
        TokenData tokenData = TokenData.takeFromRequest();
        Date now = new Date();
        FlowWorkOrder flowWorkOrder = new FlowWorkOrder();
        flowWorkOrder.setWorkOrderId(idGenerator.nextLongId());
        flowWorkOrder.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        flowWorkOrder.setProcessDefinitionName(instance.getProcessDefinitionName());
        flowWorkOrder.setProcessDefinitionId(instance.getProcessDefinitionId());
        flowWorkOrder.setProcessInstanceId(instance.getId());
        flowWorkOrder.setSubmitUsername(tokenData.getLoginName());
        flowWorkOrder.setDeptId(tokenData.getDeptId());
        flowWorkOrder.setAppCode(tokenData.getAppCode());
        flowWorkOrder.setTenantId(tokenData.getTenantId());
        flowWorkOrder.setCreateUserId(tokenData.getUserId());
        flowWorkOrder.setUpdateUserId(tokenData.getUserId());
        flowWorkOrder.setCreateTime(now);
        flowWorkOrder.setUpdateTime(now);
        flowWorkOrder.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        return flowWorkOrder;
    }

    private String generateWorkOrderCode(String processDefinitionKey) {
        FlowEntry flowEntry = flowEntryService.getFlowEntryFromCache(processDefinitionKey);
        if (StrUtil.isBlank(flowEntry.getEncodedRule())) {
            return null;
        }
        ColumnEncodedRule rule = JSON.parseObject(flowEntry.getEncodedRule(), ColumnEncodedRule.class);
        if (rule.getIdWidth() == null) {
            rule.setIdWidth(10);
        }
        return commonRedisUtil.generateTransId(
                rule.getPrefix(), rule.getPrecisionTo(), rule.getMiddle(), rule.getIdWidth());
    }
}
