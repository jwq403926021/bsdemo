package com.orangeforms.common.flow.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.orangeforms.common.core.annotation.MyDataSource;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.flow.base.service.BaseFlowOnlineService;
import com.orangeforms.common.flow.model.FlowWorkOrder;
import com.orangeforms.common.flow.util.FlowCustomExtFactory;
import com.orangeforms.common.online.exception.OnlineRuntimeException;
import com.orangeforms.common.online.model.OnlineColumn;
import com.orangeforms.common.online.model.OnlineTable;
import com.orangeforms.common.online.model.OnlineDatasource;
import com.orangeforms.common.online.model.OnlineDatasourceRelation;
import com.orangeforms.common.online.model.constant.FieldKind;
import com.orangeforms.common.online.service.OnlineDatasourceRelationService;
import com.orangeforms.common.online.service.OnlineDatasourceService;
import com.orangeforms.common.online.service.OnlineOperationService;
import com.orangeforms.common.online.service.OnlineTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 在线表单和流程监听器进行数据对接时的服务实现类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@MyDataSource(ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowOnlineBusinessService")
public class FlowOnlineBusinessServiceImpl implements BaseFlowOnlineService {

    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineDatasourceRelationService onlineDatasourceRelationService;
    @Autowired
    private OnlineOperationService onlineOperationService;

    @PostConstruct
    public void doRegister() {
        flowCustomExtFactory.getOnlineBusinessDataExtHelper().setOnlineBusinessService(this);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlowStatus(FlowWorkOrder workOrder) {
        OnlineTable onlineTable = onlineTableService.getOnlineTableFromCache(workOrder.getOnlineTableId());
        if (onlineTable == null) {
            log.error("OnlineTableId [{}] doesn't exist while calling FlowOnlineBusinessServiceImpl.updateFlowStatus",
                    workOrder.getOnlineTableId());
            return;
        }
        String dataId = workOrder.getBusinessKey();
        for (OnlineColumn column : onlineTable.getColumnMap().values()) {
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.FLOW_FINISHED_STATUS)) {
                onlineOperationService.updateColumn(onlineTable, dataId, column, workOrder.getFlowStatus());
            }
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.FLOW_APPROVAL_STATUS)) {
                onlineOperationService.updateColumn(onlineTable, dataId, column, workOrder.getLatestApprovalStatus());
            }
        }
    }

    @Override
    public void deleteBusinessData(FlowWorkOrder workOrder) {
        OnlineTable onlineTable = onlineTableService.getOnlineTableFromCache(workOrder.getOnlineTableId());
        if (onlineTable == null) {
            log.error("OnlineTableId [{}] doesn't exist while calling FlowOnlineBusinessServiceImpl.deleteBusinessData",
                    workOrder.getOnlineTableId());
            return;
        }
        OnlineDatasource datasource =
                onlineDatasourceService.getOnlineDatasourceByMasterTableId(onlineTable.getTableId());
        List<OnlineDatasourceRelation> relationList =
                onlineDatasourceRelationService.getOnlineDatasourceRelationListFromCache(CollUtil.newHashSet(datasource.getDatasourceId()));
        String dataId = workOrder.getBusinessKey();
        for (OnlineDatasourceRelation relation : relationList) {
            OnlineTable slaveTable = onlineTableService.getOnlineTableFromCache(relation.getSlaveTableId());
            if (slaveTable == null) {
                throw new OnlineRuntimeException("数据验证失败，数据源关联 [" + relation.getRelationName() + "] 的从表Id不存在！");
            }
            relation.setSlaveTable(slaveTable);
        }
        onlineOperationService.delete(onlineTable, relationList, dataId);
    }
}
