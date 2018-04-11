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
import io.github.biezhi.anima.annotation.BelongsTo;
import io.github.biezhi.anima.annotation.HasMany;
import io.github.biezhi.anima.annotation.HasOne;
import io.github.biezhi.anima.core.functions.TypeFunction;
import io.github.biezhi.anima.core.relation.RelationParamBuilder;
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
import java.lang.annotation.Annotation;
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

    private StringBuilder                    conditionSQL    = new StringBuilder();
    private StringBuilder                    orderBySQL      = new StringBuilder();
    private List<String>                     excludedColumns = new ArrayList<>(8);
    private List<Object>                     paramValues     = new ArrayList<>(8);
    private Map<String, Object>              updateColumns   = new LinkedHashMap<>(8);
    private Set<Class<? extends Annotation>> relations       = new HashSet<>();

    private boolean isSQLLimit;

    private String selectColumns;

    private String  primaryKeyColumn;
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
        this.tableName = AnimaCache.getTableName(modelClass);
        this.primaryKeyColumn = AnimaCache.getPKColumn(modelClass);
        return this;
    }

    public AnimaQuery<T> exclude(String... columnNames) {
        Collections.addAll(excludedColumns, columnNames);
        return this;
    }

    public AnimaQuery<T> exclude(Class<? extends Annotation> relation) {
        this.relations.add(relation);
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

    public <S extends Model, R> AnimaQuery<T> where(TypeFunction<S, R> function) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        conditionSQL.append(" AND ").append(columnName);
        return this;
    }

    public <S extends Model, R> AnimaQuery<T> where(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        conditionSQL.append(" AND ").append(columnName).append(" = ?");
        paramValues.add(value);
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
        return this.where(function);
    }

    public <R> AnimaQuery<T> and(TypeFunction<T, R> function, Object value) {
        return this.where(function, value);
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

    public AnimaQuery<T> notEmpty() {
        return this.not("");
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

    public <S extends Model, R> AnimaQuery<T> like(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.like(columnName, value);
    }

    public AnimaQuery<T> like(Object value) {
        conditionSQL.append(" LIKE ?");
        paramValues.add(value);
        return this;
    }

    public AnimaQuery<T> between(String column, Object a, Object b) {
        conditionSQL.append(" AND ").append(column).append(" BETWEEN ? and ?");
        paramValues.add(a);
        paramValues.add(b);
        return this;
    }

    public <S extends Model, R> AnimaQuery<T> between(TypeFunction<S, R> function, Object a, Object b) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.between(columnName, a, b);
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

    public <S extends Model, R> AnimaQuery<T> gt(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.gt(columnName, value);
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

    public <S extends Model, R> AnimaQuery<T> gte(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.gte(columnName, value);
    }

    public AnimaQuery<T> lt(Object value) {
        conditionSQL.append(" < ?");
        paramValues.add(value);
        return this;
    }

    public <S extends Model, R> AnimaQuery<T> lt(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.lt(columnName, value);
    }

    public AnimaQuery<T> lte(Object value) {
        conditionSQL.append(" <= ?");
        paramValues.add(value);
        return this;
    }

    public <S extends Model, R> AnimaQuery<T> lte(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.lte(columnName, value);
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

    public <S extends Model, R> AnimaQuery<T> in(TypeFunction<S, R> function, Object... values) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.in(columnName, values);
    }

    public <S extends Model, R> AnimaQuery<T> in(TypeFunction<S, R> function, List<T> values) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.in(columnName, values);
    }

    public AnimaQuery<T> order(String order) {
        if (this.orderBySQL.length() > 0) {
            this.orderBySQL.append(',');
        }
        this.orderBySQL.append(' ').append(order);
        return this;
    }

    public AnimaQuery<T> order(String columnName, OrderBy orderBy) {
        if (this.orderBySQL.length() > 0) {
            this.orderBySQL.append(',');
        }
        this.orderBySQL.append(' ').append(columnName).append(' ').append(orderBy.toString());
        return this;
    }

    public <R> AnimaQuery<T> order(TypeFunction<T, R> function, OrderBy orderBy) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return order(columnName, orderBy);
    }

    public T find(Class<T> returnType, String sql, Object[] params) {
        return this.queryOne(returnType, sql, params);
    }

    public T byId(Object id) {
        this.beforeCheck();
        this.where(primaryKeyColumn, id);
        String sql   = this.buildSelectSQL(false);
        T      model = this.queryOne(modelClass, sql, paramValues);
        this.setRelate(Collections.singletonList(model));
        return model;
    }

    public List<T> byIds(Object... ids) {
        this.in(this.primaryKeyColumn, ids);
        return this.all();
    }

    public <S> ResultList<S> bySQL(Class<S> type, String sql, Object... params) {
        List<S> list = this.queryList(type, sql, params);
        return new ResultList<>(list);
    }

    public T one() {
        this.beforeCheck();
        String sql   = this.buildSelectSQL(true);
        T      model = this.queryOne(modelClass, sql, paramValues);
        this.setRelate(Collections.singletonList(model));
        return model;
    }

    public List<T> all() {
        this.beforeCheck();
        String  sql    = this.buildSelectSQL(true);
        List<T> models = this.queryList(modelClass, sql, paramValues);
        this.setRelate(models);
        return models;
    }

    public Stream<T> stream() {
        List<T> all = all();
        if (null == all || all.isEmpty()) {
            return Stream.empty();
        }
        return all.stream();
    }

    public Stream<T> parallel() {
        return stream().parallel();
    }

    public <R> Stream<R> map(Function<T, R> function) {
        return stream().map(function);
    }

    public Stream<T> filter(Predicate<T> predicate) {
        return stream().filter(predicate);
    }

    public List<T> limit(int limit) {
        if (Anima.me().isUseSQLLimit()) {
            isSQLLimit = true;
            paramValues.add(limit);
            return all();
        }
        return stream().limit(limit).collect(Collectors.toList());
    }

    public Page<T> page(String sql, int page, int limit) {
        return this.page(sql, new PageRow(page, limit));
    }

    public Page<T> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

    public Page<T> page(String sql, PageRow pageRow) {
        this.beforeCheck();
        String     countSql = "SELECT COUNT(*) FROM (" + sql + ") tmp";
        Connection conn     = getConn();
        try {
            long    count    = conn.createQuery(countSql).withParams(paramValues).executeAndFetchFirst(Long.class);
            String  pageSQL  = this.buildPageSQL(pageRow);
            List<T> list     = conn.createQuery(pageSQL).withParams(paramValues).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetch(modelClass);
            Page<T> pageBean = new Page<>(count, pageRow.getPageNum(), pageRow.getPageSize());
            pageBean.setRows(list);
            return pageBean;
        } finally {
            if (null == connectionThreadLocal.get() && null != conn) {
                conn.close();
            }
            this.clean(null);
        }
    }

    public Page<T> page(PageRow pageRow) {
        String sql = this.buildSelectSQL(false);
        return this.page(sql, pageRow);
    }

    public long count() {
        this.beforeCheck();
        String sql = this.buildCountSQL();
        return this.queryOne(Long.class, sql, paramValues);
    }

    public AnimaQuery<T> set(String column, Object value) {
        updateColumns.put(column, value);
        return this;
    }

    public <S extends Model, R> AnimaQuery<T> set(TypeFunction<S, R> function, Object value) {
        return this.set(AnimaUtils.getLambdaColumnName(function), value);
    }

    private <S> S queryOne(Class<S> type, String sql, Object[] params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql).withParams(params).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetchFirst(type);
        } finally {
            if (null == connectionThreadLocal.get() && null != conn) {
                conn.close();
            }
            this.clean(null);
        }
    }

    private <S> S queryOne(Class<S> type, String sql, List<Object> params) {
        if (Anima.me().isUseSQLLimit()) {
            sql += " LIMIT 1";
        }
        List<S> list = queryList(type, sql, params);
        return AnimaUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    private <S> List<S> queryList(Class<S> type, String sql, Object[] params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql).withParams(params).setAutoDeriveColumnNames(true).throwOnMappingFailure(false).executeAndFetch(type);
        } finally {
            if (null == connectionThreadLocal.get() && null != conn) {
                conn.close();
            }
            this.clean(null);
        }
    }

    private <S> List<S> queryList(Class<S> type, String sql, List<Object> params) {
        return this.queryList(type, sql, params.toArray());
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
            if (null == connectionThreadLocal.get() && null != conn) {
                conn.close();
            }
            this.clean(conn);
        }
    }

    public int execute(String sql, List<Object> params) {
        return this.execute(sql, params.toArray());
    }

    public <S extends Model> ResultKey save(S model) {
        String       sql             = this.buildInsertSQL(model);
        List<Object> columnValueList = AnimaUtils.toColumnValues(model, true);
        Connection   conn            = getConn();
        try {
            return new ResultKey(conn.createQuery(sql).withParams(columnValueList).executeUpdate().getKey());
        } finally {
            if (null == connectionThreadLocal.get() && null != conn) {
                conn.close();
            }
            this.clean(conn);
        }
    }

    public int delete() {
        String sql = this.buildDeleteSQL(null);
        return this.execute(sql, paramValues);
    }

    public <S extends Serializable> int deleteById(S id) {
        this.where(primaryKeyColumn, id);
        return this.delete();
    }

    public <S extends Model> int deleteByModel(S model) {
        this.beforeCheck();
        String       sql             = this.buildDeleteSQL(model);
        List<Object> columnValueList = AnimaUtils.toColumnValues(model, false);
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
        this.where(primaryKeyColumn, id);
        return this.update();
    }

    public <S extends Model> int updateById(S model, Serializable id) {
        this.where(primaryKeyColumn, id);
        String       sql             = this.buildUpdateSQL(model, null);
        List<Object> columnValueList = AnimaUtils.toColumnValues(model, false);
        columnValueList.add(id);
        return this.execute(sql, columnValueList);
    }

    public <S extends Model> int updateByModel(S model) {
        this.beforeCheck();
        String       sql             = this.buildUpdateSQL(model, null);
        List<Object> columnValueList = AnimaUtils.toColumnValues(model, false);
        return this.execute(sql, columnValueList);
    }

    private String buildSelectSQL(boolean addOrderBy) {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .selectColumns(this.selectColumns)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .conditionSQL(this.conditionSQL)
                .excludedColumns(this.excludedColumns)
                .isSQLLimit(isSQLLimit)
                .build();

        if (addOrderBy) {
            sqlParams.setOrderBy(this.orderBySQL.toString());
        }
        return Anima.me().getDialect().select(sqlParams);
    }

    private String buildCountSQL() {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .conditionSQL(this.conditionSQL)
                .build();
        return Anima.me().getDialect().count(sqlParams);
    }

    private String buildPageSQL(PageRow pageRow) {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .selectColumns(this.selectColumns)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .conditionSQL(this.conditionSQL)
                .excludedColumns(this.excludedColumns)
                .orderBy(this.orderBySQL.toString())
                .pageRow(pageRow)
                .build();
        return Anima.me().getDialect().paginate(sqlParams);
    }

    private <S extends Model> String buildInsertSQL(S model) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .build();

        return Anima.me().getDialect().insert(sqlParams);
    }

    private <S extends Model> String buildUpdateSQL(S model, Map<String, Object> updateColumns) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .updateColumns(updateColumns)
                .conditionSQL(this.conditionSQL)
                .build();

        return Anima.me().getDialect().update(sqlParams);
    }

    private <S extends Model> String buildDeleteSQL(S model) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
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

    private void setRelate(List<T> models) {
        if (null == models || models.isEmpty()) {
            return;
        }
        if (!relations.contains(BelongsTo.class) && AnimaCache.hasBelongsTo(modelClass)) {
            this.setBelongs(models);
        }
        if (!relations.contains(HasMany.class) && AnimaCache.hasMany(modelClass)) {
            this.setHasMany(models);
        }
        if (!relations.contains(HasOne.class) && AnimaCache.hasOne(modelClass)) {
            this.setHasOne(models);
        }
        this.relations.clear();
    }

    private void setBelongs(List<T> models) {
        Arrays.stream(modelClass.getDeclaredFields())
                .filter(field -> null != field.getAnnotation(BelongsTo.class))
                .map(RelationParamBuilder::buildBelongsTo)
                .forEach(relationParams -> {
                    if (models.size() == 1) {
                        Object fkValue = AnimaUtils.getFieldValue(relationParams.getFk(), models.get(0));
                        Object fkVal   = this.queryOne(relationParams.getType(), relationParams.getRelateSQL(), new Object[]{fkValue});
                        AnimaUtils.setFieldValue(relationParams.getFieldName(), models.get(0), fkVal);
                    } else {
                        Object[]      pkIds       = new Object[models.size()];
                        int           pos         = 0;
                        StringBuilder placeholder = new StringBuilder();
                        for (T model : models) {
                            Object fkValue = AnimaUtils.getFieldValue(relationParams.getFk(), model);
                            pkIds[pos++] = fkValue;
                            placeholder.append(",?");
                        }

                        String belongSQL = "SELECT * FROM " + relationParams.getTableName() + " WHERE " + relationParams.getPk() + " in (" + placeholder.substring(1) + ")";
                        List<?> list = this.queryList(relationParams.getType(),
                                belongSQL, pkIds);
                        for (int i = 0; i < models.size(); i++) {
                            if (list.size() > i) {
                                AnimaUtils.setFieldValue(relationParams.getFieldName(), models.get(i), list.get(i));
                            }
                        }
                    }
                });
    }

    private void setHasMany(List<T> models) {
        Arrays.stream(modelClass.getDeclaredFields())
                .filter(field -> null != field.getAnnotation(HasMany.class))
                .map(RelationParamBuilder::buildHasMany)
                .forEach(relationParams -> {
                    for (T model : models) {
                        Object fkValue = AnimaUtils.getPKFieldValue(model);
                        Object fkVal = this.queryList(relationParams.getType(), relationParams.getRelateSQL(),
                                new Object[]{fkValue});
                        AnimaUtils.setFieldValue(relationParams.getFieldName(), model, fkVal);
                    }
                });
    }

    private void setHasOne(List<T> models) {
        Arrays.stream(modelClass.getDeclaredFields())
                .filter(field -> null != field.getAnnotation(HasOne.class))
                .map(RelationParamBuilder::buildHasOne)
                .forEach(relationParams -> {
                    if (models.size() == 1) {
                        Object fkValue = AnimaUtils.getPKFieldValue(models.get(0));
                        Object fkVal   = this.queryOne(relationParams.getType(), relationParams.getRelateSQL(), new Object[]{fkValue});
                        AnimaUtils.setFieldValue(relationParams.getFieldName(), models.get(0), fkVal);
                    } else {
                        Object[]      pkIds       = new Object[models.size()];
                        int           pos         = 0;
                        StringBuilder placeholder = new StringBuilder();
                        for (T model : models) {
                            Object fkValue = AnimaUtils.getFieldValue(AnimaUtils.methodToFieldName(primaryKeyColumn), model);
                            pkIds[pos++] = fkValue;
                            placeholder.append(",?");
                        }

                        String  hasOneSQL = "SELECT * FROM " + relationParams.getTableName() + " WHERE " + relationParams.getFk() + " in (" + placeholder.substring(1) + ")";
                        List<?> list      = this.queryList(relationParams.getType(), hasOneSQL, pkIds);
                        for (int i = 0; i < models.size(); i++) {
                            if (list.size() > i) {
                                AnimaUtils.setFieldValue(relationParams.getFieldName(), models.get(i), list.get(i));
                            }
                        }
                    }
                });
    }

    private void clean(Connection conn) {
        this.selectColumns = null;
        this.isSQLLimit = false;
        this.orderBySQL = new StringBuilder();
        this.conditionSQL = new StringBuilder();
        this.paramValues.clear();
        this.excludedColumns.clear();
        this.updateColumns.clear();
        this.relations.clear();
        if (null == connectionThreadLocal.get() && null != conn) {
            conn.close();
        }
    }

}
