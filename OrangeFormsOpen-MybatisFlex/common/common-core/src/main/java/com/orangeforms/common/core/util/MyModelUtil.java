package com.orangeforms.common.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.core.exception.InvalidDataFieldException;
import com.orangeforms.common.core.annotation.*;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.object.Tuple2;
import com.orangeforms.common.core.upload.UploadResponseInfo;
import com.orangeforms.common.core.upload.UploadStoreInfo;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 负责Model数据操作、类型转换和关系关联等行为的工具类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class MyModelUtil {

    /**
     * 数值型字段。
     */
    public static final Integer NUMERIC_FIELD_TYPE = 0;
    /**
     * 字符型字段。
     */
    public static final Integer STRING_FIELD_TYPE = 1;
    /**
     * 日期型字段。
     */
    public static final Integer DATE_FIELD_TYPE = 2;
    /**
     * 整个工程的实体对象中，创建者Id字段的Java对象名。
     */
    public static final String CREATE_USER_ID_FIELD_NAME = "createUserId";
    /**
     * 整个工程的实体对象中，创建时间字段的Java对象名。
     */
    public static final String CREATE_TIME_FIELD_NAME = "createTime";
    /**
     * 整个工程的实体对象中，更新者Id字段的Java对象名。
     */
    public static final String UPDATE_USER_ID_FIELD_NAME = "updateUserId";
    /**
     * 整个工程的实体对象中，更新时间字段的Java对象名。
     */
    public static final String UPDATE_TIME_FIELD_NAME = "updateTime";
    /**
     * mapToColumnName和mapToColumnInfo使用的缓存。
     */
    private static final Map<String, Tuple2<String, Integer>> CACHED_COLUMNINFO_MAP = new ConcurrentHashMap<>();

    /**
     * 将Bean转换为Map。
     *
     * @param data Bean数据对象。
     * @param <T>  Bean对象类型。
     * @return 转换后的Map。
     */
    public static <T> Map<String, Object> beanToMap(T data) {
        return BeanUtil.beanToMap(data);
    }

    /**
     * 将Bean的数据列表转换为Map列表。
     *
     * @param dataList Bean数据列表。
     * @param <T>      Bean对象类型。
     * @return 转换后的Map列表。
     */
    public static <T> List<Map<String, Object>> beanToMapList(List<T> dataList) {
        return CollUtil.isEmpty(dataList) ? new LinkedList<>()
                : dataList.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
    }

    /**
     * 将Map的数据列表转换为Bean列表。
     *
     * @param dataList Map数据列表。
     * @param <T>      Bean对象类型。
     * @return 转换后的Bean对象列表。
     */
    public static <T> List<T> mapToBeanList(List<Map<String, Object>> dataList, Class<T> clazz) {
        return CollUtil.isEmpty(dataList) ? new LinkedList<>()
                : dataList.stream().map(data -> BeanUtil.toBeanIgnoreError(data, clazz)).collect(Collectors.toList());
    }

    /**
     * 拷贝源类型的集合数据到目标类型的集合中，其中源类型和目标类型中的对象字段类型完全相同。
     * NOTE: 该函数主要应用于框架中，Dto和Model之间的copy，特别针对一对一关联的深度copy。
     * 在Dto中，一对一对象可以使用Map来表示，而不需要使用从表对象的Dto。
     *
     * @param sourceCollection 源类型集合。
     * @param targetClazz      目标类型的Class对象。
     * @param <S>              源类型。
     * @param <T>              目标类型。
     * @return copy后的目标类型对象集合。
     */
    public static <S, T> List<T> copyCollectionTo(Collection<S> sourceCollection, Class<T> targetClazz) {
        List<T> targetList = null;
        if (sourceCollection == null) {
            return targetList;
        }
        targetList = new LinkedList<>();
        if (CollUtil.isNotEmpty(sourceCollection)) {
            for (S source : sourceCollection) {
                try {
                    T target = targetClazz.newInstance();
                    BeanUtil.copyProperties(source, target);
                    targetList.add(target);
                } catch (Exception e) {
                    log.error("Failed to call MyModelUtil.copyCollectionTo", e);
                    return Collections.emptyList();
                }
            }
        }
        return targetList;
    }

    /**
     * 拷贝源类型的对象数据到目标类型的对象中，其中源类型和目标类型中的对象字段类型完全相同。
     * NOTE: 该函数主要应用于框架中，Dto和Model之间的copy，特别针对一对一关联的深度copy。
     * 在Dto中，一对一对象可以使用Map来表示，而不需要使用从表对象的Dto。
     *
     * @param source      源类型对象。
     * @param targetClazz 目标类型的Class对象。
     * @param <S>         源类型。
     * @param <T>         目标类型。
     * @return copy后的目标类型对象。
     */
    public static <S, T> T copyTo(S source, Class<T> targetClazz) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClazz.newInstance();
            BeanUtil.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Failed to call MyModelUtil.copyTo", e);
            return null;
        }
    }

    /**
     * 映射Model对象的字段反射对象，获取与该字段对应的数据库列名称。
     *
     * @param field      字段反射对象。
     * @param modelClazz Model对象的Class类。
     * @return 该字段所对应的数据表列名称。
     */
    public static String mapToColumnName(Field field, Class<?> modelClazz) {
        return mapToColumnName(field.getName(), modelClazz);
    }

    /**
     * 映射Model对象的字段名称，获取与该字段对应的数据库列名称。
     *
     * @param fieldName  字段名称。
     * @param modelClazz Model对象的Class类。
     * @return 该字段所对应的数据表列名称。
     */
    public static String mapToColumnName(String fieldName, Class<?> modelClazz) {
        Tuple2<String, Integer> columnInfo = mapToColumnInfo(fieldName, modelClazz);
        return columnInfo == null ? null : columnInfo.getFirst();
    }

    /**
     * 映射Model对象的字段反射对象，获取与该字段对应的数据库列名称。
     * 如果没有匹配到ColumnName，则立刻抛出异常。
     *
     * @param field      字段反射对象。
     * @param modelClazz Model对象的Class类。
     * @return 该字段所对应的数据表列名称。
     */
    public static String safeMapToColumnName(Field field, Class<?> modelClazz) {
        return safeMapToColumnName(field.getName(), modelClazz);
    }

    /**
     * 映射Model对象的字段名称，获取与该字段对应的数据库列名称。
     * 如果没有匹配到ColumnName，则立刻抛出异常。
     *
     * @param fieldName  字段名称。
     * @param modelClazz Model对象的Class类。
     * @return 该字段所对应的数据表列名称。
     */
    public static String safeMapToColumnName(String fieldName, Class<?> modelClazz) {
        String columnName = mapToColumnName(fieldName, modelClazz);
        if (columnName == null) {
            throw new InvalidDataFieldException(modelClazz.getSimpleName(), fieldName);
        }
        return columnName;
    }

    /**
     * 映射Model对象的字段名称，获取与该字段对应的数据库列名称和字段类型。
     *
     * @param fieldName  字段名称。
     * @param modelClazz Model对象的Class类。
     * @return 该字段所对应的数据表列名称和Java字段类型。
     */
    public static Tuple2<String, Integer> mapToColumnInfo(String fieldName, Class<?> modelClazz) {
        if (StrUtil.isBlank(fieldName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append(modelClazz.getName()).append("-#-").append(fieldName);
        Tuple2<String, Integer> columnInfo = CACHED_COLUMNINFO_MAP.get(sb.toString());
        if (columnInfo != null) {
            return columnInfo;
        }
        Field field = ReflectUtil.getField(modelClazz, fieldName);
        if (field == null) {
            return null;
        }
        Column c = field.getAnnotation(Column.class);
        String columnName = null;
        if (c == null) {
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                columnName = id.value();
            }
        }
        if (StrUtil.isBlank(columnName)) {
            columnName = c == null ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName) : c.value();
            if (StrUtil.isBlank(columnName)) {
                columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
            }
        }
        // 这里缺省情况下都是按照整型去处理，因为他覆盖太多的类型了。
        // 如Integer/Long/Double/BigDecimal，可根据实际情况完善和扩充。
        String typeName = field.getType().getSimpleName();
        Integer type = NUMERIC_FIELD_TYPE;
        if (String.class.getSimpleName().equals(typeName)) {
            type = STRING_FIELD_TYPE;
        } else if (Date.class.getSimpleName().equals(typeName)) {
            type = DATE_FIELD_TYPE;
        }
        columnInfo = new Tuple2<>(columnName, type);
        CACHED_COLUMNINFO_MAP.put(sb.toString(), columnInfo);
        return columnInfo;
    }

    /**
     * 映射Model主对象的Class名称，到Model所对应的表名称。
     *
     * @param modelClazz Model主对象的Class。
     * @return Model对象对应的数据表名称。
     */
    public static String mapToTableName(Class<?> modelClazz) {
        Table t = modelClazz.getAnnotation(Table.class);
        return t == null ? null : t.value();
    }

    /**
     * 主Model类型中，遍历所有包含RelationConstDict注解的字段，并将关联的静态字典中的数据，
     * 填充到thisModel对象的被注解字段中。
     *
     * @param thisClazz 主对象的Class对象。
     * @param thisModel 主对象。
     * @param <T>       主表对象类型。
     */
    @SuppressWarnings("unchecked")
    public static <T> void makeConstDictRelation(Class<T> thisClazz, T thisModel) {
        if (thisModel == null) {
            return;
        }
        Field[] fields = ReflectUtil.getFields(thisClazz);
        for (Field field : fields) {
            // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
            Field thisTargetField = ReflectUtil.getField(thisClazz, field.getName());
            RelationConstDict r = thisTargetField.getAnnotation(RelationConstDict.class);
            if (r == null) {
                continue;
            }
            Field dictMapField = ReflectUtil.getField(r.constantDictClass(), "DICT_MAP");
            Map<Object, String> dictMap =
                    (Map<Object, String>) ReflectUtil.getFieldValue(r.constantDictClass(), dictMapField);
            Object id = ReflectUtil.getFieldValue(thisModel, r.masterIdField());
            if (id != null) {
                String name = dictMap.get(id);
                if (name != null) {
                    Map<String, Object> m = new HashMap<>(2);
                    m.put("id", id);
                    m.put("name", name);
                    ReflectUtil.setFieldValue(thisModel, thisTargetField, m);
                }
            }
        }
    }

    /**
     * 主Model类型中，遍历所有包含RelationConstDict注解的字段，并将关联的静态字典中的数据，
     * 填充到thisModelList集合元素对象的被注解字段中。
     *
     * @param thisClazz     主对象的Class对象。
     * @param thisModelList 主对象列表。
     * @param <T>           主表对象类型。
     */
    @SuppressWarnings("unchecked")
    public static <T> void makeConstDictRelation(Class<T> thisClazz, List<T> thisModelList) {
        if (CollUtil.isEmpty(thisModelList)) {
            return;
        }
        List<T> thisModelList2 = thisModelList.stream().filter(Objects::nonNull).toList();
        Field[] fields = ReflectUtil.getFields(thisClazz);
        for (Field field : fields) {
            // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
            Field thisTargetField = ReflectUtil.getField(thisClazz, field.getName());
            RelationConstDict r = thisTargetField.getAnnotation(RelationConstDict.class);
            if (r == null) {
                continue;
            }
            Field dictMapField = ReflectUtil.getField(r.constantDictClass(), "DICT_MAP");
            Map<Object, String> dictMap =
                    (Map<Object, String>) ReflectUtil.getFieldValue(r.constantDictClass(), dictMapField);
            for (T thisModel : thisModelList2) {
                Object id = ReflectUtil.getFieldValue(thisModel, r.masterIdField());
                if (id != null) {
                    String name = dictMap.get(id);
                    if (name != null) {
                        Map<String, Object> m = new HashMap<>(2);
                        m.put("id", id);
                        m.put("name", name);
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, m);
                    }
                }
            }
        }
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationDict注解参数，将被关联对象thatModel中的数据，
     * 关联到thisModel对象的thisRelationField字段中。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModel         主对象。
     * @param thatModel         字典关联对象。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeDictRelation(
            Class<T> thisClazz, T thisModel, R thatModel, String thisRelationField) {
        if (thatModel == null || thisModel == null) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationDict r = thisTargetField.getAnnotation(RelationDict.class);
        Object slaveId = ReflectUtil.getFieldValue(thatModel, r.slaveIdField());
        if (slaveId != null) {
            Map<String, Object> m = new HashMap<>(2);
            m.put("id", slaveId);
            m.put("name", ReflectUtil.getFieldValue(thatModel, r.slaveNameField()));
            ReflectUtil.setFieldValue(thisModel, thisTargetField, m);
        }
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationDict注解参数，将被关联对象集合thatModelList中的数据，
     * 逐个关联到thisModelList每一个元素的thisRelationField字段中。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param thatModelList     字典关联对象列表集合。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeDictRelation(
            Class<T> thisClazz, List<T> thisModelList, List<R> thatModelList, String thisRelationField) {
        if (CollUtil.isEmpty(thatModelList) || CollUtil.isEmpty(thisModelList)) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationDict r = thisTargetField.getAnnotation(RelationDict.class);
        Field masterIdField = ReflectUtil.getField(thisClazz, r.masterIdField());
        Class<?> thatClass = r.slaveModelClass();
        Field slaveIdField = ReflectUtil.getField(thatClass, r.slaveIdField());
        Field slaveNameField = ReflectUtil.getField(thatClass, r.slaveNameField());
        Map<Object, R> thatMap = new HashMap<>(20);
        thatModelList.forEach(thatModel -> {
            Object id = ReflectUtil.getFieldValue(thatModel, slaveIdField);
            if (id != null) {
                thatMap.put(id, thatModel);
            }
        });
        thisModelList.forEach(thisModel -> {
            if (thisModel != null) {
                Object id = ReflectUtil.getFieldValue(thisModel, masterIdField);
                if (id != null) {
                    R thatModel = thatMap.get(id);
                    if (thatModel != null) {
                        Map<String, Object> m = new HashMap<>(4);
                        m.put("id", id);
                        m.put("name", ReflectUtil.getFieldValue(thatModel, slaveNameField));
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, m);
                    }
                }
            }
        });
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationDict注解参数，将被关联对象集合thatModelMap中的数据，
     * 逐个关联到thisModelList每一个元素的thisRelationField字段中。
     * 该函数之所以使用Map，主要出于性能优化考虑，在连续使用thatModelMap进行关联时，有效的避免了从多次从List转换到Map的过程。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param thatMadelMap      字典关联对象映射集合。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeDictRelation(
            Class<T> thisClazz, List<T> thisModelList, Map<Object, R> thatMadelMap, String thisRelationField) {
        if (MapUtil.isEmpty(thatMadelMap) || CollUtil.isEmpty(thisModelList)) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationDict r = thisTargetField.getAnnotation(RelationDict.class);
        Field masterIdField = ReflectUtil.getField(thisClazz, r.masterIdField());
        Class<?> thatClass = r.slaveModelClass();
        Field slaveNameField = ReflectUtil.getField(thatClass, r.slaveNameField());
        thisModelList.forEach(thisModel -> {
            if (thisModel != null) {
                Object id = ReflectUtil.getFieldValue(thisModel, masterIdField);
                if (id != null) {
                    R thatModel = thatMadelMap.get(id);
                    if (thatModel != null) {
                        Map<String, Object> m = new HashMap<>(4);
                        m.put("id", id);
                        m.put("name", ReflectUtil.getFieldValue(thatModel, slaveNameField));
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, m);
                    }
                }
            }
        });
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationGlobalDict注解参数，全局字典dictMap中的字典数据，
     * 逐个关联到thisModelList每一个元素的thisRelationField字段中。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param dictMap           全局字典数据。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     */
    public static <T> void makeGlobalDictRelation(
            Class<T> thisClazz, List<T> thisModelList, Map<Serializable, String> dictMap, String thisRelationField) {
        if (MapUtil.isEmpty(dictMap) || CollUtil.isEmpty(thisModelList)) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationGlobalDict r = thisTargetField.getAnnotation(RelationGlobalDict.class);
        Field masterIdField = ReflectUtil.getField(thisClazz, r.masterIdField());
        thisModelList.forEach(thisModel -> {
            if (thisModel != null) {
                Object id = ReflectUtil.getFieldValue(thisModel, masterIdField);
                if (id != null) {
                    String name = dictMap.get(id.toString());
                    if (name != null) {
                        Map<String, Object> m = new HashMap<>(2);
                        m.put("id", id);
                        m.put("name", name);
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, m);
                    }
                }
            }
        });
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationOneToOne注解参数，将被关联对象列表thatModelList中的数据，
     * 逐个关联到thisModelList每一个元素的thisRelationField字段中。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param thatModelList     一对一关联对象列表。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeOneToOneRelation(
            Class<T> thisClazz, List<T> thisModelList, List<R> thatModelList, String thisRelationField) {
        if (CollUtil.isEmpty(thatModelList) || CollUtil.isEmpty(thisModelList)) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationOneToOne r = thisTargetField.getAnnotation(RelationOneToOne.class);
        Field masterIdField = ReflectUtil.getField(thisClazz, r.masterIdField());
        Class<?> thatClass = r.slaveModelClass();
        Field slaveIdField = ReflectUtil.getField(thatClass, r.slaveIdField());
        Map<Object, R> thatMap = new HashMap<>(20);
        thatModelList.forEach(thatModel -> {
            Object id = ReflectUtil.getFieldValue(thatModel, slaveIdField);
            if (id != null) {
                thatMap.put(id, thatModel);
            }
        });
        thisModelList.forEach(thisModel -> {
            Object id = ReflectUtil.getFieldValue(thisModel, masterIdField);
            if (id != null) {
                R thatModel = thatMap.get(id);
                if (thatModel != null) {
                    if (thisTargetField.getType().equals(Map.class)) {
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, BeanUtil.beanToMap(thatModel));
                    } else {
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, thatModel);
                    }
                }
            }
        });
    }

    /**
     * 根据主对象和关联对象各自的关联Id函数，将主对象列表和关联对象列表中的数据关联到一起，并将关联对象
     * 设置到主对象的指定关联字段中。
     * NOTE: 用于主对象关联字段中，没有包含RelationOneToOne注解的场景。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param thisIdGetterFunc  主对象Id的Getter函数。
     * @param thatModelList     关联对象列表。
     * @param thatIdGetterFunc  关联对象Id的Getter函数。
     * @param thisRelationField 主对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeOneToOneRelation(
            Class<T> thisClazz,
            List<T> thisModelList,
            Function<T, Object> thisIdGetterFunc,
            List<R> thatModelList,
            Function<R, Object> thatIdGetterFunc,
            String thisRelationField) {
        makeOneToOneRelation(thisClazz, thisModelList,
                thisIdGetterFunc, thatModelList, thatIdGetterFunc, thisRelationField, false);
    }

    /**
     * 根据主对象和关联对象各自的关联Id函数，将主对象列表和关联对象列表中的数据关联到一起，并将关联对象
     * 设置到主对象的指定关联字段中。
     * NOTE: 用于主对象关联字段中，没有包含RelationOneToOne注解的场景。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param thisIdGetterFunc  主对象Id的Getter函数。
     * @param thatModelList     关联对象列表。
     * @param thatIdGetterFunc  关联对象Id的Getter函数。
     * @param thisRelationField 主对象中保存被关联对象的字段名称。
     * @param orderByThatList   如果为true，则按照ThatModelList的顺序输出。同时thisModelList被排序后的新列表替换。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeOneToOneRelation(
            Class<T> thisClazz,
            List<T> thisModelList,
            Function<T, Object> thisIdGetterFunc,
            List<R> thatModelList,
            Function<R, Object> thatIdGetterFunc,
            String thisRelationField,
            boolean orderByThatList) {
        if (CollUtil.isEmpty(thisModelList)) {
            return;
        }
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        boolean isMap = thisTargetField.getType().equals(Map.class);
        if (orderByThatList) {
            List<T> newThisModelList = new LinkedList<>();
            Map<Object, ? extends T> thisModelMap =
                    thisModelList.stream().collect(Collectors.toMap(thisIdGetterFunc, c -> c));
            thatModelList.forEach(thatModel -> {
                Object thatId = thatIdGetterFunc.apply(thatModel);
                if (thatId != null) {
                    T thisModel = thisModelMap.get(thatId);
                    if (thisModel != null) {
                        ReflectUtil.setFieldValue(thisModel, thisTargetField, normalize(isMap, thatModel));
                        newThisModelList.add(thisModel);
                    }
                }
            });
            thisModelList.clear();
            thisModelList.addAll(newThisModelList);
            return;
        }
        Map<Object, R> thatMadelMap =
                thatModelList.stream().collect(Collectors.toMap(thatIdGetterFunc, c -> c));
        thisModelList.forEach(thisModel -> {
            Object thisId = thisIdGetterFunc.apply(thisModel);
            if (thisId != null) {
                R thatModel = thatMadelMap.get(thisId);
                if (thatModel != null) {
                    ReflectUtil.setFieldValue(thisModel, thisTargetField, normalize(isMap, thatModel));
                }
            }
        });
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationOneToMany注解参数，将被关联对象列表thatModelList中的数据，
     * 逐个关联到thisModelList每一个元素的thisRelationField字段中。
     *
     * @param thisClazz         主对象的Class对象。
     * @param thisModelList     主对象列表。
     * @param thatModelList     一对多关联对象列表。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               从表对象类型。
     */
    public static <T, R> void makeOneToManyRelation(
            Class<T> thisClazz, List<T> thisModelList, List<R> thatModelList, String thisRelationField) {
        if (CollUtil.isEmpty(thatModelList) || CollUtil.isEmpty(thisModelList)) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationOneToMany r = thisTargetField.getAnnotation(RelationOneToMany.class);
        Field masterIdField = ReflectUtil.getField(thisClazz, r.masterIdField());
        Class<?> thatClass = r.slaveModelClass();
        Field slaveIdField = ReflectUtil.getField(thatClass, r.slaveIdField());
        Map<Object, List<R>> thatMap = new HashMap<>(20);
        thatModelList.forEach(thatModel -> {
            Object id = ReflectUtil.getFieldValue(thatModel, slaveIdField);
            if (id != null) {
                List<R> thatModelSubList = thatMap.computeIfAbsent(id, k -> new LinkedList<>());
                thatModelSubList.add(thatModel);
            }
        });
        thisModelList.forEach(thisModel -> {
            Object id = ReflectUtil.getFieldValue(thisModel, masterIdField);
            if (id != null) {
                List<R> thatModel = thatMap.get(id);
                if (thatModel != null) {
                    ReflectUtil.setFieldValue(thisModel, thisTargetField, thatModel);
                }
            }
        });
    }

    /**
     * 在主Model类型中，根据thisRelationField字段的RelationManyToMany注解参数，将被关联对象列表relationModelList中的数据，
     * 逐个关联到thisModelList每一个元素的thisRelationField字段中。
     *
     * @param thisClazz         主对象的Class对象。
     * @param idFieldName       主表主键Id字段名。
     * @param thisModelList     主对象列表。
     * @param relationModelList 多对多关联对象列表。
     * @param thisRelationField 主表对象中保存被关联对象的字段名称。
     * @param <T>               主表对象类型。
     * @param <R>               关联表对象类型。
     */
    public static <T, R> void makeManyToManyRelation(
            Class<T> thisClazz, String idFieldName, List<T> thisModelList, List<R> relationModelList, String thisRelationField) {
        if (CollUtil.isEmpty(relationModelList) || CollUtil.isEmpty(thisModelList)) {
            return;
        }
        // 这里不做任何空值判断，从而让配置错误在调试期间即可抛出
        Field thisTargetField = ReflectUtil.getField(thisClazz, thisRelationField);
        RelationManyToMany r = thisTargetField.getAnnotation(RelationManyToMany.class);
        Field masterIdField = ReflectUtil.getField(thisClazz, idFieldName);
        Class<?> thatClass = r.relationModelClass();
        Field slaveIdField = ReflectUtil.getField(thatClass, r.relationMasterIdField());
        Map<Object, List<R>> thatMap = new HashMap<>(20);
        relationModelList.forEach(thatModel -> {
            Object id = ReflectUtil.getFieldValue(thatModel, slaveIdField);
            if (id != null) {
                thatMap.computeIfAbsent(id, k -> new LinkedList<>()).add(thatModel);
            }
        });
        thisModelList.forEach(thisModel -> {
            Object id = ReflectUtil.getFieldValue(thisModel, masterIdField);
            if (id != null) {
                List<R> thatModel = thatMap.get(id);
                if (thatModel != null) {
                    ReflectUtil.setFieldValue(thisModel, thisTargetField, thatModel);
                }
            }
        });
    }

    private static <M> Object normalize(boolean isMap, M model) {
        return isMap ? BeanUtil.beanToMap(model) : model;
    }

    /**
     * 获取上传字段的存储信息。
     *
     * @param modelClass      model的class对象。
     * @param uploadFieldName 上传字段名。
     * @param <T>             model的类型。
     * @return 字段的上传存储信息对象。该值始终不会返回null。
     */
    public static <T> UploadStoreInfo getUploadStoreInfo(Class<T> modelClass, String uploadFieldName) {
        UploadStoreInfo uploadStoreInfo = new UploadStoreInfo();
        Field uploadField = ReflectUtil.getField(modelClass, uploadFieldName);
        if (uploadField == null) {
            throw new UnsupportedOperationException("The Field ["
                    + uploadFieldName + "] doesn't exist in Model [" + modelClass.getSimpleName() + "].");
        }
        uploadStoreInfo.setSupportUpload(false);
        UploadFlagColumn anno = uploadField.getAnnotation(UploadFlagColumn.class);
        if (anno != null) {
            uploadStoreInfo.setSupportUpload(true);
            uploadStoreInfo.setStoreType(anno.storeType());
        }
        return uploadStoreInfo;
    }

    /**
     * 在插入实体对象数据之前，可以调用该方法，初始化通用字段的数据。
     *
     * @param data 实体对象。
     * @param <M>  实体对象类型。
     */
    public static <M> void fillCommonsForInsert(M data) {
        Field createdByField = ReflectUtil.getField(data.getClass(), CREATE_USER_ID_FIELD_NAME);
        if (createdByField != null) {
            ReflectUtil.setFieldValue(data, createdByField, TokenData.takeFromRequest().getUserId());
        }
        Field createTimeField = ReflectUtil.getField(data.getClass(), CREATE_TIME_FIELD_NAME);
        if (createTimeField != null) {
            ReflectUtil.setFieldValue(data, createTimeField, new Date());
        }
        Field updatedByField = ReflectUtil.getField(data.getClass(), UPDATE_USER_ID_FIELD_NAME);
        if (updatedByField != null) {
            ReflectUtil.setFieldValue(data, updatedByField, TokenData.takeFromRequest().getUserId());
        }
        Field updateTimeField = ReflectUtil.getField(data.getClass(), UPDATE_TIME_FIELD_NAME);
        if (updateTimeField != null) {
            ReflectUtil.setFieldValue(data, updateTimeField, new Date());
        }
    }

    /**
     * 在更新实体对象数据之前，可以调用该方法，更新通用字段的数据。
     *
     * @param data         实体对象。
     * @param originalData 原有实体对象。
     * @param <M>          实体对象类型。
     */
    public static <M> void fillCommonsForUpdate(M data, M originalData) {
        Object createdByValue = ReflectUtil.getFieldValue(originalData, CREATE_USER_ID_FIELD_NAME);
        if (createdByValue != null) {
            ReflectUtil.setFieldValue(data, CREATE_USER_ID_FIELD_NAME, createdByValue);
        }
        Object createTimeValue = ReflectUtil.getFieldValue(originalData, CREATE_TIME_FIELD_NAME);
        if (createTimeValue != null) {
            ReflectUtil.setFieldValue(data, CREATE_TIME_FIELD_NAME, createTimeValue);
        }
        Field updatedByField = ReflectUtil.getField(data.getClass(), UPDATE_USER_ID_FIELD_NAME);
        if (updatedByField != null) {
            ReflectUtil.setFieldValue(data, updatedByField, TokenData.takeFromRequest().getUserId());
        }
        Field updateTimeField = ReflectUtil.getField(data.getClass(), UPDATE_TIME_FIELD_NAME);
        if (updateTimeField != null) {
            ReflectUtil.setFieldValue(data, updateTimeField, new Date());
        }
    }

    /**
     * 为实体对象字段设置缺省值。如果data对象中指定字段的值为NULL，则设置缺省值，否则跳过。
     *
     * @param data         实体对象。
     * @param fieldName    实体对象字段名。
     * @param defaultValue 缺省值。
     * @param <M>          实体对象类型。
     * @param <V>          缺省值类型。
     */
    public static <M, V> void setDefaultValue(M data, String fieldName, V defaultValue) {
        Object v = ReflectUtil.getFieldValue(data, fieldName);
        if (v == null) {
            ReflectUtil.setFieldValue(data, fieldName, defaultValue);
        }
    }

    /**
     * 获取当前数据对象中，所有上传文件字段的数据，并将上传后的文件名存到集合中并返回。
     *
     * @param data  数据对象。
     * @param clazz 数据对象的Class类型。
     * @param <M>   数据对象类型。
     * @return 当前数据对象中，所有上传文件字段中，文件名属性的集合。
     */
    public static <M> Set<String> extractDownloadFileName(M data, Class<M> clazz) {
        Set<String> resultSet = new HashSet<>();
        if (data == null) {
            return resultSet;
        }
        Field[] fields = ReflectUtil.getFields(clazz);
        for (Field field : fields) {
            if (field.isAnnotationPresent(UploadFlagColumn.class)) {
                String v = (String) ReflectUtil.getFieldValue(data, field);
                List<UploadResponseInfo> fileInfoList = JSON.parseArray(v, UploadResponseInfo.class);
                if (CollUtil.isNotEmpty(fileInfoList)) {
                    fileInfoList.forEach(fileInfo -> resultSet.add(fileInfo.getFilename()));
                }
            }
        }
        return resultSet;
    }

    /**
     * 获取当前数据对象列表中，所有上传文件字段的数据，并将上传后的文件名存到集合中并返回。
     *
     * @param dataList 数据对象。
     * @param clazz    数据对象的Class类型。
     * @param <M>      数据对象类型。
     * @return 当前数据对象中，所有上传文件字段中，文件名属性的集合。
     */
    public static <M> Set<String> extractDownloadFileName(List<M> dataList, Class<M> clazz) {
        Set<String> resultSet = new HashSet<>();
        if (CollUtil.isEmpty(dataList)) {
            return resultSet;
        }
        dataList.forEach(data -> resultSet.addAll(extractDownloadFileName(data, clazz)));
        return resultSet;
    }

    /**
     * 根据数据对象指定字段的类型，将参数中的字段值集合转换为匹配的值类型集合。
     * @param clazz       数据对象的Class。
     * @param fieldName   字段名。
     * @param fieldValues 字符型的字段值集合。
     * @param <M> 对象类型。
     * @return 转换后的字段值集合。
     */
    public static <M> Set<? extends Serializable> convertToTypeValues(
            Class<M> clazz, String fieldName, List<String> fieldValues) {
        Field f = ReflectUtil.getField(clazz, fieldName);
        if (f == null) {
            String errorMsg = "数据对象 [" + clazz.getSimpleName() + " ] 中，不存在该数据字段 [" + fieldName + "]!";
            throw new MyRuntimeException(errorMsg);
        }
        if (f.getType().equals(Long.class)) {
            return fieldValues.stream().map(Long::valueOf).collect(Collectors.toSet());
        } else if (f.getType().equals(Integer.class)) {
            return fieldValues.stream().map(Integer::valueOf).collect(Collectors.toSet());
        }
        return new HashSet<>(fieldValues);
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private MyModelUtil() {
    }
}
