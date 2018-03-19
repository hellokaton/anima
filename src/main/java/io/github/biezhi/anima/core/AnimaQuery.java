/**
 * Copyright (c) 2018, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Table;
import io.github.biezhi.anima.core.functions.TypeFunction;
import io.github.biezhi.anima.enums.DMLType;
import io.github.biezhi.anima.enums.ErrorCode;
import io.github.biezhi.anima.enums.OrderBy;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.page.Page;
import io.github.biezhi.anima.page.PageRow;
import io.github.biezhi.anima.utils.AnimaUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Operational database core class
 *
 * @author biezhi
 */
@Slf4j
@NoArgsConstructor
public class AnimaQuery<T extends Model> {

    private Class<T> modelClass;

    private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    private StringBuilder       conditionSQL    = new StringBuilder();
    private List<String>        excludedColumns = new ArrayList<>(8);
    private List<Object>        paramValues     = new ArrayList<>(8);
    private Map<String, Object> updateColumns   = new LinkedHashMap<>(8);

    private String orderBy;
    private String selectColumns;

    private String  pkName;
    private String  tableName;
    private DMLType dmlType;

    public AnimaQuery(DMLType dmlType) {
        this.dmlType = dmlType;
    }

    public AnimaQuery(Class<T> modelClass) {
        this.parse(modelClass);
    }

    public AnimaQuery<T> parse(Class<T> modelClass) {
        this.modelClass = modelClass;
        Table table = modelClass.getAnnotation(Table.class);
        this.tableName = null != table && AnimaUtils.isNotEmpty(table.name()) ? table.name() :
                AnimaUtils.toTableName(modelClass.getSimpleName(), Anima.me().getTablePrefix());
        this.pkName = null != table ? table.pk() : "id";
        return this;
    }

    public AnimaQuery<T> exclude(String... columnNames) {
        Collections.addAll(excludedColumns, columnNames);
        return this;
    }

    public AnimaQuery<T> select(String columns) {
        if (null != this.selectColumns) {
            throw new AnimaException("Select method can only be called once.");
        }
        this.selectColumns = columns;
        return this;
    }

    public AnimaQuery<T> where(String statement) {
        conditionSQL.append(" AND ").append(statement);
        return this;
    }

    public <R> AnimaQuery<T> where(TypeFunction<T, R> function) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        conditionSQL.append(" AND ").append(columnName);
        return this;
    }

    public AnimaQuery<T> eq(Object value) {
        conditionSQL.append(" = ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> where(String statement, Object value) {
        conditionSQL.append(" AND ").append(statement);
        if (!statement.contains("?")) {
            conditionSQL.append(" = ?");
        }
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> notNull() {
        conditionSQL.append(" IS NOT NULL");
        return this;
    }

    public AnimaQuery<T> and(String statement, Object value) {
        return this.where(statement, value);
    }

    public <R> AnimaQuery<T> and(TypeFunction<T, R> function) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        conditionSQL.append(" AND ").append(columnName);
        return this;
    }

    public AnimaQuery<T> or(String statement, Object value) {
        conditionSQL.append(" OR (").append(statement);
        if (!statement.contains("?")) {
            conditionSQL.append(" = ?");
        }
        conditionSQL.append(')');
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> not(String key, Object value) {
        conditionSQL.append(" AND ").append(key).append(" != ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> not(Object value) {
        conditionSQL.append(" != ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> notNull(String key) {
        conditionSQL.append(" AND ").append(key).append(" IS NOT NULL");
        return this;
    }

    public AnimaQuery<T> like(String key, Object value) {
        conditionSQL.append(" AND ").append(key).append(" LIKE ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> like(Object value) {
        conditionSQL.append(" LIKE ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> between(String coulmn, Object a, Object b) {
        conditionSQL.append(" AND ").append(coulmn).append(" BETWEEN ? and ?");
        paramValues.add(a);
        paramValues.add(b);
        return this;
    }

    public AnimaQuery<T> between(Object a, Object b) {
        conditionSQL.append(" BETWEEN ? and ?");
        paramValues.add(a);
        paramValues.add(b);
        return this;
    }

    public AnimaQuery<T> gt(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" > ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> gt(Object value) {
        conditionSQL.append(" > ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> gte(Object value) {
        conditionSQL.append(" >= ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> lt(Object value) {
        conditionSQL.append(" < ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> lte(Object value) {
        conditionSQL.append(" <= ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> gte(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" >= ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> lt(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" < ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> lte(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" <= ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> in(String column, Object... args) {
        if (null == args || args.length == 0) {
            log.warn("Column: {}, query params is empty.");
            return this;
        }
        conditionSQL.append(" AND ").append(column).append(" IN (");
        for (int i = 0; i < args.length; i++) {
            if (i == args.length - 1) {
                conditionSQL.append("?");
            } else {
                conditionSQL.append("?, ");
            }
            paramValues.add(args[i]);
        }
        conditionSQL.append(")");
        return this;
    }

    public AnimaQuery<T> in(String key, List<T> args) {
        if (null == args || args.isEmpty()) {
            log.warn("Column: {}, query params is empty.");
            return this;
        }
        conditionSQL.append(" AND ").append(key).append(" IN (");
        for (int i = 0; i < args.size(); i++) {
            if (i == args.size() - 1) {
                conditionSQL.append("?");
            } else {
                conditionSQL.append("?, ");
            }
            paramValues.add(args.get(i));
        }
        conditionSQL.append(")");
        return this;
    }

    public AnimaQuery<T> order(String order) {
        this.orderBy = order;
        return this;
    }

    public <R> AnimaQuery<T> order(String columnName, OrderBy orderBy) {
        this.orderBy = columnName + " " + orderBy.toString();
        return this;
    }

    public <R> AnimaQuery<T> order(TypeFunction<T, R> function, OrderBy orderBy) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return order(columnName, orderBy);
    }

    public T find(Class<T> returnType, String sql, Object[] params) {
        return this.queryOne(returnType, sql, params);
    }

    public T byId(Serializable id) {
        this.beforeCheck();
        this.where(pkName, id);
        String sql = this.buildSelectSQL(false);
        return this.queryOne(modelClass, sql, paramValues);
    }

    public List<T> byIds(Serializable... ids) {
        this.in(pkName, ids);
        return this.all();
    }

    public <S> ResultList<S> bySQL(Class<S> type, String sql, Object... params) {
        List<S> list = this.queryList(type, sql, params);
        return new ResultList<>(list);
    }

    public List<T> all() {
        this.beforeCheck();
        String sql = this.buildSelectSQL(true);
        return this.queryList(modelClass, sql, paramValues);
    }

    public Stream<T> stream() {
        return all().stream();
    }

    public Stream<T> parallel() {
        return all().parallelStream();
    }

    public <R> Stream<R> map(Function<T, R> function) {
        return stream().map(function);
    }

    public Stream<T> filter(Predicate<T> predicate) {
        return stream().filter(predicate);
    }

    public T one() {
        this.beforeCheck();
        String sql = this.buildSelectSQL(true);
        return this.queryOne(modelClass, sql, paramValues);
    }

    public List<T> limit(int limit) {
        return stream().limit(limit).collect(Collectors.toList());
    }

    public Page<T> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

    public Page<T> page(PageRow pageRow) {
        this.beforeCheck();
        String sql      = this.buildSelectSQL(false);
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") tmp";

        try (Connection conn = getConn()) {
            long    count    = conn.createQuery(countSql).withParams(paramValues).executeAndFetchFirst(Long.class);
            String  pageSQL  = this.buildPageSQL(pageRow);
            List<T> list     = conn.createQuery(pageSQL).withParams(paramValues).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetch((Class<T>) modelClass);
            Page<T> pageBean = new Page<>(count, pageRow.getPageNum(), pageRow.getPageSize());
            pageBean.setRows(list);
            return pageBean;
        } finally {
            this.clean(null);
        }
    }

    public long count() {
        this.beforeCheck();
        String sql = this.buildCountSQL(null);
        return this.queryOne(Long.class, sql, paramValues);
    }

    public AnimaQuery<T> set(String column, Object value) {
        updateColumns.put(column, value);
        return this;
    }

    public <R> AnimaQuery<T> set(TypeFunction<T, R> function, Object value) {
        return this.set(AnimaUtils.getLambdaColumnName(function), value);
    }

    private <S> S queryOne(Class<S> type, String sql, Object[] params) {
        try (Connection conn = getConn()) {
            return conn.createQuery(sql).withParams(params).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetchFirst(type);
        } finally {
            this.clean(null);
        }
    }

    private <S> S queryOne(Class<S> type, String sql, List<Object> params) {
        List<S> list = queryList(type, sql, params);
        return AnimaUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    private <S> List<S> queryList(Class<S> type, String sql, Object[] params) {
        try (Connection conn = getConn()) {
            return conn.createQuery(sql).withParams(params).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetch(type);
        } finally {
            this.clean(null);
        }
    }

    private <S> List<S> queryList(Class<S> type, String sql, List<Object> params) {
        try (Connection conn = getConn()) {
            return conn.createQuery(sql).withParams(params).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetch(type);
        } finally {
            this.clean(null);
        }
    }

    public int execute() {
        switch (dmlType) {
            case UPDATE:
                return this.update();
            case DELETE:
                return this.delete();
            default:
                throw new AnimaException("Please check if your use is correct.");
        }
    }

    public int execute(String sql, Object... params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql).withParams(params).executeUpdate().getResult();
        } finally {
            this.clean(conn);
        }
    }

    public int execute(String sql, List<Object> params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql).withParams(params).executeUpdate().getResult();
        } finally {
            this.clean(conn);
        }
    }

    public ResultKey save(T model) {
        String       sql             = this.buildInsertSQL(model);
        List<Object> columnValueList = AnimaUtils.columnValues(model, true);
        Connection   conn            = getConn();
        try {
            return new ResultKey(conn.createQuery(sql).withParams(columnValueList).executeUpdate().getKey());
        } finally {
            this.clean(conn);
        }
    }

    public int delete() {
        String sql = this.buildDeleteSQL(null);
        return this.execute(sql, paramValues);
    }

    public int deleteById(Serializable id) {
        this.where(pkName, id);
        return this.delete();
    }

    public int deleteByModel(T model) {
        this.beforeCheck();
        String       sql             = this.buildDeleteSQL(model);
        List<Object> columnValueList = AnimaUtils.columnValues(model, false);
        return this.execute(sql, columnValueList);
    }

    public int update() {
        this.beforeCheck();
        String       sql             = this.buildUpdateSQL(null, updateColumns);
        List<Object> columnValueList = new ArrayList<>();
        updateColumns.forEach((key, value) -> columnValueList.add(value));
        columnValueList.addAll(paramValues);
        return this.execute(sql, columnValueList);
    }

    public int updateById(Serializable id) {
        this.where(pkName, id);
        return this.update();
    }

    public int updateById(T model, Serializable id) {
        this.where(pkName, id);
        String       sql             = this.buildUpdateSQL(model, null);
        List<Object> columnValueList = AnimaUtils.columnValues(model, false);
        columnValueList.add(id);
        return this.execute(sql, columnValueList);
    }


    public int updateByModel(T model) {
        this.beforeCheck();
        String       sql             = this.buildUpdateSQL(model, null);
        List<Object> columnValueList = AnimaUtils.columnValues(model, false);
        return this.execute(sql, columnValueList);
    }

    private String buildSelectSQL(boolean addOrderBy) {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .selectColumns(this.selectColumns)
                .tableName(this.tableName)
                .pkName(this.pkName)
                .conditionSQL(this.conditionSQL)
                .excludedColumns(this.excludedColumns)
                .build();

        if (addOrderBy) {
            sqlParams.setOrderBy(this.orderBy);
        }
        return Anima.me().getDialect().select(sqlParams);
    }

    private String buildCountSQL(Object model) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.pkName)
                .conditionSQL(this.conditionSQL)
                .build();
        return Anima.me().getDialect().count(sqlParams);
    }

    private String buildPageSQL(PageRow pageRow) {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .selectColumns(this.selectColumns)
                .tableName(this.tableName)
                .pkName(this.pkName)
                .conditionSQL(this.conditionSQL)
                .excludedColumns(this.excludedColumns)
                .orderBy(this.orderBy)
                .pageRow(pageRow)
                .build();
        return Anima.me().getDialect().paginate(sqlParams);
    }

    private String buildInsertSQL(T model) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.pkName)
                .build();

        return Anima.me().getDialect().insert(sqlParams);
    }

    private String buildUpdateSQL(T model, Map<String, Object> updateColumns) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.pkName)
                .updateColumns(updateColumns)
                .conditionSQL(this.conditionSQL)
                .build();

        return Anima.me().getDialect().update(sqlParams);
    }

    private String buildDeleteSQL(T model) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.pkName)
                .conditionSQL(this.conditionSQL)
                .build();
        return Anima.me().getDialect().delete(sqlParams);
    }

    private void beforeCheck() {
        if (null == this.modelClass) {
            throw new AnimaException(ErrorCode.FROM_NOT_NULL);
        }
    }

    private static Connection getConn() {
        Connection connection = connectionThreadLocal.get();
        if (null == connection) {
            return getSql2o().open();
        }
        return connection;
    }

    public static void beginTransaction() {
        Connection connection = AnimaQuery.getSql2o().beginTransaction();
        connectionThreadLocal.set(connection);
    }

    public static void endTransaction() {
        if (null != connectionThreadLocal.get()) {
            Connection connection = connectionThreadLocal.get();
            if (connection.isRollbackOnClose()) {
                connection.close();
            }
            connectionThreadLocal.remove();
        }
    }

    public static void commit() {
        connectionThreadLocal.get().commit();
    }

    public static void rollback() {
        if (null != connectionThreadLocal.get()) {
            log.warn("Rollback connection.");
            connectionThreadLocal.get().rollback();
        }
    }

    public static Sql2o getSql2o() {
        Sql2o sql2o = Anima.me().getSql2o();
        if (null == sql2o) {
            throw new AnimaException("SQL2O instance not is null.");
        }
        return sql2o;
    }

    private void clean(Connection conn) {
        selectColumns = null;
        orderBy = null;
        conditionSQL = new StringBuilder();
        paramValues.clear();
        excludedColumns.clear();
        updateColumns.clear();
        if (null == connectionThreadLocal.get() && null != conn) {
            conn.close();
        }
    }

}
