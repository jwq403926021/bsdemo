package com.orangeforms.webadmin.upms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.core.object.CallResult;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import com.orangeforms.webadmin.upms.bo.SysMenuExtraData;
import com.orangeforms.webadmin.upms.dao.SysMenuMapper;
import com.orangeforms.webadmin.upms.dao.SysRoleMenuMapper;
import com.orangeforms.webadmin.upms.model.SysMenu;
import com.orangeforms.webadmin.upms.model.SysRoleMenu;
import com.orangeforms.webadmin.upms.model.constant.SysMenuType;
import com.orangeforms.webadmin.upms.model.constant.SysOnlineMenuPermType;
import com.orangeforms.webadmin.upms.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单数据服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Service("sysMenuService")
public class SysMenuServiceImpl extends BaseService<SysMenu, Long> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回主对象的Mapper对象。
     *
     * @return 主对象的Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysMenu> mapper() {
        return sysMenuMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysMenu saveNew(SysMenu sysMenu) {
        sysMenu.setMenuId(idGenerator.nextLongId());
        MyModelUtil.fillCommonsForInsert(sysMenu);
        sysMenuMapper.insert(sysMenu);
        // 判断当前菜单是否为指向在线表单的菜单，并将根据约定，动态插入两个子菜单。
        if (sysMenu.getOnlineFormId() != null && sysMenu.getOnlineFlowEntryId() == null) {
            SysMenu viewSubMenu = new SysMenu();
            viewSubMenu.setMenuId(idGenerator.nextLongId());
            viewSubMenu.setParentId(sysMenu.getMenuId());
            viewSubMenu.setMenuType(SysMenuType.TYPE_BUTTON);
            viewSubMenu.setMenuName("查看");
            viewSubMenu.setShowOrder(0);
            viewSubMenu.setOnlineFormId(sysMenu.getOnlineFormId());
            viewSubMenu.setOnlineMenuPermType(SysOnlineMenuPermType.TYPE_VIEW);
            MyModelUtil.fillCommonsForInsert(viewSubMenu);
            sysMenuMapper.insert(viewSubMenu);
            SysMenu editSubMenu = new SysMenu();
            editSubMenu.setMenuId(idGenerator.nextLongId());
            editSubMenu.setParentId(sysMenu.getMenuId());
            editSubMenu.setMenuType(SysMenuType.TYPE_BUTTON);
            editSubMenu.setMenuName("编辑");
            editSubMenu.setShowOrder(1);
            editSubMenu.setOnlineFormId(sysMenu.getOnlineFormId());
            editSubMenu.setOnlineMenuPermType(SysOnlineMenuPermType.TYPE_EDIT);
            MyModelUtil.fillCommonsForInsert(editSubMenu);
            sysMenuMapper.insert(editSubMenu);
        }
        return sysMenu;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysMenu sysMenu, SysMenu originalSysMenu) {
        MyModelUtil.fillCommonsForUpdate(sysMenu, originalSysMenu);
        sysMenu.setMenuType(originalSysMenu.getMenuType());
        if (sysMenuMapper.update(sysMenu, false) != 1) {
            return false;
        }
        // 如果当前菜单的在线表单Id变化了，就需要同步更新他的内置子菜单也同步更新。
        if (ObjectUtil.notEqual(originalSysMenu.getOnlineFormId(), sysMenu.getOnlineFormId())) {
            SysMenu onlineSubMenu = new SysMenu();
            onlineSubMenu.setOnlineFormId(sysMenu.getOnlineFormId());
            sysMenuMapper.updateByQuery(onlineSubMenu,
                    new QueryWrapper().eq(SysMenu::getParentId, sysMenu.getMenuId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(SysMenu menu) {
        Long menuId = menu.getMenuId();
        if (sysMenuMapper.deleteByQuery(new QueryWrapper().eq(SysMenu::getMenuId, menuId)) != 1) {
            return false;
        }
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setMenuId(menuId);
        sysRoleMenuMapper.deleteByQuery(QueryWrapper.create(roleMenu));
        // 如果为指向在线表单的菜单，则连同删除子菜单
        if (menu.getOnlineFormId() != null) {
            SysMenu filter = new SysMenu();
            filter.setParentId(menuId);
            List<SysMenu> childMenus = sysMenuMapper.selectListByQuery(QueryWrapper.create(filter));
            sysMenuMapper.deleteByQuery(new QueryWrapper().eq(SysMenu::getParentId, menuId));
            if (CollUtil.isNotEmpty(childMenus)) {
                List<Long> childMenuIds = childMenus.stream().map(SysMenu::getMenuId).collect(Collectors.toList());
                sysRoleMenuMapper.deleteByQuery(new QueryWrapper().in(SysRoleMenu::getMenuId, childMenuIds));
            }
        }
        return true;
    }

    @Override
    public Collection<SysMenu> getMenuListByUserId(Long userId) {
        List<SysMenu> menuList = sysMenuMapper.getMenuListByUserId(userId);
        return this.distinctMenuList(menuList);
    }

    @Override
    public Collection<SysMenu> getMenuListByRoleIds(String roleIds) {
        if (StrUtil.isBlank(roleIds)) {
            return CollUtil.empty(Long.class);
        }
        Set<Long> roleIdSet = StrUtil.split(roleIds, ",").stream().map(Long::valueOf).collect(Collectors.toSet());
        List<SysMenu> menuList = sysMenuMapper.getMenuListByRoleIds(roleIdSet);
        return this.distinctMenuList(menuList);
    }

    @Override
    public boolean hasChildren(Long menuId) {
        SysMenu menu = new SysMenu();
        menu.setParentId(menuId);
        return this.getCountByFilter(menu) > 0;
    }

    @Override
    public CallResult verifyRelatedData(SysMenu sysMenu, SysMenu originalSysMenu) {
        // menu、ui fragment和button类型的menu不能没有parentId
        if (sysMenu.getParentId() == null && sysMenu.getMenuType() != SysMenuType.TYPE_DIRECTORY) {
            return CallResult.error("数据验证失败，当前类型菜单项的上级菜单不能为空！");
        }
        if (this.needToVerify(sysMenu, originalSysMenu, SysMenu::getParentId)) {
            String errorMessage = checkErrorOfNonDirectoryMenu(sysMenu);
            if (errorMessage != null) {
                return CallResult.error(errorMessage);
            }
        }
        if (!this.verifyMenuCode(sysMenu, originalSysMenu)) {
            return CallResult.error("数据验证失败，菜单编码已存在，不能重复使用！");
        }
        return CallResult.ok();
    }

    @Override
    public List<SysMenu> getAllOnlineMenuList(Integer menuType) {
        QueryWrapper queryWrapper = new QueryWrapper().isNotNull(SysMenu::getOnlineFormId);
        if (menuType != null) {
            queryWrapper.eq(SysMenu::getMenuType, menuType);
        }
        return sysMenuMapper.selectListByQuery(queryWrapper);
    }

    private boolean verifyMenuCode(SysMenu sysMenu, SysMenu originalSysMenu) {
        if (sysMenu.getExtraData() == null) {
            return true;
        }
        String menuCode = JSON.parseObject(sysMenu.getExtraData(), SysMenuExtraData.class).getMenuCode();
        if (StrUtil.isBlank(menuCode)) {
            return true;
        }
        String originalMenuCode = "";
        if (originalSysMenu != null && originalSysMenu.getExtraData() != null) {
            originalMenuCode = JSON.parseObject(originalSysMenu.getExtraData(), SysMenuExtraData.class).getMenuCode();
        }
        return StrUtil.equals(menuCode, originalMenuCode)
                || sysMenuMapper.countMenuCode("\"menuCode\":\"" + menuCode + "\"") == 0;
    }

    private String checkErrorOfNonDirectoryMenu(SysMenu sysMenu) {
        // 判断父节点是否存在
        SysMenu parentSysMenu = getById(sysMenu.getParentId());
        if (parentSysMenu == null) {
            return "数据验证失败，关联的上级菜单并不存在，请刷新后重试！";
        }
        // 逐个判断每种类型的菜单，他的父菜单的合法性，先从目录类型和菜单类型开始
        if (sysMenu.getMenuType() == SysMenuType.TYPE_DIRECTORY
                || sysMenu.getMenuType() == SysMenuType.TYPE_MENU) {
            // 他们的上级只能是目录
            if (parentSysMenu.getMenuType() != SysMenuType.TYPE_DIRECTORY) {
                return "数据验证失败，当前类型菜单项的上级菜单只能是目录类型！";
            }
        } else if (sysMenu.getMenuType() == SysMenuType.TYPE_UI_FRAGMENT) {
            // ui fragment的上级只能是menu类型
            if (parentSysMenu.getMenuType() != SysMenuType.TYPE_MENU) {
                return "数据验证失败，当前类型菜单项的上级菜单只能是菜单类型和按钮类型！";
            }
        } else if (sysMenu.getMenuType() == SysMenuType.TYPE_BUTTON) {
            // button的上级只能是menu和ui fragment
            if (parentSysMenu.getMenuType() != SysMenuType.TYPE_MENU
                    && parentSysMenu.getMenuType() != SysMenuType.TYPE_UI_FRAGMENT) {
                return "数据验证失败，当前类型菜单项的上级菜单只能是菜单类型和UI片段类型！";
            }
        } else {
            return "数据验证失败，不支持的菜单类型！";
        }
        return null;
    }

    private Collection<SysMenu> distinctMenuList(List<SysMenu> menuList) {
        LinkedHashMap<Long, SysMenu> menuMap = new LinkedHashMap<>();
        for (SysMenu menu : menuList) {
            menuMap.put(menu.getMenuId(), menu);
        }
        return menuMap.values();
    }
}
