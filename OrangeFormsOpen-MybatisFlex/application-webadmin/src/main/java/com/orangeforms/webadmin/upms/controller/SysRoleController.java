package com.orangeforms.webadmin.upms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import com.orangeforms.webadmin.upms.dto.SysRoleDto;
import com.orangeforms.webadmin.upms.dto.SysUserDto;
import com.orangeforms.webadmin.upms.vo.SysRoleVo;
import com.orangeforms.webadmin.upms.vo.SysUserVo;
import com.orangeforms.webadmin.upms.model.SysRole;
import com.orangeforms.webadmin.upms.model.SysUser;
import com.orangeforms.webadmin.upms.model.SysUserRole;
import com.orangeforms.webadmin.upms.service.SysRoleService;
import com.orangeforms.webadmin.upms.service.SysUserService;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.*;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.groups.Default;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理接口控制器类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "角色管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/upms/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserService sysUserService;

    /**
     * 新增角色操作。
     *
     * @param sysRoleDto       新增角色对象。
     * @param menuIdListString 与当前角色Id绑定的menuId列表，多个menuId之间逗号分隔。
     * @return 应答结果对象，包含新增角色的主键Id。
     */
    @ApiOperationSupport(ignoreParameters = {"sysRoleDto.roleId", "sysRoleDto.createTimeStart", "sysRoleDto.createTimeEnd"})
    @SaCheckPermission("sysRole.add")
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(
            @MyRequestBody SysRoleDto sysRoleDto, @MyRequestBody String menuIdListString) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysRoleDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysRole sysRole = MyModelUtil.copyTo(sysRoleDto, SysRole.class);
        CallResult result = sysRoleService.verifyRelatedData(sysRole, null, menuIdListString);
        if (!result.isSuccess()) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
        }
        Set<Long> menuIdSet = null;
        if (result.getData() != null) {
            menuIdSet = result.getData().getObject("menuIdSet", new TypeReference<Set<Long>>(){});
        }
        sysRoleService.saveNew(sysRole, menuIdSet);
        return ResponseResult.success(sysRole.getRoleId());
    }

    /**
     * 更新角色操作。
     *
     * @param sysRoleDto       更新角色对象。
     * @param menuIdListString 与当前角色Id绑定的menuId列表，多个menuId之间逗号分隔。
     * @return 应答结果对象。
     */
    @ApiOperationSupport(ignoreParameters = {"sysRoleDto.createTimeStart", "sysRoleDto.createTimeEnd"})
    @SaCheckPermission("sysRole.update")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(
            @MyRequestBody SysRoleDto sysRoleDto, @MyRequestBody String menuIdListString) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysRoleDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysRole originalSysRole = sysRoleService.getById(sysRoleDto.getRoleId());
        if (originalSysRole == null) {
            errorMessage = "数据验证失败，当前角色并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        SysRole sysRole = MyModelUtil.copyTo(sysRoleDto, SysRole.class);
        CallResult result = sysRoleService.verifyRelatedData(sysRole, originalSysRole, menuIdListString);
        if (!result.isSuccess()) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
        }
        Set<Long> menuIdSet = null;
        if (result.getData() != null) {
            menuIdSet = result.getData().getObject("menuIdSet", new TypeReference<Set<Long>>(){});
        }
        if (!sysRoleService.update(sysRole, originalSysRole, menuIdSet)) {
            errorMessage = "更新失败，数据不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 删除指定角色操作。
     *
     * @param roleId 指定角色主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysRole.delete")
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long roleId) {
        if (MyCommonUtil.existBlankArgument(roleId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysRoleService.remove(roleId)) {
            String errorMessage = "数据操作失败，角色不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 查看角色列表。
     *
     * @param sysRoleDtoFilter 角色过滤对象。
     * @param orderParam       排序参数。
     * @param pageParam        分页参数。
     * @return 应答结果对象，包含角色列表。
     */
    @SaCheckPermission("sysRole.view")
    @PostMapping("/list")
    public ResponseResult<MyPageData<SysRoleVo>> list(
            @MyRequestBody SysRoleDto sysRoleDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysRole filter = MyModelUtil.copyTo(sysRoleDtoFilter, SysRole.class);
        List<SysRole> roleList = sysRoleService.getSysRoleList(
                filter, MyOrderParam.buildOrderBy(orderParam, SysRole.class));
        List<SysRoleVo> roleVoList = MyModelUtil.copyCollectionTo(roleList, SysRoleVo.class);
        long totalCount = 0L;
        if (roleList instanceof Page) {
            totalCount = ((Page<SysRole>) roleList).getTotal();
        }
        return ResponseResult.success(MyPageUtil.makeResponseData(roleVoList, totalCount));
    }

    /**
     * 查看角色详情。
     *
     * @param roleId 指定角色主键Id。
     * @return 应答结果对象，包含角色详情对象。
     */
    @SaCheckPermission("sysRole.view")
    @GetMapping("/view")
    public ResponseResult<SysRoleVo> view(@RequestParam Long roleId) {
        if (MyCommonUtil.existBlankArgument(roleId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        SysRole sysRole = sysRoleService.getByIdWithRelation(roleId, MyRelationParam.full());
        if (sysRole == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        SysRoleVo sysRoleVo = MyModelUtil.copyTo(sysRole, SysRoleVo.class);
        return ResponseResult.success(sysRoleVo);
    }

    /**
     * 拥有指定角色的用户列表。
     *
     * @param roleId           角色主键Id。
     * @param sysUserDtoFilter 用户过滤对象。
     * @param orderParam       排序参数。
     * @param pageParam        分页参数。
     * @return 应答结果对象，包含用户列表数据。
     */
    @SaCheckPermission("sysRole.view")
    @PostMapping("/listUserRole")
    public ResponseResult<MyPageData<SysUserVo>> listUserRole(
            @MyRequestBody Long roleId,
            @MyRequestBody SysUserDto sysUserDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<Void> verifyResult = this.doRoleUserVerify(roleId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysUser filter = MyModelUtil.copyTo(sysUserDtoFilter, SysUser.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysUser.class);
        List<SysUser> userList = sysUserService.getSysUserListByRoleId(roleId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(userList, SysUserVo.class));
    }

    /**
     * 获取不包含指定角色Id的用户列表。
     * 用户和角色是多对多关系，当前接口将返回没有赋值指定RoleId的用户列表。可用于给角色添加新用户。
     *
     * @param roleId           角色主键Id。
     * @param sysUserDtoFilter 用户过滤对象。
     * @param orderParam       排序参数。
     * @param pageParam        分页参数。
     * @return 应答结果对象，包含用户列表数据。
     */
    @SaCheckPermission("sysRole.update")
    @PostMapping("/listNotInUserRole")
    public ResponseResult<MyPageData<SysUserVo>> listNotInUserRole(
            @MyRequestBody Long roleId,
            @MyRequestBody SysUserDto sysUserDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<Void> verifyResult = this.doRoleUserVerify(roleId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysUser filter = MyModelUtil.copyTo(sysUserDtoFilter, SysUser.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysUser.class);
        List<SysUser> userList = sysUserService.getNotInSysUserListByRoleId(roleId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(userList, SysUserVo.class));
    }

    /**
     * 为指定角色添加用户列表。该操作可同时给一批用户赋值角色，并在同一事务内完成。
     *
     * @param roleId           角色主键Id。
     * @param userIdListString 逗号分隔的用户Id列表。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysRole.update")
    @OperationLog(type = SysOperationLogType.ADD_M2M)
    @PostMapping("/addUserRole")
    public ResponseResult<Void> addUserRole(@MyRequestBody Long roleId, @MyRequestBody String userIdListString) {
        if (MyCommonUtil.existBlankArgument(roleId, userIdListString)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        Set<Long> userIdSet = Arrays.stream(
                userIdListString.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        if (!sysRoleService.existId(roleId)
                || !sysUserService.existUniqueKeyList("userId", userIdSet)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        List<SysUserRole> userRoleList = new LinkedList<>();
        for (Long userId : userIdSet) {
            SysUserRole userRole = new SysUserRole();
            userRole.setRoleId(roleId);
            userRole.setUserId(userId);
            userRoleList.add(userRole);
        }
        sysRoleService.addUserRoleList(userRoleList);
        return ResponseResult.success();
    }

    /**
     * 为指定用户移除指定角色。
     *
     * @param roleId 指定角色主键Id。
     * @param userId 指定用户主键Id。
     * @return 应答数据结果。
     */
    @SaCheckPermission("sysRole.update")
    @OperationLog(type = SysOperationLogType.DELETE_M2M)
    @PostMapping("/deleteUserRole")
    public ResponseResult<Void> deleteUserRole(@MyRequestBody Long roleId, @MyRequestBody Long userId) {
        if (MyCommonUtil.existBlankArgument(roleId, userId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysRoleService.removeUserRole(roleId, userId)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 以字典形式返回全部角色管理数据集合。字典的键值为[roleId, roleName]。
     * 白名单接口，登录用户均可访问。
     *
     * @param filter 过滤对象。
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict(@ParameterObject SysRoleDto filter) {
        List<SysRole> resultList = sysRoleService.getListByFilter(MyModelUtil.copyTo(filter, SysRole.class));
        return ResponseResult.success(MyCommonUtil.toDictDataList(resultList, SysRole::getRoleId, SysRole::getRoleName));
    }

    /**
     * 根据字典Id集合，获取查询后的字典数据。
     *
     * @param dictIds 字典Id集合。
     * @return 应答结果对象，包含字典形式的数据集合。
     */
    @GetMapping("/listDictByIds")
    public ResponseResult<List<Map<String, Object>>> listDictByIds(@RequestParam List<Long> dictIds) {
        List<SysRole> resultList = sysRoleService.getInList(new HashSet<>(dictIds));
        return ResponseResult.success(MyCommonUtil.toDictDataList(resultList, SysRole::getRoleId, SysRole::getRoleName));
    }

    private ResponseResult<Void> doRoleUserVerify(Long roleId) {
        if (MyCommonUtil.existBlankArgument(roleId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysRoleService.existId(roleId)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        return ResponseResult.success();
    }
}
