package com.orangeforms.webadmin.upms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import com.github.pagehelper.page.PageMethod;
import com.orangeforms.webadmin.upms.vo.*;
import com.orangeforms.webadmin.upms.dto.*;
import com.orangeforms.webadmin.upms.model.*;
import com.orangeforms.webadmin.upms.service.*;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.*;
import com.orangeforms.common.core.constant.*;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理操作控制器类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "部门管理管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/upms/sysDept")
public class SysDeptController {

    @Autowired
    private SysPostService sysPostService;
    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 新增部门管理数据。
     *
     * @param sysDeptDto 新增对象。
     * @return 应答结果对象，包含新增对象主键Id。
     */
    @ApiOperationSupport(ignoreParameters = {"sysDeptDto.deptId"})
    @SaCheckPermission("sysDept.add")
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody SysDeptDto sysDeptDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysDeptDto, false);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysDept sysDept = MyModelUtil.copyTo(sysDeptDto, SysDept.class);
        // 验证父Id的数据合法性
        SysDept parentSysDept = null;
        if (MyCommonUtil.isNotBlankOrNull(sysDept.getParentId())) {
            parentSysDept = sysDeptService.getById(sysDept.getParentId());
            if (parentSysDept == null) {
                errorMessage = "数据验证失败，关联的父节点并不存在，请刷新后重试！";
                return ResponseResult.error(ErrorCodeEnum.DATA_PARENT_ID_NOT_EXIST, errorMessage);
            }
        }
        sysDept = sysDeptService.saveNew(sysDept, parentSysDept);
        return ResponseResult.success(sysDept.getDeptId());
    }

    /**
     * 更新部门管理数据。
     *
     * @param sysDeptDto 更新对象。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDept.update")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody SysDeptDto sysDeptDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysDeptDto, true);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysDept sysDept = MyModelUtil.copyTo(sysDeptDto, SysDept.class);
        SysDept originalSysDept = sysDeptService.getById(sysDept.getDeptId());
        if (originalSysDept == null) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [数据] 并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        // 验证父Id的数据合法性
        if (MyCommonUtil.isNotBlankOrNull(sysDept.getParentId())
                && ObjectUtil.notEqual(sysDept.getParentId(), originalSysDept.getParentId())) {
            SysDept parentSysDept = sysDeptService.getById(sysDept.getParentId());
            if (parentSysDept == null) {
                // NOTE: 修改下面方括号中的话述
                errorMessage = "数据验证失败，关联的 [父节点] 并不存在，请刷新后重试！";
                return ResponseResult.error(ErrorCodeEnum.DATA_PARENT_ID_NOT_EXIST, errorMessage);
            }
        }
        if (!sysDeptService.update(sysDept, originalSysDept)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 删除部门管理数据。
     *
     * @param deptId 删除对象主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDept.delete")
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long deptId) {
        if (MyCommonUtil.existBlankArgument(deptId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        return this.doDelete(deptId);
    }

    /**
     * 批量删除部门管理数据。
     *
     * @param deptIdList 待删除对象的主键Id列表。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDept.delete")
    @OperationLog(type = SysOperationLogType.DELETE_BATCH)
    @PostMapping("/deleteBatch")
    public ResponseResult<Void> deleteBatch(@MyRequestBody List<Long> deptIdList) {
        if (MyCommonUtil.existBlankArgument(deptIdList)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        for (Long deptId : deptIdList) {
            ResponseResult<Void> responseResult = this.doDelete(deptId);
            if (!responseResult.isSuccess()) {
                return responseResult;
            }
        }
        return ResponseResult.success();
    }

    /**
     * 列出符合过滤条件的部门管理列表。
     *
     * @param sysDeptDtoFilter 过滤对象。
     * @param orderParam 排序参数。
     * @param pageParam 分页参数。
     * @return 应答结果对象，包含查询结果集。
     */
    @SaCheckPermission("sysDept.view")
    @PostMapping("/list")
    public ResponseResult<MyPageData<SysDeptVo>> list(
            @MyRequestBody SysDeptDto sysDeptDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        SysDept sysDeptFilter = MyModelUtil.copyTo(sysDeptDtoFilter, SysDept.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysDept.class);
        List<SysDept> sysDeptList = sysDeptService.getSysDeptListWithRelation(sysDeptFilter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(sysDeptList, SysDeptVo.class));
    }

    /**
     * 查看指定部门管理对象详情。
     *
     * @param deptId 指定对象主键Id。
     * @return 应答结果对象，包含对象详情。
     */
    @SaCheckPermission("sysDept.view")
    @GetMapping("/view")
    public ResponseResult<SysDeptVo> view(@RequestParam Long deptId) {
        SysDept sysDept = sysDeptService.getByIdWithRelation(deptId, MyRelationParam.full());
        if (sysDept == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        SysDeptVo sysDeptVo = MyModelUtil.copyTo(sysDept, SysDeptVo.class);
        return ResponseResult.success(sysDeptVo);
    }

    /**
     * 列出不与指定部门管理存在多对多关系的 [岗位管理] 列表数据。通常用于查看添加新 [岗位管理] 对象的候选列表。
     *
     * @param deptId 主表关联字段。
     * @param sysPostDtoFilter [岗位管理] 过滤对象。
     * @param orderParam 排序参数。
     * @param pageParam 分页参数。
     * @return 应答结果对象，返回符合条件的数据列表。
     */
    @SaCheckPermission("sysDept.update")
    @PostMapping("/listNotInSysDeptPost")
    public ResponseResult<MyPageData<SysPostVo>> listNotInSysDeptPost(
            @MyRequestBody Long deptId,
            @MyRequestBody SysPostDto sysPostDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (MyCommonUtil.isNotBlankOrNull(deptId) && !sysDeptService.existId(deptId)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysPost filter = MyModelUtil.copyTo(sysPostDtoFilter, SysPost.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysPost.class);
        List<SysPost> sysPostList;
        if (MyCommonUtil.isNotBlankOrNull(deptId)) {
            sysPostList = sysPostService.getNotInSysPostListByDeptId(deptId, filter, orderBy);
        } else {
            sysPostList = sysPostService.getSysPostList(filter, orderBy);
            sysPostService.buildRelationForDataList(sysPostList, MyRelationParam.dictOnly());
        }
        return ResponseResult.success(MyPageUtil.makeResponseData(sysPostList, SysPostVo.class));
    }

    /**
     * 列出与指定部门管理存在多对多关系的 [岗位管理] 列表数据。
     *
     * @param deptId 主表关联字段。
     * @param sysPostDtoFilter [岗位管理] 过滤对象。
     * @param orderParam 排序参数。
     * @param pageParam 分页参数。
     * @return 应答结果对象，返回符合条件的数据列表。
     */
    @SaCheckPermission("sysDept.view")
    @PostMapping("/listSysDeptPost")
    public ResponseResult<MyPageData<SysPostVo>> listSysDeptPost(
            @MyRequestBody(required = true) Long deptId,
            @MyRequestBody SysPostDto sysPostDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (!sysDeptService.existId(deptId)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysPost filter = MyModelUtil.copyTo(sysPostDtoFilter, SysPost.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysPost.class);
        List<SysPost> sysPostList = sysPostService.getSysPostListByDeptId(deptId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(sysPostList, SysPostVo.class));
    }

    /**
     * 批量添加部门管理和 [岗位管理] 对象的多对多关联关系数据。
     *
     * @param deptId 主表主键Id。
     * @param sysDeptPostDtoList 关联对象列表。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDept.update")
    @PostMapping("/addSysDeptPost")
    public ResponseResult<Void> addSysDeptPost(
            @MyRequestBody Long deptId,
            @MyRequestBody List<SysDeptPostDto> sysDeptPostDtoList) {
        if (MyCommonUtil.existBlankArgument(deptId, sysDeptPostDtoList)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        String errorMessage = MyCommonUtil.getModelValidationError(sysDeptPostDtoList);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        Set<Long> postIdSet = sysDeptPostDtoList.stream().map(SysDeptPostDto::getPostId).collect(Collectors.toSet());
        if (!sysDeptService.existId(deptId) || !sysPostService.existUniqueKeyList("postId", postIdSet)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        List<SysDeptPost> sysDeptPostList = MyModelUtil.copyCollectionTo(sysDeptPostDtoList, SysDeptPost.class);
        sysDeptService.addSysDeptPostList(sysDeptPostList, deptId);
        return ResponseResult.success();
    }

    /**
     * 更新指定部门管理和指定 [岗位管理] 的多对多关联数据。
     *
     * @param sysDeptPostDto 对多对中间表对象。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDept.update")
    @PostMapping("/updateSysDeptPost")
    public ResponseResult<Void> updateSysDeptPost(@MyRequestBody SysDeptPostDto sysDeptPostDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysDeptPostDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysDeptPost sysDeptPost = MyModelUtil.copyTo(sysDeptPostDto, SysDeptPost.class);
        if (!sysDeptService.updateSysDeptPost(sysDeptPost)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 显示部门管理和指定 [岗位管理] 的多对多关联详情数据。
     *
     * @param deptId 主表主键Id。
     * @param postId 从表主键Id。
     * @return 应答结果对象，包括中间表详情。
     */
    @SaCheckPermission("sysDept.update")
    @GetMapping("/viewSysDeptPost")
    public ResponseResult<SysDeptPostVo> viewSysDeptPost(@RequestParam Long deptId, @RequestParam Long postId) {
        SysDeptPost sysDeptPost = sysDeptService.getSysDeptPost(deptId, postId);
        if (sysDeptPost == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        SysDeptPostVo sysDeptPostVo = MyModelUtil.copyTo(sysDeptPost, SysDeptPostVo.class);
        return ResponseResult.success(sysDeptPostVo);
    }

    /**
     * 移除指定部门管理和指定 [岗位管理] 的多对多关联关系。
     *
     * @param deptId 主表主键Id。
     * @param postId 从表主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDept.update")
    @PostMapping("/deleteSysDeptPost")
    public ResponseResult<Void> deleteSysDeptPost(@MyRequestBody Long deptId, @MyRequestBody Long postId) {
        if (MyCommonUtil.existBlankArgument(deptId, postId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysDeptService.removeSysDeptPost(deptId, postId)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 获取部门岗位多对多关联数据，及其关联的部门和岗位数据。
     *
     * @param deptId 部门Id，如果为空，返回全部数据列表。
     * @return 部门岗位多对多关联数据，及其关联的部门和岗位数据
     */
    @GetMapping("/listSysDeptPostWithRelation")
    public ResponseResult<List<Map<String, Object>>> listSysDeptPostWithRelation(
            @RequestParam(required = false) Long deptId) {
        return ResponseResult.success(sysDeptService.getSysDeptPostListWithRelationByDeptId(deptId));
    }

    /**
     * 以字典形式返回全部部门管理数据集合。字典的键值为[deptId, deptName]。
     * 白名单接口，登录用户均可访问。
     *
     * @param filter 过滤对象。
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict(@ParameterObject SysDeptDto filter) {
        List<SysDept> resultList =
                sysDeptService.getListByFilter(MyModelUtil.copyTo(filter, SysDept.class));
        return ResponseResult.success(MyCommonUtil.toDictDataList(
                resultList, SysDept::getDeptId, SysDept::getDeptName, SysDept::getParentId));
    }

    /**
     * 根据字典Id集合，获取查询后的字典数据。
     *
     * @param dictIds 字典Id集合。
     * @return 应答结果对象，包含字典形式的数据集合。
     */
    @GetMapping("/listDictByIds")
    public ResponseResult<List<Map<String, Object>>> listDictByIds(@RequestParam List<Long> dictIds) {
        List<SysDept> resultList = sysDeptService.getInList(new HashSet<>(dictIds));
        return ResponseResult.success(MyCommonUtil.toDictDataList(
                resultList, SysDept::getDeptId, SysDept::getDeptName, SysDept::getParentId));
    }

    /**
     * 根据父主键Id，以字典的形式返回其下级数据列表。
     * 白名单接口，登录用户均可访问。
     *
     * @param parentId 父主键Id。
     * @return 按照字典的形式返回下级数据列表。
     */
    @GetMapping("/listDictByParentId")
    public ResponseResult<List<Map<String, Object>>> listDictByParentId(@RequestParam(required = false) Long parentId) {
        List<SysDept> resultList = sysDeptService.getListByParentId("parentId", parentId);
        return ResponseResult.success(MyCommonUtil.toDictDataList(
                resultList, SysDept::getDeptId, SysDept::getDeptName, SysDept::getParentId));
    }
    
    /**
     * 根据父主键Id列表，获取当前部门Id及其所有下级部门Id列表。
     * 白名单接口，登录用户均可访问。
     *
     * @param parentIds 父主键Id列表，多个Id之间逗号分隔。
     * @return 获取当前部门Id及其所有下级部门Id列表。
     */
    @GetMapping("/listAllChildDeptIdByParentIds")
    public ResponseResult<List<Long>> listAllChildDeptIdByParentIds(
            @RequestParam(required = false) String parentIds) {
        List<Long> parentIdList = StrUtil.split(parentIds, ',')
                .stream().map(Long::valueOf).collect(Collectors.toList());
        return ResponseResult.success(sysDeptService.getAllChildDeptIdByParentIds(parentIdList));
    }

    private ResponseResult<Void> doDelete(Long deptId) {
        String errorMessage;
        // 验证关联Id的数据合法性
        SysDept originalSysDept = sysDeptService.getById(deptId);
        if (originalSysDept == null) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [对象] 并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        if (sysDeptService.hasChildren(deptId)) {
            // NOTE: 修改下面方括号中的话述
            errorMessage = "数据验证失败，当前 [对象存在子对象] ，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.HAS_CHILDREN_DATA, errorMessage);
        }
        if (sysDeptService.hasChildrenUser(deptId)) {
            errorMessage = "数据验证失败，请先移除部门用户数据后，再删除当前部门！";
            return ResponseResult.error(ErrorCodeEnum.HAS_CHILDREN_DATA, errorMessage);
        }
        if (!sysDeptService.remove(deptId)) {
            errorMessage = "数据操作失败，删除的对象不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }
}
