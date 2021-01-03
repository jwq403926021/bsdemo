package com.orange.demo.app.service.impl;

import com.orange.demo.common.core.base.service.BaseDictService;
import com.orange.demo.common.core.base.dao.BaseDaoMapper;
import com.orange.demo.common.core.cache.MapDictionaryCache;
import com.orange.demo.app.service.GradeService;
import com.orange.demo.app.dao.GradeMapper;
import com.orange.demo.app.model.Grade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 年级字典数据操作服务类。
 *
 * @author Jerry
 * @date 2020-09-24
 */
@Service("gradeService")
public class GradeServiceImpl extends BaseDictService<Grade, Integer> implements GradeService {

    @Autowired
    private GradeMapper gradeMapper;

    public GradeServiceImpl() {
        super();
        this.dictionaryCache = MapDictionaryCache.create(Grade::getGradeId);
    }

    /**
     * 返回当前Service的主表Mapper对象。
     *
     * @return 主表Mapper对象。
     */
    @Override
    protected BaseDaoMapper<Grade> mapper() {
        return gradeMapper;
    }
}
