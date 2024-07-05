package com.orangeforms.webadmin.upms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.page.PageMethod;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.core.util.MyPageUtil;
import com.orangeforms.common.ext.base.BizWidgetDatasource;
import com.orangeforms.common.ext.constant.BizWidgetDatasourceType;
import com.orangeforms.common.ext.util.BizWidgetDatasourceExtHelper;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import com.orangeforms.webadmin.upms.dao.SysRoleMapper;
import com.orangeforms.webadmin.upms.dao.SysRoleMenuMapper;
import com.orangeforms.webadmin.upms.dao.SysUserRoleMapper;
import com.orangeforms.webadmin.upms.model.SysRole;
import com.orangeforms.webadmin.upms.model.SysRoleMenu;
import com.orangeforms.webadmin.upms.model.SysUserRole;
import com.orangeforms.webadmin.upms.service.SysMenuService;
import com.orangeforms.webadmin.upms.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色数据服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Service("sysRoleService")
public class SysRoleServiceImpl extends BaseService<SysRole, Long> implements SysRoleService, BizWidgetDatasource {

    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private BizWidgetDatasourceExtHelper bizWidgetDatasourceExtHelper;

    /**
     * 返回主对象的Mapper对象。
     *
     * @return 主对象的Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysRole> mapper() {
        return sysRoleMapper;
    }

    @PostConstruct
    private void registerBizWidgetDatasource() {
        bizWidgetDatasourceExtHelper.registerDatasource(BizWidgetDatasourceType.UPMS_ROLE_TYPE, this);
    }

    @Override
    public MyPageData<Map<String, Object>> getDataList(
            String type, Map<String, Object> filter, MyOrderParam orderParam, MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        String orderBy = orderParam == null ? null : MyOrderParam.buildOrderBy(orderParam, SysRole.class);
        SysRole roleFilter = filter == null ? null : BeanUtil.toBean(filter, SysRole.class);
        List<SysRole> roleList = this.getSysRoleList(roleFilter, orderBy);
        return MyPageUtil.makeResponseData(roleList, BeanUtil::beanToMap);
    }

    @Override
    public List<Map<String, Object>> getDataListWithInList(String type, String fieldName, List<String> fieldValues) {
        List<SysRole> roleList;
        if (StrUtil.isBlank(fieldName)) {
            roleList = this.getInList(fieldValues.stream().map(Long::valueOf).collect(Collectors.toSet()));
        } else {
            roleList = this.getInList(fieldName, MyModelUtil.convertToTypeValues(SysRole.class, fieldName, fieldValues));
        }
        return MyModelUtil.beanToMapList(roleList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysRole saveNew(SysRole role, Set<Long> menuIdSet) {
        role.setRoleId(idGenerator.nextLongId());
        MyModelUtil.fillCommonsForInsert(role);
        sysRoleMapper.insert(role);
        if (menuIdSet != null) {
            for (Long menuId : menuIdSet) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getRoleId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysRole role, SysRole originalRole, Set<Long> menuIdSet) {
        MyModelUtil.fillCommonsForUpdate(role, originalRole);
        if (sysRoleMapper.update(role) != 1) {
            return false;
        }
        SysRoleMenu deletedRoleMenu = new SysRoleMenu();
        deletedRoleMenu.setRoleId(role.getRoleId());
        sysRoleMenuMapper.deleteByQuery(QueryWrapper.create(deletedRoleMenu));
        if (menuIdSet != null) {
            for (Long menuId : menuIdSet) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getRoleId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long roleId) {
        if (sysRoleMapper.deleteById(roleId) != 1) {
            return false;
        }
        sysRoleMenuMapper.deleteByQuery(new QueryWrapper().eq(SysRoleMenu::getRoleId, roleId));
        sysUserRoleMapper.deleteByQuery(new QueryWrapper().eq(SysUserRole::getRoleId, roleId));
        return true;
    }

    @Override
    public List<SysRole> getSysRoleList(SysRole filter, String orderBy) {
        return sysRoleMapper.getSysRoleList(filter, orderBy);
    }

    @Override
    public List<SysUserRole> getSysUserRoleListByUserId(Long userId) {
        SysUserRole filter = new SysUserRole();
        filter.setUserId(userId);
        return sysUserRoleMapper.selectListByQuery(new QueryWrapper().eq(SysUserRole::getUserId, userId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserRoleList(List<SysUserRole> userRoleList) {
        for (SysUserRole userRole : userRoleList) {
            sysUserRoleMapper.insert(userRole);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeUserRole(Long roleId, Long userId) {
        SysUserRole userRole  = new SysUserRole();
        userRole.setRoleId(roleId);
        userRole.setUserId(userId);
        return sysUserRoleMapper.deleteByQuery(QueryWrapper.create(userRole)) == 1;
    }

    @Override
    public CallResult verifyRelatedData(SysRole sysRole, SysRole originalSysRole, String menuIdListString) {
        JSONObject jsonObject = null;
        if (StringUtils.isNotBlank(menuIdListString)) {
            Set<Long> menuIdSet = Arrays.stream(
                    menuIdListString.split(",")).map(Long::valueOf).collect(Collectors.toSet());
            if (!sysMenuService.existAllPrimaryKeys(menuIdSet)) {
                return CallResult.error("数据验证失败，存在不合法的菜单权限，请刷新后重试！");
            }
            jsonObject = new JSONObject();
            jsonObject.put("menuIdSet", menuIdSet);
        }
        return CallResult.ok(jsonObject);
    }
}
