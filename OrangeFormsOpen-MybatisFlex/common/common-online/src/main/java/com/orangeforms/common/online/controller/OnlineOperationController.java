package com.orangeforms.common.online.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.*;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.constant.DictType;
import com.orangeforms.common.core.constant.ObjectFieldType;
import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.*;
import com.orangeforms.common.dict.model.GlobalDictItem;
import com.orangeforms.common.dict.service.GlobalDictService;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import com.orangeforms.common.redis.cache.SessionCacheHelper;
import com.orangeforms.common.redis.util.CommonRedisUtil;
import com.orangeforms.common.online.config.OnlineProperties;
import com.orangeforms.common.online.exception.OnlineRuntimeException;
import com.orangeforms.common.online.util.OnlineOperationHelper;
import com.orangeforms.common.online.util.OnlineConstant;
import com.orangeforms.common.online.dto.OnlineFilterDto;
import com.orangeforms.common.online.model.*;
import com.orangeforms.common.online.model.constant.FieldKind;
import com.orangeforms.common.online.model.constant.RelationType;
import com.orangeforms.common.online.model.constant.FieldFilterType;
import com.orangeforms.common.online.service.*;
import com.orangeforms.common.satoken.annotation.SaTokenDenyAuth;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线表单数据操作接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "在线表单数据操作接口")
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineOperation")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineOperationController {

    @Autowired
    private OnlineOperationService onlineOperationService;
    @Autowired
    private OnlineDictService onlineDictService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineDatasourceRelationService onlineDatasourceRelationService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineOperationHelper onlineOperationHelper;
    @Autowired
    private OnlineVirtualColumnService onlineVirtualColumnService;
    @Autowired
    private OnlineProperties onlineProperties;
    @Autowired
    private GlobalDictService globalDictService;
    @Autowired
    private CommonRedisUtil commonRedisUtil;
    @Autowired
    private SessionCacheHelper sessionCacheHelper;

    /**
     * 新增数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表的数据源Id。
     * @param masterData             主表新增数据。
     * @param slaveData              一对多从表新增数据列表。
     * @return 应答结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/addDatasource/{datasourceVariableName}")
    public ResponseResult<Void> addDatasource(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) JSONObject masterData,
            @MyRequestBody JSONObject slaveData) {
        // 验证数据源的合法性，同时获取主表对象。
        ResponseResult<OnlineDatasource> datasourceResult = onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            return ResponseResult.errorFrom(datasourceResult);
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ContextUtil.getHttpResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION);
        }
        OnlineTable masterTable = datasource.getMasterTable();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        if (slaveData == null) {
            onlineOperationService.saveNew(masterTable, masterData);
        } else {
            ResponseResult<Map<OnlineDatasourceRelation, List<JSONObject>>> slaveDataListResult =
                    onlineOperationHelper.buildSlaveDataList(datasourceId, slaveData);
            if (!slaveDataListResult.isSuccess()) {
                return ResponseResult.errorFrom(slaveDataListResult);
            }
            onlineOperationService.saveNewWithRelation(masterTable, masterData, slaveDataListResult.getData());
        }
        return ResponseResult.success();
    }

    /**
     * 新增一对多从表数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表的数据源Id。
     * @param relationId             一对多的关联Id。
     * @param slaveData              一对多从表的新增数据列表。
     * @return 应答结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/addOneToManyRelation/{datasourceVariableName}")
    public ResponseResult<Void> addOneToManyRelation(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) Long relationId,
            @MyRequestBody(required = true) JSONObject slaveData) {
        ResponseResult<OnlineDatasourceRelation> verifyResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        onlineOperationService.saveNew(verifyResult.getData().getSlaveTable(), slaveData);
        return ResponseResult.success();
    }

    /**
     * 更新主数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表数据源Id。
     * @param masterData             表数据。这里没有包含的字段将视为NULL。
     * @param slaveData              从表数据，key是relationId。
     * @return 应该结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/updateDatasource/{datasourceVariableName}")
    public ResponseResult<Void> updateDatasource(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) JSONObject masterData,
            @MyRequestBody JSONObject slaveData) {
        ResponseResult<OnlineDatasource> datasourceResult =
                onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            return ResponseResult.errorFrom(datasourceResult);
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ContextUtil.getHttpResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION);
        }
        OnlineTable masterTable = datasource.getMasterTable();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        if (slaveData == null) {
            if (!onlineOperationService.update(masterTable, masterData)) {
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
            }
        } else {
            ResponseResult<Map<OnlineDatasourceRelation, List<JSONObject>>> slaveDataListResult =
                    onlineOperationHelper.buildSlaveDataList(datasourceId, slaveData);
            if (!slaveDataListResult.isSuccess()) {
                return ResponseResult.errorFrom(slaveDataListResult);
            }
            onlineOperationService.updateWithRelation(
                    masterTable, masterData, datasourceId, slaveDataListResult.getData());
        }
        return ResponseResult.success();
    }

    /**
     * 更新一对多关联数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表数据源Id。
     * @param relationId             一对多关联Id。
     * @param slaveData              一对多关联从表数据。这里没有包含的字段将视为NULL。
     * @return 应该结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/updateOneToManyRelation/{datasourceVariableName}")
    public ResponseResult<Void> updateOneToManyRelation(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) Long relationId,
            @MyRequestBody(required = true) JSONObject slaveData) {
        ResponseResult<OnlineDatasourceRelation> verifyResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineTable slaveTable = verifyResult.getData().getSlaveTable();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        if (!onlineOperationService.update(slaveTable, slaveData)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除主数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表数据源Id。
     * @param dataId                 待删除的数据表主键Id。
     * @return 应该结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/deleteDatasource/{datasourceVariableName}")
    public ResponseResult<Void> deleteDatasource(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) String dataId) {
        return this.doDelete(datasourceVariableName, datasourceId, CollUtil.newArrayList(dataId));
    }

    /**
     * 批量删除主数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表数据源Id。
     * @param dataIdList             待删除的数据表主键Id列表。
     * @return 应该结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.DELETE_BATCH)
    @PostMapping("/deleteBatchDatasource/{datasourceVariableName}")
    public ResponseResult<Void> deleteBatchDatasource(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) List<String> dataIdList) {
        return this.doDelete(datasourceVariableName, datasourceId, dataIdList);
    }

    /**
     * 删除一对多关联表单条数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表数据源Id。
     * @param relationId             一对多关联Id。
     * @param dataId                 一对多关联表主键Id。
     * @return 应该结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/deleteOneToManyRelation/{datasourceVariableName}")
    public ResponseResult<Void> deleteOneToManyRelation(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) Long relationId,
            @MyRequestBody(required = true) String dataId) {
        return this.doDelete(datasourceVariableName, datasourceId, relationId, CollUtil.newArrayList(dataId));
    }

    /**
     * 批量删除一对多关联表单条数据接口。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           主表数据源Id。
     * @param relationId             一对多关联Id。
     * @param dataIdList             一对多关联表主键Id列表。
     * @return 应该结果。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.DELETE_BATCH)
    @PostMapping("/deleteBatchOneToManyRelation/{datasourceVariableName}")
    public ResponseResult<Void> deleteBatchOneToManyRelation(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) Long relationId,
            @MyRequestBody(required = true) List<String> dataIdList) {
        return this.doDelete(datasourceVariableName, datasourceId, relationId, dataIdList);
    }

    /**
     * 根据数据源Id为动态表单查询数据详情。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param dataId                 数据主键Id。
     * @return 详情结果。
     */
    @SaTokenDenyAuth
    @GetMapping("/viewByDatasourceId/{datasourceVariableName}")
    public ResponseResult<Map<String, Object>> viewByDatasourceId(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @RequestParam Long datasourceId,
            @RequestParam String dataId) {
        // 验证数据源及其关联
        ResponseResult<OnlineDatasource> datasourceResult =
                this.doVerifyAndGetDatasource(datasourceId, datasourceVariableName);
        if (!datasourceResult.isSuccess()) {
            return ResponseResult.errorFrom(datasourceResult);
        }
        OnlineDatasource datasource = datasourceResult.getData();
        ResponseResult<List<OnlineDatasourceRelation>> relationListResult =
                onlineOperationHelper.verifyAndGetRelationList(datasourceId, null);
        if (!relationListResult.isSuccess()) {
            return ResponseResult.errorFrom(relationListResult);
        }
        List<OnlineDatasourceRelation> allRelationList = relationListResult.getData();
        List<OnlineDatasourceRelation> oneToOneRelationList = allRelationList.stream()
                .filter(r -> r.getRelationType().equals(RelationType.ONE_TO_ONE)).collect(Collectors.toList());
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        Map<String, Object> result = onlineOperationService.getMasterData(
                datasource.getMasterTable(), oneToOneRelationList, allRelationList, dataId);
        return ResponseResult.success(result);
    }

    /**
     * 根据数据源关联Id为动态表单查询数据详情。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param relationId             一对多关联Id。
     * @param dataId                 一对多关联数据主键Id。
     * @return 详情结果。
     */
    @SaTokenDenyAuth
    @GetMapping("/viewByOneToManyRelationId/{datasourceVariableName}")
    public ResponseResult<Map<String, Object>> viewByOneToManyRelationId(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @RequestParam Long datasourceId,
            @RequestParam Long relationId,
            @RequestParam String dataId) {
        ResponseResult<OnlineDatasourceRelation> verifyResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        Map<String, Object> result = onlineOperationService.getSlaveData(verifyResult.getData(), dataId);
        return ResponseResult.success(result);
    }

    /**
     * 为数据源主表字段下载文件。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param dataId                 附件所在记录的主键Id。
     * @param fieldName              数据表字段名。
     * @param asImage                是否为图片文件。
     * @param response               Http 应答对象。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.DOWNLOAD, saveResponse = false)
    @GetMapping("/downloadDatasource/{datasourceVariableName}")
    public void downloadDatasource(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @RequestParam Long datasourceId,
            @RequestParam(required = false) String dataId,
            @RequestParam String fieldName,
            @RequestParam String filename,
            @RequestParam Boolean asImage,
            HttpServletResponse response) throws IOException {
        if (MyCommonUtil.existBlankArgument(fieldName, filename, asImage)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ResponseResult<OnlineDatasource> datasourceResult =
                onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, ResponseResult.errorFrom(datasourceResult));
            return;
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN,
                    ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION));
            return;
        }
        OnlineTable masterTable = datasource.getMasterTable();
        onlineOperationHelper.doDownload(masterTable, dataId, fieldName, filename, asImage, response);
    }

    /**
     * 为数据源一对多关联的从表字段下载文件。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param relationId             数据源的一对多关联Id。
     * @param dataId                 附件所在记录的主键Id。
     * @param fieldName              数据表字段名。
     * @param asImage                是否为图片文件。
     * @param response               Http 应答对象。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.DOWNLOAD, saveResponse = false)
    @GetMapping("/downloadOneToManyRelation/{datasourceVariableName}")
    public void downloadOneToManyRelation(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @RequestParam Long datasourceId,
            @RequestParam Long relationId,
            @RequestParam(required = false) String dataId,
            @RequestParam String fieldName,
            @RequestParam String filename,
            @RequestParam Boolean asImage,
            HttpServletResponse response) throws IOException {
        ResponseResult<OnlineDatasourceRelation> relationResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!relationResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, ResponseResult.errorFrom(relationResult));
            return;
        }
        OnlineTable slaveTable = relationResult.getData().getSlaveTable();
        onlineOperationHelper.doDownload(slaveTable, dataId, fieldName, filename, asImage, response);
    }

    /**
     * 为数据源主表字段上传文件。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param fieldName              数据表字段名。
     * @param asImage                是否为图片文件。
     * @param uploadFile             上传文件对象。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.UPLOAD, saveResponse = false)
    @PostMapping("/uploadDatasource/{datasourceVariableName}")
    public void uploadDatasource(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @RequestParam Long datasourceId,
            @RequestParam String fieldName,
            @RequestParam Boolean asImage,
            @RequestParam("uploadFile") MultipartFile uploadFile) throws IOException {
        ResponseResult<OnlineDatasource> datasourceResult =
                onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, ResponseResult.errorFrom(datasourceResult));
            return;
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN,
                    ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION));
            return;
        }
        OnlineTable masterTable = datasource.getMasterTable();
        onlineOperationHelper.doUpload(masterTable, fieldName, asImage, uploadFile);
    }

    /**
     * 为数据源一对多关联的从表字段上传文件。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param relationId             数据源的一对多关联Id。
     * @param fieldName              数据表字段名。
     * @param asImage                是否为图片文件。
     * @param uploadFile             上传文件对象。
     */
    @SaTokenDenyAuth
    @OperationLog(type = SysOperationLogType.UPLOAD, saveResponse = false)
    @PostMapping("/uploadOneToManyRelation/{datasourceVariableName}")
    public void uploadOneToManyRelation(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @RequestParam Long datasourceId,
            @RequestParam Long relationId,
            @RequestParam String fieldName,
            @RequestParam Boolean asImage,
            @RequestParam("uploadFile") MultipartFile uploadFile) throws IOException {
        ResponseResult<OnlineDatasourceRelation> relationResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!relationResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, ResponseResult.errorFrom(relationResult));
            return;
        }
        OnlineTable slaveTable = relationResult.getData().getSlaveTable();
        onlineOperationHelper.doUpload(slaveTable, fieldName, asImage, uploadFile);
    }

    /**
     * 根据数据源Id，以及接口参数，为动态表单查询数据列表。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param filterDtoList          多虑数据对象列表。
     * @param orderParam             排序对象。
     * @param pageParam              分页对象。
     */
    @SaTokenDenyAuth
    @PostMapping("/listByDatasourceId/{datasourceVariableName}")
    public ResponseResult<MyPageData<Map<String, Object>>> listByDatasourceId(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody List<OnlineFilterDto> filterDtoList,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        // 1. 验证数据源及其关联
        ResponseResult<OnlineDatasource> datasourceResult =
                this.doVerifyAndGetDatasource(datasourceId, datasourceVariableName);
        if (!datasourceResult.isSuccess()) {
            return ResponseResult.errorFrom(datasourceResult);
        }
        OnlineTable masterTable = datasourceResult.getData().getMasterTable();
        ResponseResult<List<OnlineDatasourceRelation>> relationListResult =
                onlineOperationHelper.verifyAndGetRelationList(datasourceId, null);
        if (!relationListResult.isSuccess()) {
            return ResponseResult.errorFrom(relationListResult);
        }
        List<OnlineDatasourceRelation> allRelationList = relationListResult.getData();
        // 2. 验证数据过滤对象中的表名和字段，确保没有sql注入。
        ResponseResult<Void> filterDtoListResult = this.verifyFilterDtoList(filterDtoList);
        if (!filterDtoListResult.isSuccess()) {
            return ResponseResult.errorFrom(filterDtoListResult);
        }
        // 3. 解析排序参数，同时确保没有sql注入。
        Map<String, OnlineTable> tableMap = new HashMap<>(4);
        tableMap.put(masterTable.getTableName(), masterTable);
        List<OnlineDatasourceRelation> oneToOneRelationList = relationListResult.getData().stream()
                .filter(r -> r.getRelationType().equals(RelationType.ONE_TO_ONE)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(oneToOneRelationList)) {
            Map<String, OnlineTable> relationTableMap = oneToOneRelationList.stream()
                    .map(OnlineDatasourceRelation::getSlaveTable).collect(Collectors.toMap(OnlineTable::getTableName, c -> c));
            tableMap.putAll(relationTableMap);
        }
        ResponseResult<String> orderByResult = this.makeOrderBy(orderParam, masterTable, tableMap);
        if (!orderByResult.isSuccess()) {
            return ResponseResult.errorFrom(orderByResult);
        }
        String orderBy = orderByResult.getData();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        MyPageData<Map<String, Object>> pageData = onlineOperationService.getMasterDataList(
                masterTable, oneToOneRelationList, allRelationList, filterDtoList, orderBy, pageParam);
        return ResponseResult.success(pageData);
    }

    /**
     * 根据数据源Id，以及接口参数，为动态表单导出数据列表。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param filterDtoList          多虑数据对象列表。
     * @param orderParam             排序对象。
     * @param exportInfoList         导出字段信息列表。
     */
    @SaTokenDenyAuth
    @PostMapping("/exportByDatasourceId/{datasourceVariableName}")
    public void exportByDatasourceId(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody List<OnlineFilterDto> filterDtoList,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody(required = true) List<ExportInfo> exportInfoList) throws IOException {
        // 1. 验证数据源及其关联
        ResponseResult<OnlineDatasource> datasourceResult =
                onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, datasourceResult);
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN);
        }
        OnlineTable masterTable = datasource.getMasterTable();
        ResponseResult<List<OnlineDatasourceRelation>> relationListResult =
                onlineOperationHelper.verifyAndGetRelationList(datasourceId, null);
        if (!relationListResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, relationListResult);
        }
        List<OnlineDatasourceRelation> allRelationList = relationListResult.getData();
        // 2. 验证数据过滤对象中的表名和字段，确保没有sql注入。
        ResponseResult<Void> filterDtoListResult = this.verifyFilterDtoList(filterDtoList);
        if (!filterDtoListResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, filterDtoListResult);
        }
        // 3. 解析排序参数，同时确保没有sql注入。
        Map<String, OnlineTable> tableMap = new HashMap<>(4);
        tableMap.put(masterTable.getTableName(), masterTable);
        List<OnlineDatasourceRelation> oneToOneRelationList = relationListResult.getData().stream()
                .filter(r -> r.getRelationType().equals(RelationType.ONE_TO_ONE)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(oneToOneRelationList)) {
            Map<String, OnlineTable> relationTableMap = oneToOneRelationList.stream()
                    .map(OnlineDatasourceRelation::getSlaveTable).collect(Collectors.toMap(OnlineTable::getTableName, c -> c));
            tableMap.putAll(relationTableMap);
        }
        ResponseResult<String> orderByResult = this.makeOrderBy(orderParam, masterTable, tableMap);
        if (!orderByResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, orderByResult);
        }
        String orderBy = orderByResult.getData();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        MyPageData<Map<String, Object>> pageData = onlineOperationService.getMasterDataList(
                masterTable, oneToOneRelationList, allRelationList, filterDtoList, orderBy, null);
        Map<String, String> headerMap = this.makeExportHeaderMap(masterTable, allRelationList, exportInfoList);
        if (MapUtil.isEmpty(headerMap)) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST,
                    ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，没有指定导出头信息！"));
            return;
        }
        this.normalizeExportDataList(pageData.getDataList());
        String filename = datasourceVariableName + "-" + MyDateUtil.toDateTimeString(DateTime.now()) + ".xlsx";
        ExportUtil.doExport(pageData.getDataList(), headerMap, filename);
    }

    /**
     * 根据数据源Id和数据源关联Id，以及接口参数，为动态表单查询该一对多关联的数据列表。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param relationId             数据源的一对多关联Id。
     * @param filterDtoList          多虑数据对象列表。
     * @param orderParam             排序对象。
     * @param pageParam              分页对象。
     * @return 查询结果。
     */
    @SaTokenDenyAuth
    @PostMapping("/listByOneToManyRelationId/{datasourceVariableName}")
    public ResponseResult<MyPageData<Map<String, Object>>> listByOneToManyRelationId(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) Long relationId,
            @MyRequestBody List<OnlineFilterDto> filterDtoList,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<OnlineDatasourceRelation> verifyResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDatasourceRelation relation = verifyResult.getData();
        OnlineTable slaveTable = relation.getSlaveTable();
        // 验证数据过滤对象中的表名和字段，确保没有sql注入。
        ResponseResult<Void> filterDtoListResult = this.verifyFilterDtoList(filterDtoList);
        if (!filterDtoListResult.isSuccess()) {
            return ResponseResult.errorFrom(filterDtoListResult);
        }
        Map<String, OnlineTable> tableMap = new HashMap<>(1);
        tableMap.put(slaveTable.getTableName(), slaveTable);
        if (CollUtil.isNotEmpty(orderParam)) {
            for (MyOrderParam.OrderInfo orderInfo : orderParam) {
                orderInfo.setFieldName(StrUtil.removePrefix(orderInfo.getFieldName(),
                        relation.getVariableName() + OnlineConstant.RELATION_TABLE_COLUMN_SEPARATOR));
            }
        }
        ResponseResult<String> orderByResult = this.makeOrderBy(orderParam, slaveTable, tableMap);
        if (!orderByResult.isSuccess()) {
            return ResponseResult.errorFrom(orderByResult);
        }
        String orderBy = orderByResult.getData();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        MyPageData<Map<String, Object>> pageData =
                onlineOperationService.getSlaveDataList(relation, filterDtoList, orderBy, pageParam);
        return ResponseResult.success(pageData);
    }

    /**
     * 根据数据源Id和数据源关联Id，以及接口参数，为动态表单查询该一对多关联的数据列表。
     *
     * @param datasourceVariableName 数据源名称。
     * @param datasourceId           数据源Id。
     * @param relationId             数据源的一对多关联Id。
     * @param filterDtoList          多虑数据对象列表。
     * @param orderParam             排序对象。
     * @param exportInfoList         导出字段信息列表。
     */
    @SaTokenDenyAuth
    @PostMapping("/exportByOneToManyRelationId/{datasourceVariableName}")
    public void exportByOneToManyRelationId(
            @PathVariable("datasourceVariableName") String datasourceVariableName,
            @MyRequestBody(required = true) Long datasourceId,
            @MyRequestBody(required = true) Long relationId,
            @MyRequestBody List<OnlineFilterDto> filterDtoList,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody(required = true) List<ExportInfo> exportInfoList) throws IOException {
        ResponseResult<OnlineDatasourceRelation> relationResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!relationResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, relationResult);
            return;
        }
        OnlineDatasourceRelation relation = relationResult.getData();
        OnlineTable slaveTable = relation.getSlaveTable();
        // 验证数据过滤对象中的表名和字段，确保没有sql注入。
        ResponseResult<Void> filterDtoListResult = this.verifyFilterDtoList(filterDtoList);
        if (!filterDtoListResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, filterDtoListResult);
            return;
        }
        Map<String, OnlineTable> tableMap = new HashMap<>(1);
        tableMap.put(slaveTable.getTableName(), slaveTable);
        if (CollUtil.isNotEmpty(orderParam)) {
            for (MyOrderParam.OrderInfo orderInfo : orderParam) {
                orderInfo.setFieldName(StrUtil.removePrefix(orderInfo.getFieldName(),
                        relation.getVariableName() + OnlineConstant.RELATION_TABLE_COLUMN_SEPARATOR));
            }
        }
        ResponseResult<String> orderByResult = this.makeOrderBy(orderParam, slaveTable, tableMap);
        if (!orderByResult.isSuccess()) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST, orderByResult);
            return;
        }
        String orderBy = orderByResult.getData();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        MyPageData<Map<String, Object>> pageData =
                onlineOperationService.getSlaveDataList(relation, filterDtoList, orderBy, null);
        Map<String, String> headerMap =
                this.makeExportHeaderMap(relation.getSlaveTable(), null, exportInfoList);
        if (MapUtil.isEmpty(headerMap)) {
            ResponseResult.output(HttpServletResponse.SC_BAD_REQUEST,
                    ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，没有指定导出头信息！"));
            return;
        }
        this.normalizeExportDataList(pageData.getDataList());
        String filename = datasourceVariableName + "-relation-" + MyDateUtil.toDateTimeString(DateTime.now()) + ".xlsx";
        ExportUtil.doExport(pageData.getDataList(), headerMap, filename);
    }

    /**
     * 查询字典数据，并以字典的约定方式，返回数据结果集。
     *
     * @param dictId        字典Id。
     * @param filterDtoList 字典的过滤对象列表。
     * @return 字典数据列表。
     */
    @PostMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict(
            @MyRequestBody(required = true) Long dictId,
            @MyRequestBody List<OnlineFilterDto> filterDtoList) {
        String errorMessage;
        OnlineDict dict = onlineDictService.getOnlineDictFromCache(dictId);
        if (dict == null) {
            errorMessage = "数据验证失败，字典Id并不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (!StrUtil.equals(dict.getAppCode(), tokenData.getAppCode())) {
            errorMessage = "数据验证失败，当前应用并不包含该字典Id！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!dict.getDictType().equals(DictType.TABLE)
                && !dict.getDictType().equals(DictType.GLOBAL_DICT)) {
            errorMessage = "数据验证失败，该接口仅支持数据表字典和全局编码字典！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (dict.getDictType().equals(DictType.GLOBAL_DICT)) {
            List<GlobalDictItem> dictItems =
                    globalDictService.getGlobalDictItemListFromCache(dict.getDictCode(), null);
            List<Map<String, Object>> dataMapList =
                    MyCommonUtil.toDictDataList(dictItems, GlobalDictItem::getItemId, GlobalDictItem::getItemName);
            return ResponseResult.success(dataMapList);
        }
        if (CollUtil.isNotEmpty(filterDtoList)) {
            for (OnlineFilterDto filter : filterDtoList) {
                if (!this.checkTableAndColumnName(filter.getColumnName())) {
                    errorMessage = StrFormatter.format(
                            "数据验证失败，过滤字段名 [{}] 包含 (数字、字母和下划线) 之外的非法字符!", filter.getColumnName());
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
                }
            }
        }
        List<Map<String, Object>> resultList = onlineOperationService.getDictDataList(dict, filterDtoList);
        return ResponseResult.success(resultList);
    }

    /**
     * 获取在线表单所关联的权限数据，包括权限字列表和权限资源列表。
     * 注：该接口仅用于微服务间调用使用，无需对前端开放。
     *
     * @param menuFormIds 菜单关联的表单Id集合。
     * @param viewFormIds 查询权限的表单Id集合。
     * @param editFormIds 编辑权限的表单Id集合。
     * @return 参数中在线表单所关联的权限数据。
     */
    @GetMapping("/calculatePermData")
    public ResponseResult<Map<String, Object>> calculatePermData(
            @RequestParam Set<Long> menuFormIds,
            @RequestParam Set<Long> viewFormIds,
            @RequestParam Set<Long> editFormIds) {
        return ResponseResult.success(onlineOperationService.calculatePermData(menuFormIds, viewFormIds, editFormIds));
    }

    private ResponseResult<Void> doDelete(
            String datasourceVariableName, Long datasourceId, List<String> dataIdList) {
        ResponseResult<OnlineDatasource> datasourceResult =
                onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            return ResponseResult.errorFrom(datasourceResult);
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ContextUtil.getHttpResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION);
        }
        OnlineTable masterTable = datasource.getMasterTable();
        ResponseResult<List<OnlineDatasourceRelation>> relationListResult =
                onlineOperationHelper.verifyAndGetRelationList(datasourceId, RelationType.ONE_TO_MANY);
        if (!relationListResult.isSuccess()) {
            return ResponseResult.errorFrom(relationListResult);
        }
        List<OnlineDatasourceRelation> relationList = relationListResult.getData();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        for (String dataId : dataIdList) {
            if (!onlineOperationService.delete(masterTable, relationList, dataId)) {
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
            }
        }
        return ResponseResult.success();
    }

    private ResponseResult<Void> doDelete(
            String datasourceVariableName, Long datasourceId, Long relationId, List<String> dataIdList) {
        ResponseResult<OnlineDatasourceRelation> verifyResult =
                this.doVerifyAndGetRelation(datasourceId, datasourceVariableName, relationId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineDatasourceRelation relation = verifyResult.getData();
        onlineOperationHelper.enableOnlineExtendExecutor(datasourceId);
        for (String dataId : dataIdList) {
            if (!onlineOperationService.delete(relation.getSlaveTable(), null, dataId)) {
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
            }
        }
        return ResponseResult.success();
    }

    private ResponseResult<OnlineDatasource> doVerifyAndGetDatasource(
            Long datasourceId, String datasourceVariableName) {
        ResponseResult<OnlineDatasource> datasourceResult =
                onlineOperationHelper.verifyAndGetDatasource(datasourceId);
        if (!datasourceResult.isSuccess()) {
            return ResponseResult.errorFrom(datasourceResult);
        }
        OnlineDatasource datasource = datasourceResult.getData();
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ContextUtil.getHttpResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION);
        }
        return ResponseResult.success(datasource);
    }

    private ResponseResult<OnlineDatasourceRelation> doVerifyAndGetRelation(
            Long datasourceId, String datasourceVariableName, Long relationId) {
        OnlineDatasource datasource = onlineDatasourceService.getOnlineDatasourceFromCache(datasourceId);
        if (datasource == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, "数据验证失败，数据源Id并不存在！");
        }
        if (!datasource.getVariableName().equals(datasourceVariableName)) {
            ContextUtil.getHttpResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ResponseResult.error(ErrorCodeEnum.NO_OPERATION_PERMISSION);
        }
        return onlineOperationHelper.verifyAndGetRelation(datasourceId, relationId);
    }

    private ResponseResult<Void> verifyFilterDtoList(List<OnlineFilterDto> filterDtoList) {
        if (CollUtil.isEmpty(filterDtoList)) {
            return ResponseResult.success();
        }
        String errorMessage;
        for (OnlineFilterDto filter : filterDtoList) {
            if (!this.checkTableAndColumnName(filter.getTableName())) {
                errorMessage = StrFormatter.format(
                        "数据验证失败，过滤表名 [{}] 包含 (数字、字母和下划线) 之外的非法字符！", filter.getColumnName());
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            if (!this.checkTableAndColumnName(filter.getColumnName())) {
                errorMessage = StrFormatter.format(
                        "数据验证失败，过滤字段名 [{}] 包含 (数字、字母和下划线) 之外的非法字符！", filter.getColumnName());
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            if (!filter.getFilterType().equals(FieldFilterType.RANGE_FILTER)
                    && ObjectUtil.isEmpty(filter.getColumnValue())) {
                errorMessage = StrFormatter.format(
                        "数据验证失败，过滤字段名 [{}] 过滤值不能为空！", filter.getColumnName());
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        return ResponseResult.success();
    }

    private boolean checkTableAndColumnName(String name) {
        if (StrUtil.isBlank(name)) {
            return true;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!CharUtil.isLetterOrNumber(c) && !CharUtil.equals('_', c, false)) {
                return false;
            }
        }
        return true;
    }

    private ResponseResult<String> makeOrderBy(
            MyOrderParam orderParam, OnlineTable masterTable, Map<String, OnlineTable> tableMap) {
        if (CollUtil.isEmpty(orderParam)) {
            return ResponseResult.success(null);
        }
        String errorMessage;
        StringBuilder sb = new StringBuilder(128);
        for (MyOrderParam.OrderInfo orderInfo : orderParam) {
            String[] orderArray = StrUtil.splitToArray(orderInfo.getFieldName(), '.');
            // 如果没有前缀，我们就可以默认为主表的字段。
            if (orderArray.length == 1) {
                try {
                    sb.append(this.makeOrderByForOrderInfo(masterTable, orderArray[0], orderInfo));
                } catch (OnlineRuntimeException e) {
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, e.getMessage());
                }
            } else {
                String tableName = orderArray[0];
                String columnName = orderArray[1];
                OnlineTable table = tableMap.get(tableName);
                if (table == null) {
                    errorMessage = StrFormatter.format(
                            "数据验证失败，排序字段 [{}] 的数据表 [{}] 并不属于当前数据源！",
                            orderInfo.getFieldName(), tableName);
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
                }
                try {
                    sb.append(this.makeOrderByForOrderInfo(table, columnName, orderInfo));
                } catch (OnlineRuntimeException e) {
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, e.getMessage());
                }
            }
        }
        return ResponseResult.success(sb.substring(0, sb.length() - 2));
    }

    private String makeOrderByForOrderInfo(
            OnlineTable table, String columnName, MyOrderParam.OrderInfo orderInfo) {
        StringBuilder sb = new StringBuilder(64);
        boolean found = false;
        for (OnlineColumn column : table.getColumnMap().values()) {
            if (column.getColumnName().equals(columnName)) {
                sb.append(table.getTableName()).append(".").append(columnName);
                if (BooleanUtil.isFalse(orderInfo.getAsc())) {
                    sb.append(" DESC");
                }
                sb.append(", ");
                found = true;
                break;
            }
        }
        if (!found) {
            String errorMessage = StrFormatter.format(
                    "数据验证失败，排序字段 [{}] 在数据表 [{}] 中并不存在!",
                    orderInfo.getFieldName(), table.getTableName());
            throw new OnlineRuntimeException(errorMessage);
        }
        return sb.toString();
    }

    private int makeImportHeaderInfoByFieldType(String objectFieldType) {
        return switch (objectFieldType) {
            case ObjectFieldType.INTEGER -> ImportUtil.INT_TYPE;
            case ObjectFieldType.LONG -> ImportUtil.LONG_TYPE;
            case ObjectFieldType.STRING -> ImportUtil.STRING_TYPE;
            case ObjectFieldType.BOOLEAN -> ImportUtil.BOOLEAN_TYPE;
            case ObjectFieldType.DATE -> ImportUtil.DATE_TYPE;
            case ObjectFieldType.DOUBLE -> ImportUtil.DOUBLE_TYPE;
            case ObjectFieldType.BIG_DECIMAL -> ImportUtil.BIG_DECIMAL_TYPE;
            default -> throw new MyRuntimeException("Unsupport Import FieldType");
        };
    }

    private Map<String, String> makeExportHeaderMap(
            OnlineTable masterTable,
            List<OnlineDatasourceRelation> allRelationList,
            List<ExportInfo> exportInfoList) {
        Map<String, String> headerMap = new LinkedHashMap<>(16);
        Map<Long, OnlineDatasourceRelation> allRelationMap = null;
        if (allRelationList != null) {
            allRelationMap = allRelationList.stream()
                    .collect(Collectors.toMap(OnlineDatasourceRelation::getSlaveTableId, r -> r));
        }
        for (ExportInfo exportInfo : exportInfoList) {
            if (exportInfo.getVirtualColumnId() != null) {
                OnlineVirtualColumn virtualColumn =
                        onlineVirtualColumnService.getById(exportInfo.getVirtualColumnId());
                if (virtualColumn != null) {
                    headerMap.put(virtualColumn.getObjectFieldName(), exportInfo.showName);
                }
                continue;
            }
            if (masterTable != null && exportInfo.getTableId().equals(masterTable.getTableId())) {
                OnlineColumn column = masterTable.getColumnMap().get(exportInfo.getColumnId());
                String columnName = this.appendSuffixForDictColumn(column, column.getColumnName());
                headerMap.put(columnName, exportInfo.getShowName());
            } else {
                OnlineDatasourceRelation relation =
                        MapUtil.get(allRelationMap, exportInfo.getTableId(), OnlineDatasourceRelation.class);
                if (relation != null) {
                    OnlineColumn column = relation.getSlaveTable().getColumnMap().get(exportInfo.getColumnId());
                    String columnName = this.appendSuffixForDictColumn(
                            column, relation.getVariableName() + "." + column.getColumnName());
                    headerMap.put(columnName, exportInfo.getShowName());
                }
            }
        }
        return headerMap;
    }

    private void normalizeExportDataList(List<Map<String, Object>> dataList) {
        for (Map<String, Object> columnData : dataList) {
            for (Map.Entry<String, Object> entry : columnData.entrySet()) {
                if (entry.getValue() instanceof Long || entry.getValue() instanceof BigDecimal) {
                    columnData.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString());
                }
            }
        }
    }

    private String appendSuffixForDictColumn(OnlineColumn column, String columnName) {
        if (column.getDictId() != null) {
            if (ObjectUtil.equal(column.getFieldKind(), FieldKind.DICT_MULTI_SELECT)) {
                columnName += "DictMapList";
            } else {
                columnName += "DictMap.name";
            }
        }
        return columnName;
    }

    @Data
    public static class ExportInfo {
        private Long tableId;
        private Long columnId;
        private Long virtualColumnId;
        private String showName;
    }
}
