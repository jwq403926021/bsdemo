package com.orangeforms.common.online.object;

import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.object.CallResult;
import com.orangeforms.common.online.dto.OnlineFilterDto;
import com.orangeforms.common.online.model.OnlineTable;

import java.util.List;
import java.util.Map;

/**
 * 在线表单后台扩展可执行接口，所有在线表单的后台扩展类都需要继承该接口。
 *
 * @author Jerry
 * @date 2024-04-15
 */
public class BaseOnlineExtendExecutor {

    /**
     * 主从表数据级联插入之前执行的操作。通常可以做一些数据插入之前的验证操作，或者是对待插入的数据，根据需要进行调整和计算。
     *
     * @param masterTable          主表对象。
     * @param masterData           主表数据。key是字段名，value是字段值。
     * @param slaveTableAndDataMap 从表对象和数据的映射关系。key是关联从表对象。
     *                             value是关联的从表数据列表。value是JSONObject对象列表，每个JSONObject的key是从表字段名，value是从表字段值。
     * @return 返回调用结果对象，成功则继续执行，否则调用时会抛出异常，异常信息为具体的错误信息。
     */
    public CallResult beforeInsertWithRelation(
            OnlineTable masterTable, JSONObject masterData, Map<OnlineTable, List<JSONObject>> slaveTableAndDataMap) {
        return CallResult.ok();
    }

    /**
     * 主从表数据级联插入之后执行的操作。
     *
     * @param masterTable          主表对象。
     * @param masterData           主表数据。key是字段名，value是字段值。
     * @param slaveTableAndDataMap 从表对象和数据的映射关系。key是关联从表对象。
     *                             value是关联的从表数据列表。value是JSONObject对象列表，每个JSONObject的key是从表字段名，value是从表字段值。
     */
    public void afterInsertWithRelation(
            OnlineTable masterTable, JSONObject masterData, Map<OnlineTable, List<JSONObject>> slaveTableAndDataMap) {
    }

    /**
     * 表数据插入之前执行的操作。
     *
     * @param table 表对象。
     * @param data  表数据。key是字段名，value是字段值。
     * @return 返回调用结果对象，成功则继续执行，否则调用时会抛出异常，异常信息为具体的错误信息。
     */
    public CallResult beforeInsert(OnlineTable table, JSONObject data) {
        return CallResult.ok();
    }

    /**
     * 表数据插入之后执行的操作。
     *
     * @param table 表对象。
     * @param data  表数据。key是字段名，value是字段值。
     */
    public void afterInsert(OnlineTable table, JSONObject data) {
    }

    /**
     * 主从表数据级联更新之前执行的操作。
     *
     * @param masterTable          主表对象。
     * @param masterData           主表数据。key是字段名，value是字段值。
     * @param slaveTableAndDataMap 从表对象和数据的映射关系。key是关联从表对象。
     *                             value是关联的从表数据列表。value是JSONObject对象列表，每个JSONObject的key是从表字段名，value是从表字段值。
     * @return 返回调用结果对象，成功则继续执行，否则调用时会抛出异常，异常信息为具体的错误信息。
     */
    public CallResult beforeUpdateWithRelation(
            OnlineTable masterTable, JSONObject masterData, Map<OnlineTable, List<JSONObject>> slaveTableAndDataMap) {
        return CallResult.ok();
    }

    /**
     * 主从表数据级联更新之后执行的操作。
     *
     * @param masterTable          主表对象。
     * @param masterData           主表数据。key是字段名，value是字段值。
     * @param slaveTableAndDataMap 从表对象和数据的映射关系。key是关联从表对象。
     *                             value是关联的从表数据列表。value是JSONObject对象列表，每个JSONObject的key是从表字段名，value是从表字段值。
     */
    public void afterUpdateWithRelation(
            OnlineTable masterTable, JSONObject masterData, Map<OnlineTable, List<JSONObject>> slaveTableAndDataMap) {
    }

    /**
     * 表数据更新之前执行的操作。
     *
     * @param table 表对象。
     * @param data  表数据。key是字段名，value是字段值。
     * @return 返回调用结果对象，成功则继续执行，否则调用时会抛出异常，异常信息为具体的错误信息。
     */
    public CallResult beforeUpdate(OnlineTable table, JSONObject data) {
        return CallResult.ok();
    }

    /**
     * 表数据更新之后执行的操作。
     *
     * @param table 表对象。
     * @param data  表数据。key是字段名，value是字段值。
     */
    public void afterUpdate(OnlineTable table, JSONObject data) {
    }

    /**
     * 表数据删除之前执行的操作。
     *
     * @param table  表对象。
     * @param dataId 表数据主键Id。对象类型和实际字段值类型一致。
     * @return 返回调用结果对象，成功则继续执行，否则调用时会抛出异常，异常信息为具体的错误信息。
     */
    public CallResult beforeDelete(OnlineTable table, Object dataId) {
        return CallResult.ok();
    }

    /**
     * 表数据删除之后执行的操作。
     *
     * @param table  表对象。
     * @param dataId 表数据主键Id。对象类型和实际字段值类型一致。
     */
    public void afterDelete(OnlineTable table, Object dataId) {
    }

    /**
     * 表数据列表查询之前执行的操作。
     *
     * @param table      表对象。
     * @param filterList 过滤条件。如果有新的过滤条件，可直接添加到该过滤列表。
     */
    public void beforeSelectList(OnlineTable table, List<OnlineFilterDto> filterList) {
    }

    /**
     * 表数据列表查询之后执行的操作。
     *
     * @param table      表对象。
     * @param resultList 查询结果。如果有修改，可直接在当前参数修改后即可生效。
     */
    public void afterSelectList(OnlineTable table, List<Map<String, Object>> resultList) {
    }

    /**
     * 表数据单条详情查询之后执行的操作。
     *
     * @param table  表对象。
     * @param result 查询结果。如果有修改，可直接在当前参数修改后即可生效。
     */
    public void afterSelectOne(OnlineTable table, Map<String, Object> result) {
    }
}
