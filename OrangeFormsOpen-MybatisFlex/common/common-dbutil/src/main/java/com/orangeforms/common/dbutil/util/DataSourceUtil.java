package com.orangeforms.common.dbutil.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.constant.FieldFilterType;
import com.orangeforms.common.core.exception.InvalidDblinkTypeException;
import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.core.object.MyPageParam;
import com.orangeforms.common.core.object.Tuple2;
import com.orangeforms.common.core.util.MyDateUtil;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.dbutil.constant.DblinkType;
import com.orangeforms.common.dbutil.provider.*;
import com.orangeforms.common.dbutil.constant.CustomDateValueType;
import com.orangeforms.common.dbutil.object.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.joda.time.DateTime;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 动态加载的数据源工具类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public abstract class DataSourceUtil {

    private final Lock lock = new ReentrantLock();
    private final Map<Long, DataSource> datasourceMap = MapUtil.newHashMap();
    private static final Map<Integer, DataSourceProvider> PROVIDER_MAP = new HashMap<>(5);
    protected final Map<Long, DataSourceProvider> dblinkProviderMap = new ConcurrentHashMap<>(4);

    public static final String SQL_SELECT = " SELECT ";
    public static final String SQL_SELECT_FROM = " SELECT * FROM (";
    public static final String SQL_AS_TMP = " ) tmp ";
    public static final String SQL_ORDER_BY = " ORDER BY ";
    public static final String SQL_AND = " AND ";
    public static final String SQL_WHERE = " WHERE ";
    private static final String LOG_PREPARING_FORMAT = "==>  Preparing: {}";
    private static final String LOG_PARMS_FORMAT = "==> Parameters: {}";
    private static final String LOG_TOTAL_FORMAT = "<==      Total: {}";

    static {
        PROVIDER_MAP.put(DblinkType.MYSQL, new MySqlProvider());
    }

    /**
     * 由子类实现，根据dblinkId获取数据库链接类型的方法。
     *
     * @param dblinkId 数据库链接Id。
     * @return 数据库链接类型。
     */
    protected abstract int getDblinkTypeByDblinkId(Long dblinkId);

    /**
     * 由子类实现，根据dblinkId获取数据库链接配置信息的方法。
     *
     * @param dblinkId 数据库链接Id。
     * @return 数据库链接配置信息。
     */
    protected abstract String getDblinkConfigurationByDblinkId(Long dblinkId);

    /**
     * 获取指定数据库类型的Provider实现类。
     *
     * @param dblinkType 数据库类型。
     * @return 指定数据库类型的Provider实现类。
     */
    public DataSourceProvider getProvider(Integer dblinkType) {
        return PROVIDER_MAP.get(dblinkType);
    }

    /**
     * 获取指定数据库链接的Provider实现类。
     *
     * @param dblinkId 数据库链接Id。
     * @return 指定数据库类型的Provider实现类。
     */
    public DataSourceProvider getProvider(Long dblinkId) {
        int dblinkType = this.getDblinkTypeByDblinkId(dblinkId);
        DataSourceProvider provider = PROVIDER_MAP.get(dblinkType);
        if (provider == null) {
            throw new InvalidDblinkTypeException(dblinkType);
        }
        return provider;
    }

    /**
     * 测试数据库链接。
     *
     * @param dblinkId 数据库链接Id。
     */
    public void testConnection(Long dblinkId) throws Exception {
        DataSourceProvider provider = this.getProvider(dblinkId);
        this.query(dblinkId, provider.getTestQuery());
    }

    /**
     * 通过JDBC方式测试链接。
     *
     * @param databaseType 数据库类型。参考DblinkType常量值。
     * @param host         主机名。
     * @param port         端口号。
     * @param schemaName   模式名。
     * @param databaseName 数据库名。
     * @param username     用户名。
     * @param password     密码。
     */
    public static void testConnection(
            int databaseType,
            String host,
            Integer port,
            String schemaName,
            String databaseName,
            String username,
            String password) {
        StringBuilder urlBuilder = new StringBuilder(256);
        String hostAndPort = host + ":" + port;
        urlBuilder.append("jdbc:mysql://")
                .append(hostAndPort)
                .append("/")
                .append(databaseName)
                .append("?characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai");
        try {
            Connection conn = DriverManager.getConnection(urlBuilder.toString(), username, password);
            conn.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new MyRuntimeException(e.getMessage());
        }
    }

    /**
     * 根据Dblink对象获取关联的数据源。如果不存在会创建该数据库连接池的数据源，
     * 并保存到Map中缓存，下次调用时可直接返回。
     *
     * @param dblinkId 数据库链接Id。
     * @return 关联的数据库连接池的数据源。
     */
    public DataSource getDataSource(Long dblinkId) throws Exception {
        DataSource dataSource = datasourceMap.get(dblinkId);
        if (dataSource != null) {
            return dataSource;
        }
        int dblinkType = this.getDblinkTypeByDblinkId(dblinkId);
        DataSourceProvider provider = PROVIDER_MAP.get(dblinkType);
        if (provider == null) {
            throw new InvalidDblinkTypeException(dblinkType);
        }
        DruidDataSource druidDataSource = null;
        lock.lock();
        try {
            dataSource = datasourceMap.get(dblinkId);
            if (dataSource != null) {
                return dataSource;
            }
            JdbcConfig jdbcConfig = provider.getJdbcConfig(this.getDblinkConfigurationByDblinkId(dblinkId));
            Properties properties = new Properties();
            druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
            druidDataSource.setUrl(jdbcConfig.getJdbcConnectionString());
            druidDataSource.setDriverClassName(jdbcConfig.getDriver());
            druidDataSource.setValidationQuery(jdbcConfig.getValidationQuery());
            druidDataSource.setUsername(jdbcConfig.getUsername());
            druidDataSource.setPassword(jdbcConfig.getPassword());
            druidDataSource.setInitialSize(jdbcConfig.getInitialPoolSize());
            druidDataSource.setMinIdle(jdbcConfig.getMinPoolSize());
            druidDataSource.setMaxActive(jdbcConfig.getMaxPoolSize());
            druidDataSource.setConnectionErrorRetryAttempts(2);
            druidDataSource.setTimeBetweenConnectErrorMillis(500);
            druidDataSource.setBreakAfterAcquireFailure(true);
            druidDataSource.init();
            datasourceMap.put(dblinkId, druidDataSource);
            return druidDataSource;
        } catch (Exception e) {
            if (druidDataSource != null) {
                druidDataSource.close();
            }
            log.error("Failed to create DruidDatasource", e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关闭指定数据库链接Id关联的数据源，同时从缓存中移除该数据源对象。
     *
     * @param dblinkId 数据库链接Id。
     */
    public void removeDataSource(Long dblinkId) {
        lock.lock();
        try {
            DataSource dataSource = datasourceMap.get(dblinkId);
            if (dataSource == null) {
                return;
            }
            ((DruidDataSource) dataSource).close();
            datasourceMap.remove(dblinkId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取指定数据源的数据库连接对象。
     *
     * @param dblinkId 数据库链接Id。
     * @return 数据库连接对象。
     */
    public Connection getConnection(Long dblinkId) throws Exception {
        DataSource dataSource = this.getDataSource(dblinkId);
        return dataSource == null ? null : dataSource.getConnection();
    }

    /**
     * 获取指定数据库链接的数据表列表。
     *
     * @param dblinkId     数据库链接Id。
     * @param searchString 表名的模糊匹配字符串。如果为空，则没有前缀规律。
     * @return 数据表对象列表。
     */
    public List<SqlTable> getTableList(Long dblinkId, String searchString) {
        DataSourceProvider provider = this.getProvider(dblinkId);
        List<Object> paramList = null;
        if (StrUtil.isNotBlank(searchString)) {
            paramList = new LinkedList<>();
            paramList.add("%" + searchString + "%");
        }
        String querySql = provider.getTableMetaListSql(searchString);
        try {
            return this.query(dblinkId, querySql, paramList, SqlTable.class);
        } catch (Exception e) {
            log.error("Failed to call getTableList", e);
            throw new MyRuntimeException(e);
        }
    }

    /**
     * 获取指定数据库链接的数据表对象。
     *
     * @param dblinkId  数据库链接Id。
     * @param tableName 表名称。
     * @return 数据表对象。
     */
    public SqlTable getTable(Long dblinkId, String tableName) {
        DataSourceProvider provider = this.getProvider(dblinkId);
        String querySql = provider.getTableMetaSql();
        List<Object> paramList = new LinkedList<>();
        paramList.add(tableName);
        try {
            return this.queryOne(dblinkId, querySql, paramList, SqlTable.class);
        } catch (Exception e) {
            log.error("Failed to call getTable", e);
            throw new MyRuntimeException(e);
        }
    }

    /**
     * 获取指定数据库链接下数据表的字段列表。
     *
     * @param dblinkId  数据库链接Id。
     * @param tableName 表名称。
     * @return 数据表的字段列表。
     */
    public List<SqlTableColumn> getTableColumnList(Long dblinkId, String tableName) {
        try {
            DataSource dataSource = this.getDataSource(dblinkId);
            try (Connection conn = dataSource.getConnection()) {
                return this.getTableColumnList(dblinkId, conn, tableName);
            }
        } catch (Exception e) {
            log.error("Failed to call getTableColumnList", e);
            throw new MyRuntimeException(e);
        }
    }

    /**
     * 获取指定数据库链接下数据表的字段列表。
     *
     * @param dblinkId  数据库链接Id。
     * @param conn      数据库连接对象。
     * @param tableName 表名称。
     * @return 数据表的字段列表。
     */
    public List<SqlTableColumn> getTableColumnList(Long dblinkId, Connection conn, String tableName) {
        DataSourceProvider provider = this.getProvider(dblinkId);
        String querySql = provider.getTableColumnMetaListSql();
        List<Object> paramList = new LinkedList<>();
        paramList.add(tableName);
        try {
            List<Map<String, Object>> dataList = this.query(conn, querySql, paramList);
            return this.toTypedDataList(dataList, SqlTableColumn.class);
        } catch (Exception e) {
            log.error("Failed to call getTableColumnList", e);
            throw new MyRuntimeException(e);
        }
    }

    /**
     * 获取指定表的数据。
     *
     * @param dblinkId     数据库链接Id。
     * @param tableName    表名。
     * @param datasetParam 数据集查询参数对象。
     * @return 表的数据结果。
     */
    public SqlResultSet<Map<String, Object>> getTableDataList(
            Long dblinkId, String tableName, DatasetParam datasetParam) throws Exception {
        SqlTable table = this.getTable(dblinkId, tableName);
        if (table == null) {
            return null;
        }
        DataSourceProvider provider = this.getProvider(dblinkId);
        if (datasetParam == null) {
            datasetParam = new DatasetParam();
        }
        String sql = "SELECT * FROM " + tableName;
        if (CollUtil.isNotEmpty(datasetParam.getSelectColumnNameList())) {
            sql = SQL_SELECT + StrUtil.join(",", datasetParam.getSelectColumnNameList()) + " FROM " + tableName;
        }
        Tuple2<String, List<Object>> filterTuple = this.buildWhereClauseByFilters(dblinkId, datasetParam.getFilter());
        sql += filterTuple.getFirst();
        List<Object> paramList = filterTuple.getSecond();
        String sqlCount = null;
        MyPageParam pageParam = datasetParam.getPageParam();
        if (pageParam != null) {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
            Select select = (Select) statement;
            PlainSelect selectBody = (PlainSelect) select.getSelectBody();
            List<SelectItem> countSelectItems = new LinkedList<>();
            countSelectItems.add(new SelectExpressionItem(new Column("COUNT(1) AS CNT")));
            selectBody.setSelectItems(countSelectItems);
            sqlCount = select.toString();
            sql = provider.makePageSql(sql, pageParam.getPageNum(), pageParam.getPageSize());
        }
        return this.getDataListInternnally(dblinkId, provider, sqlCount, sql, datasetParam, paramList);
    }

    /**
     * 执行包含参数变量的增删改操作。
     *
     * @param connection 数据库链接。
     * @param sql        SQL语句。
     * @param paramList  参数列表。
     * @return 影响的行数。
     */
    public int execute(Connection connection, String sql, List<Object> paramList) {
        try (PreparedStatement stat = connection.prepareStatement(sql)) {
            for (int i = 0; i < paramList.size(); i++) {
                stat.setObject(i + 1, paramList.get(i));
            }
            stat.execute();
            return stat.getUpdateCount();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new MyRuntimeException(e);
        }
    }

    /**
     * 在指定数据库链接上执行查询语句，并返回指定映射对象类型的单条数据对象。
     *
     * @param dblinkId  数据库链接Id。
     * @param query     待执行的SQL语句。
     * @param paramList 参数列表。
     * @param clazz     返回的映射对象Class类型。
     * @return 查询的结果对象。
     */
    public <T> T queryOne(Long dblinkId, String query, List<Object> paramList, Class<T> clazz) throws Exception {
        List<T> dataList = this.query(dblinkId, query, paramList, clazz);
        return CollUtil.isEmpty(dataList) ? null : dataList.get(0);
    }

    /**
     * 在指定数据库链接上执行查询语句，并返回指定映射对象类型的数据列表。
     *
     * @param dblinkId  数据库链接Id。
     * @param query     待执行的SQL语句。
     * @param paramList 参数列表。
     * @param clazz     返回的映射对象Class类型。
     * @return 查询的结果集。
     */
    public <T> List<T> query(Long dblinkId, String query, List<Object> paramList, Class<T> clazz) throws Exception {
        List<Map<String, Object>> dataList = this.query(dblinkId, query, paramList);
        return this.toTypedDataList(dataList, clazz);
    }

    /**
     * 在指定数据库链接上执行查询语句。
     *
     * @param dblinkId 数据库链接Id。
     * @param query    待执行的SQL语句。
     * @return 查询的结果集。
     */
    public List<Map<String, Object>> query(Long dblinkId, String query) throws Exception {
        DataSource dataSource = this.getDataSource(dblinkId);
        try (Connection conn = dataSource.getConnection()) {
            return this.query(conn, query);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 在指定数据库链接上执行查询语句。
     *
     * @param dblinkId  数据库链接Id。
     * @param query     待执行的SQL语句。
     * @param paramList 参数列表。
     * @return 查询的结果集。
     */
    public List<Map<String, Object>> query(Long dblinkId, String query, List<Object> paramList) throws Exception {
        DataSource dataSource = this.getDataSource(dblinkId);
        try (Connection conn = dataSource.getConnection()) {
            return this.query(conn, query, paramList);
        }
    }

    /**
     * 根据数据库链接的类型，将数据表字段类型转换为Java的字段属性类型。
     *
     * @param column     数据表字段对象。
     * @param dblinkType 数据库链接类型。
     * @return 返回与Java字段属性的类型名。
     */
    public String convertToJavaType(SqlTableColumn column, int dblinkType) {
        return this.convertToJavaType(
                column.getColumnType(), column.getNumericPrecision(), column.getNumericScale(), dblinkType);
    }

    /**
     * 根据数据库链接的类型，将数据表字段类型转换为Java的字段属性类型。
     *
     * @param columnType       表字段类型。
     * @param numericPrecision 数值的精度。
     * @param numericScale     数值的刻度。
     * @param dblinkType       数据库链接类型。
     * @return 返回与Java字段属性的类型名。
     */
    public String convertToJavaType(String columnType, Integer numericPrecision, Integer numericScale, int dblinkType) {
        DataSourceProvider provider = this.getProvider(dblinkType);
        if (provider == null) {
            throw new MyRuntimeException("Unsupported Data Type");
        }
        return provider.convertColumnTypeToJavaType(columnType, numericPrecision, numericScale);
    }

    /**
     * 根据Java字段属性的类型，转换参数中的values值到与fieldType匹配的值类型数据列表。
     *
     * @param fieldType Java字段属性值。
     * @param values     字符串类型的参数值列表，该参数为JSON数组。
     * @return 目标类型的参数值列表。
     */
    public List<Serializable> convertToColumnValues(String fieldType, String values) {
        List<Serializable> valueList = new LinkedList<>();
        if (StrUtil.isBlank(values)) {
            return valueList;
        }
        JSONArray valueArray = JSON.parseArray(values);
        for (int i = 0; i < valueArray.size(); i++) {
            String v = valueArray.getString(i);
            valueList.add(this.convertToColumnValue(fieldType, v));
        }
        return valueList;
    }

    /**
     * 根据Java字段属性的类型，转换参数中的value值到与fieldType匹配的值类型。
     *
     * @param fieldType Java字段属性值。
     * @param value     参数值。
     * @return 目标类型的参数值。
     */
    public Serializable convertToColumnValue(String fieldType, Serializable value) {
        if (value == null) {
            return null;
        }
        switch (fieldType) {
            case "Long":
                return Convert.toLong(value);
            case "Integer":
                return Convert.toInt(value);
            case "BigDecimal":
                return Convert.toBigDecimal(value);
            case "Double":
                return Convert.toDouble(value);
            case "Boolean":
                return Convert.toBool(value);
            case "Date":
                return value;
            case "String":
                return value.toString();
            default:
                break;
        }
        return null;
    }

    /**
     * 计算过滤从句和过滤参数。
     *
     * @param dblinkId 数据库链接Id。
     * @param filter   过滤参数列表。
     * @return 返回的Tuple对象的第一个参数是WHERE从句，第二个参数是过滤从句用到的参数列表。
     */
    public Tuple2<String, List<Object>> buildWhereClauseByFilters(Long dblinkId, DatasetFilter filter) {
        filter = this.normalizeFilter(filter);
        if (CollUtil.isEmpty(filter)) {
            return new Tuple2<>("", null);
        }
        DataSourceProvider provider = this.getProvider(dblinkId);
        StringBuilder where = new StringBuilder();
        int i = 0;
        List<Object> paramList = new LinkedList<>();
        for (DatasetFilter.FilterInfo filterInfo : filter) {
            if (i++ == 0) {
                where.append(SQL_WHERE);
            } else {
                where.append(SQL_AND);
            }
            this.doBuildWhereClauseByFilter(filterInfo, provider, where, paramList);
        }
        return new Tuple2<>(where.toString(), paramList);
    }

    private void doBuildWhereClauseByFilter(
            DatasetFilter.FilterInfo filterInfo,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        where.append(filterInfo.getParamName());
        if (filterInfo.getFilterType().equals(FieldFilterType.EQUAL)) {
            this.doBuildWhereClauseByEqualFilter(filterInfo, provider, where, paramList);
        } else if (filterInfo.getFilterType().equals(FieldFilterType.NOT_EQUAL)) {
            where.append(" <> ?");
            paramList.add(filterInfo.getParamValue());
        } else if (filterInfo.getFilterType().equals(FieldFilterType.GE)) {
            this.doBuildWhereClauseByGeFilter(filterInfo, provider, where, paramList);
        } else if (filterInfo.getFilterType().equals(FieldFilterType.GT)) {
            this.doBuildWhereClauseByGtFilter(filterInfo, provider, where, paramList);
        } else if (filterInfo.getFilterType().equals(FieldFilterType.LE)) {
            this.doBuildWhereClauseByLeFilter(filterInfo, provider, where, paramList);
        } else if (filterInfo.getFilterType().equals(FieldFilterType.LT)) {
            this.doBuildWhereClauseByLtFilter(filterInfo, provider, where, paramList);
        } else if (filterInfo.getFilterType().equals(FieldFilterType.BETWEEN)) {
            this.doBuildWhereClauseByBetweenFilter(filterInfo, provider, where, paramList);
        } else if (filterInfo.getFilterType().equals(FieldFilterType.LIKE)) {
            where.append(" LIKE ?");
            paramList.add("%" + filterInfo.getParamValue() + "%");
        } else if (filterInfo.getFilterType().equals(FieldFilterType.IN)) {
            where.append(" IN (");
            where.append(StrUtil.repeatAndJoin("?", filterInfo.getParamValueList().size(), ","));
            where.append(")");
            paramList.addAll(filterInfo.getParamValueList());
        } else if (filterInfo.getFilterType().equals(FieldFilterType.NOT_IN)) {
            where.append(" NOT IN (");
            where.append(StrUtil.repeatAndJoin("?", filterInfo.getParamValueList().size(), ","));
            where.append(")");
            paramList.addAll(filterInfo.getParamValueList());
        } else if (filterInfo.getFilterType().equals(FieldFilterType.IS_NOT_NULL)) {
            where.append(" IS NOT NULL");
        } else if (filterInfo.getFilterType().equals(FieldFilterType.IS_NULL)) {
            where.append(" IS NULL");
        }
    }

    private void doBuildWhereClauseByEqualFilter(
            DatasetFilter.FilterInfo filter,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        if (BooleanUtil.isTrue(filter.getDateValueFilter())) {
            String beginDateTime = this.getBeginDateTime(filter.getParamValue().toString(), filter.getDateRange());
            String endDateTime = this.getEndDateTime(filter.getParamValue().toString(), filter.getDateRange());
            where.append(provider.makeDateTimeFilterSql(null, ">="));
            where.append(SQL_AND);
            where.append(provider.makeDateTimeFilterSql(filter.getParamName(), "<="));
            paramList.add(beginDateTime);
            paramList.add(endDateTime);
        } else {
            where.append(" = ?");
            paramList.add(filter.getParamValue());
        }
    }

    private void doBuildWhereClauseByGeFilter(
            DatasetFilter.FilterInfo filter,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        if (BooleanUtil.isTrue(filter.getDateValueFilter())) {
            where.append(provider.makeDateTimeFilterSql(null, ">="));
            paramList.add(this.getBeginDateTime(filter.getParamValue().toString(), filter.getDateRange()));
        } else {
            paramList.add(filter.getParamValue());
            where.append(" >= ?");
        }
    }

    private void doBuildWhereClauseByGtFilter(
            DatasetFilter.FilterInfo filter,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        if (BooleanUtil.isTrue(filter.getDateValueFilter())) {
            where.append(provider.makeDateTimeFilterSql(null, ">"));
            paramList.add(this.getEndDateTime(filter.getParamValue().toString(), filter.getDateRange()));
        } else {
            where.append(" > ?");
            paramList.add(filter.getParamValue());
        }
    }

    private void doBuildWhereClauseByLeFilter(
            DatasetFilter.FilterInfo filter,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        if (BooleanUtil.isTrue(filter.getDateValueFilter())) {
            where.append(provider.makeDateTimeFilterSql(null, "<="));
            paramList.add(this.getEndDateTime(filter.getParamValue().toString(), filter.getDateRange()));
        } else {
            where.append(" <= ?");
            paramList.add(filter.getParamValue());
        }
    }

    private void doBuildWhereClauseByLtFilter(
            DatasetFilter.FilterInfo filter,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        if (BooleanUtil.isTrue(filter.getDateValueFilter())) {
            where.append(provider.makeDateTimeFilterSql(null, "<"));
            paramList.add(this.getBeginDateTime(filter.getParamValue().toString(), filter.getDateRange()));
        } else {
            where.append(" < ?");
            paramList.add(filter.getParamValue());
        }
    }

    private void doBuildWhereClauseByBetweenFilter(
            DatasetFilter.FilterInfo filter,
            DataSourceProvider provider,
            StringBuilder where,
            List<Object> paramList) {
        if (CollUtil.isEmpty(filter.getParamValueList())) {
            return;
        }
        if (BooleanUtil.isTrue(filter.getDateValueFilter())) {
            Object[] filterArray = filter.getParamValueList().toArray();
            where.append(provider.makeDateTimeFilterSql(null, ">="));
            paramList.add(this.getBeginDateTime(filterArray[0].toString(), filter.getDateRange()));
            where.append(SQL_AND);
            where.append(filter.getParamName());
            where.append(provider.makeDateTimeFilterSql(null, "<="));
            paramList.add(this.getEndDateTime(filterArray[1].toString(), filter.getDateRange()));
        } else {
            where.append(" BETWEEN ? AND ?");
            paramList.add(filter.getParamValueList());
        }
    }

    private SqlResultSet<Map<String, Object>> getDataListInternnally(
            Long dblinkId,
            DataSourceProvider provider,
            String sqlCount,
            String sql,
            DatasetParam datasetParam,
            List<Object> paramList) throws Exception {
        Long totalCount = 0L;
        SqlResultSet<Map<String, Object>> resultSet = null;
        try (Connection connection = this.getConnection(dblinkId)) {
            boolean ignoreQueryData = false;
            if (sqlCount != null) {
                Map<String, Object> data = this.query(connection, sqlCount, paramList).get(0);
                String key = data.entrySet().iterator().next().getKey();
                totalCount = (Long) data.get(key);
                if (totalCount == 0L) {
                    ignoreQueryData = true;
                }
            }
            if (!ignoreQueryData) {
                if (datasetParam.getOrderBy() != null) {
                    sql += SQL_ORDER_BY + datasetParam.getOrderBy();
                }
                resultSet = this.queryWithMeta(connection, sql, paramList);
                resultSet.setTotalCount(totalCount);
            }
        }
        return resultSet == null ? new SqlResultSet<>() : resultSet;
    }

    private List<Map<String, Object>> query(Connection conn, String query) throws SQLException {
        try (Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery(query)) {
            log.info(LOG_PREPARING_FORMAT, query);
            List<Map<String, Object>> resultList = this.fetchResult(rs);
            log.info(LOG_TOTAL_FORMAT, resultList.size());
            return resultList;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private List<Map<String, Object>> query(Connection conn, String query, List<Object> paramList) throws SQLException {
        if (CollUtil.isEmpty(paramList)) {
            return this.query(conn, query);
        }
        ResultSet rs = null;
        try (PreparedStatement stat = conn.prepareStatement(query)) {
            for (int i = 0; i < paramList.size(); i++) {
                stat.setObject(i + 1, paramList.get(i));
            }
            rs = stat.executeQuery();
            log.info(LOG_PREPARING_FORMAT, query);
            List<Map<String, Object>> resultList = this.fetchResult(rs);
            log.info(LOG_TOTAL_FORMAT, resultList.size());
            return resultList;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    log.error("Failed to call rs.close", e);
                }
            }
        }
    }

    private SqlResultSet<Map<String, Object>> queryWithMeta(
            Connection connection, String query, List<Object> paramList) throws SQLException {
        if (CollUtil.isEmpty(paramList)) {
            try (Statement stat = connection.createStatement();
                 ResultSet rs = stat.executeQuery(query)) {
                log.info(LOG_PREPARING_FORMAT, query);
                SqlResultSet<Map<String, Object>> resultSet = this.fetchResultWithMeta(rs);
                log.info(LOG_TOTAL_FORMAT, resultSet.getDataList() == null ? 0 : resultSet.getDataList().size());
                return resultSet;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        }
        ResultSet rs = null;
        try (PreparedStatement stat = connection.prepareStatement(query)) {
            for (int i = 0; i < paramList.size(); i++) {
                stat.setObject(i + 1, paramList.get(i));
            }
            rs = stat.executeQuery();
            log.info(LOG_PREPARING_FORMAT, query);
            SqlResultSet<Map<String, Object>> resultSet = this.fetchResultWithMeta(rs);
            log.info(LOG_TOTAL_FORMAT, resultSet.getDataList() == null ? 0 : resultSet.getDataList().size());
            return resultSet;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    log.error("Failed to call rs.close", e);
                }
            }
        }
    }

    private List<Map<String, Object>> fetchResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> resultList = new LinkedList<>();
        while (rs.next()) {
            JSONObject rowData = new JSONObject();
            for (int i = 0; i < columnCount; i++) {
                rowData.put(metaData.getColumnLabel(i + 1), rs.getObject(i + 1));
            }
            resultList.add(rowData);
        }
        return resultList;
    }

    private SqlResultSet<Map<String, Object>> fetchResultWithMeta(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        List<SqlTableColumn> columnMetaList = new LinkedList<>();
        int columnCount = metaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            SqlTableColumn tableColumn = new SqlTableColumn();
            String columnLabel = metaData.getColumnLabel(i + 1);
            tableColumn.setColumnName(columnLabel);
            tableColumn.setColumnType(metaData.getColumnTypeName(i + 1));
            columnMetaList.add(tableColumn);
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        while (rs.next()) {
            JSONObject rowData = new JSONObject();
            for (int i = 0; i < columnCount; i++) {
                rowData.put(metaData.getColumnLabel(i + 1), rs.getObject(i + 1));
            }
            resultList.add(rowData);
        }
        return new SqlResultSet<>(columnMetaList, resultList);
    }

    private <T> List<T> toTypedDataList(List<Map<String, Object>> dataList, Class<T> clazz) {
        return MyModelUtil.mapToBeanList(dataList, clazz);
    }

    private String getBeginDateTime(String dateValueType, String dateRange) {
        DateTime now = DateTime.now();
        switch (dateValueType) {
            case CustomDateValueType.CURRENT_DAY:
                return MyDateUtil.getBeginTimeOfDayWithShort(now);
            case CustomDateValueType.CURRENT_WEEK:
                return MyDateUtil.getBeginDateTimeOfWeek(now);
            case CustomDateValueType.CURRENT_MONTH:
                return MyDateUtil.getBeginDateTimeOfMonth(now);
            case CustomDateValueType.CURRENT_YEAR:
                return MyDateUtil.getBeginDateTimeOfYear(now);
            case CustomDateValueType.CURRENT_QUARTER:
                return MyDateUtil.getBeginDateTimeOfQuarter(now);
            case CustomDateValueType.LAST_DAY:
                return MyDateUtil.getBeginTimeOfDay(now.minusDays(1));
            case CustomDateValueType.LAST_WEEK:
                return MyDateUtil.getBeginDateTimeOfWeek(now.minusWeeks(1));
            case CustomDateValueType.LAST_MONTH:
                return MyDateUtil.getBeginDateTimeOfMonth(now.minusMonths(1));
            case CustomDateValueType.LAST_YEAR:
                return MyDateUtil.getBeginDateTimeOfYear(now.minusYears(1));
            case CustomDateValueType.LAST_QUARTER:
                return MyDateUtil.getBeginDateTimeOfQuarter(now.minusMonths(3));
            default:
                break;
        }
        // 执行到这里，基本就是自定义日期数据了
        if (StrUtil.isBlank(dateRange)) {
            return dateValueType;
        }
        DateTime dateValue = MyDateUtil.toDateTimeWithoutMs(dateValueType);
        switch (dateRange) {
            case "year":
                return MyDateUtil.getBeginDateTimeOfYear(dateValue);
            case "month":
                return MyDateUtil.getBeginDateTimeOfMonth(dateValue);
            case "week":
                return MyDateUtil.getBeginDateTimeOfWeek(dateValue);
            case "date":
                return MyDateUtil.getBeginTimeOfDayWithShort(dateValue);
            default:
                break;
        }
        return dateValueType;
    }

    private String getEndDateTime(String dateValueType, String dateRange) {
        DateTime now = DateTime.now();
        switch (dateValueType) {
            case CustomDateValueType.CURRENT_DAY:
                return MyDateUtil.getEndTimeOfDayWithShort(now);
            case CustomDateValueType.CURRENT_WEEK:
                return MyDateUtil.getEndDateTimeOfWeek(now);
            case CustomDateValueType.CURRENT_MONTH:
                return MyDateUtil.getEndDateTimeOfMonth(now);
            case CustomDateValueType.CURRENT_YEAR:
                return MyDateUtil.getEndDateTimeOfYear(now);
            case CustomDateValueType.CURRENT_QUARTER:
                return MyDateUtil.getEndDateTimeOfQuarter(now);
            case CustomDateValueType.LAST_DAY:
                return MyDateUtil.getEndTimeOfDay(now.minusDays(1));
            case CustomDateValueType.LAST_WEEK:
                return MyDateUtil.getEndDateTimeOfWeek(now.minusWeeks(1));
            case CustomDateValueType.LAST_MONTH:
                return MyDateUtil.getEndDateTimeOfMonth(now.minusMonths(1));
            case CustomDateValueType.LAST_YEAR:
                return MyDateUtil.getEndDateTimeOfYear(now.minusYears(1));
            case CustomDateValueType.LAST_QUARTER:
                return MyDateUtil.getEndDateTimeOfQuarter(now.minusMonths(3));
            default:
                break;
        }
        // 执行到这里，基本就是自定义日期数据了
        if (StrUtil.isBlank(dateRange)) {
            return dateValueType;
        }
        DateTime dateValue = MyDateUtil.toDateTimeWithoutMs(dateValueType);
        switch (dateRange) {
            case "year":
                return MyDateUtil.getEndDateTimeOfYear(dateValue);
            case "month":
                return MyDateUtil.getEndDateTimeOfMonth(dateValue);
            case "week":
                return MyDateUtil.getEndDateTimeOfWeek(dateValue);
            case "date":
                return MyDateUtil.getEndTimeOfDayWithShort(dateValue);
            default:
                break;
        }
        return dateValueType;
    }

    private DatasetFilter normalizeFilter(DatasetFilter filter) {
        if (CollUtil.isEmpty(filter)) {
            return filter;
        }
        DatasetFilter normalizedFilter = new DatasetFilter();
        for (DatasetFilter.FilterInfo filterInfo : filter) {
            if (filterInfo.getFilterType().equals(FieldFilterType.IS_NULL)
                    || filterInfo.getFilterType().equals(FieldFilterType.IS_NOT_NULL)
                    || filterInfo.getParamValue() != null
                    || filterInfo.getParamValueList() != null) {
                normalizedFilter.add(filterInfo);
            }
        }
        return normalizedFilter;
    }
}
