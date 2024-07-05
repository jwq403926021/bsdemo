package com.orangeforms.webadmin.upms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mybatisflex.core.query.QueryWrapper;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import com.orangeforms.common.core.constant.DataPermRuleType;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.object.MyRelationParam;
import com.orangeforms.common.core.object.CallResult;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.core.util.RedisKeyUtil;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.webadmin.config.ApplicationConfig;
import com.orangeforms.webadmin.upms.dao.SysDataPermDeptMapper;
import com.orangeforms.webadmin.upms.dao.SysDataPermMapper;
import com.orangeforms.webadmin.upms.dao.SysDataPermUserMapper;
import com.orangeforms.webadmin.upms.dao.SysDataPermMenuMapper;
import com.orangeforms.webadmin.upms.model.*;
import com.orangeforms.webadmin.upms.service.SysDataPermService;
import com.orangeforms.webadmin.upms.service.SysDeptService;
import com.orangeforms.webadmin.upms.service.SysMenuService;
import com.orangeforms.webadmin.upms.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据权限数据服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Service("sysDataPermService")
public class SysDataPermServiceImpl extends BaseService<SysDataPerm, Long> implements SysDataPermService {

    @Autowired
    private SysDataPermMapper sysDataPermMapper;
    @Autowired
    private SysDataPermDeptMapper sysDataPermDeptMapper;
    @Autowired
    private SysDataPermUserMapper sysDataPermUserMapper;
    @Autowired
    private SysDataPermMenuMapper sysDataPermMenuMapper;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ApplicationConfig applicationConfig;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回主对象的Mapper对象。
     *
     * @return 主对象的Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysDataPerm> mapper() {
        return sysDataPermMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysDataPerm saveNew(SysDataPerm dataPerm, Set<Long> deptIdSet, Set<Long> menuIdSet) {
        dataPerm.setDataPermId(idGenerator.nextLongId());
        MyModelUtil.fillCommonsForInsert(dataPerm);
        sysDataPermMapper.insert(dataPerm);
        this.insertRelationData(dataPerm, deptIdSet, menuIdSet);
        return dataPerm;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(
            SysDataPerm dataPerm, SysDataPerm originalDataPerm, Set<Long> deptIdSet, Set<Long> menuIdSet) {
        MyModelUtil.fillCommonsForUpdate(dataPerm, originalDataPerm);
        if (sysDataPermMapper.update(dataPerm, false) != 1) {
            return false;
        }
        sysDataPermDeptMapper.deleteByQuery(
                new QueryWrapper().eq(SysDataPermDept::getDataPermId, dataPerm.getDataPermId()));
        sysDataPermMenuMapper.deleteByQuery(
                new QueryWrapper().eq(SysDataPermMenu::getDataPermId, dataPerm.getDataPermId()));
        this.insertRelationData(dataPerm, deptIdSet, menuIdSet);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long dataPermId) {
        if (sysDataPermMapper.deleteById(dataPermId) != 1) {
            return false;
        }
        sysDataPermDeptMapper.deleteByQuery(new QueryWrapper().eq(SysDataPermDept::getDataPermId, dataPermId));
        sysDataPermUserMapper.deleteByQuery(new QueryWrapper().eq(SysDataPermUser::getDataPermId, dataPermId));
        sysDataPermMenuMapper.deleteByQuery(new QueryWrapper().eq(SysDataPermMenu::getDataPermId, dataPermId));
        return true;
    }

    @Override
    public List<SysDataPerm> getSysDataPermListWithRelation(SysDataPerm filter, String orderBy) {
        List<SysDataPerm> resultList = sysDataPermMapper.getSysDataPermList(filter, orderBy);
        buildRelationForDataList(resultList, MyRelationParam.full(), CollUtil.newHashSet("dataPermDeptList"));
        return resultList;
    }

    @Override
    public void putDataPermCache(String sessionId, Long userId, Long deptId) {
        Map<String, Map<Integer, String>> menuDataPermMap = getSysDataPermListByUserId(userId, deptId);
        if (menuDataPermMap.size() > 0) {
            String dataPermSessionKey = RedisKeyUtil.makeSessionDataPermIdKey(sessionId);
            RBucket<String> bucket = redissonClient.getBucket(dataPermSessionKey);
            bucket.set(JSON.toJSONString(menuDataPermMap),
                    applicationConfig.getSessionExpiredSeconds(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void removeDataPermCache(String sessionId) {
        String sessionPermKey = RedisKeyUtil.makeSessionDataPermIdKey(sessionId);
        redissonClient.getBucket(sessionPermKey).deleteAsync();
    }

    @Override
    public Map<String, Map<Integer, String>> getSysDataPermListByUserId(Long userId, Long deptId) {
        List<SysDataPerm> dataPermList = sysDataPermMapper.getSysDataPermListByUserId(userId);
        dataPermList.forEach(dataPerm -> {
            if (CollUtil.isNotEmpty(dataPerm.getDataPermDeptList())) {
                Set<Long> deptIdSet = dataPerm.getDataPermDeptList().stream()
                        .map(SysDataPermDept::getDeptId).collect(Collectors.toSet());
                dataPerm.setDeptIdListString(StrUtil.join(",", deptIdSet));
            }
        });
        Map<String, List<SysDataPerm>> menuIdMap = new HashMap<>(4);
        for (SysDataPerm dataPerm : dataPermList) {
            if (CollUtil.isNotEmpty(dataPerm.getDataPermMenuList())) {
                for (SysDataPermMenu dataPermMenu : dataPerm.getDataPermMenuList()) {
                    menuIdMap.computeIfAbsent(
                            dataPermMenu.getMenuId().toString(), k -> new LinkedList<>()).add(dataPerm);
                }
            } else {
                menuIdMap.computeIfAbsent(
                        ApplicationConstant.DATA_PERM_ALL_MENU_ID, k -> new LinkedList<>()).add(dataPerm);
            }
        }
        Map<String, Map<Integer, String>> menuResultMap = new HashMap<>(menuIdMap.size());
        for (Map.Entry<String, List<SysDataPerm>> entry : menuIdMap.entrySet()) {
            Map<Integer, String> resultMap = this.mergeAndOptimizeDataPermRule(entry.getValue(), deptId);
            menuResultMap.put(entry.getKey(), resultMap);
        }
        return menuResultMap;
    }

    @Override
    public List<SysDataPerm> getSysDataPermListByMenuId(Long menuId) {
        return sysDataPermMapper.getSysDataPermListByMenuId(menuId);
    }

    private Map<Integer, String> mergeAndOptimizeDataPermRule(List<SysDataPerm> dataPermList, Long deptId) {
        // 为了更方便进行后续的合并优化处理，这里再基于菜单Id和规则类型进行分组。ruleMap的key是规则类型。
        Map<Integer, List<SysDataPerm>> ruleMap =
                dataPermList.stream().collect(Collectors.groupingBy(SysDataPerm::getRuleType));
        Map<Integer, String> resultMap = new HashMap<>(ruleMap.size());
        // 如有有ALL存在，就可以直接退出了，没有必要在处理后续的规则了。
        if (ruleMap.containsKey(DataPermRuleType.TYPE_ALL)) {
            resultMap.put(DataPermRuleType.TYPE_ALL, "null");
            return resultMap;
        }
        // 这里优先合并最复杂的多部门及子部门场景。
        String deptIds = processMultiDeptAndChildren(ruleMap, deptId);
        if (deptIds != null) {
            resultMap.put(DataPermRuleType.TYPE_MULTI_DEPT_AND_CHILD_DEPT, deptIds);
        }
        // 合并当前部门及子部门的优化
        if (ruleMap.get(DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT) != null) {
            // 需要与仅仅当前部门规则进行合并。
            ruleMap.remove(DataPermRuleType.TYPE_DEPT_ONLY);
            resultMap.put(DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT, "null");
        }
        // 合并自定义部门了。
        deptIds = processMultiDept(ruleMap, deptId);
        if (deptIds != null) {
            resultMap.put(DataPermRuleType.TYPE_CUSTOM_DEPT_LIST, deptIds);
        }
        // 最后处理当前部门和当前用户。
        if (ruleMap.get(DataPermRuleType.TYPE_DEPT_ONLY) != null) {
            resultMap.put(DataPermRuleType.TYPE_DEPT_ONLY, "null");
        }
        if (ruleMap.get(DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT_USERS) != null) {
            // 合并当前部门用户和当前用户
            ruleMap.remove(DataPermRuleType.TYPE_USER_ONLY);
            ruleMap.remove(DataPermRuleType.TYPE_DEPT_USERS);
            SysUser filter = new SysUser();
            filter.setDeptId(deptId);
            List<SysUser> userList = sysUserService.getSysUserList(filter, null);
            Set<Long> userIdSet = userList.stream().map(SysUser::getUserId).collect(Collectors.toSet());
            resultMap.put(DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT_USERS, CollUtil.join(userIdSet, ","));
        }
        if (ruleMap.get(DataPermRuleType.TYPE_DEPT_USERS) != null) {
            SysUser filter = new SysUser();
            filter.setDeptId(deptId);
            List<SysUser> userList = sysUserService.getListByFilter(filter);
            Set<Long> userIdSet = userList.stream().map(SysUser::getUserId).collect(Collectors.toSet());
            // 合并仅当前用户
            ruleMap.remove(DataPermRuleType.TYPE_USER_ONLY);
            resultMap.put(DataPermRuleType.TYPE_DEPT_USERS, CollUtil.join(userIdSet, ","));
        }
        if (ruleMap.get(DataPermRuleType.TYPE_USER_ONLY) != null) {
            resultMap.put(DataPermRuleType.TYPE_USER_ONLY, "null");
        }
        return resultMap;
    }

    private String processMultiDeptAndChildren(Map<Integer, List<SysDataPerm>> ruleMap, Long deptId) {
        List<SysDataPerm> parentDeptList = ruleMap.get(DataPermRuleType.TYPE_MULTI_DEPT_AND_CHILD_DEPT);
        if (parentDeptList == null) {
            return null;
        }
        Set<Long> deptIdSet = new HashSet<>();
        for (SysDataPerm parentDept : parentDeptList) {
            deptIdSet.addAll(StrUtil.split(parentDept.getDeptIdListString(), ',')
                    .stream().map(Long::valueOf).collect(Collectors.toSet()));
        }
        // 在合并所有的多父部门Id之后，需要判断是否有本部门及子部门的规则。如果有，就继续合并。
        if (ruleMap.containsKey(DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT)) {
            // 如果多父部门列表中包含当前部门，那么可以直接删除该规则了，如果没包含，就加入到多部门的DEPT_ID的IN LIST中。
            deptIdSet.add(deptId);
            ruleMap.remove(DataPermRuleType.TYPE_DEPT_AND_CHILD_DEPT);
        }
        // 需要与仅仅当前部门规则进行合并。
        if (ruleMap.containsKey(DataPermRuleType.TYPE_DEPT_ONLY) && deptIdSet.contains(deptId)) {
            ruleMap.remove(DataPermRuleType.TYPE_DEPT_ONLY);
        }
        return StrUtil.join(",", deptIdSet);
    }

    private String processMultiDept(Map<Integer, List<SysDataPerm>> ruleMap, Long deptId) {
        List<SysDataPerm> customDeptList = ruleMap.get(DataPermRuleType.TYPE_CUSTOM_DEPT_LIST);
        if (customDeptList == null) {
            return null;
        }
        Set<Long> deptIdSet = new HashSet<>();
        for (SysDataPerm customDept : customDeptList) {
            deptIdSet.addAll(StrUtil.split(customDept.getDeptIdListString(), ',')
                    .stream().map(Long::valueOf).collect(Collectors.toSet()));
        }
        if (ruleMap.containsKey(DataPermRuleType.TYPE_DEPT_ONLY)) {
            deptIdSet.add(deptId);
            ruleMap.remove(DataPermRuleType.TYPE_DEPT_ONLY);
        }
        return StrUtil.join(",", deptIdSet);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addDataPermUserList(Long dataPermId, Set<Long> userIdSet) {
        for (Long userId : userIdSet) {
            SysDataPermUser dataPermUser = new SysDataPermUser();
            dataPermUser.setDataPermId(dataPermId);
            dataPermUser.setUserId(userId);
            sysDataPermUserMapper.insert(dataPermUser);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeDataPermUser(Long dataPermId, Long userId) {
        SysDataPermUser dataPermUser = new SysDataPermUser();
        dataPermUser.setDataPermId(dataPermId);
        dataPermUser.setUserId(userId);
        return sysDataPermUserMapper.deleteByQuery(QueryWrapper.create(dataPermUser)) == 1;
    }

    @Override
    public CallResult verifyRelatedData(SysDataPerm dataPerm, String deptIdListString, String menuIdListString) {
        JSONObject jsonObject = new JSONObject();
        if (dataPerm.getRuleType() == DataPermRuleType.TYPE_MULTI_DEPT_AND_CHILD_DEPT
                || dataPerm.getRuleType() == DataPermRuleType.TYPE_CUSTOM_DEPT_LIST) {
            if (StrUtil.isBlank(deptIdListString)) {
                return CallResult.error("数据验证失败，部门列表不能为空！");
            }
            Set<Long> deptIdSet = StrUtil.split(
                    deptIdListString, ",").stream().map(Long::valueOf).collect(Collectors.toSet());
            if (!sysDeptService.existAllPrimaryKeys(deptIdSet)) {
                return CallResult.error("数据验证失败，存在不合法的部门数据，请刷新后重试！");
            }
            jsonObject.put("deptIdSet", deptIdSet);
        }
        if (StrUtil.isNotBlank(menuIdListString)) {
            Set<Long> menuIdSet = StrUtil.split(
                    menuIdListString, ",").stream().map(Long::valueOf).collect(Collectors.toSet());
            if (!sysMenuService.existAllPrimaryKeys(menuIdSet)) {
                return CallResult.error("数据验证失败，存在不合法的菜单数据，请刷新后重试！");
            }
            jsonObject.put("menuIdSet", menuIdSet);
        }
        return CallResult.ok(jsonObject);
    }

    private void insertRelationData(SysDataPerm dataPerm, Set<Long> deptIdSet, Set<Long> menuIdSet) {
        if (CollUtil.isNotEmpty(deptIdSet)) {
            for (Long deptId : deptIdSet) {
                SysDataPermDept dataPermDept = new SysDataPermDept();
                dataPermDept.setDataPermId(dataPerm.getDataPermId());
                dataPermDept.setDeptId(deptId);
                sysDataPermDeptMapper.insert(dataPermDept);
            }
        }
        if (CollUtil.isNotEmpty(menuIdSet)) {
            for (Long menuId : menuIdSet) {
                SysDataPermMenu dataPermMenu = new SysDataPermMenu();
                dataPermMenu.setDataPermId(dataPerm.getDataPermId());
                dataPermMenu.setMenuId(menuId);
                sysDataPermMenuMapper.insert(dataPermMenu);
            }
        }
    }
}
