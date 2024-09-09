package com.orangeforms.common.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.page.PageMethod;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.cache.CacheConfig;
import com.orangeforms.common.core.constant.*;
import com.orangeforms.common.core.exception.NoDataPermException;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.*;
import com.orangeforms.common.core.annotation.MultiDatabaseWriteMethod;
import com.orangeforms.common.datafilter.config.DataFilterProperties;
import com.orangeforms.common.dbutil.constant.DblinkType;
import com.orangeforms.common.dbutil.provider.DataSourceProvider;
import com.orangeforms.common.dict.service.GlobalDictService;
import com.orangeforms.common.online.dao.OnlineOperationMapper;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import com.orangeforms.common.redis.util.CommonRedisUtil;
import com.orangeforms.common.online.config.OnlineProperties;
import com.orangeforms.common.online.util.*;
import com.orangeforms.common.online.dto.OnlineFilterDto;
import com.orangeforms.common.online.model.*;
import com.orangeforms.common.online.model.constant.*;
import com.orangeforms.common.online.model.constant.FieldFilterType;
import com.orangeforms.common.online.object.ConstDictInfo;
import com.orangeforms.common.online.object.ColumnData;
import com.orangeforms.common.online.object.JoinTableInfo;
import com.orangeforms.common.online.service.*;
import com.orangeforms.common.online.exception.OnlineRuntimeException;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineOperationService")
public class OnlineOperationServiceImpl implements OnlineOperationService {

    @Autowired
    private OnlineOperationMapper onlineOperationMapper;
    @Autowired
    private OnlineDblinkService onlineDblinkService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineDictService onlineDictService;
    @Autowired
    private OnlineVirtualColumnService onlineVirtualColumnService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineOperationHelper onlineOperationHelper;
    @Autowired
    private OnlineProperties onlineProperties;
    @Autowired
    private OnlineCustomExtFactory customExtFactory;
    @Autowired
    private GlobalDictService globalDictService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private DataFilterProperties dataFilterProperties;
    @Autowired
    private CommonRedisUtil commonRedisUtil;
    @Resource(name = "caffeineCacheManager")
    private CacheManager cacheManager;
    @Autowired
    private OnlineDataSourceUtil dataSourceUtil;
    @Autowired
    private OnlineExtendExecutorUtil onlineExtendExecutorUtil;

    private static final String DICT_MAP_SUFFIX = "DictMap";
    private static final String DICT_MAP_LIST_SUFFIX = "DictMapList";
    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";
    private static final String AND = " AND ";

    /**
     * 聚合返回数据中，聚合键的常量字段名。
     * 如select groupColumn grouped_key, max(aggregationColumn) aggregated_value。
     */
    private static final String KEY_NAME = "grouped_key";
    /**
     * 聚合返回数据中，聚合值的常量字段名。
     * 如select groupColumn grouped_key, max(aggregationColumn) aggregated_value。
     */
    private static final String VALUE_NAME = "aggregated_value";

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewBatch(OnlineTable table, List<JSONObject> dataList) {
        for (JSONObject data : dataList) {
            this.saveNew(table, data);
        }
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object saveNew(OnlineTable table, JSONObject data) {
        ResponseResult<List<ColumnData>> columnDataListResult =
                onlineOperationHelper.buildTableData(table, data, false, null);
        if (!columnDataListResult.isSuccess()) {
            throw new OnlineRuntimeException(columnDataListResult.getErrorMessage());
        }
        List<ColumnData> columnDataList = columnDataListResult.getData();
        String columnNames = this.makeColumnNames(columnDataList);
        List<Object> columnValueList = new LinkedList<>();
        Object id = null;
        // 这里逐个处理每一行数据，特别是非自增主键、createUserId、createTime、逻辑删除等特殊属性的字段。
        for (ColumnData columnData : columnDataList) {
            this.makeupColumnValue(columnData);
            if (BooleanUtil.isFalse(columnData.getColumn().getAutoIncrement())) {
                columnValueList.add(columnData.getColumnValue());
                if (BooleanUtil.isTrue(columnData.getColumn().getPrimaryKey())) {
                    id = columnData.getColumnValue();
                    // 这里必须补齐主键值到JSON对象，后面的从表关联字段值填充可能会用到该值。
                    data.put(columnData.getColumn().getColumnName(), id);
                }
            }
        }
        onlineExtendExecutorUtil.doBeforeInsert(table, data);
        onlineOperationMapper.insert(table.getTableName(), columnNames, columnValueList);
        return id;
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object saveNewWithRelation(
            OnlineTable masterTable,
            JSONObject masterData,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Map<OnlineTable, List<JSONObject>> slaveDataMap = this.convertToSlaveTableAndDataList(slaveDataListMap);
        onlineExtendExecutorUtil.doBeforeInsertWithRelation(masterTable, masterData, slaveDataMap);
        Object id = this.saveNew(masterTable, masterData);
        if (slaveDataListMap != null) {
            // 迭代多个关联列表。
            for (Map.Entry<OnlineDatasourceRelation, List<JSONObject>> entry : slaveDataListMap.entrySet()) {
                Long masterColumnId = entry.getKey().getMasterColumnId();
                OnlineColumn masterColumn = masterTable.getColumnMap().get(masterColumnId);
                Object columnValue = masterData.get(masterColumn.getColumnName());
                OnlineTable slaveTable = entry.getKey().getSlaveTable();
                OnlineColumn slaveColumn = slaveTable.getColumnMap().get(entry.getKey().getSlaveColumnId());
                // 迭代关联中的数据集合
                for (JSONObject slaveData : entry.getValue()) {
                    if (!slaveData.containsKey(slaveTable.getPrimaryKeyColumn().getColumnName())) {
                        slaveData.put(slaveColumn.getColumnName(), columnValue);
                        this.saveNew(slaveTable, slaveData);
                    }
                }
            }
        }
        onlineExtendExecutorUtil.doAfterInsertWithRelation(masterTable, masterData, slaveDataMap);
        return id;
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(OnlineTable table, JSONObject data) {
        ResponseResult<List<ColumnData>> columnDataListResult =
                onlineOperationHelper.buildTableData(table, data, true, null);
        if (!columnDataListResult.isSuccess()) {
            throw new OnlineRuntimeException(columnDataListResult.getErrorMessage());
        }
        List<ColumnData> columnDataList = columnDataListResult.getData();
        String tableName = table.getTableName();
        List<ColumnData> updateColumnList = new LinkedList<>();
        List<OnlineFilterDto> filterList = new LinkedList<>();
        String dataId = null;
        for (ColumnData columnData : columnDataList) {
            this.makeupColumnValue(columnData);
            // 对于以下几种类型的字段，忽略更新。
            if (BooleanUtil.isTrue(columnData.getColumn().getPrimaryKey())
                    || ObjectUtil.equal(columnData.getColumn().getFieldKind(), FieldKind.LOGIC_DELETE)) {
                OnlineFilterDto filter = new OnlineFilterDto();
                filter.setTableName(tableName);
                filter.setColumnName(columnData.getColumn().getColumnName());
                filter.setColumnValue(columnData.getColumnValue());
                filterList.add(filter);
                if (BooleanUtil.isTrue(columnData.getColumn().getPrimaryKey())) {
                    dataId = columnData.getColumnValue().toString();
                }
                continue;
            }
            if (!MyCommonUtil.equalsAny(columnData.getColumn().getFieldKind(),
                    FieldKind.CREATE_TIME, FieldKind.CREATE_USER_ID, FieldKind.CREATE_DEPT_ID, FieldKind.TENANT_FILTER)) {
                updateColumnList.add(columnData);
            }
        }
        if (CollUtil.isEmpty(updateColumnList)) {
            return true;
        }
        String dataPermFilter = this.buildDataPermFilter(table);
        onlineExtendExecutorUtil.doBeforeUpdate(table, data);
        boolean r = this.doUpdate(table, updateColumnList, filterList, dataPermFilter);
        onlineExtendExecutorUtil.doAfterUpdate(table, data);
        return r;
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public <T> boolean updateColumn(OnlineTable table, String dataId, OnlineColumn column, T dataValue) {
        List<ColumnData> updateColumnList = new LinkedList<>();
        ColumnData updateColumnData = new ColumnData();
        updateColumnData.setColumn(column);
        updateColumnData.setColumnValue(dataValue);
        updateColumnList.add(updateColumnData);
        List<OnlineFilterDto> filterList = this.makeDefaultFilter(table, table.getPrimaryKeyColumn(), dataId);
        String dataPermFilter = this.buildDataPermFilter(table);
        return this.doUpdate(table, updateColumnList, filterList, dataPermFilter);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateWithRelation(
            OnlineTable masterTable,
            JSONObject masterData,
            Long datasourceId,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Map<OnlineTable, List<JSONObject>> slaveDataMap = this.convertToSlaveTableAndDataList(slaveDataListMap);
        onlineExtendExecutorUtil.doBeforeUpdateWithRelationn(masterTable, masterData, slaveDataMap);
        this.update(masterTable, masterData);
        if (slaveDataListMap != null) {
            String masterDataId = masterData.get(masterTable.getPrimaryKeyColumn().getColumnName()).toString();
            for (Map.Entry<OnlineDatasourceRelation, List<JSONObject>> relationEntry : slaveDataListMap.entrySet()) {
                Long relationId = relationEntry.getKey().getRelationId();
                this.updateRelationData(
                        masterTable, masterData, masterDataId, datasourceId, relationId, relationEntry.getValue());
            }
        }
        onlineExtendExecutorUtil.doAfterUpdateWithRelationn(masterTable, masterData, slaveDataMap);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRelationData(
            OnlineTable masterTable,
            Map<String, Object> masterData,
            String masterDataId,
            Long datasourceId,
            Long relationId,
            List<JSONObject> slaveDataList) {
        ResponseResult<OnlineDatasourceRelation> relationResult =
                onlineOperationHelper.verifyAndGetRelation(datasourceId, relationId);
        if (!relationResult.isSuccess()) {
            throw new OnlineRuntimeException(relationResult.getErrorMessage());
        }
        OnlineDatasourceRelation relation = relationResult.getData();
        OnlineTable slaveTable = relation.getSlaveTable();
        if (relation.getRelationType().equals(RelationType.ONE_TO_ONE)) {
            JSONObject slaveData = null;
            if (CollUtil.isNotEmpty(slaveDataList)) {
                slaveData = slaveDataList.get(0);
            }
            this.saveNewOrUpdateOneToOneRelationData(
                    masterTable, masterData, masterDataId, slaveTable, slaveData, relation);
        } else if (relation.getRelationType().equals(RelationType.ONE_TO_MANY)) {
            if (slaveDataList == null) {
                return;
            }
            this.saveNewOrUpdateOneToManyRelationData(
                    masterTable, masterData, masterDataId, slaveTable, slaveDataList, relation);
        }
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(OnlineTable table, List<OnlineDatasourceRelation> relationList, String dataId) {
        List<OnlineFilterDto> filterList =
                this.makeDefaultFilter(table, table.getPrimaryKeyColumn(), dataId);
        String dataPermFilter = this.buildDataPermFilter(table);
        OnlineColumn pkCol = table.getPrimaryKeyColumn();
        Object id = onlineOperationHelper.convertToTypeValue(pkCol, dataId);
        onlineExtendExecutorUtil.doBeforeDelete(table, id);
        if (table.getLogicDeleteColumn() == null) {
            if (this.doDelete(table, filterList, dataPermFilter) != 1) {
                return false;
            }
        } else {
            this.doLogicDelete(table, table.getPrimaryKeyColumn(), dataId, dataPermFilter);
        }
        if (CollUtil.isEmpty(relationList)) {
            onlineExtendExecutorUtil.doAfterDelete(table, id);
            return true;
        }
        Map<String, Object> masterData = getMasterData(table, null, null, dataId);
        for (OnlineDatasourceRelation relation : relationList) {
            if (BooleanUtil.isFalse(relation.getCascadeDelete())) {
                continue;
            }
            OnlineTable slaveTable = relation.getSlaveTable();
            OnlineColumn slaveColumn =
                    relation.getSlaveTable().getColumnMap().get(relation.getSlaveColumnId());
            String columnValue = dataId;
            if (!relation.getMasterColumnId().equals(table.getPrimaryKeyColumn().getColumnId())) {
                OnlineColumn relationMasterColumn = table.getColumnMap().get(relation.getMasterColumnId());
                columnValue = masterData.get(relationMasterColumn.getColumnName()).toString();
            }
            List<OnlineFilterDto> slaveFilterList =
                    this.makeDefaultFilter(relation.getSlaveTable(), slaveColumn, columnValue);
            if (slaveTable.getLogicDeleteColumn() == null) {
                this.doDelete(slaveTable, slaveFilterList, null);
            } else {
                this.doLogicDelete(slaveTable, slaveColumn, columnValue, null);
            }
        }
        onlineExtendExecutorUtil.doAfterDelete(table, id);
        return true;
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteOneToManySlaveData(
            OnlineTable table, OnlineColumn column, String columnValue, Set<String> keptIdSet) {
        List<OnlineFilterDto> filterList = this.makeDefaultFilter(table, column, columnValue);
        if (CollUtil.isNotEmpty(keptIdSet)) {
            OnlineFilterDto keptIdSetFilter = new OnlineFilterDto();
            Set<Serializable> convertedIdSet =
                    onlineOperationHelper.convertToTypeValue(table.getPrimaryKeyColumn(), keptIdSet);
            keptIdSetFilter.setColumnValueList(new HashSet<>(convertedIdSet));
            keptIdSetFilter.setTableName(table.getTableName());
            keptIdSetFilter.setColumnName(table.getPrimaryKeyColumn().getColumnName());
            keptIdSetFilter.setFilterType(FieldFilterType.NOT_IN_LIST_FILTER);
            filterList.add(keptIdSetFilter);
        }
        if (table.getLogicDeleteColumn() == null) {
            this.doDelete(table, filterList, null);
        } else {
            this.doLogicDelete(table, filterList, null);
        }
    }

    @Override
    public boolean existId(OnlineTable table, String dataId) {
        return this.getMasterData(table, null, null, dataId) != null;
    }

    @Override
    public Map<String, Object> getMasterData(
            OnlineTable table,
            List<OnlineDatasourceRelation> oneToOneRelationList,
            List<OnlineDatasourceRelation> allRelationList,
            String dataId) {
        List<OnlineFilterDto> filterList =
                this.makeDefaultFilter(table, table.getPrimaryKeyColumn(), dataId);
        // 组件表关联数据。
        List<JoinTableInfo> joinInfoList = this.makeJoinInfoList(table, oneToOneRelationList);
        // 拼接关联表的select fields字段。
        String selectFields = this.makeSelectFieldsWithRelation(table, oneToOneRelationList);
        String dataPermFilter = this.buildDataPermFilter(table);
        this.normalizeFiltersSlaveTableAlias(oneToOneRelationList, filterList);
        selectFields = this.normalizeSlaveTableAlias(oneToOneRelationList, selectFields);
        MyPageData<Map<String, Object>> pageData = this.getList(
                table, joinInfoList, selectFields, filterList, dataPermFilter, null, null);
        List<Map<String, Object>> resultList = pageData.getDataList();
        this.buildDataListWithDict(resultList, table, oneToOneRelationList);
        if (CollUtil.isEmpty(resultList)) {
            return null;
        }
        if (CollUtil.isNotEmpty(allRelationList)) {
            // 针对一对多和多对多关联，计算虚拟聚合字段。
            List<OnlineDatasourceRelation> toManyRelationList = allRelationList.stream()
                    .filter(r -> !r.getRelationType().equals(RelationType.ONE_TO_ONE)).collect(Collectors.toList());
            this.buildVirtualColumn(resultList, table, toManyRelationList);
        }
        this.reformatResultListWithOneToOneRelation(resultList, oneToOneRelationList);
        Map<String, Object> result = resultList.get(0);
        onlineExtendExecutorUtil.doAfterSelectOne(table, result);
        return result;
    }

    @Override
    public Map<String, Object> getSlaveData(OnlineDatasourceRelation relation, String dataId) {
        OnlineTable slaveTable = relation.getSlaveTable();
        List<OnlineFilterDto> filterList =
                this.makeDefaultFilter(slaveTable, slaveTable.getPrimaryKeyColumn(), dataId);
        // 拼接关联表的select fields字段。
        String selectFields = this.makeSelectFields(slaveTable, null);
        String dataPermFilter = this.buildDataPermFilter(slaveTable);
        MyPageData<Map<String, Object>> pageData = this.getList(
                slaveTable, null, selectFields, filterList, dataPermFilter, null, null);
        List<Map<String, Object>> resultList = pageData.getDataList();
        this.buildDataListWithDict(resultList, slaveTable);
        Map<String, Object> result = CollUtil.isEmpty(resultList) ? null : resultList.get(0);
        if (result != null) {
            onlineExtendExecutorUtil.doAfterSelectOne(slaveTable, result);
        }
        return result;
    }

    @Override
    public MyPageData<Map<String, Object>> getMasterDataList(
            OnlineTable table,
            List<OnlineDatasourceRelation> oneToOneRelationList,
            List<OnlineDatasourceRelation> allRelationList,
            List<OnlineFilterDto> filterList,
            String orderBy,
            MyPageParam pageParam) {
        this.normalizeFilterList(table, oneToOneRelationList, filterList);
        onlineExtendExecutorUtil.doBeforeSelectList(table, filterList);
        // 组件表关联数据。
        List<JoinTableInfo> joinInfoList = this.makeJoinInfoList(table, oneToOneRelationList);
        // 拼接关联表的select fields字段。
        String selectFields = this.makeSelectFieldsWithRelation(table, oneToOneRelationList);
        String dataPermFilter = this.buildDataPermFilter(table);
        this.normalizeFiltersSlaveTableAlias(oneToOneRelationList, filterList);
        selectFields = this.normalizeSlaveTableAlias(oneToOneRelationList, selectFields);
        orderBy = this.normalizeSlaveTableAlias(oneToOneRelationList, orderBy);
        MyPageData<Map<String, Object>> pageData =
                this.getList(table, joinInfoList, selectFields, filterList, dataPermFilter, orderBy, pageParam);
        List<Map<String, Object>> resultList = pageData.getDataList();
        this.buildDataListWithDict(resultList, table, oneToOneRelationList);
        // 针对一对多和多对多关联，计算虚拟聚合字段。
        if (CollUtil.isNotEmpty(allRelationList)) {
            List<OnlineDatasourceRelation> toManyRelationList = allRelationList.stream()
                    .filter(r -> !r.getRelationType().equals(RelationType.ONE_TO_ONE)).collect(Collectors.toList());
            this.buildVirtualColumn(resultList, table, toManyRelationList);
        }
        this.reformatResultListWithOneToOneRelation(resultList, oneToOneRelationList);
        onlineExtendExecutorUtil.doAfterSelectList(table, resultList);
        return pageData;
    }

    @Override
    public MyPageData<Map<String, Object>> getSlaveDataList(
            OnlineDatasourceRelation relation, List<OnlineFilterDto> filterList, String orderBy, MyPageParam pageParam) {
        OnlineTable slaveTable = relation.getSlaveTable();
        onlineExtendExecutorUtil.doBeforeSelectList(slaveTable, filterList);
        this.normalizeFilterList(slaveTable, null, filterList);
        // 拼接关联表的select fields字段。
        String selectFields = this.makeSelectFields(slaveTable, null);
        String dataPermFilter = this.buildDataPermFilter(slaveTable);
        MyPageData<Map<String, Object>> pageData =
                this.getList(slaveTable, null, selectFields, filterList, dataPermFilter, orderBy, pageParam);
        this.buildDataListWithDict(pageData.getDataList(), slaveTable);
        onlineExtendExecutorUtil.doAfterSelectList(slaveTable, pageData.getDataList());
        return pageData;
    }

    @Override
    public List<Map<String, Object>> getDictDataList(OnlineDict dict, List<OnlineFilterDto> filterList) {
        if (StrUtil.isNotBlank(dict.getDeletedColumnName())) {
            if (filterList == null) {
                filterList = new LinkedList<>();
            }
            OnlineFilterDto filter = new OnlineFilterDto();
            filter.setColumnName(dict.getDeletedColumnName());
            filter.setColumnValue(GlobalDeletedFlag.NORMAL);
            filterList.add(filter);
        }
        if (StrUtil.isNotBlank(dict.getTenantFilterColumnName())) {
            if (filterList == null) {
                filterList = new LinkedList<>();
            }
            OnlineFilterDto filter = new OnlineFilterDto();
            filter.setColumnName(dict.getTenantFilterColumnName());
            filter.setColumnValue(TokenData.takeFromRequest().getTenantId());
            filterList.add(filter);
        }
        String selectFields = this.makeDictSelectFields(dict, false);
        String dataPermFilter = this.buildDataPermFilter(
                dict.getTableName(), dict.getDeptFilterColumnName(), dict.getUserFilterColumnName());
        return this.getDictList(dict.getDblinkId(), dict.getTableName(), selectFields, filterList, dataPermFilter);
    }

    @Override
    public void buildDataListWithDict(
            OnlineTable masterTable, List<OnlineDatasourceRelation> relationList, List<Map<String, Object>> dataList) {
        this.buildDataListWithDict(dataList, masterTable, relationList);
    }

    @Override
    public Map<String, Object> calculatePermData(Set<Long> menuFormIds, Set<Long> viewFormIds, Set<Long> editFormIds) {
        Map<Long, Set<String>> formMenuPermMap = new HashMap<>(menuFormIds.size());
        for (Long menuFormId : menuFormIds) {
            formMenuPermMap.put(menuFormId, new HashSet<>());
        }
        Set<String> permCodeSet = new HashSet<>(10);
        Set<String> permUrlSet = new HashSet<>(10);
        if (CollUtil.isNotEmpty(viewFormIds)) {
            List<OnlineDatasource> datasourceList =
                    onlineDatasourceService.getOnlineDatasourceListByFormIds(viewFormIds);
            for (OnlineDatasource datasource : datasourceList) {
                permCodeSet.add(OnlineUtil.makeViewPermCode(datasource.getVariableName()));
                Set<String> permUrls = onlineProperties.getViewUrlList().stream()
                        .map(url -> url + datasource.getVariableName()).collect(Collectors.toSet());
                permUrlSet.addAll(permUrls);
                datasource.getOnlineFormDatasourceList().forEach(formDatasource ->
                        formMenuPermMap.get(formDatasource.getFormId()).addAll(permUrls));
            }
        }
        if (CollUtil.isNotEmpty(editFormIds)) {
            List<OnlineDatasource> datasourceList =
                    onlineDatasourceService.getOnlineDatasourceListByFormIds(editFormIds);
            for (OnlineDatasource datasource : datasourceList) {
                permCodeSet.add(OnlineUtil.makeEditPermCode(datasource.getVariableName()));
                Set<String> permUrls = onlineProperties.getEditUrlList().stream()
                        .map(url -> url + datasource.getVariableName()).collect(Collectors.toSet());
                permUrlSet.addAll(permUrls);
                datasource.getOnlineFormDatasourceList().forEach(formDatasource ->
                        formMenuPermMap.get(formDatasource.getFormId()).addAll(permUrls));
            }
        }
        List<String> onlineWhitelistUrls = CollUtil.newArrayList(
                onlineProperties.getUrlPrefix() + "/onlineOperation/listDict",
                onlineProperties.getUrlPrefix() + "/onlineForm/render",
                onlineProperties.getUrlPrefix() + "/onlineForm/view");
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("permCodeSet", permCodeSet);
        resultMap.put("permUrlSet", permUrlSet);
        resultMap.put("formMenuPermMap", formMenuPermMap);
        resultMap.put("onlineWhitelistUrls", onlineWhitelistUrls);
        return resultMap;
    }

    private boolean doUpdate(
            OnlineTable table, List<ColumnData> updateColumns, List<OnlineFilterDto> filters, String dataPermFilter) {
        return onlineOperationMapper.update(table.getTableName(), updateColumns, filters, dataPermFilter) == 1;
    }

    private int doDelete(OnlineTable table, List<OnlineFilterDto> filters, String dataPermFilter) {
        return onlineOperationMapper.delete(table.getTableName(), filters, dataPermFilter);
    }

    private List<Map<String, Object>> getGroupedListByCondition(
            Long dblinkId, String selectTable, String selectFields, String whereClause, String groupBy) {
        return onlineOperationMapper.getGroupedListByCondition(selectTable, selectFields, whereClause, groupBy);
    }

    private List<Map<String, Object>> getDictList(
            Long dblinkId, String tableName, String selectFields, List<OnlineFilterDto> filterList, String dataPermFilter) {
        return onlineOperationMapper.getDictList(tableName, selectFields, filterList, dataPermFilter);
    }

    private MyPageData<Map<String, Object>> getList(
            OnlineTable table,
            List<JoinTableInfo> joinInfoList,
            String selectFields,
            List<OnlineFilterDto> filterList,
            String dataPermFilter,
            String orderBy,
            MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        List<Map<String, Object>> resultList = onlineOperationMapper.getList(
                table.getTableName(), joinInfoList, selectFields, filterList, dataPermFilter, orderBy);
        return MyPageUtil.makeResponseData(resultList);
    }

    private String makeWhereClause(List<OnlineFilterDto> filters, String dataPermFilter, List<Object> paramList) {
        if (CollUtil.isEmpty(filters) && StrUtil.isBlank(dataPermFilter)) {
            return "";
        }
        StringBuilder where = new StringBuilder(512);
        List<String> normalizedFilters = new LinkedList<>();
        if (CollUtil.isNotEmpty(filters)) {
            for (OnlineFilterDto filter : filters) {
                String filterString = this.makeSubWhereClause(filter, paramList);
                if (StrUtil.isNotBlank(filterString)) {
                    normalizedFilters.add(filterString);
                }
            }
        }
        if (CollUtil.isNotEmpty(normalizedFilters)) {
            where.append(WHERE);
            where.append(CollUtil.join(normalizedFilters, AND));
        }
        if (StrUtil.isNotBlank(dataPermFilter)) {
            if (CollUtil.isNotEmpty(normalizedFilters)) {
                where.append(AND);
            } else {
                where.append(WHERE);
            }
            where.append(dataPermFilter);
        }
        return where.toString();
    }

    private String makeSubWhereClause(OnlineFilterDto filter, List<Object> paramList) {
        StringBuilder where = new StringBuilder(256);
        if (filter.getFilterType().equals(FieldFilterType.EQUAL_FILTER)) {
            where.append(this.makeWhereLeftOperator(filter));
            where.append(" = ? ");
            paramList.add(filter.getColumnValue());
        } else if (filter.getFilterType().equals(FieldFilterType.RANGE_FILTER)) {
            where.append(this.makeRangeFilterClause(filter, paramList));
        } else if (filter.getFilterType().equals(FieldFilterType.LIKE_FILTER)) {
            where.append(this.makeWhereLeftOperator(filter));
            where.append(" LIKE ? ");
            paramList.add(filter.getColumnValue());
        } else if (filter.getFilterType().equals(FieldFilterType.IN_LIST_FILTER)) {
            where.append(this.makeWhereLeftOperator(filter));
            where.append(" IN ( ");
            where.append(StrUtil.repeat("?,", filter.getColumnValueList().size()));
            where.setLength(where.length() - 1);
            where.append(")");
            paramList.addAll(filter.getColumnValueList());
        } else if (filter.getFilterType().equals(FieldFilterType.MULTI_LIKE)) {
            where.append("(");
            StringBuilder sb = new StringBuilder(128);
            sb.append(this.makeWhereLeftOperator(filter)).append(" LIKE ? OR ");
            String s = StrUtil.repeat(sb.toString(), filter.getColumnValueList().size());
            where.append(s, 0, s.length() - 4);
            where.append(")");
            paramList.addAll(filter.getColumnValueList());
        } else if (filter.getFilterType().equals(FieldFilterType.NOT_IN_LIST_FILTER)) {
            where.append(this.makeWhereLeftOperator(filter));
            where.append(" NOT IN (");
            where.append(StrUtil.repeat("?,", filter.getColumnValueList().size()));
            where.setLength(where.length() - 1);
            where.append(")");
            paramList.addAll(filter.getColumnValueList());
        } else if (filter.getFilterType().equals(FieldFilterType.IS_NULL)) {
            where.append(this.makeWhereLeftOperator(filter));
            where.append(" IS NULL ");
        } else if (filter.getFilterType().equals(FieldFilterType.IS_NOT_NULL)) {
            where.append(this.makeWhereLeftOperator(filter));
            where.append(" IS NOT NULL ");
        }
        return where.toString();
    }

    private String makeRangeFilterClause(OnlineFilterDto filter, List<Object> paramList) {
        StringBuilder where = new StringBuilder(256);
        if (ObjectUtil.isNotEmpty(filter.getColumnValueStart())) {
            where.append(this.makeWhereLeftOperator(filter));
            if (BooleanUtil.isTrue(filter.getIsOracleDate())) {
                where.append(" >= ").append(filter.getColumnValueStart());
            } else {
                where.append(" >= ? ");
                paramList.add(filter.getColumnValueStart());
            }
        }
        if (ObjectUtil.isNotEmpty(filter.getColumnValueEnd())) {
            if (ObjectUtil.isNotEmpty(filter.getColumnValueStart())) {
                where.append(AND);
            }
            where.append(this.makeWhereLeftOperator(filter));
            if (BooleanUtil.isTrue(filter.getIsOracleDate())) {
                where.append(" <= ").append(filter.getColumnValueEnd());
            } else {
                where.append(" <= ? ");
                paramList.add(filter.getColumnValueEnd());
            }
        }
        return where.toString();
    }

    private String makeWhereLeftOperator(OnlineFilterDto filter) {
        if (StrUtil.isBlank(filter.getTableName())) {
            return filter.getColumnName();
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append(filter.getTableName()).append(".").append(filter.getColumnName());
        return sb.toString();
    }

    private void saveNewOrUpdateOneToManyRelationData(
            OnlineTable masterTable,
            Map<String, Object> masterData,
            String masterDataId,
            OnlineTable slaveTable,
            List<JSONObject> relationDataList,
            OnlineDatasourceRelation relation) {
        if (masterData == null) {
            masterData = this.getMasterData(masterTable, null, null, masterDataId);
        }
        Set<String> idSet = new HashSet<>(relationDataList.size());
        for (JSONObject relationData : relationDataList) {
            Object id = relationData.get(relation.getSlaveTable().getPrimaryKeyColumn().getColumnName());
            if (ObjectUtil.isNotEmpty(id)) {
                idSet.add(id.toString());
            }
        }
        // 自动补齐主表关联数据。
        OnlineColumn masterColumn = masterTable.getColumnMap().get(relation.getMasterColumnId());
        Object masterColumnValue = masterData.get(masterColumn.getColumnName());
        OnlineColumn slaveColumn = relation.getSlaveTable().getColumnMap().get(relation.getSlaveColumnId());
        // 在从表中删除本地批量更新不存在的数据。
        this.deleteOneToManySlaveData(
                relation.getSlaveTable(), slaveColumn, masterColumnValue.toString(), idSet);
        for (JSONObject relationData : relationDataList) {
            // 自动补齐主表关联数据。
            relationData.put(slaveColumn.getColumnName(), masterColumnValue);
            // 拆解主表和一对多关联从表的输入参数，并构建出数据表的待插入数据列表。
            Object id = relationData.get(relation.getSlaveTable().getPrimaryKeyColumn().getColumnName());
            if (id == null) {
                this.saveNew(slaveTable, relationData);
            } else {
                this.update(slaveTable, relationData);
            }
        }
    }

    private void saveNewOrUpdateOneToOneRelationData(
            OnlineTable masterTable,
            Map<String, Object> masterData,
            String masterDataId,
            OnlineTable slaveTable,
            JSONObject slaveData,
            OnlineDatasourceRelation relation) {
        if (MapUtil.isEmpty(slaveData)) {
            return;
        }
        String keyColumnName = slaveTable.getPrimaryKeyColumn().getColumnName();
        String slaveDataId = slaveData.getString(keyColumnName);
        if (slaveDataId == null) {
            if (masterData == null) {
                masterData = this.getMasterData(masterTable, null, null, masterDataId);
            }
            // 自动补齐主表关联数据。
            OnlineColumn masterColumn = masterTable.getColumnMap().get(relation.getMasterColumnId());
            Object masterColumnValue = masterData.get(masterColumn.getColumnName());
            OnlineColumn slaveColumn = slaveTable.getColumnMap().get(relation.getSlaveColumnId());
            slaveData.put(slaveColumn.getColumnName(), masterColumnValue);
            this.saveNew(slaveTable, slaveData);
        } else {
            Map<String, Object> originalSlaveData =
                    this.getMasterData(slaveTable, null, null, slaveDataId);
            for (Map.Entry<String, Object> entry : originalSlaveData.entrySet()) {
                slaveData.putIfAbsent(entry.getKey(), entry.getValue());
            }
            if (!this.update(slaveTable, slaveData)) {
                throw new OnlineRuntimeException("关联从表 [" + slaveTable.getTableName() + "] 中的更新数据不存在");
            }
        }
    }

    private void reformatResultListWithOneToOneRelation(
            List<Map<String, Object>> resultList, List<OnlineDatasourceRelation> oneToOneRelationList) {
        if (CollUtil.isEmpty(oneToOneRelationList) || CollUtil.isEmpty(resultList)) {
            return;
        }
        for (OnlineDatasourceRelation r : oneToOneRelationList) {
            for (Map<String, Object> resultMap : resultList) {
                Collection<OnlineColumn> slaveColumnList = r.getSlaveTable().getColumnMap().values();
                Map<String, Object> oneToOneRelationDataMap = new HashMap<>(slaveColumnList.size());
                resultMap.put(r.getVariableName(), oneToOneRelationDataMap);
                for (OnlineColumn c : slaveColumnList) {
                    StringBuilder sb = new StringBuilder(64);
                    sb.append(r.getVariableName())
                            .append(OnlineConstant.RELATION_TABLE_COLUMN_SEPARATOR).append(c.getColumnName());
                    Object data = this.removeRelationColumnData(resultMap, sb.toString());
                    oneToOneRelationDataMap.put(c.getColumnName(), data);
                    if (c.getDictId() != null) {
                        sb.append(DICT_MAP_SUFFIX);
                        data = this.removeRelationColumnData(resultMap, sb.toString());
                        oneToOneRelationDataMap.put(c.getColumnName() + DICT_MAP_SUFFIX, data);
                    }
                }
            }
        }
    }

    private Object removeRelationColumnData(Map<String, Object> resultMap, String name) {
        Object data = resultMap.remove(name);
        if (data == null) {
            data = resultMap.remove("\"" + name + "\"");
        }
        return data;
    }

    private void buildVirtualColumn(
            List<Map<String, Object>> resultList, OnlineTable table, List<OnlineDatasourceRelation> relationList) {
        if (CollUtil.isEmpty(resultList) || CollUtil.isEmpty(relationList)) {
            return;
        }
        OnlineVirtualColumn virtualColumnFilter = new OnlineVirtualColumn();
        virtualColumnFilter.setTableId(table.getTableId());
        virtualColumnFilter.setVirtualType(VirtualType.AGGREGATION);
        List<OnlineVirtualColumn> virtualColumnList =
                onlineVirtualColumnService.getOnlineVirtualColumnList(virtualColumnFilter, null);
        if (CollUtil.isEmpty(virtualColumnList)) {
            return;
        }
        Map<Long, OnlineDatasourceRelation> relationMap =
                relationList.stream().collect(Collectors.toMap(OnlineDatasourceRelation::getRelationId, r -> r));
        for (OnlineVirtualColumn virtualColumn : virtualColumnList) {
            OnlineDatasourceRelation relation = relationMap.get(virtualColumn.getRelationId());
            if (relation.getRelationType().equals(RelationType.ONE_TO_MANY)) {
                this.doBuildVirtualColumnForOneToMany(table, resultList, virtualColumn, relation);
            }
        }
    }

    private void doBuildVirtualColumnForOneToMany(
            OnlineTable masterTable,
            List<Map<String, Object>> resultList,
            OnlineVirtualColumn virtualColumn,
            OnlineDatasourceRelation relation) {
        String slaveTableName = relation.getSlaveTable().getTableName();
        OnlineColumn slaveColumn =
                relation.getSlaveTable().getColumnMap().get(relation.getSlaveColumnId());
        String slaveColumnName = slaveColumn.getColumnName();
        OnlineColumn aggregationColumn =
                relation.getSlaveTable().getColumnMap().get(virtualColumn.getAggregationColumnId());
        String aggregationColumnName = aggregationColumn.getColumnName();
        Tuple2<String, String> selectAndGroupByTuple = makeSelectListAndGroupByClause(
                slaveTableName, slaveColumnName, slaveTableName, aggregationColumnName, virtualColumn.getAggregationType());
        String selectList = selectAndGroupByTuple.getFirst();
        String groupBy = selectAndGroupByTuple.getSecond();
        // 开始组装过滤从句。
        List<MyWhereCriteria> criteriaList = new LinkedList<>();
        // 1. 组装主表数据对从表的过滤条件。
        MyWhereCriteria inlistFilter = new MyWhereCriteria();
        OnlineColumn masterColumn = masterTable.getColumnMap().get(relation.getMasterColumnId());
        String masterColumnName = masterColumn.getColumnName();
        Set<Object> masterIdSet = resultList.stream()
                .map(r -> r.get(masterColumnName)).filter(Objects::nonNull).collect(Collectors.toSet());
        inlistFilter.setCriteria(
                slaveTableName, slaveColumnName, slaveColumn.getObjectFieldType(), MyWhereCriteria.OPERATOR_IN, masterIdSet);
        criteriaList.add(inlistFilter);
        // 2. 从表逻辑删除字段过滤。
        if (relation.getSlaveTable().getLogicDeleteColumn() != null) {
            MyWhereCriteria deleteFilter = new MyWhereCriteria();
            deleteFilter.setCriteria(
                    slaveTableName,
                    relation.getSlaveTable().getLogicDeleteColumn().getColumnName(),
                    relation.getSlaveTable().getLogicDeleteColumn().getObjectFieldType(),
                    MyWhereCriteria.OPERATOR_EQUAL,
                    GlobalDeletedFlag.NORMAL);
            criteriaList.add(deleteFilter);
        }
        if (StrUtil.isNotBlank(virtualColumn.getWhereClauseJson())) {
            List<VirtualColumnWhereClause> whereClauseList =
                    JSONArray.parseArray(virtualColumn.getWhereClauseJson(), VirtualColumnWhereClause.class);
            if (CollUtil.isNotEmpty(whereClauseList)) {
                for (VirtualColumnWhereClause whereClause : whereClauseList) {
                    MyWhereCriteria whereClauseFilter = new MyWhereCriteria();
                    OnlineColumn c = relation.getSlaveTable().getColumnMap().get(whereClause.getColumnId());
                    whereClauseFilter.setCriteria(
                            slaveTableName,
                            c.getColumnName(),
                            c.getObjectFieldType(),
                            whereClause.getOperatorType(),
                            whereClause.getValue());
                    criteriaList.add(whereClauseFilter);
                }
            }
        }
        String criteriaString = MyWhereCriteria.makeCriteriaString(criteriaList);
        List<Map<String, Object>> aggregationMapList =
                getGroupedListByCondition(masterTable.getDblinkId(), slaveTableName, selectList, criteriaString, groupBy);
        this.doMakeAggregationData(resultList, aggregationMapList, masterColumnName, virtualColumn.getObjectFieldName());
    }

    private void doMakeAggregationData(
            List<Map<String, Object>> resultList,
            List<Map<String, Object>> aggregationMapList,
            String masterColumnName,
            String virtualColumnName) {
        // 根据获取的分组聚合结果集，绑定到主表总的关联字段。
        if (CollUtil.isEmpty(aggregationMapList)) {
            return;
        }
        Map<String, Object> relatedMap = new HashMap<>(aggregationMapList.size());
        for (Map<String, Object> map : aggregationMapList) {
            relatedMap.put(map.get(KEY_NAME).toString(), map.get(VALUE_NAME));
        }
        for (Map<String, Object> dataObject : resultList) {
            String masterIdValue = dataObject.get(masterColumnName).toString();
            if (masterIdValue != null) {
                Object value = relatedMap.get(masterIdValue);
                if (value != null) {
                    dataObject.put(virtualColumnName, value);
                }
            }
        }
    }

    private Tuple2<String, String> makeSelectListAndGroupByClause(
            String groupTableName,
            String groupColumnName,
            String aggregationTableName,
            String aggregationColumnName,
            Integer aggregationType) {
        String aggregationFunc = AggregationType.getAggregationFunction(aggregationType);
        // 构建Select List
        // 如：r_table.master_id groupedKey, SUM(r_table.aggr_column) aggregated_value
        StringBuilder groupedSelectList = new StringBuilder(128);
        groupedSelectList.append(groupTableName)
                .append(".")
                .append(groupColumnName)
                .append(" ")
                .append(KEY_NAME)
                .append(", ")
                .append(aggregationFunc)
                .append("(")
                .append(aggregationTableName)
                .append(".")
                .append(aggregationColumnName)
                .append(") ")
                .append(VALUE_NAME)
                .append(" ");
        StringBuilder groupBy = new StringBuilder(64);
        groupBy.append(groupTableName).append(".").append(groupColumnName);
        return new Tuple2<>(groupedSelectList.toString(), groupBy.toString());
    }

    private void buildDataListWithDict(List<Map<String, Object>> resultList, OnlineTable slaveTable) {
        if (CollUtil.isEmpty(resultList)) {
            return;
        }
        Set<Long> dictIdSet = new HashSet<>();
        // 先找主表字段对字典的依赖。
        Multimap<Long, OnlineColumn> dictColumnMap = LinkedHashMultimap.create();
        for (OnlineColumn column : slaveTable.getColumnMap().values()) {
            if (column.getDictId() != null) {
                dictIdSet.add(column.getDictId());
                column.setColumnAliasName(column.getColumnName());
                dictColumnMap.put(column.getDictId(), column);
            }
        }
        this.doBuildDataListWithDict(resultList, dictIdSet, dictColumnMap);
    }

    private void buildDataListWithDict(
            List<Map<String, Object>> resultList,
            OnlineTable masterTable,
            List<OnlineDatasourceRelation> relationList) {
        if (CollUtil.isEmpty(resultList)) {
            return;
        }
        Set<Long> dictIdSet = new HashSet<>();
        // 先找主表字段对字典的依赖。
        Multimap<Long, OnlineColumn> dictColumnMap = LinkedHashMultimap.create();
        for (OnlineColumn column : masterTable.getColumnMap().values()) {
            if (column.getDictId() != null) {
                dictIdSet.add(column.getDictId());
                column.setColumnAliasName(column.getColumnName());
                dictColumnMap.put(column.getDictId(), column);
            }
        }
        // 再找关联表字段对字典的依赖。
        if (CollUtil.isEmpty(relationList)) {
            this.doBuildDataListWithDict(resultList, dictIdSet, dictColumnMap);
            return;
        }
        for (OnlineDatasourceRelation relation : relationList) {
            for (OnlineColumn column : relation.getSlaveTable().getColumnMap().values()) {
                if (column.getDictId() != null) {
                    dictIdSet.add(column.getDictId());
                    String columnAliasName = relation.getVariableName()
                            + OnlineConstant.RELATION_TABLE_COLUMN_SEPARATOR + column.getColumnName();
                    column.setColumnAliasName(columnAliasName);
                    dictColumnMap.put(column.getDictId(), column);
                }
            }
        }
        this.doBuildDataListWithDict(resultList, dictIdSet, dictColumnMap);
    }

    private void doBuildDataListWithDict(
            List<Map<String, Object>> resultList, Set<Long> dictIdSet, Multimap<Long, OnlineColumn> dictColumnMap) {
        if (CollUtil.isEmpty(dictIdSet)) {
            return;
        }
        List<OnlineDict> allDictList = onlineDictService.getOnlineDictListFromCache(dictIdSet);
        for (OnlineDict dict : allDictList) {
            Collection<OnlineColumn> columnList = dictColumnMap.get(dict.getDictId());
            for (OnlineColumn column : columnList) {
                Set<Serializable> dictIdDataSet = this.extractColumnDictIds(resultList, column);
                if (CollUtil.isNotEmpty(dictIdDataSet)) {
                    this.doBindColumnDictData(resultList, column, dict, dictIdDataSet);
                }
            }
        }
    }

    private Set<Serializable> extractColumnDictValues(List<Map<String, Object>> dataList, OnlineColumn column) {
        Set<Serializable> dictValueDataSet = new HashSet<>();
        for (Map<String, Object> data : dataList) {
            String dictValueData = (String) data.get(column.getColumnAliasName());
            if (StrUtil.isNotBlank(dictValueData)) {
                if (ObjectUtil.equals(column.getFieldKind(), FieldKind.DICT_MULTI_SELECT)) {
                    Set<String> dictValueDataList = StrUtil.split(dictValueData, ",")
                            .stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
                    CollUtil.addAll(dictValueDataSet, dictValueDataList);
                } else {
                    dictValueDataSet.add(dictValueData);
                }
            }
        }
        return dictValueDataSet;
    }

    private Set<Serializable> extractColumnDictIds(List<Map<String, Object>> resultList, OnlineColumn column) {
        Set<Serializable> dictIdDataSet = new HashSet<>();
        for (Map<String, Object> result : resultList) {
            Object dictIdData = result.get(column.getColumnAliasName());
            if (ObjectUtil.isEmpty(dictIdData)) {
                continue;
            }
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.DICT_MULTI_SELECT)) {
                Set<Serializable> dictIdDataList = StrUtil.split(dictIdData.toString(), ",")
                        .stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
                if (ObjectFieldType.LONG.equals(column.getObjectFieldType())) {
                    dictIdDataList = dictIdDataSet.stream()
                            .map(c -> (Serializable) Long.valueOf(c.toString())).collect(Collectors.toSet());
                }
                CollUtil.addAll(dictIdDataSet, dictIdDataList);
            } else {
                dictIdDataSet.add((Serializable) dictIdData);
            }
        }
        return dictIdDataSet;
    }

    private Map<Serializable, String> getGlobalDictItemDictMapFromCache(String dictCode, Set<Serializable> itemIds) {
        return globalDictService.getGlobalDictItemDictMapFromCache(dictCode, itemIds);
    }

    private void doTranslateColumnDictData(
            List<Map<String, Object>> dataList,
            OnlineColumn column,
            OnlineDict dict,
            Set<Serializable> dictValueDataSet) {
        Map<String, Object> dictResultMap = this.doTranslateColumnDictDataMap(dict, dictValueDataSet);
        for (Map<String, Object> data : dataList) {
            String dictValueData = (String) data.get(column.getColumnAliasName());
            if (StrUtil.isBlank(dictValueData)) {
                continue;
            }
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.DICT_MULTI_SELECT)) {
                List<String> dictValueDataList = StrUtil.splitTrim(dictValueData, ",");
                List<Object> dictIdList = dictValueDataList.stream()
                        .map(dictResultMap::get).filter(Objects::nonNull).collect(Collectors.toList());
                data.put(column.getColumnAliasName(), CollUtil.join(dictIdList, ","));
            } else {
                Object dictId = dictResultMap.get(dictValueData);
                if (dictId != null) {
                    data.put(column.getColumnAliasName(), dictId);
                }
            }
        }
    }

    private Map<String, Object> doTranslateColumnDictDataMap(OnlineDict dict, Set<Serializable> dictValueDataSet) {
        Map<String, Object> dictResultMap = new HashMap<>(dictValueDataSet.size());
        if (dict.getDictType().equals(DictType.CUSTOM)) {
            ConstDictInfo dictInfo =
                    JSONObject.parseObject(dict.getDictDataJson(), ConstDictInfo.class);
            List<ConstDictInfo.ConstDictData> dictDataList = dictInfo.getDictData();
            for (ConstDictInfo.ConstDictData dictData : dictDataList) {
                dictResultMap.put(dictData.getName(), dictData.getId());
            }
        } else if (dict.getDictType().equals(DictType.GLOBAL_DICT)) {
            Map<Serializable, String> dictDataMap =
                    this.getGlobalDictItemDictMapFromCache(dict.getDictCode(), null);
            dictDataMap.entrySet().stream()
                    .filter(entry -> dictValueDataSet.contains(entry.getValue()))
                    .forEach(entry -> dictResultMap.put(entry.getValue(), entry.getKey()));
        } else if (dict.getDictType().equals(DictType.TABLE)) {
            String selectFields = this.makeDictSelectFields(dict, true);
            List<OnlineFilterDto> filterList = this.createDefaultFilter(dict);
            OnlineFilterDto inlistFilter = new OnlineFilterDto();
            inlistFilter.setTableName(dict.getTableName());
            inlistFilter.setColumnName(dict.getValueColumnName());
            inlistFilter.setColumnValueList(dictValueDataSet);
            inlistFilter.setFilterType(FieldFilterType.IN_LIST_FILTER);
            filterList.add(inlistFilter);
            List<Map<String, Object>> dictResultList =
                    this.getDictList(dict.getDblinkId(), dict.getTableName(), selectFields, filterList, null);
            if (CollUtil.isNotEmpty(dictResultList)) {
                for (Map<String, Object> dictResult : dictResultList) {
                    dictResultMap.put(dictResult.get("name").toString(), dictResult.get("id"));
                }
            }
        } else if (dict.getDictType().equals(DictType.URL)) {
            this.buildUrlDictDataMap(dict, dictResultMap, false);
        }
        return dictResultMap;
    }

    private Map<String, Object> doBuildColumnDictDataMap(OnlineDict dict, Set<Serializable> dictIdDataSet) {
        Map<String, Object> dictResultMap = new HashMap<>(dictIdDataSet.size());
        if (dict.getDictType().equals(DictType.CUSTOM)) {
            ConstDictInfo dictInfo =
                    JSONObject.parseObject(dict.getDictDataJson(), ConstDictInfo.class);
            List<ConstDictInfo.ConstDictData> dictDataList = dictInfo.getDictData();
            for (ConstDictInfo.ConstDictData dictData : dictDataList) {
                dictResultMap.put(dictData.getId().toString(), dictData.getName());
            }
        } else if (dict.getDictType().equals(DictType.GLOBAL_DICT)) {
            Map<Serializable, String> dictDataMap =
                    this.getGlobalDictItemDictMapFromCache(dict.getDictCode(), dictIdDataSet);
            for (Map.Entry<Serializable, String> entry : dictDataMap.entrySet()) {
                dictResultMap.put(entry.getKey().toString(), entry.getValue());
            }
        } else if (dict.getDictType().equals(DictType.TABLE)) {
            String selectFields = this.makeDictSelectFields(dict, true);
            List<OnlineFilterDto> filterList = this.createDefaultFilter(dict);
            OnlineFilterDto inlistFilter = new OnlineFilterDto();
            inlistFilter.setTableName(dict.getTableName());
            inlistFilter.setColumnName(dict.getKeyColumnName());
            inlistFilter.setColumnValueList(dictIdDataSet);
            inlistFilter.setFilterType(FieldFilterType.IN_LIST_FILTER);
            filterList.add(inlistFilter);
            List<Map<String, Object>> dictResultList =
                    this.getDictList(dict.getDblinkId(), dict.getTableName(), selectFields, filterList, null);
            if (CollUtil.isNotEmpty(dictResultList)) {
                for (Map<String, Object> dictResult : dictResultList) {
                    dictResultMap.put(dictResult.get("id").toString(), dictResult.get("name"));
                }
            }
        } else if (dict.getDictType().equals(DictType.URL)) {
            this.buildUrlDictDataMap(dict, dictResultMap, true);
        }
        return dictResultMap;
    }

    private List<OnlineFilterDto> createDefaultFilter(OnlineDict dict) {
        List<OnlineFilterDto> filterList = new LinkedList<>();
        if (StrUtil.isNotBlank(dict.getDeletedColumnName())) {
            OnlineFilterDto filter = new OnlineFilterDto();
            filter.setTableName(dict.getTableName());
            filter.setColumnName(dict.getDeletedColumnName());
            filter.setColumnValue(GlobalDeletedFlag.NORMAL);
            filterList.add(filter);
        }
        return filterList;
    }

    private void buildUrlDictDataMap(OnlineDict dict, Map<String, Object> dictResultMap, boolean keyToValue) {
        Map<String, Object> param = new HashMap<>(1);
        param.put("Authorization", TokenData.takeFromRequest().getToken());
        String responseData = HttpUtil.get(dict.getDictListUrl(), param);
        ResponseResult<JSONArray> responseResult =
                JSON.parseObject(responseData, new TypeReference<ResponseResult<JSONArray>>() {
                });
        if (!responseResult.isSuccess()) {
            throw new OnlineRuntimeException(responseResult.getErrorMessage());
        }
        JSONArray dictDataArray = responseResult.getData();
        for (int i = 0; i < dictDataArray.size(); i++) {
            JSONObject dictData = dictDataArray.getJSONObject(i);
            if (keyToValue) {
                dictResultMap.put(dictData.getString(dict.getKeyColumnName()), dictData.get(dict.getValueColumnName()));
            } else {
                dictResultMap.put(dictData.getString(dict.getValueColumnName()), dictData.get(dict.getKeyColumnName()));
            }
        }
    }

    private void doBindColumnDictData(
            List<Map<String, Object>> resultList,
            OnlineColumn column,
            OnlineDict dict,
            Set<Serializable> dictIdDataSet) {
        Map<String, Object> dictResultMap = this.doBuildColumnDictDataMap(dict, dictIdDataSet);
        String dictKeyName;
        if (ObjectUtil.equals(column.getFieldKind(), FieldKind.DICT_MULTI_SELECT)) {
            dictKeyName = column.getColumnAliasName() + DICT_MAP_LIST_SUFFIX;
        } else {
            dictKeyName = column.getColumnAliasName() + DICT_MAP_SUFFIX;
        }
        for (Map<String, Object> result : resultList) {
            Object dictIdData = result.get(column.getColumnAliasName());
            if (ObjectUtil.isEmpty(dictIdData)) {
                continue;
            }
            if (ObjectUtil.equals(column.getFieldKind(), FieldKind.DICT_MULTI_SELECT)) {
                List<String> dictIdDataList = StrUtil.splitTrim(dictIdData.toString(), ",");
                List<Map<String, Object>> dictMapList = new LinkedList<>();
                for (String data : dictIdDataList) {
                    Object dictNameData = dictResultMap.get(data);
                    Map<String, Object> dictMap = new HashMap<>(2);
                    dictMap.put("id", data);
                    dictMap.put("name", dictNameData);
                    dictMapList.add(dictMap);
                }
                result.put(dictKeyName, dictMapList);
            } else {
                Object dictNameData = dictResultMap.get(dictIdData.toString());
                Map<String, Object> dictMap = new HashMap<>(2);
                dictMap.put("id", dictIdData);
                dictMap.put("name", dictNameData);
                result.put(dictKeyName, dictMap);
            }
        }
    }

    private List<JoinTableInfo> makeJoinInfoList(
            OnlineTable masterTable, List<OnlineDatasourceRelation> relationList) {
        List<JoinTableInfo> joinInfoList = new LinkedList<>();
        if (CollUtil.isEmpty(relationList)) {
            return joinInfoList;
        }
        Map<Long, OnlineColumn> masterTableColumnMap = masterTable.getColumnMap();
        for (OnlineDatasourceRelation relation : relationList) {
            JoinTableInfo joinInfo = new JoinTableInfo();
            joinInfo.setLeftJoin(relation.getLeftJoin());
            joinInfo.setJoinTableName(relation.getSlaveTable().getTableName() + " " + relation.getVariableName());
            // 根据配置动态拼接JOIN的关联条件，同时要考虑从表的逻辑删除过滤。
            OnlineColumn masterColumn = masterTableColumnMap.get(relation.getMasterColumnId());
            OnlineColumn slaveColumn = relation.getSlaveTable().getColumnMap().get(relation.getSlaveColumnId());
            StringBuilder conditionBuilder = new StringBuilder(64);
            conditionBuilder
                    .append(masterTable.getTableName())
                    .append(".")
                    .append(masterColumn.getColumnName())
                    .append(" = ")
                    .append(relation.getVariableName())
                    .append(".")
                    .append(slaveColumn.getColumnName());
            if (relation.getSlaveTable().getLogicDeleteColumn() != null) {
                conditionBuilder
                        .append(AND)
                        .append(relation.getVariableName())
                        .append(".")
                        .append(relation.getSlaveTable().getLogicDeleteColumn().getColumnName())
                        .append(" = ")
                        .append(GlobalDeletedFlag.NORMAL);
            }
            joinInfo.setJoinCondition(conditionBuilder.toString());
            joinInfoList.add(joinInfo);
        }
        return joinInfoList;
    }

    private Map<OnlineTable, List<JSONObject>> convertToSlaveTableAndDataList(
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Map<OnlineTable, List<JSONObject>> resultMap = MapUtil.newHashMap();
        if (slaveDataListMap == null) {
            return resultMap;
        }
        for (Map.Entry<OnlineDatasourceRelation, List<JSONObject>> entry : slaveDataListMap.entrySet()) {
            resultMap.put(entry.getKey().getSlaveTable(), entry.getValue());
        }
        return resultMap;
    }

    private String makeSelectFields(OnlineTable table, String relationVariable) {
        DataSourceProvider provider = dataSourceUtil.getProvider(table.getDblinkId());
        StringBuilder selectFieldBuider = new StringBuilder(512);
        String intString = "SIGNED";
        if (provider.getDblinkType() == DblinkType.POSTGRESQL|| provider.getDblinkType() == DblinkType.OPENGAUSS) {
            intString = "INT8";
        }
        // 拼装主表的select fields字段。
        for (OnlineColumn column : table.getColumnMap().values()) {
            OnlineColumn deletedColumn = table.getLogicDeleteColumn();
            String columnAliasName = column.getColumnName();
            if (relationVariable != null) {
                columnAliasName = relationVariable
                        + OnlineConstant.RELATION_TABLE_COLUMN_SEPARATOR + column.getColumnName();
            }
            if (deletedColumn != null && StrUtil.equals(column.getColumnName(), deletedColumn.getColumnName())) {
                continue;
            }
            if (this.castToInteger(column)) {
                selectFieldBuider
                        .append("CAST(")
                        .append(table.getTableName())
                        .append(".")
                        .append(column.getColumnName())
                        .append(" AS ")
                        .append(intString)
                        .append(") \"")
                        .append(columnAliasName)
                        .append("\",");
            } else if ("date".equals(column.getColumnType())) {
                selectFieldBuider
                        .append("CAST(")
                        .append(table.getTableName())
                        .append(".")
                        .append(column.getColumnName())
                        .append(" AS CHAR(10)) \"")
                        .append(columnAliasName)
                        .append("\",");
            } else {
                selectFieldBuider
                        .append(table.getTableName())
                        .append(".")
                        .append(column.getColumnName())
                        .append(" \"")
                        .append(columnAliasName)
                        .append("\",");
            }
        }
        return selectFieldBuider.substring(0, selectFieldBuider.length() - 1);
    }

    private String makeSelectFieldsWithRelation(
            OnlineTable masterTable, List<OnlineDatasourceRelation> relationList) {
        String masterTableSelectFields = this.makeSelectFields(masterTable, null);
        if (CollUtil.isEmpty(relationList)) {
            return masterTableSelectFields;
        }
        StringBuilder selectFieldBuider = new StringBuilder(512);
        selectFieldBuider.append(masterTableSelectFields).append(",");
        for (OnlineDatasourceRelation relation : relationList) {
            OnlineTable slaveTable = relation.getSlaveTable();
            String relationTableSelectFields = this.makeSelectFields(slaveTable, relation.getVariableName());
            selectFieldBuider.append(relationTableSelectFields).append(",");
        }
        return selectFieldBuider.substring(0, selectFieldBuider.length() - 1);
    }

    private String makeDictSelectFields(OnlineDict onlineDict, boolean ignoreParentId) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(onlineDict.getKeyColumnName()).append(" \"id\", ");
        sb.append(onlineDict.getValueColumnName()).append(" \"name\"");
        if (!ignoreParentId && BooleanUtil.isTrue(onlineDict.getTreeFlag())) {
            sb.append(", ").append(onlineDict.getParentKeyColumnName()).append(" \"parentId\"");
        }
        return sb.toString();
    }

    private boolean castToInteger(OnlineColumn column) {
        return "tinyint(1)".equals(column.getFullColumnType());
    }

    private String makeColumnNames(List<ColumnData> columnDataList) {
        StringBuilder sb = new StringBuilder(512);
        for (ColumnData columnData : columnDataList) {
            if (BooleanUtil.isTrue(columnData.getColumn().getAutoIncrement())) {
                continue;
            }
            sb.append(columnData.getColumn().getColumnName()).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private void makeupColumnValue(ColumnData columnData) {
        if (BooleanUtil.isTrue(columnData.getColumn().getAutoIncrement())) {
            return;
        }
        if (BooleanUtil.isTrue(columnData.getColumn().getPrimaryKey())) {
            if (columnData.getColumnValue() == null
                    && BooleanUtil.isFalse(columnData.getColumn().getAutoIncrement())) {
                if (ObjectFieldType.LONG.equals(columnData.getColumn().getObjectFieldType())) {
                    columnData.setColumnValue(idGenerator.nextLongId());
                } else {
                    columnData.setColumnValue(idGenerator.nextStringId());
                }
            }
        } else if (columnData.getColumn().getFieldKind() != null) {
            this.makeupColumnValueForFieldKind(columnData);
        } else if (columnData.getColumn().getColumnDefault() != null
                && columnData.getColumnValue() == null) {
            Object v = onlineOperationHelper.convertToTypeValue(
                    columnData.getColumn(), columnData.getColumn().getColumnDefault());
            columnData.setColumnValue(v);
        }
    }

    private void makeupColumnValueForFieldKind(ColumnData columnData) {
        switch (columnData.getColumn().getFieldKind()) {
            case FieldKind.CREATE_TIME:
            case FieldKind.UPDATE_TIME:
                columnData.setColumnValue(LocalDateTime.now());
                break;
            case FieldKind.CREATE_USER_ID:
            case FieldKind.UPDATE_USER_ID:
                columnData.setColumnValue(TokenData.takeFromRequest().getUserId());
                break;
            case FieldKind.CREATE_DEPT_ID:
                columnData.setColumnValue(TokenData.takeFromRequest().getDeptId());
                break;
            case FieldKind.LOGIC_DELETE:
                columnData.setColumnValue(GlobalDeletedFlag.NORMAL);
                break;
            default:
                break;
        }
    }

    private List<OnlineFilterDto> makeDefaultFilter(OnlineTable table, OnlineColumn column, String columnValue) {
        List<OnlineFilterDto> filterList = new LinkedList<>();
        OnlineFilterDto dataIdFilter = new OnlineFilterDto();
        dataIdFilter.setTableName(table.getTableName());
        dataIdFilter.setColumnName(column.getColumnName());
        dataIdFilter.setColumnValue(onlineOperationHelper.convertToTypeValue(column, columnValue));
        filterList.add(dataIdFilter);
        if (table.getLogicDeleteColumn() != null) {
            OnlineFilterDto filter = new OnlineFilterDto();
            filter.setTableName(table.getTableName());
            filter.setColumnName(table.getLogicDeleteColumn().getColumnName());
            filter.setColumnValue(GlobalDeletedFlag.NORMAL);
            filterList.add(filter);
        }
        return filterList;
    }

    private void doLogicDelete(
            OnlineTable table, List<OnlineFilterDto> filterList, String dataPermFilter) {
        List<ColumnData> updateColumnList = new LinkedList<>();
        ColumnData logicDeleteColumnData = new ColumnData();
        logicDeleteColumnData.setColumn(table.getLogicDeleteColumn());
        logicDeleteColumnData.setColumnValue(GlobalDeletedFlag.DELETED);
        updateColumnList.add(logicDeleteColumnData);
        this.doUpdate(table, updateColumnList, filterList, dataPermFilter);
    }

    private void doLogicDelete(
            OnlineTable table, OnlineColumn filterColumn, String filterColumnValue, String dataPermFilter) {
        List<OnlineFilterDto> filterList = new LinkedList<>();
        OnlineFilterDto filter = new OnlineFilterDto();
        filter.setTableName(table.getTableName());
        filter.setColumnName(filterColumn.getColumnName());
        filter.setColumnValue(onlineOperationHelper.convertToTypeValue(filterColumn, filterColumnValue));
        filterList.add(filter);
        this.doLogicDelete(table, filterList, dataPermFilter);
    }

    private void normalizeFilterList(
            OnlineTable table, List<OnlineDatasourceRelation> oneToOneRelationList, List<OnlineFilterDto> filterList) {
        if (table.getLogicDeleteColumn() != null) {
            if (filterList == null) {
                filterList = new LinkedList<>();
            }
            OnlineFilterDto filter = new OnlineFilterDto();
            filter.setTableName(table.getTableName());
            filter.setColumnName(table.getLogicDeleteColumn().getColumnName());
            filter.setColumnValue(GlobalDeletedFlag.NORMAL);
            filterList.add(filter);
        }
        if (CollUtil.isEmpty(filterList)) {
            return;
        }
        OnlineDblink dblink = onlineDblinkService.getById(table.getDblinkId());
        for (OnlineFilterDto filter : filterList) {
            // oracle 日期字段的，后面要重写这段代码，以便具有更好的通用性。
            if (filter.getFilterType().equals(FieldFilterType.RANGE_FILTER)) {
                this.makeRangeFilter(dblink, table, oneToOneRelationList, filter);
            }
            if (BooleanUtil.isTrue(filter.getDictMultiSelect())) {
                filter.setFilterType(FieldFilterType.MULTI_LIKE);
                List<String> dictValueSet = StrUtil.split(filter.getColumnValue().toString(), ",");
                filter.setColumnValueList(
                        dictValueSet.stream().map(v -> "%" + v + ",%").collect(Collectors.toSet()));
            }
            if (filter.getFilterType().equals(FieldFilterType.LIKE_FILTER)) {
                filter.setColumnValue("%" + filter.getColumnValue() + "%");
            } else if (filter.getFilterType().equals(FieldFilterType.IN_LIST_FILTER)
                    && ObjectUtil.isNotEmpty(filter.getColumnValue())) {
                filter.setColumnValueList(
                        new HashSet<>(StrUtil.split(filter.getColumnValue().toString(), ",")));
            }
        }
    }

    private String normalizeSlaveTableAlias(List<OnlineDatasourceRelation> relationList, String s) {
        if (CollUtil.isEmpty(relationList) || StrUtil.isBlank(s)) {
            return s;
        }
        for (OnlineDatasourceRelation r : relationList) {
            s = StrUtil.replace(s, r.getSlaveTable().getTableName() + ".", r.getVariableName() + ".");
        }
        return s;
    }

    private void normalizeFiltersSlaveTableAlias(
            List<OnlineDatasourceRelation> relationList, List<OnlineFilterDto> filters) {
        if (CollUtil.isEmpty(relationList) || CollUtil.isEmpty(filters)) {
            return;
        }
        for (OnlineDatasourceRelation r : relationList) {
            for (OnlineFilterDto filter : filters) {
                if (StrUtil.equals(filter.getTableName(), r.getSlaveTable().getTableName())) {
                    filter.setTableName(r.getVariableName());
                }
            }
        }
    }

    private void makeRangeFilter(
            OnlineDblink dblink,
            OnlineTable table,
            List<OnlineDatasourceRelation> oneToOneRelationList,
            OnlineFilterDto filter) {
        if (!dblink.getDblinkType().equals(DblinkType.ORACLE)) {
            return;
        }
        OnlineColumn column = table.getColumnMap().values().stream()
                .filter(c -> c.getColumnName().equals(filter.getColumnName())).findFirst().orElse(null);
        if (column == null && oneToOneRelationList != null) {
            for (OnlineDatasourceRelation r : oneToOneRelationList) {
                column = r.getSlaveTable().getColumnMap().values().stream()
                        .filter(c -> c.getColumnName().equals(filter.getColumnName())).findFirst().orElse(null);
                if (column != null) {
                    break;
                }
            }
        }
        org.springframework.util.Assert.notNull(column, "column can't be NULL.");
        filter.setIsOracleDate(StrUtil.equals(column.getObjectFieldType(), "Date"));
        if (BooleanUtil.isTrue(filter.getIsOracleDate())) {
            if (filter.getColumnValueStart() != null) {
                filter.setColumnValueStart("TO_DATE('" + filter.getColumnValueStart() + "','YYYY-MM-DD HH24:MI:SS')");
            }
            if (filter.getColumnValueEnd() != null) {
                filter.setColumnValueEnd("TO_DATE('" + filter.getColumnValueEnd() + "','YYYY-MM-DD HH24:MI:SS')");
            }
        }
    }

    private String buildDataPermFilter(String tableName, String deptFilterColumnName, String userFilterColumnName) {
        if (BooleanUtil.isFalse(dataFilterProperties.getEnabledDataPermFilter())) {
            return null;
        }
        if (!GlobalThreadLocal.enabledDataFilter()) {
            return null;
        }
        return processDataPerm(tableName, deptFilterColumnName, userFilterColumnName);
    }

    private String buildDataPermFilter(OnlineTable table) {
        if (BooleanUtil.isFalse(dataFilterProperties.getEnabledDataPermFilter())) {
            return null;
        }
        if (!GlobalThreadLocal.enabledDataFilter()) {
            return null;
        }
        String deptFilterColumnName = null;
        String userFilterColumnName = null;
        for (OnlineColumn column : table.getColumnMap().values()) {
            if (BooleanUtil.isTrue(column.getDeptFilter())) {
                deptFilterColumnName = column.getColumnName();
            }
            if (BooleanUtil.isTrue(column.getUserFilter())) {
                userFilterColumnName = column.getColumnName();
            }
        }
        return processDataPerm(table.getTableName(), deptFilterColumnName, userFilterColumnName);
    }

    private String processDataPerm(String tableName, String deptFilterColumnName, String userFilterColumnName) {
        TokenData tokenData = TokenData.takeFromRequest();
        if (Boolean.TRUE.equals(tokenData.getIsAdmin())) {
            return null;
        }
        if (StrUtil.isAllBlank(deptFilterColumnName, userFilterColumnName)) {
            return null;
        }
        String dataPermSessionKey = RedisKeyUtil.makeSessionDataPermIdKey(tokenData.getSessionId());
        Object cachedData = this.getCachedData(dataPermSessionKey);
        if (cachedData == null) {
            throw new NoDataPermException("No Related DataPerm found For OnlineForm Module.");
        }
        JSONObject allMenuDataPermMap = cachedData instanceof JSONObject
                ? (JSONObject) cachedData : JSON.parseObject(cachedData.toString());
        JSONObject menuDataPermMap = this.getAndVerifyMenuDataPerm(allMenuDataPermMap, tableName);
        Map<Integer, String> dataPermMap = new HashMap<>(8);
        for (Map.Entry<String, Object> entry : menuDataPermMap.entrySet()) {
            dataPermMap.put(Integer.valueOf(entry.getKey()), entry.getValue().toString());
        }
        if (MapUtil.isEmpty(dataPermMap)) {
            throw new NoDataPermException(StrFormatter.format(
                    "No Related OnlineForm DataPerm found for table [{}].", tableName));
        }
        if (dataPermMap.containsKey(DataPermRuleType.TYPE_ALL)) {
            return null;
        }
        return doProcessDataPerm(tableName, deptFilterColumnName, userFilterColumnName, dataPermMap);
    }

    private JSONObject getAndVerifyMenuDataPerm(JSONObject allMenuDataPermMap, String tableName) {
        String menuId = ContextUtil.getHttpRequest().getHeader(ApplicationConstant.HTTP_HEADER_MENU_ID);
        if (menuId == null) {
            menuId = ContextUtil.getHttpRequest().getParameter(ApplicationConstant.HTTP_HEADER_MENU_ID);
        }
        if (BooleanUtil.isFalse(dataFilterProperties.getEnableMenuPermVerify()) && menuId == null) {
            menuId = ApplicationConstant.DATA_PERM_ALL_MENU_ID;
        }
        Assert.notNull(menuId);
        JSONObject menuDataPermMap = allMenuDataPermMap.getJSONObject(menuId);
        if (menuDataPermMap == null) {
            menuDataPermMap = allMenuDataPermMap.getJSONObject(ApplicationConstant.DATA_PERM_ALL_MENU_ID);
        }
        if (menuDataPermMap == null) {
            throw new NoDataPermException(StrFormatter.format(
                    "No Related OnlineForm DataPerm found for menuId [{}] and table [{}].",
                    menuId, tableName));
        }
        if (BooleanUtil.isTrue(dataFilterProperties.getEnableMenuPermVerify())) {
            String url = ContextUtil.getHttpRequest().getHeader(ApplicationConstant.HTTP_HEADER_ORIGINAL_REQUEST_URL);
            if (StrUtil.isBlank(url)) {
                url = ContextUtil.getHttpRequest().getRequestURI();
            }
            Assert.notNull(url);
            if (!this.verifyMenuPerm(null, url, tableName) && !this.verifyMenuPerm(menuId, url, tableName)) {
                String msg = StrFormatter.format("Mismatched OnlineForm DataPerm " +
                        "for menuId [{}] and url [{}] and SQL_ID [{}].", menuId, url, tableName);
                throw new NoDataPermException(msg);
            }
        }
        return menuDataPermMap;
    }

    private Object getCachedData(String dataPermSessionKey) {
        Object cachedData = null;
        Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.DATA_PERMISSION_CACHE.name());
        if (cache == null) {
            return cachedData;
        }
        Cache.ValueWrapper wrapper = cache.get(dataPermSessionKey);
        if (wrapper == null) {
            cachedData = redissonClient.getBucket(dataPermSessionKey).get();
            if (cachedData != null) {
                cache.put(dataPermSessionKey, JSON.parseObject(cachedData.toString()));
            }
        } else {
            cachedData = wrapper.get();
        }
        return cachedData;
    }

    @SuppressWarnings("unchecked")
    private boolean verifyMenuPerm(String menuId, String url, String tableName) {
        String sessionId = TokenData.takeFromRequest().getSessionId();
        String menuPermSessionKey;
        if (menuId != null) {
            menuPermSessionKey = RedisKeyUtil.makeSessionMenuPermKey(sessionId, menuId);
        } else {
            menuPermSessionKey = RedisKeyUtil.makeSessionWhiteListPermKey(sessionId);
        }
        Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.MENU_PERM_CACHE.name());
        if (cache == null) {
            return false;
        }
        Cache.ValueWrapper wrapper = cache.get(menuPermSessionKey);
        if (wrapper != null) {
            Object cacheData = wrapper.get();
            if (cacheData != null) {
                return ((Set<String>) cacheData).contains(url);
            }
        }
        RBucket<String> bucket = redissonClient.getBucket(menuPermSessionKey);
        if (!bucket.isExists()) {
            String msg;
            if (menuId == null) {
                msg = StrFormatter.format("No Related MenuPerm found " +
                        "in Redis Cache for WHITE_LIST and tableName [{}] with sessionId [{}].", tableName, sessionId);
            } else {
                msg = StrFormatter.format("No Related MenuPerm found " +
                        "in Redis Cache for menuId [{}] and tableName[{}] with sessionId [{}].", menuId, tableName, sessionId);
            }
            throw new NoDataPermException(msg);
        }
        Set<String> cachedMenuPermSet = new HashSet<>(JSONArray.parseArray(bucket.get(), String.class));
        cache.put(menuPermSessionKey, cachedMenuPermSet);
        return cachedMenuPermSet.contains(url);
    }

    private String doProcessDataPerm(
            String tableName, String deptFilterColumnName, String userFilterColumnName, Map<Integer, String> dataPermMap) {
        List<String> criteriaList = new LinkedList<>();
        for (Map.Entry<Integer, String> entry : dataPermMap.entrySet()) {
            String filterClause = processDataPermRule(
                    tableName, deptFilterColumnName, userFilterColumnName, entry.getKey(), entry.getValue());
            if (StrUtil.isNotBlank(filterClause)) {
                criteriaList.add(filterClause);
            }
        }
        if (CollUtil.isEmpty(criteriaList)) {
            return null;
        }
        StringBuilder filterBuilder = new StringBuilder(128);
        filterBuilder.append("(");
        filterBuilder.append(CollUtil.join(criteriaList, " OR "));
        filterBuilder.append(")");
        return filterBuilder.toString();
    }

    private String processDataPermRule(
            String tableName, String deptFilterColumnName, String userFilterColumnName, Integer ruleType, String dataIds) {
        TokenData tokenData = TokenData.takeFromRequest();
        StringBuilder filter = new StringBuilder(128);
        if (ruleType != DataPermRuleType.TYPE_USER_ONLY
                && ruleType != DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT_USERS
                && ruleType != DataPermRuleType.TYPE_DEPT_USERS) {
            return this.processDeptDataPermRule(tableName, deptFilterColumnName, ruleType, dataIds);
        }
        if (StrUtil.isBlank(userFilterColumnName)) {
            log.warn("No UserFilterColumn for ONLINE table [{}] but USER_FILTER_DATA_PERM exists", tableName);
            return filter.toString();
        }
        if (BooleanUtil.isTrue(dataFilterProperties.getAddTableNamePrefix())) {
            filter.append(tableName).append(".");
        }
        if (ruleType == DataPermRuleType.TYPE_USER_ONLY) {
            filter.append(userFilterColumnName).append(" = ").append(tokenData.getUserId());
        } else {
            filter.append(userFilterColumnName)
                    .append(" IN (")
                    .append(dataIds)
                    .append(") ");
        }
        return filter.toString();
    }

    private String processDeptDataPermRule(
            String tableName, String deptFilterColumnName, Integer ruleType, String deptIds) {
        TokenData tokenData = TokenData.takeFromRequest();
        StringBuilder filter = new StringBuilder(256);
        if (StrUtil.isBlank(deptFilterColumnName)) {
            log.warn("No DeptFilterColumn for ONLINE table [{}] but DEPT_FILTER_DATA_PERM exists", tableName);
            return filter.toString();
        }
        if (ruleType == DataPermRuleType.TYPE_DEPT_ONLY) {
            if (BooleanUtil.isTrue(dataFilterProperties.getAddTableNamePrefix())) {
                filter.append(tableName).append(".");
            }
            filter.append(deptFilterColumnName).append(" = ").append(tokenData.getDeptId());
        } else if (ruleType == DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT) {
            filter.append(" EXISTS ")
                    .append("(SELECT 1 FROM ")
                    .append(dataFilterProperties.getDeptRelationTablePrefix())
                    .append("sys_dept_relation WHERE ")
                    .append(dataFilterProperties.getDeptRelationTablePrefix())
                    .append("sys_dept_relation.parent_dept_id = ")
                    .append(tokenData.getDeptId())
                    .append(AND);
            if (BooleanUtil.isTrue(dataFilterProperties.getAddTableNamePrefix())) {
                filter.append(tableName).append(".");
            }
            filter.append(deptFilterColumnName)
                    .append(" = ")
                    .append(dataFilterProperties.getDeptRelationTablePrefix())
                    .append("sys_dept_relation.dept_id) ");
        } else if (ruleType == DataPermRuleType.TYPE_MULTI_DEPT_AND_CHILD_DEPT) {
            filter.append(" EXISTS ")
                    .append("(SELECT 1 FROM ")
                    .append(dataFilterProperties.getDeptRelationTablePrefix())
                    .append("sys_dept_relation WHERE ")
                    .append(dataFilterProperties.getDeptRelationTablePrefix())
                    .append("sys_dept_relation.parent_dept_id IN (")
                    .append(deptIds)
                    .append(") AND ");
            if (BooleanUtil.isTrue(dataFilterProperties.getAddTableNamePrefix())) {
                filter.append(tableName).append(".");
            }
            filter.append(deptFilterColumnName)
                    .append(" = ")
                    .append(dataFilterProperties.getDeptRelationTablePrefix())
                    .append("sys_dept_relation.dept_id) ");
        } else if (ruleType == DataPermRuleType.TYPE_CUSTOM_DEPT_LIST) {
            if (BooleanUtil.isTrue(dataFilterProperties.getAddTableNamePrefix())) {
                filter.append(tableName).append(".");
            }
            filter.append(deptFilterColumnName).append(" IN (").append(deptIds).append(") ");
        }
        return filter.toString();
    }

    @Data
    private static class VirtualColumnWhereClause {
        private Long tableId;
        private Long columnId;
        private Integer operatorType;
        private Object value;
    }
}
