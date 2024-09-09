package com.orangeforms.common.online.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.cache.CacheConfig;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.constant.ObjectFieldType;
import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.core.object.ResponseResult;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.upload.BaseUpDownloader;
import com.orangeforms.common.core.upload.UpDownloaderFactory;
import com.orangeforms.common.core.upload.UploadResponseInfo;
import com.orangeforms.common.core.upload.UploadStoreTypeEnum;
import com.orangeforms.common.core.util.ApplicationContextHolder;
import com.orangeforms.common.online.config.OnlineProperties;
import com.orangeforms.common.online.model.*;
import com.orangeforms.common.online.model.constant.FieldKind;
import com.orangeforms.common.online.model.constant.RelationType;
import com.orangeforms.common.online.object.BaseOnlineExtendExecutor;
import com.orangeforms.common.online.object.ColumnData;
import com.orangeforms.common.online.object.OnlinePageExtra;
import com.orangeforms.common.online.service.*;
import com.orangeforms.common.redis.cache.SessionCacheHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线表单操作的通用帮助对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Component
public class OnlineOperationHelper {

    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineDatasourceRelationService onlineDatasourceRelationService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlinePageService onlinePageService;
    @Autowired
    private OnlineOperationService onlineOperationService;
    @Autowired
    private OnlineProperties onlineProperties;
    @Autowired
    private UpDownloaderFactory upDownloaderFactory;
    @Autowired
    private SessionCacheHelper cacheHelper;
    @Resource(name = "caffeineCacheManager")
    private CacheManager cacheManager;

    /**
     * 验证并获取数据源数据。
     *
     * @param datasourceId 数据源Id。
     * @return 数据源详情数据。
     */
    public ResponseResult<OnlineDatasource> verifyAndGetDatasource(Long datasourceId) {
        String errorMessage;
        OnlineDatasource datasource = onlineDatasourceService.getOnlineDatasourceFromCache(datasourceId);
        if (datasource == null) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!StrUtil.equals(datasource.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            errorMessage = "数据验证失败，当前应用不包含该数据源Id";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineTable masterTable = onlineTableService.getOnlineTableFromCache(datasource.getMasterTableId());
        if (masterTable == null) {
            errorMessage = "数据验证失败，数据源主表Id不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        datasource.setMasterTable(masterTable);
        return ResponseResult.success(datasource);
    }

    /**
     * 验证并获取数据源的关联数据。
     *
     * @param datasourceId 数据源Id。
     * @param relationId   数据源关联Id。
     * @return 数据源的关联详情数据。
     */
    public ResponseResult<OnlineDatasourceRelation> verifyAndGetRelation(Long datasourceId, Long relationId) {
        String errorMessage;
        OnlineDatasourceRelation relation =
                onlineDatasourceRelationService.getOnlineDatasourceRelationFromCache(datasourceId, relationId);
        if (relation == null || !relation.getDatasourceId().equals(datasourceId)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (!StrUtil.equals(relation.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            errorMessage = "数据验证失败，当前应用不包含该数据源关联Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineTable slaveTable = onlineTableService.getOnlineTableFromCache(relation.getSlaveTableId());
        if (slaveTable == null) {
            errorMessage = "数据验证失败，数据源关联 [" + relation.getRelationName() + " ] 引用的从表不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        relation.setSlaveTable(slaveTable);
        relation.setSlaveColumn(slaveTable.getColumnMap().get(relation.getSlaveColumnId()));
        return ResponseResult.success(relation);
    }

    /**
     * 验证并获取数据源的指定类型关联数据。
     *
     * @param datasourceId 数据源Id。
     * @param relationType 数据源关联类型。
     * @return 数据源指定关联类型的关联数据详情列表。
     */
    public ResponseResult<List<OnlineDatasourceRelation>> verifyAndGetRelationList(
            Long datasourceId, Integer relationType) {
        String errorMessage;
        List<OnlineDatasourceRelation> relationList = onlineDatasourceRelationService
                .getOnlineDatasourceRelationListFromCache(CollUtil.newHashSet(datasourceId));
        if (relationType != null) {
            relationList = relationList.stream()
                    .filter(r -> r.getRelationType().equals(relationType)).collect(Collectors.toList());
        }
        for (OnlineDatasourceRelation relation : relationList) {
            OnlineTable slaveTable = onlineTableService.getOnlineTableFromCache(relation.getSlaveTableId());
            if (slaveTable == null) {
                errorMessage = "数据验证失败，数据源关联 [" + relation.getRelationName() + "] 的从表Id不存在！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            relation.setSlaveTable(slaveTable);
        }
        return ResponseResult.success(relationList);
    }

    /**
     * 构建在线表的数据记录。
     *
     * @param table             在线数据表对象。
     * @param tableData         在线数据表数据。
     * @param forUpdate         是否为更新。
     * @param ignoreSetColumnId 忽略设置的字段Id。
     * @return 在线表的数据记录。
     */
    public ResponseResult<List<ColumnData>> buildTableData(
            OnlineTable table, JSONObject tableData, boolean forUpdate, Long ignoreSetColumnId) {
        List<ColumnData> columnDataList = new LinkedList<>();
        String errorMessage;
        for (OnlineColumn column : table.getColumnMap().values()) {
            // 判断一下是否为需要自动填入的字段，如果是，这里就都暂时给空值了，后续操作会自动填补。
            // 这里还能避免一次基于tableData的查询，能快几纳秒也是好的。
            if (this.isAutoSettingField(column) || ObjectUtil.equal(column.getColumnId(), ignoreSetColumnId)) {
                columnDataList.add(new ColumnData(column, null));
                continue;
            }
            Object value = this.getColumnValue(tableData, column);
            // 对于主键数据的处理。
            if (BooleanUtil.isTrue(column.getPrimaryKey())) {
                // 如果是更新则必须包含主键参数。
                if (forUpdate && value == null) {
                    errorMessage = "数据验证失败，数据表 ["
                            + table.getTableName() + "] 主键字段 [" + column.getColumnName() + "] 不能为空值！";
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
                }
            } else {
                if (value == null && !column.getNullable() && StrUtil.isBlank(column.getEncodedRule())) {
                    errorMessage = "数据验证失败，数据表 ["
                            + table.getTableName() + "] 字段 [" + column.getColumnName() + "] 不能为空值！";
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
                }
            }
            columnDataList.add(new ColumnData(column, value));
        }
        return ResponseResult.success(columnDataList);
    }

    /**
     * 构建多个一对多从表的数据列表。
     *
     * @param datasourceId 数据源Id。
     * @param slaveData    多个一对多从表数据的JSON对象。
     * @return 构建后的多个一对多从表数据列表。
     */
    public ResponseResult<Map<OnlineDatasourceRelation, List<JSONObject>>> buildSlaveDataList(
            Long datasourceId, JSONObject slaveData) {
        if (slaveData == null) {
            return ResponseResult.success(null);
        }
        Map<OnlineDatasourceRelation, List<JSONObject>> relationDataMap = new HashMap<>(slaveData.size());
        for (String key : slaveData.keySet()) {
            Long relationId = Long.parseLong(key);
            ResponseResult<OnlineDatasourceRelation> relationResult = this.verifyAndGetRelation(datasourceId, relationId);
            if (!relationResult.isSuccess()) {
                return ResponseResult.errorFrom(relationResult);
            }
            OnlineDatasourceRelation relation = relationResult.getData();
            List<JSONObject> relationDataList = new LinkedList<>();
            relationDataMap.put(relation, relationDataList);
            if (relation.getRelationType().equals(RelationType.ONE_TO_MANY)) {
                JSONArray slaveObjectArray = slaveData.getJSONArray(key);
                for (int i = 0; i < slaveObjectArray.size(); i++) {
                    relationDataList.add(slaveObjectArray.getJSONObject(i));
                }
            } else if (relation.getRelationType().equals(RelationType.ONE_TO_ONE)) {
                JSONObject o = slaveData.getJSONObject(key);
                if (MapUtil.isNotEmpty(o)) {
                    relationDataList.add(o);
                }
            }
        }
        return ResponseResult.success(relationDataMap);
    }

    /**
     * 将字符型字段值转换为与参数字段类型匹配的字段值。
     *
     * @param column 在线表单字段。
     * @param dataId 字符型字段值。
     * @return 转换后与参数字段类型匹配的字段值。
     */
    public Serializable convertToTypeValue(OnlineColumn column, String dataId) {
        if (dataId == null) {
            return null;
        }
        if (column == null) {
            return dataId;
        }
        if ("Long".equals(column.getObjectFieldType())) {
            return Long.valueOf(dataId);
        } else if ("Integer".equals(column.getObjectFieldType())) {
            return Integer.valueOf(dataId);
        }
        return dataId;
    }

    /**
     * 将字符型字段值集合转换为与参数字段类型匹配的字段值集合。
     *
     * @param column    在线表单字段。
     * @param dataIdSet 字符型字段值集合。
     * @return 转换后与参数字段类型匹配的字段值集合。
     */
    public Set<Serializable> convertToTypeValue(OnlineColumn column, Set<String> dataIdSet) {
        Set<Serializable> resultSet = new HashSet<>();
        if (dataIdSet == null) {
            return resultSet;
        }
        if ("Long".equals(column.getObjectFieldType())) {
            return dataIdSet.stream().map(Long::valueOf).collect(Collectors.toSet());
        } else if ("Integer".equals(column.getObjectFieldType())) {
            return dataIdSet.stream().map(Integer::valueOf).collect(Collectors.toSet());
        } else {
            resultSet.addAll(dataIdSet);
        }
        return resultSet;
    }

    /**
     * 下载数据。
     *
     * @param table     在线表对象。
     * @param dataId    在线表数据主键Id。
     * @param fieldName 数据表字段名。
     * @param filename  下载文件名。
     * @param asImage   是否为图片。
     * @param response  HTTP 应对对象。
     */
    public void doDownload(
            OnlineTable table, String dataId, String fieldName, String filename, Boolean asImage, HttpServletResponse response) {
        // 使用try来捕获异常，是为了保证一旦出现异常可以返回500的错误状态，便于调试。
        // 否则有可能给前端返回的是200的错误码。
        try {
            // 如果请求参数中没有包含主键Id，就判断该文件是否为当前session上传的。
            if (ObjectUtil.isEmpty(dataId)) {
                if (!cacheHelper.existSessionUploadFile(filename)) {
                    ResponseResult.output(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else {
                Map<String, Object> dataMap =
                        onlineOperationService.getMasterData(table, null, null, dataId);
                if (dataMap == null) {
                    ResponseResult.output(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                String fieldJsonData = (String) dataMap.get(fieldName);
                if (!this.canDownload(fieldJsonData, filename)) {
                    ResponseResult.output(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            ResponseResult<OnlineColumn> verifyResult = this.doVerifyUpDownloadFileColumn(table, fieldName, asImage);
            if (!verifyResult.isSuccess()) {
                ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, verifyResult);
                return;
            }
            OnlineColumn downloadColumn = verifyResult.getData();
            if (downloadColumn.getUploadFileSystemType() == null) {
                downloadColumn.setUploadFileSystemType(UploadStoreTypeEnum.LOCAL_SYSTEM.ordinal());
            }
            if (!downloadColumn.getUploadFileSystemType().equals(UploadStoreTypeEnum.LOCAL_SYSTEM.ordinal())) {
                downloadColumn.setUploadFileSystemType(onlineProperties.getDistributeStoreType());
            }
            UploadStoreTypeEnum uploadStoreType =
                    UploadStoreTypeEnum.values()[downloadColumn.getUploadFileSystemType()];
            BaseUpDownloader upDownloader = upDownloaderFactory.get(uploadStoreType);
            upDownloader.doDownload(onlineProperties.getUploadFileBaseDir(),
                    table.getModelName(), fieldName, filename, asImage, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 上传数据。
     *
     * @param table      在线表对象。
     * @param fieldName  数据表字段名。
     * @param asImage    是否为图片。
     * @param uploadFile 上传的文件。
     */
    public void doUpload(OnlineTable table, String fieldName, Boolean asImage, MultipartFile uploadFile)
            throws IOException {
        ResponseResult<OnlineColumn> verifyResult = this.doVerifyUpDownloadFileColumn(table, fieldName, asImage);
        if (!verifyResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, verifyResult);
            return;
        }
        OnlineColumn uploadColumn = verifyResult.getData();
        if (uploadColumn.getUploadFileSystemType() == null) {
            uploadColumn.setUploadFileSystemType(UploadStoreTypeEnum.LOCAL_SYSTEM.ordinal());
        }
        if (!uploadColumn.getUploadFileSystemType().equals(UploadStoreTypeEnum.LOCAL_SYSTEM.ordinal())) {
            uploadColumn.setUploadFileSystemType(onlineProperties.getDistributeStoreType());
        }
        UploadStoreTypeEnum uploadStoreType = UploadStoreTypeEnum.values()[uploadColumn.getUploadFileSystemType()];
        BaseUpDownloader upDownloader = upDownloaderFactory.get(uploadStoreType);
        UploadResponseInfo responseInfo = upDownloader.doUpload(null,
                onlineProperties.getUploadFileBaseDir(), table.getModelName(), fieldName, asImage, uploadFile);
        if (BooleanUtil.isTrue(responseInfo.getUploadFailed())) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN,
                    ResponseResult.error(ErrorCodeEnum.UPLOAD_FAILED, responseInfo.getErrorMessage()));
            return;
        }
        // 动态表单的下载url和普通表单有所不同，由前端负责动态拼接。
        responseInfo.setDownloadUri(null);
        cacheHelper.putSessionUploadFile(responseInfo.getFilename());
        ResponseResult.output(ResponseResult.success(responseInfo));
    }

    /**
     * 将与指定数据源Id关联的OnlinePage对象中，配置的在线表单后台扩展执行器对象写入本地现成。
     *
     * @param datasourceId 数据源Id。
     */
    public void enableOnlineExtendExecutor(Long datasourceId) {
        Cache cache = cacheManager.getCache(CacheConfig.CacheEnum.ONLINE_EXTEND_EXECUTOR_CACHE.name());
        Assert.notNull(cache, "Cache ONLINE_EXTEND_EXECUTOR_CACHE can't be NULL");
        BaseOnlineExtendExecutor executor = cache.get(datasourceId, BaseOnlineExtendExecutor.class);
        if (executor != null) {
            OnlineExtendExecutorUtil.setOnlineExtendExecutorToLocal(executor);
        }
        OnlinePage page = onlinePageService.getOnlinePageListByDatasourceId(datasourceId).get(0);
        if (StrUtil.isNotBlank(page.getExtraJson())) {
            OnlinePageExtra pageExtra = JSON.parseObject(page.getExtraJson(), OnlinePageExtra.class);
            if (StrUtil.isNotBlank(pageExtra.getExtendClass())) {
                try {
                    Object extendClass = ApplicationContextHolder.getBean(Class.forName(pageExtra.getExtendClass()));
                    if (!(extendClass instanceof BaseOnlineExtendExecutor)) {
                        throw new MyRuntimeException("在线表单扩展类没有实现 [BaseOnlineExtendExecutor] 接口！");
                    }
                    executor = (BaseOnlineExtendExecutor) extendClass;
                } catch (ClassNotFoundException e) {
                    throw new MyRuntimeException("在线表单扩展类没有实现 [BaseOnlineExtendExecutor] 接口！");
                }
            }
        }
        cache.put(datasourceId, executor);
        OnlineExtendExecutorUtil.setOnlineExtendExecutorToLocal(executor);
    }

    private ResponseResult<OnlineColumn> doVerifyUpDownloadFileColumn(
            OnlineTable table, String fieldName, Boolean asImage) {
        OnlineColumn column = this.getOnlineColumnByName(table, fieldName);
        if (column == null) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_DATA_FIELD);
        }
        if (BooleanUtil.isTrue(asImage)) {
            if (ObjectUtil.notEqual(column.getFieldKind(), FieldKind.UPLOAD_IMAGE)) {
                return ResponseResult.error(ErrorCodeEnum.INVALID_UPLOAD_FIELD);
            }
        } else {
            if (ObjectUtil.notEqual(column.getFieldKind(), FieldKind.UPLOAD)) {
                return ResponseResult.error(ErrorCodeEnum.INVALID_UPLOAD_FIELD);
            }
        }
        return ResponseResult.success(column);
    }

    private OnlineColumn getOnlineColumnByName(OnlineTable table, String fieldName) {
        for (OnlineColumn column : table.getColumnMap().values()) {
            if (column.getColumnName().equals(fieldName)) {
                return column;
            }
        }
        return null;
    }

    private Object getColumnValue(JSONObject tableData, OnlineColumn column) {
        Object value = tableData.get(column.getColumnName());
        if (value != null) {
            if (ObjectFieldType.LONG.equals(column.getObjectFieldType())) {
                value = Long.valueOf(value.toString());
            } else if (ObjectFieldType.DATE.equals(column.getObjectFieldType())) {
                value = Convert.toLocalDateTime(value);
            }
        }
        return value;
    }

    private boolean isAutoSettingField(OnlineColumn column) {
        return ObjectUtil.equal(column.getFieldKind(), FieldKind.CREATE_TIME)
                || ObjectUtil.equal(column.getFieldKind(), FieldKind.CREATE_USER_ID)
                || ObjectUtil.equal(column.getFieldKind(), FieldKind.UPDATE_TIME)
                || ObjectUtil.equal(column.getFieldKind(), FieldKind.UPDATE_USER_ID)
                || ObjectUtil.equal(column.getFieldKind(), FieldKind.CREATE_DEPT_ID)
                || ObjectUtil.equal(column.getFieldKind(), FieldKind.LOGIC_DELETE);
    }

    private boolean canDownload(String fieldJsonData, String filename) {
        if (fieldJsonData == null && !cacheHelper.existSessionUploadFile(filename)) {
            return false;
        }
        return BaseUpDownloader.containFile(fieldJsonData, filename)
                || cacheHelper.existSessionUploadFile(filename);
    }
}
