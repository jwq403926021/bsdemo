package com.orangeforms.common.dict.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.constant.GlobalDeletedFlag;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.dict.constant.GlobalDictItemStatus;
import com.orangeforms.common.dict.dao.GlobalDictItemMapper;
import com.orangeforms.common.dict.model.GlobalDictItem;
import com.orangeforms.common.dict.service.GlobalDictItemService;
import com.orangeforms.common.dict.service.GlobalDictService;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 全局字典项目数据操作服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_GLOBAL_DICT_TYPE)
@Service("globalDictItemService")
public class GlobalDictItemServiceImpl
        extends BaseService<GlobalDictItem, Long> implements GlobalDictItemService {

    @Autowired
    private GlobalDictItemMapper globalDictItemMapper;
    @Autowired
    private GlobalDictService globalDictService;
    @Autowired
    private IdGeneratorWrapper idGenerator;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<GlobalDictItem> mapper() {
        return globalDictItemMapper;
    }

    @Override
    public GlobalDictItem saveNew(GlobalDictItem globalDictItem) {
        globalDictService.removeCache(globalDictItem.getDictCode());
        globalDictItem.setId(idGenerator.nextLongId());
        globalDictItem.setDeletedFlag(GlobalDeletedFlag.NORMAL);
        globalDictItem.setStatus(GlobalDictItemStatus.NORMAL);
        globalDictItem.setCreateUserId(TokenData.takeFromRequest().getUserId());
        globalDictItem.setUpdateUserId(globalDictItem.getCreateUserId());
        globalDictItem.setCreateTime(new Date());
        globalDictItem.setUpdateTime(globalDictItem.getCreateTime());
        globalDictItemMapper.insert(globalDictItem);
        return globalDictItem;
    }

    @Override
    public boolean update(GlobalDictItem globalDictItem, GlobalDictItem originalGlobalDictItem) {
        globalDictService.removeCache(globalDictItem.getDictCode());
        // 该方法不能直接修改字典状态。
        globalDictItem.setStatus(originalGlobalDictItem.getStatus());
        globalDictItem.setCreateUserId(originalGlobalDictItem.getCreateUserId());
        globalDictItem.setCreateTime(originalGlobalDictItem.getCreateTime());
        globalDictItem.setUpdateUserId(TokenData.takeFromRequest().getUserId());
        globalDictItem.setUpdateTime(new Date());
        return globalDictItemMapper.update(globalDictItem) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateNewCode(String oldCode, String newCode) {
        GlobalDictItem globalDictItem = new GlobalDictItem();
        globalDictItem.setDictCode(newCode);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(GlobalDictItem::getDictCode, oldCode);
        globalDictItemMapper.updateByQuery(globalDictItem, queryWrapper);
    }

    @Override
    public void updateStatus(GlobalDictItem globalDictItem, Integer status) {
        globalDictService.removeCache(globalDictItem.getDictCode());
        globalDictItem.setStatus(status);
        globalDictItem.setUpdateUserId(TokenData.takeFromRequest().getUserId());
        globalDictItem.setUpdateTime(new Date());
        globalDictItemMapper.update(globalDictItem);
    }

    @Override
    public boolean remove(GlobalDictItem globalDictItem) {
        globalDictService.removeCache(globalDictItem.getDictCode());
        return this.removeById(globalDictItem.getId());
    }

    @Override
    public boolean existDictCodeAndItemId(String dictCode, Serializable itemId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(GlobalDictItem::getDictCode, dictCode);
        queryWrapper.eq(GlobalDictItem::getItemId, itemId.toString());
        return globalDictItemMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public GlobalDictItem getGlobalDictItemByDictCodeAndItemId(String dictCode, Serializable itemId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(GlobalDictItem::getDictCode, dictCode);
        queryWrapper.eq(GlobalDictItem::getItemId, itemId.toString());
        return globalDictItemMapper.selectOneByQuery(queryWrapper);
    }

    @Override
    public List<GlobalDictItem> getGlobalDictItemList(GlobalDictItem filter, String orderBy) {
        QueryWrapper queryWrapper = filter == null ? QueryWrapper.create() : QueryWrapper.create(filter);
        if (StrUtil.isNotBlank(orderBy)) {
            queryWrapper.orderBy(orderBy);
        } else {
            queryWrapper.orderBy(GlobalDictItem::getShowOrder, true);
        }
        return globalDictItemMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public List<GlobalDictItem> getGlobalDictItemListByDictCode(String dictCode) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(GlobalDictItem::getDictCode, dictCode);
        queryWrapper.orderBy(GlobalDictItem::getShowOrder, true);
        return globalDictItemMapper.selectListByQuery(queryWrapper);
    }
}
