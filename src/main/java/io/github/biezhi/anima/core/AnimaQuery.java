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
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.github.biezhi.anima.core.AnimaCache.*;
import static io.github.biezhi.anima.utils.Functions.*;
import static java.util.stream.Collectors.toList;

/**
 * Operational database core class
 *
 * @author biezhi
 */
@Slf4j
@NoArgsConstructor
public class AnimaQuery<T extends Model> {

    /**
     * SQL 2 o objects for the current query.
     */
    private static Sql2o sql2o;

    /**
     * Java Model, a table of corresponding databases.
     */
    private Class<T> modelClass;

    /**
     * The currently connected ThreadLocal, which stores the connection objects for SQL2O.
     */
    private static ThreadLocal<Connection> localConnection = new ThreadLocal<>();

    /**
     * Storage condition clause.
     */
    private StringBuilder conditionSQL = new StringBuilder();

    /**
     * Storage order by clause.
     */
    private StringBuilder orderBySQL = new StringBuilder();

    /**
     * Store the column names to be excluded.
     */
    private List<String> excludedColumns = new ArrayList<>(8);

    /**
     * Storage parameter list
     */
    private List<Object> paramValues = new ArrayList<>(8);

    /**
     * A column that stores updates.
     */
    private Map<String, Object> updateColumns = new LinkedHashMap<>(8);

    /**
     * Do you use SQL for limit operations and use "limit ?" if enabled.
     * The method of querying data is opened by default, and partial database does not support this operation.
     */
    private boolean isSQLLimit;

    /**
     * Is this query a custom SQL statement
     */
    private boolean useSQL;

    /**
     * Specify a few columns, such as “uid, name, age”
     */
    private String selectColumns;

    /**
     * Primary key column name
     */
    private String primaryKeyColumn;

    /**
     * Model table name
     */
    private String tableName;

    /**
     * @see DMLType
     */
    private DMLType dmlType;

    /**
     * Join model params
     */
    private List<JoinParam> joinParams = new ArrayList<>();

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

    /**
     * Excluded columns
     *
     * @param columnNames table column name
     * @return AnimaQuery
     */
    public AnimaQuery<T> exclude(String... columnNames) {
        Collections.addAll(excludedColumns, columnNames);
        return this;
    }

    /**
     * Excluded columns by lambda
     *
     * @param functions lambda columns
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> exclude(TypeFunction<T, R>... functions) {
        String[] columnNames = Arrays.stream(functions)
                .map(AnimaUtils::getLambdaColumnName)
                .collect(toList())
                .toArray(new String[functions.length]);
        return this.exclude(columnNames);
    }

    /**
     * Sets the query to specify the column.
     *
     * @param columns table column name
     * @return AnimaQuery
     */
    public AnimaQuery<T> select(String columns) {
        if (null != this.selectColumns) {
            throw new AnimaException("Select method can only be called once.");
        }
        this.selectColumns = columns;
        return this;
    }

    /**
     * where condition
     *
     * @param statement like "age > ?" "name = ?"
     * @return AnimaQuery
     */
    public AnimaQuery<T> where(String statement) {
        conditionSQL.append(" AND ").append(statement);
        return this;
    }

    /**
     * where condition, simultaneous setting value
     *
     * @param statement like "age > ?" "name = ?"
     * @param value     column name
     * @return AnimaQuery
     */
    public AnimaQuery<T> where(String statement, Object value) {
        conditionSQL.append(" AND ").append(statement);
        if (!statement.contains("?")) {
            conditionSQL.append(" = ?");
        }
        paramValues.add(value);
        return this;
    }

    /**
     * Set the column name using lambda
     *
     * @param function lambda expressions, use the Model::getXXX
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> where(TypeFunction<T, R> function) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        conditionSQL.append(" AND ").append(columnName);
        return this;
    }

    /**
     * Set the column name using lambda, at the same time setting the value, the SQL generated is "column = ?"
     *
     * @param function lambda expressions, use the Model::getXXX
     * @param value    column value
     * @param <S>
     * @param <R>
     * @return AnimaQuery
     */
    public <S extends Model, R> AnimaQuery<T> where(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        conditionSQL.append(" AND ").append(columnName).append(" = ?");
        paramValues.add(value);
        return this;
    }

    /**
     * Set the where parameter according to model,
     * and generate sql like where where age = ? and name = ?
     *
     * @param model
     * @return AnimaQuery
     */
    public AnimaQuery<T> where(T model) {
        Field[] declaredFields = model.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {

            Object value = AnimaUtils.invokeMethod(model, getGetterName(declaredField.getName()), AnimaUtils.EMPTY_ARG);
            if (null == value) {
                continue;
            }
            if (declaredField.getType().equals(String.class) && AnimaUtils.isEmpty(value.toString())) {
                continue;
            }
            String columnName = AnimaCache.getColumnName(declaredField);
            this.where(columnName, value);
        }
        return this;
    }

    /**
     * Equals statement
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> eq(Object value) {
        conditionSQL.append(" = ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "IS NOT NULL" statement
     *
     * @return AnimaQuery
     */
    public AnimaQuery<T> notNull() {
        conditionSQL.append(" IS NOT NULL");
        return this;
    }

    /**
     * generate AND statement, simultaneous setting value
     *
     * @param statement condition clause
     * @param value     column value
     * @return
     */
    public AnimaQuery<T> and(String statement, Object value) {
        return this.where(statement, value);
    }

    /**
     * generate AND statement with lambda
     *
     * @param function column name with lambda
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> and(TypeFunction<T, R> function) {
        return this.where(function);
    }

    /**
     * generate AND statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> and(TypeFunction<T, R> function, Object value) {
        return this.where(function, value);
    }

    /**
     * generate OR statement, simultaneous setting value
     *
     * @param statement like "name = ?" "age > ?"
     * @param value     column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> or(String statement, Object value) {
        conditionSQL.append(" OR (").append(statement);
        if (!statement.contains("?")) {
            conditionSQL.append(" = ?");
        }
        conditionSQL.append(')');
        paramValues.add(value);
        return this;
    }

    /**
     * generate "!=" statement, simultaneous setting value
     *
     * @param columnName column name [sql]
     * @param value      column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> notEq(String columnName, Object value) {
        conditionSQL.append(" AND ").append(columnName).append(" != ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "!=" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> notEq(TypeFunction<T, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.notEq(columnName, value);
    }

    /**
     * generate "!=" statement, simultaneous setting value
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> notEq(Object value) {
        conditionSQL.append(" != ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "!= ''" statement
     *
     * @param columnName column name
     * @return AnimaQuery
     */
    public AnimaQuery<T> notEmpty(String columnName) {
        conditionSQL.append(" AND ").append(columnName).append(" != ''");
        return this;
    }

    /**
     * generate "!= ''" statement with lambda
     *
     * @param function column name with lambda
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> notEmpty(TypeFunction<T, R> function) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.notEmpty(columnName);
    }

    /**
     * generate "!= ''" statement
     *
     * @return AnimaQuery
     */
    public AnimaQuery<T> notEmpty() {
        conditionSQL.append(" != ''");
        return this;
    }

    /**
     * generate "IS NOT NULL" statement
     *
     * @param columnName column name
     * @return
     */
    public AnimaQuery<T> notNull(String columnName) {
        conditionSQL.append(" AND ").append(columnName).append(" IS NOT NULL");
        return this;
    }

    /**
     * generate like statement, simultaneous setting value
     *
     * @param columnName column name
     * @param value      column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> like(String columnName, Object value) {
        conditionSQL.append(" AND ").append(columnName).append(" LIKE ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate like statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> like(TypeFunction<T, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.like(columnName, value);
    }

    /**
     * generate like statement, simultaneous setting value
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> like(Object value) {
        conditionSQL.append(" LIKE ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate between statement, simultaneous setting value
     *
     * @param columnName column name
     * @param a          first range value
     * @param b          second range value
     * @return AnimaQuery
     */
    public AnimaQuery<T> between(String columnName, Object a, Object b) {
        conditionSQL.append(" AND ").append(columnName).append(" BETWEEN ? and ?");
        paramValues.add(a);
        paramValues.add(b);
        return this;
    }

    /**
     * generate between statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param a        first range value
     * @param b        second range value
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> between(TypeFunction<T, R> function, Object a, Object b) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.between(columnName, a, b);
    }

    /**
     * generate between values
     *
     * @param a first range value
     * @param b second range value
     * @return AnimaQuery
     */
    public AnimaQuery<T> between(Object a, Object b) {
        conditionSQL.append(" BETWEEN ? and ?");
        paramValues.add(a);
        paramValues.add(b);
        return this;
    }

    /**
     * generate ">" statement, simultaneous setting value
     *
     * @param columnName table column name [sql]
     * @param value      column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> gt(String columnName, Object value) {
        conditionSQL.append(" AND ").append(columnName).append(" > ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate ">" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> gt(TypeFunction<T, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.gt(columnName, value);
    }

    /**
     * generate ">" statement value
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> gt(Object value) {
        conditionSQL.append(" > ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate ">=" statement value
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> gte(Object value) {
        conditionSQL.append(" >= ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate ">=" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <S extends Model, R> AnimaQuery<T> gte(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.gte(columnName, value);
    }

    /**
     * generate "<" statement value
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> lt(Object value) {
        conditionSQL.append(" < ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "<" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <S extends Model, R> AnimaQuery<T> lt(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.lt(columnName, value);
    }

    /**
     * generate "<=" statement value
     *
     * @param value column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> lte(Object value) {
        conditionSQL.append(" <= ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "<=" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <R>
     * @return AnimaQuery
     */
    public <S extends Model, R> AnimaQuery<T> lte(TypeFunction<S, R> function, Object value) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.lte(columnName, value);
    }

    /**
     * generate ">=" statement, simultaneous setting value
     *
     * @param column table column name [sql]
     * @param value  column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> gte(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" >= ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "<" statement, simultaneous setting value
     *
     * @param column table column name [sql]
     * @param value  column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> lt(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" < ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "<=" statement, simultaneous setting value
     *
     * @param column table column name [sql]
     * @param value  column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> lte(String column, Object value) {
        conditionSQL.append(" AND ").append(column).append(" <= ?");
        paramValues.add(value);
        return this;
    }

    /**
     * generate "in" statement, simultaneous setting value
     *
     * @param column table column name [sql]
     * @param args   column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> in(String column, Object... args) {
        if (null == args || args.length == 0) {
            log.warn("Column: {}, query params is empty.");
            return this;
        }
        conditionSQL.append(" AND ").append(column).append(" IN (");
        this.setArguments(args);
        conditionSQL.append(")");
        return this;
    }

    /**
     * generate "in" statement value
     *
     * @param args column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> in(Object... args) {
        if (null == args || args.length == 0) {
            log.warn("Column: {}, query params is empty.");
            return this;
        }
        conditionSQL.append(" IN (");
        this.setArguments(args);
        conditionSQL.append(")");
        return this;
    }

    /**
     * Set in params
     *
     * @param list in param values
     * @param <S>
     * @return AnimaQuery
     */
    public <S> AnimaQuery<T> in(List<S> list) {
        return this.in(list.toArray());
    }

    /**
     * generate "in" statement, simultaneous setting value
     *
     * @param column column name
     * @param args   in param values
     * @param <S>
     * @return AnimaQuery
     */
    public <S> AnimaQuery<T> in(String column, List<S> args) {
        return this.in(column, args.toArray());
    }

    /**
     * generate "in" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param values   in param values
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> in(TypeFunction<T, R> function, Object... values) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.in(columnName, values);
    }

    /**
     * generate "in" statement with lambda, simultaneous setting value
     *
     * @param function column name with lambda
     * @param values   in param values
     * @param <R>
     * @return AnimaQuery
     */
    public <S, R> AnimaQuery<T> in(TypeFunction<T, R> function, List<S> values) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return this.in(columnName, values);
    }

    /**
     * generate order by statement
     *
     * @param order like "id desc"
     * @return AnimaQuery
     */
    public AnimaQuery<T> order(String order) {
        if (this.orderBySQL.length() > 0) {
            this.orderBySQL.append(',');
        }
        this.orderBySQL.append(' ').append(order);
        return this;
    }

    /**
     * generate order by statement
     *
     * @param columnName column name
     * @param orderBy    order by @see OrderBy
     * @return AnimaQuery
     */
    public AnimaQuery<T> order(String columnName, OrderBy orderBy) {
        if (this.orderBySQL.length() > 0) {
            this.orderBySQL.append(',');
        }
        this.orderBySQL.append(' ').append(columnName).append(' ').append(orderBy.toString());
        return this;
    }

    /**
     * generate order by statement with lambda
     *
     * @param function column name with lambda
     * @param orderBy  order by @see OrderBy
     * @param <R>
     * @return AnimaQuery
     */
    public <R> AnimaQuery<T> order(TypeFunction<T, R> function, OrderBy orderBy) {
        String columnName = AnimaUtils.getLambdaColumnName(function);
        return order(columnName, orderBy);
    }

    /**
     * query model by primary key
     *
     * @param id primary key value
     * @return model instance
     */
    public T byId(Object id) {
        this.beforeCheck();
        this.where(primaryKeyColumn, id);

        String sql = this.buildSelectSQL(false);

        T model = this.queryOne(modelClass, sql, paramValues);

        ifNotNullThen(model, () -> this.setJoin(Collections.singletonList(model)));

        return model;
    }

    /**
     * query models by primary keys
     *
     * @param ids primary key values
     * @return models
     */
    public List<T> byIds(Object... ids) {
        this.in(this.primaryKeyColumn, ids);
        return this.all();
    }

    /**
     * query and find one model
     *
     * @return one model
     */
    public T one() {
        this.beforeCheck();

        String sql = this.buildSelectSQL(true);

        T model = this.queryOne(modelClass, sql, paramValues);

        ifThen(null != model && null != joinParams,
                () -> this.setJoin(Collections.singletonList(model)));

        return model;
    }

    /**
     * query and find all model
     *
     * @return model list
     */
    public List<T> all() {
        this.beforeCheck();
        String  sql    = this.buildSelectSQL(true);
        List<T> models = this.queryList(modelClass, sql, paramValues);
        this.setJoin(models);
        return models;
    }

    /**
     * query List<Map>
     *
     * @return List<Map>
     */
    public List<Map<String, Object>> maps() {
        this.beforeCheck();
        String sql = this.buildSelectSQL(true);
        return this.queryListMap(sql, paramValues);
    }

    /**
     * @return models stream
     */
    public Stream<T> stream() {
        List<T> all = all();

        return ifReturn(null == all || all.isEmpty(),
                Stream.empty(),
                Objects.requireNonNull(all).stream());
    }

    /**
     * Parallel processing of the model list.
     *
     * @return parallel stream
     */
    public Stream<T> parallel() {
        return stream().parallel();
    }

    /**
     * Transform the results of the model.
     *
     * @param function transform lambda
     * @param <R>
     * @return Stream
     */
    public <R> Stream<R> map(Function<T, R> function) {
        return stream().map(function);
    }

    /**
     * Filter the list of models that are found.
     *
     * @param predicate predicate lambda
     * @return Stream
     */
    public Stream<T> filter(Predicate<T> predicate) {
        return stream().filter(predicate);
    }

    /**
     * Take the data of the fixed number from the result.
     *
     * @param limit model size
     * @return model list
     */
    public List<T> limit(int limit) {
        return ifReturn(Anima.of().isUseSQLLimit(),
                () -> {
                    isSQLLimit = true;
                    paramValues.add(limit);
                    return all();
                },
                () -> {
                    List<T> all = all();
                    return ifReturn(all.size() > limit,
                            all.stream().limit(limit).collect(toList()),
                            all);
                });
    }

    /**
     * Paging query results
     *
     * @param page  page number
     * @param limit number each page
     * @return Page
     */
    public Page<T> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

    /**
     * Paging query results by sql
     *
     * @param sql     sql statement
     * @param pageRow page param
     * @return Page
     */
    public Page<T> page(String sql, PageRow pageRow) {
        return this.page(sql, paramValues, pageRow);
    }

    /**
     * Paging query results by sql
     *
     * @param sql         sql statement
     * @param paramValues param values
     * @param pageRow     page param
     * @return Page
     */
    public Page<T> page(String sql, List<Object> paramValues, PageRow pageRow) {
        return this.page(sql, paramValues.toArray(), pageRow);
    }

    /**
     * Paging query results by sql
     *
     * @param sql     sql statement
     * @param params  param values
     * @param pageRow page param
     * @return Page
     */
    public Page<T> page(String sql, Object[] params, PageRow pageRow) {
        this.beforeCheck();
        Connection conn = getConn();
        try {
            String countSql = useSQL ? "SELECT COUNT(*) FROM (" + sql + ") tmp" : buildCountSQL(sql);

            long count = conn.createQuery(countSql)
                    .withParams(params)
                    .executeAndFetchFirst(Long.class);

            Page<T> pageBean = new Page<>(count, pageRow.getPageNum(), pageRow.getPageSize());

            ifThen(count > 0, () -> {
                String pageSQL = this.buildPageSQL(sql, pageRow);
                List<T> list = conn.createQuery(pageSQL)
                        .withParams(params)
                        .setAutoDeriveColumnNames(true)
                        .throwOnMappingFailure(false)
                        .executeAndFetch(modelClass);

                this.setJoin(list);
                pageBean.setRows(list);
            });

            return pageBean;
        } finally {
            this.closeConn(conn);
            this.clean(null);
        }
    }

    private String buildCountSQL(String sql) {
        return "SELECT COUNT(*) " + sql.substring(sql.indexOf("FROM"));
    }

    /**
     * Paging query results
     *
     * @param pageRow page params
     * @return Page
     */
    public Page<T> page(PageRow pageRow) {
        String sql = this.buildSelectSQL(false);
        return this.page(sql, pageRow);
    }

    /**
     * Count the number of rows.
     *
     * @return models count
     */
    public long count() {
        this.beforeCheck();
        String sql = this.buildCountSQL();
        return this.queryOne(Long.class, sql, paramValues);
    }

    /**
     * Update columns set value
     *
     * @param column column name
     * @param value  column value
     * @return AnimaQuery
     */
    public AnimaQuery<T> set(String column, Object value) {
        updateColumns.put(column, value);
        return this;
    }

    /**
     * Update the model sets column.
     *
     * @param function column name with lambda
     * @param value    column value
     * @param <S>
     * @param <R>
     * @return
     */
    public <S extends Model, R> AnimaQuery<T> set(TypeFunction<S, R> function, Object value) {
        return this.set(AnimaUtils.getLambdaColumnName(function), value);
    }

    /**
     * Add a cascading query.
     *
     * @param joinParam Join params
     * @return AnimaQuery
     */
    public AnimaQuery<T> join(JoinParam joinParam) {
        ifNullThrow(joinParam,
                new AnimaException("Join param not null"));

        ifNullThrow(joinParam.getJoinModel(),
                new AnimaException("Join param [model] not null"));

        ifNullThrow(AnimaUtils.isEmpty(joinParam.getFieldName()),
                new AnimaException("Join param [as] not empty"));

        ifNullThrow(AnimaUtils.isEmpty(joinParam.getOnLeft()),
                new AnimaException("Join param [onLeft] not empty"));

        ifNullThrow(AnimaUtils.isEmpty(joinParam.getOnRight()),
                new AnimaException("Join param [onRight] not empty"));

        this.joinParams.add(joinParam);
        return this;
    }

    /**
     * Querying a model
     *
     * @param type   model type
     * @param sql    sql statement
     * @param params params
     * @param <S>
     * @return S
     */
    public <S> S queryOne(Class<S> type, String sql, Object[] params) {
        Connection conn = getConn();
        try {
            Query query = conn.createQuery(sql)
                    .withParams(params)
                    .setAutoDeriveColumnNames(true)
                    .throwOnMappingFailure(false);

            return ifReturn(AnimaUtils.isBasicType(type),
                    () -> query.executeScalar(type),
                    () -> query.executeAndFetchFirst(type));
        } finally {
            this.closeConn(conn);
            this.clean(null);
        }
    }

    /**
     * Querying a model
     *
     * @param type   model type
     * @param sql    sql statement
     * @param params params
     * @param <S>
     * @return S
     */
    public <S> S queryOne(Class<S> type, String sql, List<Object> params) {
        if (Anima.of().isUseSQLLimit()) {
            sql += " LIMIT 1";
        }
        List<S> list = queryList(type, sql, params);
        return AnimaUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * Querying a list
     *
     * @param type   model type
     * @param sql    sql statement
     * @param params params
     * @param <S>
     * @return List<S>
     */
    public <S> List<S> queryList(Class<S> type, String sql, Object[] params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql)
                    .withParams(params)
                    .setColumnMappings(computeModelColumnMappings(type))
                    .throwOnMappingFailure(false)
                    .executeAndFetch(type);
        } finally {
            this.closeConn(conn);
            this.clean(null);
        }
    }

    /**
     * Querying a list
     *
     * @param type   model type
     * @param sql    sql statement
     * @param params params
     * @param <S>
     * @return List<S>
     */
    public <S> List<S> queryList(Class<S> type, String sql, List<Object> params) {
        return this.queryList(type, sql, params.toArray());
    }

    /**
     * Querying a List<Map>
     *
     * @param sql    sql statement
     * @param params params
     * @return List<Map>
     */
    public List<Map<String, Object>> queryListMap(String sql, Object[] params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql)
                    .withParams(params)
                    .setAutoDeriveColumnNames(true)
                    .throwOnMappingFailure(false)
                    .executeAndFetchTable()
                    .asList();
        } finally {
            this.closeConn(conn);
            this.clean(null);
        }
    }

    /**
     * Querying a List<Map>
     *
     * @param sql    sql statement
     * @param params params
     * @return List<Map>
     */
    public List<Map<String, Object>> queryListMap(String sql, List<Object> params) {
        return this.queryListMap(sql, params.toArray());
    }

    /**
     * Execute sql statement
     *
     * @return affect the number of rows
     */
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

    /**
     * Execute sql statement
     *
     * @param sql    sql statement
     * @param params params
     * @return affect the number of rows
     */
    public int execute(String sql, Object... params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql)
                    .withParams(params)
                    .executeUpdate()
                    .getResult();
        } finally {
            this.closeConn(conn);
            this.clean(conn);
        }
    }

    /**
     * Execute sql statement
     *
     * @param sql    sql statement
     * @param params params
     * @return affect the number of rows
     */
    public int execute(String sql, List<Object> params) {
        return this.execute(sql, params.toArray());
    }

    /**
     * Save a model
     *
     * @param model model instance
     * @param <S>
     * @return ResultKey
     */
    public <S extends Model> ResultKey save(S model) {
        List<Object> columnValues = AnimaUtils.toColumnValues(model, true);
        String       sql          = this.buildInsertSQL(model, columnValues);
        Connection   conn         = getConn();
        try {

            List<Object> params = columnValues.stream()
                    .filter(Objects::nonNull)
                    .collect(toList());

            return new ResultKey(conn.createQuery(sql)
                    .withParams(params)
                    .executeUpdate()
                    .getKey());
        } finally {
            this.closeConn(conn);
            this.clean(conn);
        }
    }

    /**
     * Delete model
     *
     * @return affect the number of rows
     */
    public int delete() {
        String sql = this.buildDeleteSQL(null);
        return this.execute(sql, paramValues);
    }

    /**
     * Delete model by primary key
     *
     * @param id  primary key value
     * @param <S>
     * @return affect the number of rows, normally it's 1.
     */
    public <S extends Serializable> int deleteById(S id) {
        this.where(primaryKeyColumn, id);
        return this.delete();
    }

    /**
     * Delete model
     *
     * @param model model instance
     * @param <S>
     * @return affect the number of rows
     */
    public <S extends Model> int deleteByModel(S model) {
        this.beforeCheck();
        String       sql             = this.buildDeleteSQL(model);
        List<Object> columnValueList = AnimaUtils.toColumnValues(model, false);
        return this.execute(sql, columnValueList);
    }

    /**
     * Update operation
     *
     * @return affect the number of rows
     */
    public int update() {
        this.beforeCheck();
        String       sql             = this.buildUpdateSQL(null, updateColumns);
        List<Object> columnValueList = new ArrayList<>();
        updateColumns.forEach((key, value) -> columnValueList.add(value));
        columnValueList.addAll(paramValues);
        return this.execute(sql, columnValueList);
    }

    /**
     * Update model by primary key
     *
     * @param id primary key value
     * @return affect the number of rows, normally it's 1.
     */
    public int updateById(Serializable id) {
        this.where(primaryKeyColumn, id);
        return this.update();
    }

    /**
     * Update model by primary key
     *
     * @param model model instance
     * @param id    primary key value
     * @param <S>
     * @return affect the number of rows, normally it's 1.
     */
    public <S extends Model> int updateById(S model, Serializable id) {
        this.where(primaryKeyColumn, id);
        String       sql             = this.buildUpdateSQL(model, null);
        List<Object> columnValueList = AnimaUtils.toColumnValues(model, false);
        columnValueList.add(id);
        return this.execute(sql, columnValueList);
    }

    /**
     * Update a model
     *
     * @param model model instance
     * @param <S>
     * @return affect the number of rows
     */
    public <S extends Model> int updateByModel(S model) {
        this.beforeCheck();

        Object primaryKey = AnimaUtils.getAndRemovePrimaryKey(model);

        StringBuilder sql = new StringBuilder(this.buildUpdateSQL(model, null));

        List<Object> columnValueList = AnimaUtils.toColumnValues(model, false);

        ifNotNullThen(primaryKey, () -> {
            sql.append(" WHERE ").append(this.primaryKeyColumn).append(" = ?");
            columnValueList.add(primaryKey);
        });

        return this.execute(sql.toString(), columnValueList);
    }

    private void setArguments(Object[] args) {
        for (int i = 0; i < args.length; i++) {

            ifThen(i == args.length - 1,
                    () -> conditionSQL.append("?"),
                    () -> conditionSQL.append("?, "));

            paramValues.add(args[i]);
        }
    }

    /**
     * Build a select statement.
     *
     * @param addOrderBy add the order by clause.
     * @return select sql
     */
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

        ifThen(addOrderBy, () -> sqlParams.setOrderBy(this.orderBySQL.toString()));

        return Anima.of().dialect().select(sqlParams);
    }

    /**
     * Build a count statement.
     *
     * @return count sql
     */
    private String buildCountSQL() {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .conditionSQL(this.conditionSQL)
                .build();
        return Anima.of().dialect().count(sqlParams);
    }

    /**
     * Build a paging statement
     *
     * @param pageRow page param
     * @return paging sql
     */
    private String buildPageSQL(String sql, PageRow pageRow) {
        SQLParams sqlParams = SQLParams.builder()
                .modelClass(this.modelClass)
                .selectColumns(this.selectColumns)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .conditionSQL(this.conditionSQL)
                .excludedColumns(this.excludedColumns)
                .customSQL(sql)
                .orderBy(this.orderBySQL.toString())
                .pageRow(pageRow)
                .build();
        return Anima.of().dialect().paginate(sqlParams);
    }

    /**
     * Build a insert statement.
     *
     * @param model model instance
     * @param <S>
     * @return insert sql
     */
    private <S extends Model> String buildInsertSQL(S model, List<Object> columnValues) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .columnValues(columnValues)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .build();

        return Anima.of().dialect().insert(sqlParams);
    }

    /**
     * Build a update statement.
     *
     * @param model         model instance
     * @param updateColumns update columns
     * @param <S>
     * @return update sql
     */
    private <S extends Model> String buildUpdateSQL(S model, Map<String, Object> updateColumns) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .updateColumns(updateColumns)
                .conditionSQL(this.conditionSQL)
                .build();

        return Anima.of().dialect().update(sqlParams);
    }

    /**
     * Build a delete statement.
     *
     * @param model model instance
     * @param <S>
     * @return delete sql
     */
    private <S extends Model> String buildDeleteSQL(S model) {
        SQLParams sqlParams = SQLParams.builder()
                .model(model)
                .modelClass(this.modelClass)
                .tableName(this.tableName)
                .pkName(this.primaryKeyColumn)
                .conditionSQL(this.conditionSQL)
                .build();
        return Anima.of().dialect().delete(sqlParams);
    }

    public AnimaQuery<T> useSQL() {
        this.useSQL = true;
        return this;
    }

    /**
     * pre check
     */
    private void beforeCheck() {
        ifNullThrow(this.modelClass, new AnimaException(ErrorCode.FROM_NOT_NULL));
    }

    /**
     * Get a database connection.
     *
     * @return Connection
     */
    private Connection getConn() {
        Connection connection = localConnection.get();
        return ifNotNullReturn(connection, connection, this.getSql2o().open());
    }

    /**
     * Begin a transaction.
     */
    public static void beginTransaction() {
        ifNullThen(localConnection.get(),
                () -> {
                    Connection connection = getSql2o().beginTransaction();
                    localConnection.set(connection);
                });
    }

    /**
     * End a transaction.
     */
    public static void endTransaction() {
        ifNotNullThen(localConnection.get(),
                () -> {
                    Connection connection = localConnection.get();
                    ifThen(connection.isRollbackOnClose(), connection::close);
                    localConnection.remove();
                });
    }

    /**
     * Commit connection
     */
    public static void commit() {
        localConnection.get().commit();
    }

    /**
     * Roll back connection
     */
    public static void rollback() {
        ifNotNullThen(localConnection.get(),
                () -> {
                    log.warn("Rollback connection.");
                    localConnection.get().rollback();
                });
    }

    public AnimaQuery<T> bindSQL2o(Sql2o sql2o) {
        AnimaQuery.sql2o = sql2o;
        return this;
    }

    public static Sql2o getSql2o() {
        return ifNotNullReturn(sql2o,
                () -> {
                    Sql2o sql2o = Anima.of().getSql2o();
                    ifNullThrow(sql2o, new AnimaException("SQL2O instance not is null."));
                    return sql2o;
                });
    }

    /**
     * Set models join fields
     *
     * @param models model list
     */
    private void setJoin(List<T> models) {
        if (null == models || models.isEmpty() ||
                joinParams.size() == 0) {
            return;
        }
        models.stream().filter(Objects::nonNull).forEach(this::setJoin);
    }

    /**
     * Set model join fields
     *
     * @param model model instance
     */
    private void setJoin(T model) {
        for (JoinParam joinParam : joinParams) {
            try {
                Object leftValue = AnimaUtils.invokeMethod(
                        model,
                        getGetterName(joinParam.getOnLeft()),
                        AnimaUtils.EMPTY_ARG);

                String sql = "SELECT * FROM " + AnimaCache.getTableName(joinParam.getJoinModel()) +
                        " WHERE " + joinParam.getOnRight() + " = ?";

                Field field = model.getClass()
                        .getDeclaredField(joinParam.getFieldName());


                if (field.getType().equals(List.class)) {
                    if (AnimaUtils.isNotEmpty(joinParam.getOrderBy())) {
                        sql += " ORDER BY " + joinParam.getOrderBy();
                    }
                    List<? extends Model> list = this.queryList(joinParam.getJoinModel(), sql, new Object[]{leftValue});
                    AnimaUtils.invokeMethod(model, getSetterName(joinParam.getFieldName()), new Object[]{list});
                }

                if (field.getType().equals(joinParam.getJoinModel())) {
                    Object joinObject = this.queryOne(joinParam.getJoinModel(), sql, new Object[]{leftValue});
                    AnimaUtils.invokeMethod(model, getSetterName(joinParam.getFieldName()), new Object[]{joinObject});
                }
            } catch (NoSuchFieldException e) {
                log.error("Set join error", e);
            }
        }
    }

    private void closeConn(Connection connection) {
        ifThen(localConnection.get() == null && connection != null,
                () -> connection.close());
    }

    /**
     * Clear the battlefield after a database operation.
     *
     * @param conn sql2o connection
     */
    private void clean(Connection conn) {
        this.selectColumns = null;
        this.isSQLLimit = false;
        this.orderBySQL = new StringBuilder();
        this.conditionSQL = new StringBuilder();
        this.paramValues.clear();
        this.excludedColumns.clear();
        this.updateColumns.clear();

        ifThen(localConnection.get() == null && conn != null,
                () -> conn.close());
    }

}
