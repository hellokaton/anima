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
package io.github.biezhi.anima;

import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.Atomic;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.core.dml.Delete;
import io.github.biezhi.anima.core.dml.Select;
import io.github.biezhi.anima.core.dml.Update;
import io.github.biezhi.anima.core.functions.TypeFunction;
import io.github.biezhi.anima.dialect.Dialect;
import io.github.biezhi.anima.dialect.MySQLDialect;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.utils.AnimaUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.quirks.Quirks;
import org.sql2o.quirks.QuirksDetector;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.biezhi.anima.enums.ErrorCode.SQL2O_IS_NULL;

/**
 * Anima
 *
 * @author biezhi
 * @date 2018/3/13
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Anima {

    /**
     * The object of the underlying operation database.
     */
    @Getter
    @Setter
    private Sql2o sql2o;

    /**
     * Global table prefix
     */
    @Getter
    @Setter
    private String tablePrefix;

    /**
     * Database dialect, default by MySQL
     */
    @Getter
    @Setter
    private Dialect dialect = new MySQLDialect();

    /**
     * The type of rollback when an exception occurs, default by RuntimeException
     */
    @Setter
    private Class<? extends Exception> rollbackException = RuntimeException.class;

    /**
     * SQL performance statistics are enabled, which is enabled by default,
     * and outputs the elapsed time required.
     */
    @Getter
    private boolean enableSQLStatistic = true;

    /**
     * use the limit statement of SQL and use "limit ?" when enabled, the way to retrieve a fixed number of rows.
     */
    @Getter
    private boolean useSQLLimit = true;

    private static Anima instance;

    /**
     * see {@link #of()}
     * @return
     */
    @Deprecated
    public static Anima me() {
        return of();
    }

    public static Anima of() {
        if (null == instance.sql2o) {
            throw new AnimaException(SQL2O_IS_NULL);
        }
        return instance;
    }

    /**
     * Create anima with Sql2o
     *
     * @param sql2o sql2o instance
     */
    public Anima(Sql2o sql2o) {
        open(sql2o);
    }

    /**
     * Create anima with datasource
     *
     * @param dataSource datasource instance
     */
    public Anima(DataSource dataSource) {
        open(dataSource);
    }

    /**
     * Create anima with url and db info
     *
     * @param url  jdbc url
     * @param user database username
     * @param pass database password
     */
    public Anima(String url, String user, String pass) {
        open(url, user, pass);
    }

    /**
     * Create anima with Sql2o
     *
     * @param sql2o sql2o instance
     * @return Anima
     */
    public static Anima open(Sql2o sql2o) {
        Anima anima = new Anima();
        anima.setSql2o(sql2o);
        instance = anima;
        return anima;
    }

    /**
     * Create anima with url, like Sqlite or h2
     *
     * @param url jdbc url
     * @return Anima
     */
    public static Anima open(String url) {
        return open(url, null, null);
    }

    /**
     * Create anima with url, like Sqlite or h2
     *
     * @param url jdbc url
     * @return Anima
     */
    public static Anima open(String url, Quirks quirks) {
        return open(url, null, null, quirks);
    }

    /**
     * Create anima with datasource
     *
     * @param dataSource datasource instance
     * @return Anima
     */
    public static Anima open(DataSource dataSource) {
        return open(new Sql2o(dataSource));
    }

    /**
     * Create anima with datasource and quirks
     *
     * @param dataSource datasource instance
     * @return Anima
     */
    public static Anima open(DataSource dataSource, Quirks quirks) {
        return open(new Sql2o(dataSource, quirks));
    }

    /**
     * Create anima with url and db info
     *
     * @param url  jdbc url
     * @param user database username
     * @param pass database password
     * @return Anima
     */
    public static Anima open(String url, String user, String pass) {
        return open(url, user, pass, QuirksDetector.forURL(url));
    }

    /**
     * Create anima with url and db info
     *
     * @param url    jdbc url
     * @param user   database username
     * @param pass   database password
     * @param quirks DBQuirks
     * @return Anima
     */
    public static Anima open(String url, String user, String pass, Quirks quirks) {
        return open(new Sql2o(url, user, pass, quirks));
    }

    /**
     * Code that performs a transaction operation.
     *
     * @param runnable the code snippet to execute.
     * @return Atomic
     */
    public static Atomic atomic(Runnable runnable) {
        try {
            AnimaQuery.beginTransaction();
            runnable.run();
            AnimaQuery.commit();
            return Atomic.ok();
        } catch (Exception e) {
            boolean isRollback = false;
            if (me().rollbackException.isInstance(e)) {
                AnimaQuery.rollback();
                isRollback = true;
            }
            return Atomic.error(e).rollback(isRollback);
        } finally {
            AnimaQuery.endTransaction();
        }
    }

    /**
     * Set the type of rollback exception to trigger the transaction rollback.
     *
     * @param rollbackException roll back exception type
     * @return Anima
     */
    public Anima rollbackException(Class<? extends Exception> rollbackException) {
        this.rollbackException = rollbackException;
        return this;
    }

    /**
     * Set the global table prefix, like "t_"
     *
     * @param tablePrefix table prefix
     * @return Anima
     */
    public Anima tablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
    }

    /**
     * Specify a database dialect.
     *
     * @param dialect @see Dialect
     * @return Anima
     */
    public Anima dialect(Dialect dialect) {
        this.dialect = dialect;
        return this;
    }

    /**
     * Set whether SQL statistics are enabled.
     *
     * @param enableSQLStatistic sql statistics
     * @return Anima
     */
    public Anima enableSQLStatistic(boolean enableSQLStatistic) {
        this.enableSQLStatistic = enableSQLStatistic;
        return this;
    }

    /**
     * Set the use of SQL limit.
     *
     * @param useSQLLimit use sql limit
     * @return Anima
     */
    public Anima useSQLLimit(boolean useSQLLimit) {
        this.useSQLLimit = useSQLLimit;
        return this;
    }

    /**
     * Add custom Type converter
     *
     * @param converters converter see {@link Converter}
     * @return Anima
     */
    public Anima addConverter(Converter<?>... converters) {
        if (null == converters || converters.length == 0) {
            throw new AnimaException("converters not be null.");
        }
        for (Converter<?> converter : converters) {
            Type[]   types  = converter.getClass().getGenericInterfaces();
            Type[]   params = ((ParameterizedType) types[0]).getActualTypeArguments();
            Class<?> type   = (Class) params[0];
            sql2o.getQuirks().addConverter(type, converter);
        }
        return this;
    }

    /**
     * Open a query statement.
     *
     * @return Select
     */
    public static Select select() {
        return new Select();
    }

    /**
     * Open a query statement and specify the query for some columns.
     *
     * @param columns column names
     * @return Select
     */
    public static Select select(String columns) {
        return new Select(columns);
    }

    /**
     * Set the query to fix columns with lambda
     *
     * @param functions column lambdas
     * @param <T>
     * @param <R>
     * @return Select
     */
    @SafeVarargs
    public static <T extends Model, R> Select select(TypeFunction<T, R>... functions) {
        return select(Arrays.stream(functions).map(AnimaUtils::getLambdaColumnName).collect(Collectors.joining(", ")));
    }

    /**
     * Open an update statement.
     *
     * @return Update
     */
    public static Update update() {
        return new Update();
    }

    /**
     * Open a delete statement.
     *
     * @return Delete
     */
    public static Delete delete() {
        return new Delete();
    }

    /**
     * Save a model
     *
     * @param model database model
     * @param <T>
     * @return ResultKey
     */
    public static <T extends Model> ResultKey save(T model) {
        return model.save();
    }

    /**
     * Batch save model
     *
     * @param models model list
     * @param <T>
     */
    public static <T extends Model> void saveBatch(List<T> models) {
        atomic(() -> {
            for (T model : models) {
                save(model);
            }
        }).catchException(e -> log.error("Batch save model error, message: {}", e));
    }

    /**
     * Batch delete model
     *
     * @param model model class type
     * @param ids   mode primary id array
     * @param <T>
     * @param <S>
     */
    @SafeVarargs
    public static <T extends Model, S extends Serializable> void deleteBatch(Class<T> model, S... ids) {
        AnimaQuery<T> animaQuery = new AnimaQuery<>(model);
        atomic(() -> Arrays.stream(ids).forEach(animaQuery::deleteById)).catchException(e -> log.error("Batch save model error, message: {}", e));
    }

    /**
     * Batch delete model with List
     *
     * @param model  model class type
     * @param idList mode primary id list
     * @param <T>
     * @param <S>
     */
    public static <T extends Model, S extends Serializable> void deleteBatch(Class<T> model, List<S> idList) {
        deleteBatch(model, AnimaUtils.toArray(idList));
    }

    /**
     * Delete model by id
     *
     * @param model model type class
     * @param id    model primary key
     * @param <T>
     * @return
     */
    public static <T extends Model> int deleteById(Class<T> model, Serializable id) {
        return new AnimaQuery<>(model).deleteById(id);
    }

    /**
     * Execute SQL statement
     *
     * @param sql    sql statement
     * @param params params
     * @return number of rows affected after execution
     */
    public static int execute(String sql, Object... params) {
        return new AnimaQuery<>().execute(sql, params);
    }


}