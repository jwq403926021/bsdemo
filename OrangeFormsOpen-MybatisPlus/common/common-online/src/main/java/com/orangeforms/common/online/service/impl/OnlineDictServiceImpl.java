package com.orangeforms.common.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.common.core.base.service.BaseService;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.object.CallResult;
import com.orangeforms.common.core.object.MyRelationParam;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.redis.util.CommonRedisUtil;
import com.orangeforms.common.sequence.wrapper.IdGeneratorWrapper;
import com.orangeforms.common.online.util.OnlineRedisKeyUtil;
import com.orangeforms.common.online.dao.OnlineDictMapper;
import com.orangeforms.common.online.model.OnlineDict;
import com.orangeforms.common.online.service.OnlineDblinkService;
import com.orangeforms.common.online.service.OnlineDictService;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 在线表单字典数据操作服务类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("onlineDictService")
public class OnlineDictServiceImpl extends BaseService<OnlineDict, Long> implements OnlineDictService {

    @Autowired
    private OnlineDictMapper onlineDictMapper;
    @Autowired
    private OnlineDblinkService dblinkService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private CommonRedisUtil commonRedisUtil;

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<OnlineDict> mapper() {
        return onlineDictMapper;
    }

    /**
     * 保存新增对象。
     *
     * @param onlineDict 新增对象。
     * @return 返回新增对象。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OnlineDict saveNew(OnlineDict onlineDict) {
        onlineDict.setDictId(idGenerator.nextLongId());
        TokenData tokenData = TokenData.takeFromRequest();
        onlineDict.setAppCode(tokenData.getAppCode());
        Date now = new Date();
        onlineDict.setUpdateTime(now);
        onlineDict.setCreateTime(now);
        onlineDict.setCreateUserId(tokenData.getUserId());
        onlineDict.setUpdateUserId(tokenData.getUserId());
        onlineDictMapper.insert(onlineDict);
        return onlineDict;
    }

    /**
     * 更新数据对象。
     *
     * @param onlineDict         更新的对象。
     * @param originalOnlineDict 原有数据对象。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(OnlineDict onlineDict, OnlineDict originalOnlineDict) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDictKey(onlineDict.getDictId()));
        TokenData tokenData = TokenData.takeFromRequest();
        onlineDict.setAppCode(tokenData.getAppCode());
        onlineDict.setUpdateTime(new Date());
        onlineDict.setUpdateUserId(tokenData.getUserId());
        onlineDict.setCreateTime(originalOnlineDict.getCreateTime());
        onlineDict.setCreateUserId(originalOnlineDict.getCreateUserId());
        // 这里重点提示，在执行主表数据更新之前，如果有哪些字段不支持修改操作，请用原有数据对象字段替换当前数据字段。
        UpdateWrapper<OnlineDict> uw = this.createUpdateQueryForNullValue(onlineDict, onlineDict.getDictId());
        return onlineDictMapper.update(onlineDict, uw) == 1;
    }

    /**
     * 删除指定数据。
     *
     * @param dictId 主键Id。
     * @return 成功返回true，否则false。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long dictId) {
        commonRedisUtil.evictFormCache(OnlineRedisKeyUtil.makeOnlineDictKey(dictId));
        return onlineDictMapper.deleteById(dictId) == 1;
    }

    /**
     * 获取单表查询结果。由于没有关联数据查询，因此在仅仅获取单表数据的场景下，效率更高。
     * 如果需要同时获取关联数据，请移步(getOnlineDictListWithRelation)方法。
     *
     * @param filter  过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineDict> getOnlineDictList(OnlineDict filter, String orderBy) {
        if (filter == null) {
            filter = new OnlineDict();
        }
        filter.setAppCode(TokenData.takeFromRequest().getAppCode());
        return onlineDictMapper.getOnlineDictList(filter, orderBy);
    }

    /**
     * 获取主表的查询结果，以及主表关联的字典数据和一对一从表数据，以及一对一从表的字典数据。
     * 该查询会涉及到一对一从表的关联过滤，或一对多从表的嵌套关联过滤，因此性能不如单表过滤。
     * 如果仅仅需要获取主表数据，请移步(getOnlineDictList)，以便获取更好的查询性能。
     *
     * @param filter  主表过滤对象。
     * @param orderBy 排序参数。
     * @return 查询结果集。
     */
    @Override
    public List<OnlineDict> getOnlineDictListWithRelation(OnlineDict filter, String orderBy) {
        List<OnlineDict> resultList = this.getOnlineDictList(filter, orderBy);
        // 在缺省生成的代码中，如果查询结果resultList不是Page对象，说明没有分页，那么就很可能是数据导出接口调用了当前方法。
        // 为了避免一次性的大量数据关联，规避因此而造成的系统运行性能冲击，这里手动进行了分批次读取，开发者可按需修改该值。
        int batchSize = resultList instanceof Page ? 0 : 1000;
        this.buildRelationForDataList(resultList, MyRelationParam.normal(), batchSize);
        return resultList;
    }

    @Override
    public OnlineDict getOnlineDictFromCache(Long dictId) {
        String key = OnlineRedisKeyUtil.makeOnlineDictKey(dictId);
        return commonRedisUtil.getFromCache(key, dictId, this::getById, OnlineDict.class);
    }

    @Override
    public List<OnlineDict> getOnlineDictListFromCache(Set<Long> dictIdSet) {
        List<OnlineDict> dictList = new LinkedList<>();
        dictIdSet.forEach(dictId -> {
            OnlineDict dict = this.getOnlineDictFromCache(dictId);
            if (dict != null) {
                dictList.add(dict);
            }
        });
        return dictList;
    }

    /**
     * 根据最新对象和原有对象的数据对比，判断关联的字典数据和多对一主表数据是否都是合法数据。
     *
     * @param onlineDict         最新数据对象。
     * @param originalOnlineDict 原有数据对象。
     * @return 数据全部正确返回true，否则false。
     */
    @Override
    public CallResult verifyRelatedData(OnlineDict onlineDict, OnlineDict originalOnlineDict) {
        String errorMessageFormat = "数据验证失败，关联的%s并不存在，请刷新后重试！";
        //这里是基于字典的验证。
        if (this.needToVerify(onlineDict, originalOnlineDict, OnlineDict::getDblinkId)
                && !dblinkService.existId(onlineDict.getDblinkId())) {
            return CallResult.error(String.format(errorMessageFormat, "数据库链接主键id"));
        }
        return CallResult.ok();
    }
}
