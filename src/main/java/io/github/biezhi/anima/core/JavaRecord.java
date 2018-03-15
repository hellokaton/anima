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

import io.github.biezhi.anima.annotation.AnimaIgnore;
import io.github.biezhi.anima.annotation.Table;
import io.github.biezhi.anima.enhancer.ResultKey;
import io.github.biezhi.anima.enums.SupportedType;
import io.github.biezhi.anima.exception.AnimaException;
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

    @AnimaIgnore
    private Class<? extends ActiveRecord> modelClass;

    @AnimaIgnore
    private StringBuilder       subSQL         = new StringBuilder();
    @AnimaIgnore
    private List<String>        orderBy        = new ArrayList<>();
    @AnimaIgnore
    private List<String>        excludedFields = new ArrayList<>();
    @AnimaIgnore
    private List<Object>        paramValues    = new ArrayList<>();
    @AnimaIgnore
    private Map<String, Object> updateColumns  = new LinkedHashMap<>();

    @AnimaIgnore
    private String selectColumns;

    @AnimaIgnore
    private final String tableName;

    @AnimaIgnore
    private final String pkName;

    public JavaRecord() {
        this.tableName = null;
        this.pkName = "id";
    }

    public JavaRecord(Class<? extends ActiveRecord> modelClass) {
        this.modelClass = modelClass;
        Table table = modelClass.getAnnotation(Table.class);
        this.tableName = null != table && SqlUtils.isNotEmpty(table.name()) ? table.name() :
                SqlUtils.toTableName(modelClass.getSimpleName(), Anima.me().tablePrefix());
        this.pkName = null != table ? table.pk() : "id";
    }

    public JavaRecord execlud(String... fieldNames) {
        Collections.addAll(excludedFields, fieldNames);
        return this;
    }

    public JavaRecord select(String columns) {
        this.selectColumns = columns;
        return this;
    }

    public JavaRecord where(String statement) {
        subSQL.append(" AND ").append(statement);
        return this;
    }

    public JavaRecord where(String statement, Object value) {
        subSQL.append(" AND ").append(statement);
        paramValues.add(value);
        return this;
    }

    public JavaRecord not(String key, Object value) {
        subSQL.append(" AND ").append(key).append(" != ?");
        paramValues.add(value);
        return this;
    }

    public JavaRecord distinct(String key) {

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
        subSQL.append(" AND ").append(key).append(" IN ?");
        paramValues.add(args);
        return this;
    }

    public <T> JavaRecord in(String key, List<T> args) {
        subSQL.append(" AND ").append(key).append(" IN ?");
        paramValues.add(args);
        return this;
    }

    public JavaRecord order(String order) {
        orderBy.add(order);
        return this;
    }

    public JavaRecord group(String column) {
        return this;
    }

    public <T extends ActiveRecord> T findById(Serializable... ids) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(tableName);
        sql.append(" WHERE ").append(pkName);
        if (ids.length == 1) {
            sql.append(" = ?");
        }
        if (ids.length > 1) {
            sql.append(" in ?");
        }
        try (Connection conn = getSql2o().open()) {
            return conn.createQuery(sql.toString()).withParams(ids).executeAndFetchFirst((Class<T>) modelClass);
        } finally {
            this.cleanParams();
        }
    }

    public <T extends ActiveRecord> List<T> all() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(tableName);
        try (Connection conn = getSql2o().open()) {
            return conn.createQuery(sql.toString()).executeAndFetch((Class<T>) modelClass);
        } finally {
            this.cleanParams();
        }
    }

    public <T extends ActiveRecord> List<T> limit(int limit) {
        return null;
    }

    public void page(int page, int limit) {

    }

    public long count() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tableName);

        if (subSQL.length() > 0) {
            sql.append(" WHERE ").append(subSQL.substring(5));
        }

        try (Connection conn = getSql2o().open()) {
            return conn.createQuery(sql.toString()).withParams(paramValues).executeAndFetchFirst(Long.class);
        } finally {
            this.cleanParams();
        }
    }

    public JavaRecord set(String column, Object value) {
        updateColumns.put(column, value);
        return this;
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

        try (Connection conn = getSql2o().open()) {
            return new ResultKey(conn.createQuery(sql.toString()).withParams(columnValueList).executeUpdate().getKey());
        } finally {
            this.cleanParams();
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

        try (Connection conn = getSql2o().open()) {
            return conn.createQuery(sql.toString()).withParams(columnValueList).executeUpdate().getResult();
        } finally {
            this.cleanParams();
        }
    }

    private static Sql2o getSql2o() {
        Sql2o sql2o = Anima.me().getCommonSql2o();
        if (null == sql2o) {
            throw new AnimaException("SQL2O instance not is null.");
        }
        return sql2o;
    }

    private void cleanParams() {
        selectColumns = null;
        subSQL = new StringBuilder();
        orderBy.clear();
        paramValues.clear();
        excludedFields.clear();
    }

    static boolean isMapping(Field field) {
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

}
