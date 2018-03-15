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

import io.github.biezhi.anima.annotation.Table;
import io.github.biezhi.anima.enums.SupportedType;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.page.Page;
import io.github.biezhi.anima.page.PageRow;
import io.github.biezhi.anima.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Java ActiveRecord Implements
 *
 * @author biezhi
 */
@Slf4j
public class JavaRecord {

    private Class<? extends Model> modelClass;

    private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    private StringBuilder       subSQL         = new StringBuilder();
    private List<String>        excludedFields = new ArrayList<>();
    private List<Object>        paramValues    = new ArrayList<>();
    private Map<String, Object> updateColumns  = new LinkedHashMap<>();

    private String orderBy;
    private String selectColumns;

    private String pkName;
    private String tableName;

    public JavaRecord() {
        this.tableName = null;
        this.pkName = "id";
    }

    public JavaRecord(Class<? extends Model> modelClass) {
        this.from(modelClass);
    }

    public JavaRecord execlud(String... fieldNames) {
        Collections.addAll(excludedFields, fieldNames);
        return this;
    }

    public JavaRecord select(String columns) {
        if (null != this.selectColumns) {
            throw new AnimaException("Select method can only be called once.");
        }
        this.selectColumns = columns;
        return this;
    }

    public JavaRecord where(String statement) {
        subSQL.append(" AND ").append(statement);
        return this;
    }

    public JavaRecord where(String statement, Object value) {
        subSQL.append(" AND ").append(statement);
        if (!statement.contains("?")) {
            subSQL.append(" = ?");
        }
        paramValues.add(value);
        return this;
    }

    public JavaRecord and(String statement, Object value) {
        return this.where(statement, value);
    }

    public JavaRecord not(String key, Object value) {
        subSQL.append(" AND ").append(key).append(" != ?");
        paramValues.add(value);
        return this;
    }

    public JavaRecord isNotNull(String key) {
        subSQL.append(" AND ").append(key).append(" IS NOT NULL");
        return this;
    }

    public JavaRecord like(String key, Object value) {
        subSQL.append(" AND ").append(key).append(" LIKE ?");
        paramValues.add(value);
        return this;
    }

    public JavaRecord in(String key, Object... args) {
        if (args.length > 1) {
            subSQL.append(" AND ").append(key).append(" IN (");
            for (int i = 0; i < args.length; i++) {
                if (i == args.length - 1) {
                    subSQL.append("?");
                } else {
                    subSQL.append("?, ");
                }
                paramValues.add(args[i]);
            }
            subSQL.append(")");
        }
        return this;
    }

    public JavaRecord between(String coulmn, Object a, Object b) {
        subSQL.append(" AND ").append(coulmn).append(" BETWEEN ? and ?");
        paramValues.add(a);
        paramValues.add(b);
        return this;
    }

    public <T> JavaRecord in(String key, List<T> args) {
        if (args.size() > 1) {
            subSQL.append(" AND ").append(key).append(" IN (");
            for (int i = 0; i < args.size(); i++) {
                if (i == args.size() - 1) {
                    subSQL.append("?");
                } else {
                    subSQL.append("?, ");
                }
                paramValues.add(args.get(i));
            }
            subSQL.append(")");
        }
        return this;
    }

    public JavaRecord order(String order) {
        this.orderBy = order;
        return this;
    }

    public <T> T find(Class<T> returnType, String sql, Object[] params) {
        try (Connection conn = getConn()) {
            return conn.createQuery(sql).withParams(params).executeAndFetchFirst(returnType);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T extends Model> T findById(Serializable id) {
        this.where(pkName, id);
        StringBuilder sql = this.buildSelectSQL();
        try (Connection conn = getConn()) {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetchFirst((Class<T>) modelClass);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T extends Model> List<T> findByIds(Serializable... ids) {
        this.in(pkName, ids);
        StringBuilder sql = this.buildSelectSQL();
        try (Connection conn = getConn()) {
            return conn.createQuery(sql.toString()).withParams(ids).executeAndFetch((Class<T>) modelClass);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T> T findBySQL(Class<T> type, String sql, Object... params) {
        try (Connection conn = getConn()) {
            return conn.createQuery(sql).withParams(params).executeAndFetchFirst(type);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T> List<T> findAllBySQL(Class<T> type, String sql, Object... params) {
        try (Connection conn = getConn()) {
            return conn.createQuery(sql).withParams(params).executeAndFetch(type);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T extends Model> List<T> all() {
        StringBuilder sql = this.buildSelectSQL();
        try (Connection conn = getConn()) {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetch((Class<T>) modelClass);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T extends Model> T one() {
        StringBuilder sql = this.buildSelectSQL();
        sql.append(" LIMIT 1");
        try (Connection conn = getConn()) {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetchFirst((Class<T>) modelClass);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T extends Model> List<T> limit(int limit) {
        return this.limit(0, limit);
    }

    public <T extends Model> List<T> limit(int offset, int limit) {
        StringBuilder sql = this.buildSelectSQL();
        sql.append(" LIMIT ?, ?");
        paramValues.add(offset);
        paramValues.add(limit);

        try (Connection conn = getConn()) {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetch((Class<T>) modelClass);
        } finally {
            this.cleanParams(null);
        }
    }

    public <T extends Model> Page<T> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

    public <T extends Model> Page<T> page(PageRow pageRow) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(null != this.selectColumns ? this.selectColumns : "*").append(" FROM ").append(tableName);
        if (subSQL.length() > 0) {
            sql.append(" WHERE ").append(subSQL.substring(5));
        }

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") tmp";

        try (Connection conn = getConn()) {
            long count = conn.createQuery(countSql).withParams(paramValues).executeAndFetchFirst(Long.class);

            if (null != orderBy) {
                sql.append(" ORDER BY ").append(this.orderBy);
            }
            sql.append(" LIMIT ?, ?");
            paramValues.add(pageRow.getOffset());
            paramValues.add(pageRow.getLimit());

            List<T> list = conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetch((Class<T>) modelClass);

            Page<T> pageBean = new Page<>(count, pageRow.getPage(), pageRow.getLimit());
            pageBean.setRows(list);
            return pageBean;
        } finally {
            this.cleanParams(null);
        }
    }

    public long count() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tableName);

        if (subSQL.length() > 0) {
            sql.append(" WHERE ").append(subSQL.substring(5));
        }

        try (Connection conn = getConn()) {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetchFirst(Long.class);
        } finally {
            this.cleanParams(null);
        }
    }

    public JavaRecord set(String column, Object value) {
        updateColumns.put(column, value);
        return this;
    }

    public int execute() {
        return 1;
    }

    public int execute(String sql, Object... params) {
        Connection conn = getConn();
        try {
            return conn.createQuery(sql).withParams(params).executeUpdate().getResult();
        } finally {
            this.cleanParams(conn);
        }
    }

    public ResultKey save(Object target) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName);

        StringBuffer columnNames     = new StringBuffer();
        StringBuffer placeholder     = new StringBuffer();
        List<Object> columnValueList = new ArrayList<>();

        for (Field field : modelClass.getDeclaredFields()) {
            if (isExcluded((field.getName()))) {
                continue;
            }
            if (!isMapping(field)) {
                continue;
            }
            field.setAccessible(true);
            columnNames.append(",").append(SqlUtils.toColumnName(field.getName()));
            placeholder.append(",?");
            try {
                Object value = field.get(target);
                columnValueList.add(value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AnimaException("illegal argument or Access:", e);
            }
        }
        sql.append("(").append(columnNames.substring(1)).append(")").append(" VALUES (")
                .append(placeholder.substring(1)).append(")");

        Connection conn = getConn();
        try {
            return new ResultKey(conn.createQuery(sql.toString()).withParams(columnValueList).executeUpdate().getKey());
        } finally {
            this.cleanParams(conn);
        }
    }

    public int delete() {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName);
        sql.append(" WHERE ").append(pkName).append(" = ?");

        Connection conn = getConn();
        try {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeUpdate().getResult();
        } finally {
            this.cleanParams(conn);
        }
    }

    public int deleteById(Serializable id) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName);
        sql.append(" WHERE ").append(pkName).append(" = ?");

        Connection conn = getConn();
        try {
            return conn.createQuery(sql.toString()).withParams(id).executeUpdate().getResult();
        } finally {
            this.cleanParams(conn);
        }
    }

    public int updateById(Serializable id) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName).append(" SET ");

        List<Object> columnValueList = new ArrayList<>();

        StringBuilder setSQL = sql;
        updateColumns.forEach((key, value) -> {
            setSQL.append(key).append(" = ?, ");
            columnValueList.add(value);
        });

        sql = new StringBuilder(setSQL.substring(0, setSQL.length() - 2));

        sql.append(" WHERE ").append(pkName).append(" = ?");
        columnValueList.add(id);
        Connection conn = getConn();
        try {
            return conn.createQuery(sql.toString()).withParams(columnValueList).executeUpdate().getResult();
        } finally {
            this.cleanParams(conn);
        }
    }

    public int update() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName).append(" SET ");

        List<Object> columnValueList = new ArrayList<>();

        StringBuilder setSQL = sql;
        updateColumns.forEach((key, value) -> {
            setSQL.append(key).append(" = ?, ");
            columnValueList.add(value);
        });

        sql = new StringBuilder(setSQL.substring(0, setSQL.length() - 2));

        if (subSQL.length() > 0) {
            sql.append(" WHERE ").append(subSQL.substring(5));
            columnValueList.addAll(paramValues);
        }
        Connection conn = getConn();
        try {
            return conn.createQuery(sql.toString()).withParams(columnValueList).executeUpdate().getResult();
        } finally {
            this.cleanParams(conn);
        }
    }

    private StringBuilder buildSelectSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(null != this.selectColumns ? this.selectColumns : "*").append(" FROM ").append(tableName);
        if (subSQL.length() > 0) {
            sql.append(" WHERE ").append(subSQL.substring(5));
        }
        if (null != orderBy) {
            sql.append(" ORDER BY ").append(this.orderBy);
        }
        return sql;
    }

    private static Connection getConn() {
        Connection connection = connectionThreadLocal.get();
        if (null == connection) {
            return getSql2o().open();
        }
        return connection;
    }

    static void beginTransaction() {
        Connection connection = JavaRecord.getSql2o().beginTransaction();
        connectionThreadLocal.set(connection);
    }

    static void endTransaction() {
        connectionThreadLocal.remove();
    }

    static void commit() {
        connectionThreadLocal.get().commit();
    }

    static void rollback() {
        connectionThreadLocal.get().rollback();
    }

    public static Sql2o getSql2o() {
        Sql2o sql2o = Anima.me().getCommonSql2o();
        if (null == sql2o) {
            throw new AnimaException("SQL2O instance not is null.");
        }
        return sql2o;
    }

    private void cleanParams(Connection conn) {
        selectColumns = null;
        orderBy = null;
        subSQL = new StringBuilder();
        paramValues.clear();
        excludedFields.clear();
        updateColumns.clear();
        if (null == connectionThreadLocal.get() && null != conn) {
            conn.close();
        }
    }

    private static boolean isMapping(Field field) {
        // serialVersionUID not processed
        if ("serialVersionUID".equals(field.getName())) {
            return false;
        }
        // exclude non-basic types (including wrapper classes), strings, etc.
        String typeName = field.getType().getSimpleName().toLowerCase();
        if (!SupportedType.contains(typeName)) {
            return false;
        }
        return true;
    }

    private boolean isExcluded(String name) {
        return excludedFields.contains(name) || name.startsWith("_");
    }

    public JavaRecord from(Class<? extends Model> modelClass) {
        this.modelClass = modelClass;
        Table table = modelClass.getAnnotation(Table.class);
        this.tableName = null != table && SqlUtils.isNotEmpty(table.name()) ? table.name() :
                SqlUtils.toTableName(modelClass.getSimpleName(), Anima.me().tablePrefix());
        this.pkName = null != table ? table.pk() : "id";
        return this;
    }

}
