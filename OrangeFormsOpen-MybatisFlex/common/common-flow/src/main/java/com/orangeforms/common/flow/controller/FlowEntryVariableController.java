package com.orangeforms.common.flow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.pagehelper.page.PageMethod;
import com.orangeforms.common.flow.vo.*;
import com.orangeforms.common.flow.dto.*;
import com.orangeforms.common.flow.model.*;
import com.orangeforms.common.flow.service.*;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.*;
import com.orangeforms.common.core.constant.*;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import jakarta.validation.groups.Default;

/**
 * 工作流流程变量接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "工作流流程变量接口")
@Slf4j
@RestController
@RequestMapping("${common-flow.urlPrefix}/flowEntryVariable")
@ConditionalOnProperty(name = "common-flow.operationEnabled", havingValue = "true")
public class FlowEntryVariableController {

    @Autowired
    private FlowEntryVariableService flowEntryVariableService;

    /**
     * 新增流程变量数据。
     *
     * @param flowEntryVariableDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @ApiOperationSupport(ignoreParameters = {"flowEntryVariableDto.variableId"})
    @SaCheckPermission("flowEntry.all")
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody FlowEntryVariableDto flowEntryVariableDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(flowEntryVariableDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowEntryVariable flowEntryVariable = MyModelUtil.copyTo(flowEntryVariableDto, FlowEntryVariable.class);
        flowEntryVariable = flowEntryVariableService.saveNew(flowEntryVariable);
        return ResponseResult.success(flowEntryVariable.getVariableId());
    }

    /**
     * 更新流程变量数据。
     *
     * @param flowEntryVariableDto 更新对象。
     * @return 应答结果对象。
     */
    @SaCheckPermission("flowEntry.all")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody FlowEntryVariableDto flowEntryVariableDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(flowEntryVariableDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowEntryVariable flowEntryVariable = MyModelUtil.copyTo(flowEntryVariableDto, FlowEntryVariable.class);
        FlowEntryVariable originalFlowEntryVariable = flowEntryVariableService.getById(flowEntryVariable.getVariableId());
        if (originalFlowEntryVariable == null) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [数据] 并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!flowEntryVariableService.update(flowEntryVariable, originalFlowEntryVariable)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除流程变量数据。
     *
     * @param variableId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("flowEntry.all")
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long variableId) {
        String errorMessage;
        if (MyCommonUtil.existBlankArgument(variableId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        // 验证关联Id的数据合法性
        FlowEntryVariable originalFlowEntryVariable = flowEntryVariableService.getById(variableId);
        if (originalFlowEntryVariable == null) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [对象] 并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (!flowEntryVariableService.remove(variableId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的流程变量列表。
     *
     * @param flowEntryVariableDtoFilter 过滤对象。
     * @param orderParam 排序参数。
     * @param pageParam 分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @SaCheckPermission("flowEntry.all")
    @PostMapping("/list")
    public ResponseResult<MyPageData<FlowEntryVariableVo>> list(
            @MyRequestBody FlowEntryVariableDto flowEntryVariableDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        FlowEntryVariable flowEntryVariableFilter = MyModelUtil.copyTo(flowEntryVariableDtoFilter, FlowEntryVariable.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, FlowEntryVariable.class);
        List<FlowEntryVariable> flowEntryVariableList =
                flowEntryVariableService.getFlowEntryVariableListWithRelation(flowEntryVariableFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(flowEntryVariableList, FlowEntryVariableVo.class));
    }

    /**
     * 查看指定流程变量对象详情。
     *
     * @param variableId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @SaCheckPermission("flowEntry.all")
    @GetMapping("/view")
    public ResponseResult<FlowEntryVariableVo> view(@RequestParam Long variableId) {
        if (MyCommonUtil.existBlankArgument(variableId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        FlowEntryVariable flowEntryVariable = flowEntryVariableService.getByIdWithRelation(variableId, MyRelationParam.full());
        if (flowEntryVariable == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success(flowEntryVariable, FlowEntryVariableVo.class);
    }
}
