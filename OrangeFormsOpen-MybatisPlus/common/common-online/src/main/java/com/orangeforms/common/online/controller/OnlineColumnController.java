package com.orangeforms.common.online.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.constant.MaskFieldTypeEnum;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.MyCommonUtil;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.core.util.MyPageUtil;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.dbutil.object.SqlTableColumn;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import com.orangeforms.common.online.dto.OnlineColumnDto;
import com.orangeforms.common.online.dto.OnlineColumnRuleDto;
import com.orangeforms.common.online.dto.OnlineRuleDto;
import com.orangeforms.common.online.model.*;
import com.orangeforms.common.online.model.constant.FieldKind;
import com.orangeforms.common.online.service.*;
import com.orangeforms.common.online.vo.OnlineColumnRuleVo;
import com.orangeforms.common.online.vo.OnlineColumnVo;
import com.orangeforms.common.online.vo.OnlineRuleVo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.groups.Default;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在线表单字段数据接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "在线表单字段数据接口")
@Slf4j
@RestController
@RequestMapping("${common-online.urlPrefix}/onlineColumn")
@ConditionalOnProperty(name = "common-online.operationEnabled", havingValue = "true")
public class OnlineColumnController {

    @Autowired
    private OnlineColumnService onlineColumnService;
    @Autowired
    private OnlineTableService onlineTableService;
    @Autowired
    private OnlineVirtualColumnService onlineVirtualColumnService;
    @Autowired
    private OnlineDblinkService onlineDblinkService;
    @Autowired
    private OnlineRuleService onlineRuleService;
    @Autowired
    private OnlineDictService onlineDictService;

    /**
     * 根据数据库表字段信息，在指定在线表中添加在线表字段对象。
     *
     * @param dblinkId   数据库链接Id。
     * @param tableName  数据库表名称。
     * @param columnName 数据库表字段名。
     * @param tableId    目的表Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Void> add(
            @MyRequestBody Long dblinkId,
            @MyRequestBody String tableName,
            @MyRequestBody String columnName,
            @MyRequestBody Long tableId) {
        OnlineDblink dblink = onlineDblinkService.getById(dblinkId);
        if (dblink == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        String errorMessage;
        SqlTableColumn sqlTableColumn = onlineDblinkService.getDblinkTableColumn(dblink, tableName, columnName);
        if (sqlTableColumn == null) {
            errorMessage = "数据验证失败，指定的数据表字段不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        ResponseResult<Void> verifyResult = this.doVerifyTable(tableId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        onlineColumnService.saveNewList(CollUtil.newLinkedList(sqlTableColumn), tableId);
        return ResponseResult.success();
    }

    /**
     * 更新字段数据数据。
     *
     * @param onlineColumnDto 更新对象。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody OnlineColumnDto onlineColumnDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineColumnDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        OnlineColumn onlineColumn = MyModelUtil.copyTo(onlineColumnDto, OnlineColumn.class);
        OnlineColumn originalOnlineColumn = onlineColumnService.getById(onlineColumn.getColumnId());
        if (originalOnlineColumn == null) {
            errorMessage = "数据验证失败，当前在线表字段并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        ResponseResult<Void> verifyColumnResult = this.doVerifyColumn(onlineColumn, originalOnlineColumn);
        if (!verifyColumnResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyColumnResult);
        }
        ResponseResult<Void> verifyResult = this.doVerifyTable(originalOnlineColumn.getTableId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlineColumnService.update(onlineColumn, originalOnlineColumn)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除字段数据数据。
     *
     * @param columnId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long columnId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(columnId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        // 验证关联Id的数据合法性
        OnlineColumn originalOnlineColumn = onlineColumnService.getById(columnId);
        if (originalOnlineColumn == null) {
            errorMessage = "数据验证失败，当前在线表字段并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        ResponseResult<Void> verifyResult = this.doVerifyTable(originalOnlineColumn.getTableId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineVirtualColumn virtualColumnFilter = new OnlineVirtualColumn();
        virtualColumnFilter.setAggregationColumnId(columnId);
        List<OnlineVirtualColumn> virtualColumnList =
                onlineVirtualColumnService.getOnlineVirtualColumnList(virtualColumnFilter, null);
        if (CollUtil.isNotEmpty(virtualColumnList)) {
            OnlineVirtualColumn virtualColumn = virtualColumnList.get(0);
            errorMessage = "数据验证失败，数据源关联正在被虚拟字段 [" + virtualColumn.getColumnPrompt() + "] 使用，不能被删除！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!onlineColumnService.remove(originalOnlineColumn.getTableId(), columnId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的字段数据列表。
     *
     * @param onlineColumnDtoFilter 过滤对象。
     * @param pageParam             分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @SaCheckPermission("onlinePage.all")
    @PostMapping("/list")
    public ResponseResult<MyPageData<OnlineColumnVo>> list(
            @MyRequestBody OnlineColumnDto onlineColumnDtoFilter,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineColumn onlineColumnFilter = MyModelUtil.copyTo(onlineColumnDtoFilter, OnlineColumn.class);
        List<OnlineColumn> onlineColumnList =
                onlineColumnService.getOnlineColumnListWithRelation(onlineColumnFilter);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineColumnList, OnlineColumnVo.class));
    }

    /**
     * 查看指定字段数据对象详情。
     *
     * @param columnId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @SaCheckPermission("onlinePage.all")
    @GetMapping("/view")
    public ResponseResult<OnlineColumnVo> view(@RequestParam Long columnId) {
        if (MyCommonUtil.existBlankArgument(columnId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        OnlineColumn onlineColumn = onlineColumnService.getByIdWithRelation(columnId, MyRelationParam.full());
        if (onlineColumn == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success(onlineColumn, OnlineColumnVo.class);
    }

    /**
     * 将数据库中的表字段信息刷新到已经导入的在线表字段信息。
     *
     * @param dblinkId   数据库链接Id。
     * @param tableName  数据库表名称。
     * @param columnName 数据库表字段名。
     * @param columnId   被刷新的在线字段Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @PostMapping("/refresh")
    public ResponseResult<Void> refresh(
            @MyRequestBody Long dblinkId,
            @MyRequestBody String tableName,
            @MyRequestBody String columnName,
            @MyRequestBody Long columnId) {
        OnlineDblink dblink = onlineDblinkService.getById(dblinkId);
        if (dblink == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        String errorMsg;
        SqlTableColumn sqlTableColumn = onlineDblinkService.getDblinkTableColumn(dblink, tableName, columnName);
        if (sqlTableColumn == null) {
            errorMsg = "数据验证失败，指定的数据表字段不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMsg);
        }
        OnlineColumn onlineColumn = onlineColumnService.getById(columnId);
        if (onlineColumn == null) {
            errorMsg = "数据验证失败，指定的在线表字段Id不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMsg);
        }
        ResponseResult<Void> verifyResult = this.doVerifyTable(onlineColumn.getTableId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        onlineColumnService.refresh(sqlTableColumn, onlineColumn);
        return ResponseResult.success();
    }

    /**
     * 列出不与指定字段数据存在多对多关系的 [验证规则] 列表数据。通常用于查看添加新 [验证规则] 对象的候选列表。
     *
     * @param columnId            主表关联字段。
     * @param onlineRuleDtoFilter [验证规则] 过滤对象。
     * @param orderParam          排序参数。
     * @param pageParam           分页参数。
     * @return 应答结果对象，返回符合条件的数据列表。
     */
    @SaCheckPermission("onlinePage.all")
    @PostMapping("/listNotInOnlineColumnRule")
    public ResponseResult<MyPageData<OnlineRuleVo>> listNotInOnlineColumnRule(
            @MyRequestBody Long columnId,
            @MyRequestBody OnlineRuleDto onlineRuleDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<Void> verifyResult = this.doVerifyColumn(columnId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineRule filter = MyModelUtil.copyTo(onlineRuleDtoFilter, OnlineRule.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineRule.class);
        List<OnlineRule> onlineRuleList =
                onlineRuleService.getNotInOnlineRuleListByColumnId(columnId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineRuleList, OnlineRuleVo.class));
    }

    /**
     * 列出与指定字段数据存在多对多关系的 [验证规则] 列表数据。
     *
     * @param columnId            主表关联字段。
     * @param onlineRuleDtoFilter [验证规则] 过滤对象。
     * @param orderParam          排序参数。
     * @param pageParam           分页参数。
     * @return 应答结果对象，返回符合条件的数据列表。
     */
    @SaCheckPermission("onlinePage.all")
    @PostMapping("/listOnlineColumnRule")
    public ResponseResult<MyPageData<OnlineRuleVo>> listOnlineColumnRule(
            @MyRequestBody Long columnId,
            @MyRequestBody OnlineRuleDto onlineRuleDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<Void> verifyResult = this.doVerifyColumn(columnId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        OnlineRule filter = MyModelUtil.copyTo(onlineRuleDtoFilter, OnlineRule.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, OnlineRule.class);
        List<OnlineRule> onlineRuleList =
                onlineRuleService.getOnlineRuleListByColumnId(columnId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(onlineRuleList, OnlineRuleVo.class));
    }

    /**
     * 批量添加字段数据和 [验证规则] 对象的多对多关联关系数据。
     *
     * @param columnId                主表主键Id。
     * @param onlineColumnRuleDtoList 关联对象列表。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @OperationLog(type = SysOperationLogType.ADD_M2M)
    @PostMapping("/addOnlineColumnRule")
    public ResponseResult<Void> addOnlineColumnRule(
            @MyRequestBody Long columnId, @MyRequestBody List<OnlineColumnRuleDto> onlineColumnRuleDtoList) {
        if (MyCommonUtil.existBlankArgument(columnId, onlineColumnRuleDtoList)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        String errorMessage;
        for (OnlineColumnRuleDto onlineColumnRule : onlineColumnRuleDtoList) {
            errorMessage = MyCommonUtil.getModelValidationError(onlineColumnRule);
            if (errorMessage != null) {
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        ResponseResult<Void> verifyResult = this.doVerifyColumn(columnId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        Set<Long> ruleIdSet = onlineColumnRuleDtoList.stream()
                .map(OnlineColumnRuleDto::getRuleId).collect(Collectors.toSet());
        List<OnlineRule> ruleList = onlineRuleService.getInList(ruleIdSet);
        if (ruleIdSet.size() != ruleList.size()) {
            errorMessage = "数据验证失败，参数中存在非法字段规则Id！";
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID, errorMessage);
        }
        for (OnlineRule rule : ruleList) {
            if (BooleanUtil.isFalse(rule.getBuiltin())
                    && !StrUtil.equals(rule.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
                errorMessage = "数据验证失败，参数中存在不属于该应用的字段规则Id!";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        List<OnlineColumnRule> onlineColumnRuleList =
                MyModelUtil.copyCollectionTo(onlineColumnRuleDtoList, OnlineColumnRule.class);
        onlineColumnService.addOnlineColumnRuleList(onlineColumnRuleList, columnId);
        return ResponseResult.success();
    }

    /**
     * 更新指定字段数据和指定 [验证规则] 的多对多关联数据。
     *
     * @param onlineColumnRuleDto 对多对中间表对象。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/updateOnlineColumnRule")
    public ResponseResult<Void> updateOnlineColumnRule(@MyRequestBody OnlineColumnRuleDto onlineColumnRuleDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(onlineColumnRuleDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        ResponseResult<Void> verifyResult = this.doVerifyColumn(onlineColumnRuleDto.getColumnId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineColumnRule onlineColumnRule = MyModelUtil.copyTo(onlineColumnRuleDto, OnlineColumnRule.class);
        if (!onlineColumnService.updateOnlineColumnRule(onlineColumnRule)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 显示字段数据和指定 [验证规则] 的多对多关联详情数据。
     *
     * @param columnId 主表主键Id。
     * @param ruleId   从表主键Id。
     * @return 应答结果对象，包括中间表详情。
     */
    @SaCheckPermission("onlinePage.all")
    @GetMapping("/viewOnlineColumnRule")
    public ResponseResult<OnlineColumnRuleVo> viewOnlineColumnRule(
            @RequestParam Long columnId, @RequestParam Long ruleId) {
        if (MyCommonUtil.existBlankArgument(columnId, ruleId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        ResponseResult<Void> verifyResult = this.doVerifyColumn(columnId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        OnlineColumnRule onlineColumnRule = onlineColumnService.getOnlineColumnRule(columnId, ruleId);
        if (onlineColumnRule == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        OnlineColumnRuleVo onlineColumnRuleVo = MyModelUtil.copyTo(onlineColumnRule, OnlineColumnRuleVo.class);
        return ResponseResult.success(onlineColumnRuleVo);
    }

    /**
     * 移除指定字段数据和指定 [验证规则] 的多对多关联关系。
     *
     * @param columnId 主表主键Id。
     * @param ruleId   从表主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("onlinePage.all")
    @OperationLog(type = SysOperationLogType.DELETE_M2M)
    @PostMapping("/deleteOnlineColumnRule")
    public ResponseResult<Void> deleteOnlineColumnRule(@MyRequestBody Long columnId, @MyRequestBody Long ruleId) {
        if (MyCommonUtil.existBlankArgument(columnId, ruleId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        ResponseResult<Void> verifyResult = this.doVerifyColumn(columnId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (!onlineColumnService.removeOnlineColumnRule(columnId, ruleId)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 以字典形式返回全部字段数据数据集合。字典的键值为[columnId, columnName]。
     * 白名单接口，登录用户均可访问。
     *
     * @param filter 过滤对象。
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict(@ParameterObject OnlineColumnDto filter) {
        List<OnlineColumn> resultList =
                onlineColumnService.getListByFilter(MyModelUtil.copyTo(filter, OnlineColumn.class));
        return ResponseResult.success(
                MyCommonUtil.toDictDataList(resultList, OnlineColumn::getColumnId, OnlineColumn::getColumnName));
    }

    private ResponseResult<Void> doVerifyColumn(Long columnId) {
        if (MyCommonUtil.existBlankArgument(columnId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        OnlineColumn onlineColumn = onlineColumnService.getById(columnId);
        if (onlineColumn == null) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        ResponseResult<Void> verifyResult = this.doVerifyTable(onlineColumn.getTableId());
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        return ResponseResult.success();
    }

    private ResponseResult<Void> doVerifyColumn(OnlineColumn onlineColumn, OnlineColumn originalOnlineColumn) {
        String errorMessage;
        if (onlineColumn.getDictId() != null
                && ObjectUtil.notEqual(onlineColumn.getDictId(), originalOnlineColumn.getDictId())) {
            OnlineDict dict = onlineDictService.getById(onlineColumn.getDictId());
            if (dict == null) {
                errorMessage = "数据验证失败，关联的字典Id不存在！";
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
            }
            if (!StrUtil.equals(dict.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
                errorMessage = "数据验证失败，关联的字典Id并不属于当前应用！";
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
            }
        }
        if (MyCommonUtil.equalsAny(onlineColumn.getFieldKind(), FieldKind.UPLOAD, FieldKind.UPLOAD_IMAGE)
                && onlineColumn.getUploadFileSystemType() == null) {
            errorMessage = "数据验证失败，上传字段必须设置上传文件系统类型！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (ObjectUtil.equal(onlineColumn.getFieldKind(), FieldKind.MASK_FIELD)) {
            if (onlineColumn.getMaskFieldType() == null) {
                errorMessage = "数据验证失败，脱敏字段没有设置脱敏类型！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            if (!EnumUtil.contains(MaskFieldTypeEnum.class, onlineColumn.getMaskFieldType())) {
                errorMessage = "数据验证失败，脱敏字段设置的脱敏类型并不存在！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        if (!onlineColumn.getTableId().equals(originalOnlineColumn.getTableId())) {
            errorMessage = "数据验证失败，字段的所属表Id不能修改！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success();
    }

    private ResponseResult<Void> doVerifyTable(Long tableId) {
        String errorMessage;
        OnlineTable table = onlineTableService.getById(tableId);
        if (table == null) {
            errorMessage = "数据验证失败，指定的数据表Id不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!StrUtil.equals(table.getAppCode(), TokenData.takeFromRequest().getAppCode())) {
            errorMessage = "数据验证失败，当前应用并不包含该字段所在的表！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success();
    }
}
