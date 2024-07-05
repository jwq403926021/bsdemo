package com.orangeforms.webadmin.upms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.orangeforms.webadmin.upms.dto.SysMenuDto;
import com.orangeforms.webadmin.upms.vo.SysMenuVo;
import com.orangeforms.webadmin.upms.model.SysMenu;
import com.orangeforms.webadmin.upms.model.SysDataPerm;
import com.orangeforms.webadmin.upms.model.constant.SysMenuType;
import com.orangeforms.webadmin.upms.service.SysMenuService;
import com.orangeforms.webadmin.upms.service.SysDataPermService;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.*;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.groups.Default;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理接口控制器类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "菜单管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/upms/sysMenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysDataPermService sysDataPermService;

    /**
     * 添加新菜单操作。
     *
     * @param sysMenuDto 新菜单对象。
     * @return 应答结果对象，包含新增菜单的主键Id。
     */
    @ApiOperationSupport(ignoreParameters = {"sysMenuDto.menuId"})
    @SaCheckPermission("sysMenu.add")
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(@MyRequestBody SysMenuDto sysMenuDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysMenuDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysMenu sysMenu = MyModelUtil.copyTo(sysMenuDto, SysMenu.class);
        if (sysMenu.getParentId() != null) {
            SysMenu parentSysMenu = sysMenuService.getById(sysMenu.getParentId());
            if (parentSysMenu == null) {
                errorMessage = "数据验证失败，关联的父菜单不存在！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            if (parentSysMenu.getOnlineFormId() != null) {
                errorMessage = "数据验证失败，不能为动态表单菜单添加子菜单！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        CallResult result = sysMenuService.verifyRelatedData(sysMenu, null);
        if (!result.isSuccess()) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
        }
        sysMenuService.saveNew(sysMenu);
        return ResponseResult.success(sysMenu.getMenuId());
    }

    /**
     * 更新菜单数据操作。
     *
     * @param sysMenuDto 新菜单对象。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysMenu.update")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(@MyRequestBody SysMenuDto sysMenuDto) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysMenuDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysMenu originalSysMenu = sysMenuService.getById(sysMenuDto.getMenuId());
        if (originalSysMenu == null) {
            errorMessage = "数据验证失败，当前菜单并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        SysMenu sysMenu = MyModelUtil.copyTo(sysMenuDto, SysMenu.class);
        if (ObjectUtil.notEqual(originalSysMenu.getOnlineFormId(), sysMenu.getOnlineFormId())) {
            if (originalSysMenu.getOnlineFormId() == null) {
                errorMessage = "数据验证失败，不能为当前菜单添加在线表单Id属性！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            if (sysMenu.getOnlineFormId() == null) {
                errorMessage = "数据验证失败，不能去掉当前菜单的在线表单Id属性！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        if (originalSysMenu.getOnlineFormId() != null
                && originalSysMenu.getMenuType().equals(SysMenuType.TYPE_BUTTON)) {
            errorMessage = "数据验证失败，在线表单的内置菜单不能编辑！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        CallResult result = sysMenuService.verifyRelatedData(sysMenu, originalSysMenu);
        if (!result.isSuccess()) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
        }
        if (!sysMenuService.update(sysMenu, originalSysMenu)) {
            errorMessage = "数据验证失败，当前权限字并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 删除指定菜单操作。
     *
     * @param menuId 指定菜单主键Id。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysMenu.delete")
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long menuId) {
        if (MyCommonUtil.existBlankArgument(menuId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        String errorMessage;
        SysMenu menu = sysMenuService.getById(menuId);
        if (menu == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        if (menu.getOnlineFormId() != null && menu.getMenuType().equals(SysMenuType.TYPE_BUTTON)) {
            errorMessage = "数据验证失败，在线表单的内置菜单不能删除！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        // 对于在线表单，无需进行子菜单的验证，而是在删除的时候，连同子菜单一起删除。
        if (menu.getOnlineFormId() == null && sysMenuService.hasChildren(menuId)) {
            errorMessage = "数据验证失败，当前菜单存在下级菜单！";
            return ResponseResult.error(ErrorCodeEnum.HAS_CHILDREN_DATA, errorMessage);
        }
        List<SysDataPerm> dataPermList = sysDataPermService.getSysDataPermListByMenuId(menuId);
        if (CollUtil.isNotEmpty(dataPermList)) {
            SysDataPerm dataPerm = dataPermList.get(0);
            errorMessage = "数据验证失败，当前菜单正在被数据权限 [" + dataPerm.getDataPermName() + "] 引用，不能直接删除！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!sysMenuService.remove(menu)) {
            errorMessage = "数据操作失败，菜单不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 获取全部菜单列表。
     *
     * @return 应答结果对象，包含全部菜单数据列表。
     */
    @SaCheckPermission("sysMenu.view")
    @PostMapping("/list")
    public ResponseResult<List<SysMenuVo>> list() {
        List<SysMenu> resultList = this.getAllMenuListByShowOrder();
        return ResponseResult.success(MyModelUtil.copyCollectionTo(resultList, SysMenuVo.class));
    }

    /**
     * 查看指定菜单数据详情。
     *
     * @param menuId 指定菜单主键Id。
     * @return 应答结果对象，包含菜单详情。
     */
    @SaCheckPermission("sysMenu.view")
    @GetMapping("/view")
    public ResponseResult<SysMenuVo> view(@RequestParam Long menuId) {
        if (MyCommonUtil.existBlankArgument(menuId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        SysMenu sysMenu = sysMenuService.getByIdWithRelation(menuId, MyRelationParam.full());
        if (sysMenu == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        SysMenuVo sysMenuVo = MyModelUtil.copyTo(sysMenu, SysMenuVo.class);
        return ResponseResult.success(sysMenuVo);
    }

    /**
     * 以字典形式返回目录和菜单类型的菜单管理数据集合。字典的键值为[menuId, menuName]。
     * 白名单接口，登录用户均可访问。
     *
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listMenuDict")
    public ResponseResult<List<Map<String, Object>>> listMenuDict() {
        List<SysMenu> resultList = this.getAllMenuListByShowOrder();
        resultList = resultList.stream()
                .filter(m -> m.getMenuType() <= SysMenuType.TYPE_MENU).collect(Collectors.toList());
        return ResponseResult.success(
                MyCommonUtil.toDictDataList(resultList, SysMenu::getMenuId, SysMenu::getMenuName, SysMenu::getParentId));
    }

    /**
     * 以字典形式返回全部的菜单管理数据集合。字典的键值为[menuId, menuName]。
     * 白名单接口，登录用户均可访问。
     *
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict() {
        List<SysMenu> resultList = this.getAllMenuListByShowOrder();
        return ResponseResult.success(
                MyCommonUtil.toDictDataList(resultList, SysMenu::getMenuId, SysMenu::getMenuName, SysMenu::getParentId));
    }    

    private List<SysMenu> getAllMenuListByShowOrder() {
        return sysMenuService.getAllListByOrder("showOrder");
    }
}
