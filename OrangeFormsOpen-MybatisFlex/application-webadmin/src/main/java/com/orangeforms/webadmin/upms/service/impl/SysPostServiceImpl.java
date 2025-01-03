package com.orangeforms.webadmin.upms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.object.MyOrderParam;
import com.orangeforms.common.core.object.MyPageData;
import com.orangeforms.common.core.object.MyPageParam;
import com.orangeforms.common.core.object.MyRelationParam;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.core.util.MyPageUtil;
import com.orangeforms.common.ext.base.BizWidgetDatasource;
import com.orangeforms.common.ext.constant.BizWidgetDatasourceType;
import com.orangeforms.common.ext.util.BizWidgetDatasourceExtHelper;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import com.orangeforms.webadmin.upms.dao.SysDeptPostMapper;
import com.orangeforms.webadmin.upms.dao.SysPostMapper;
import com.orangeforms.webadmin.upms.dao.SysUserPostMapper;
import com.orangeforms.webadmin.upms.model.SysDeptPost;
import com.orangeforms.webadmin.upms.model.SysPost;
import com.orangeforms.webadmin.upms.model.SysUserPost;
import com.orangeforms.webadmin.upms.service.SysDeptService;
import com.orangeforms.webadmin.upms.service.SysPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位管理数据操作服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Service("sysPostService")
public class SysPostServiceImpl extends BaseService<SysPost, Long> implements SysPostService, BizWidgetDatasource {

    @Autowired
    private SysPostMapper sysPostMapper;
    @Autowired
    private SysUserPostMapper sysUserPostMapper;
    @Autowired
    private SysDeptPostMapper sysDeptPostMapper;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private BizWidgetDatasourceExtHelper bizWidgetDatasourceExtHelper;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysPost> mapper() {
        return sysPostMapper;
    }

    @PostConstruct
    private void registerBizWidgetDatasource() {
        bizWidgetDatasourceExtHelper.registerDatasource(BizWidgetDatasourceType.UPMS_POST_TYPE, this);
        bizWidgetDatasourceExtHelper.registerDatasource(BizWidgetDatasourceType.UPMS_DEPT_POST_TYPE, this);
    }

    @Override
    public MyPageData<Map<String, Object>> getDataList(
            String type, Map<String, Object> filter, MyOrderParam orderParam, MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        String orderBy = orderParam == null ? null : MyOrderParam.buildOrderBy(orderParam, SysPost.class);
        SysPost postFilter = filter == null ? null : BeanUtil.toBean(filter, SysPost.class);
        if (StrUtil.equals(type, BizWidgetDatasourceType.UPMS_POST_TYPE)) {
            List<SysPost> postList = this.getSysPostList(postFilter, orderBy);
            return MyPageUtil.makeResponseData(postList, BeanUtil::beanToMap);
        }
        Assert.notNull(filter, "filter can't be NULL.");
        Long deptId = (Long) filter.get("deptId");
        List<Map<String, Object>> dataList = sysDeptService.getSysDeptPostListWithRelationByDeptId(deptId);
        return MyPageUtil.makeResponseData(dataList);
    }

    @Override
    public List<Map<String, Object>> getDataListWithInList(String type, String fieldName, List<String> fieldValues) {
        List<SysPost> postList;
        if (StrUtil.isBlank(fieldName)) {
            postList = this.getInList(fieldValues.stream().map(Long::valueOf).collect(Collectors.toSet()));
        } else {
            postList = this.getInList(fieldName, MyModelUtil.convertToTypeValues(SysPost.class, fieldName, fieldValues));
        }
        return MyModelUtil.beanToMapList(postList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysPost saveNew(SysPost sysPost) {
        sysPost.setPostId(idGenerator.nextLongId());
        MyModelUtil.fillCommonsForInsert(sysPost);
        MyModelUtil.setDefaultValue(sysPost, "leaderPost", false);
        sysPostMapper.insert(sysPost);
        return sysPost;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysPost sysPost, SysPost originalSysPost) {
        MyModelUtil.fillCommonsForUpdate(sysPost, originalSysPost);
        // 这里重点提示，在执行主表数据更新之前，如果有哪些字段不支持修改操作，请用原有数据对象字段替换当前数据字段。
        return sysPostMapper.update(sysPost, false) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long postId) {
        if (sysPostMapper.deleteById(postId) != 1) {
            return false;
        }
        // 开始删除多对多父表的关联
        sysUserPostMapper.deleteByQuery(new QueryWrapper().eq(SysUserPost::getPostId, postId));
        sysDeptPostMapper.deleteByQuery(new QueryWrapper().eq(SysDeptPost::getPostId, postId));
        return true;
    }

    @Override
    public List<SysPost> getSysPostList(SysPost filter, String orderBy) {
        return sysPostMapper.getSysPostList(filter, orderBy);
    }

    @Override
    public List<SysPost> getSysPostListWithRelation(SysPost filter, String orderBy) {
        List<SysPost> resultList = sysPostMapper.getSysPostList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    @Override
    public List<SysPost> getNotInSysPostListByDeptId(Long deptId, SysPost filter, String orderBy) {
        List<SysPost> resultList = sysPostMapper.getNotInSysPostListByDeptId(deptId, filter, orderBy);
        this.buildRelationForDataList(resultList, MyRelationParam.dictOnly());
        return resultList;
    }

    @Override
    public List<SysPost> getSysPostListByDeptId(Long deptId, SysPost filter, String orderBy) {
        List<SysPost> resultList = sysPostMapper.getSysPostListByDeptId(deptId, filter, orderBy);
        this.buildRelationForDataList(resultList, MyRelationParam.dictOnly());
        return resultList;
    }

    @Override
    public List<SysUserPost> getSysUserPostListByUserId(Long userId) {
        return sysUserPostMapper.selectListByQuery(new QueryWrapper().eq(SysUserPost::getUserId, userId));
    }

    @Override
    public boolean existAllPrimaryKeys(Set<Long> deptPostIdSet, Long deptId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SysDeptPost::getDeptId, deptId);
        queryWrapper.in(SysDeptPost::getDeptPostId, deptPostIdSet);
        return sysDeptPostMapper.selectCountByQuery(queryWrapper) == deptPostIdSet.size();
    }
}
