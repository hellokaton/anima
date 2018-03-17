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

import io.github.biezhi.anima.core.AnimaDB;
import io.github.biezhi.anima.core.Atomic;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.enums.DMLType;
import io.github.biezhi.anima.exception.AnimaException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;
import org.sql2o.quirks.Quirks;
import org.sql2o.quirks.QuirksDetector;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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

    @Getter
    @Setter
    private Sql2o  sql2o;
    @Getter
    @Setter
    private String tablePrefix;

    private static Anima instance;

    public static Anima me() {
        if (null == instance.sql2o) {
            throw new AnimaException(SQL2O_IS_NULL);
        }
        return instance;
    }

    public Anima(Sql2o sql2o) {
        open(sql2o);
    }

    public Anima(DataSource dataSource){
        open(dataSource);
    }

    public Anima(String url, String user, String pass) {
        open(url, user, pass);
    }

    public static Anima open(Sql2o sql2o) {
        Anima anima = new Anima();
        anima.setSql2o(sql2o);
        instance = anima;
        return anima;
    }

    public static Anima open(String url) {
        return open(url, null, null);
    }

    public static Anima open(DataSource dataSource) {
        Sql2o sql2o = new Sql2o(dataSource);
        Anima anima = Anima.me();
        anima.sql2o = sql2o;
        return anima;
    }

    public static Anima open(String url, String user, String pass) {
        return open(url, user, pass, QuirksDetector.forURL(url));
    }

    public static Anima open(String url, String user, String pass, Quirks quirks) {
        Sql2o sql2o = new Sql2o(url, user, pass, quirks);
        return open(sql2o);
    }

    public static Atomic atomic(Runnable runnable) {
        try {
            AnimaDB.beginTransaction();
            runnable.run();
            AnimaDB.commit();
            return Atomic.ok();
        } catch (RuntimeException e) {
            AnimaDB.rollback();
            return Atomic.error(e);
        } finally {
            AnimaDB.endTransaction();
        }
    }

    public static AnimaDB select() {
        return new AnimaDB();
    }

    public static AnimaDB select(String coulmns) {
        return new AnimaDB().select(coulmns);
    }

    public static AnimaDB update() {
        return new AnimaDB(DMLType.UPDATE);
    }

    public static AnimaDB delete() {
        return new AnimaDB(DMLType.DELETE);
    }

    public static <T extends Model> ResultKey save(T model) {
        return new AnimaDB(model.getClass()).save(model);
    }

    public static <T extends Model> void saveBatch(List<T> models) {
        atomic(() -> {
            for (T model : models) {
                save(model);
            }
        }).catchException(e -> log.error("Batch save model error, message: {}", e));
    }

    public static <T extends Model, S extends Serializable> void deleteBatch(Class<T> modelClass, Serializable... ids) {
        AnimaDB animaDB = new AnimaDB(modelClass);
        atomic(() -> Arrays.stream(ids).forEach(animaDB::deleteById)).catchException(e -> log.error("Batch save model error, message: {}", e));
    }

    public static <T extends Model, S extends Serializable> void deleteBatch(Class<T> modelClass, List<S> idList) {
        AnimaDB animaDB = new AnimaDB(modelClass);
        atomic(() -> idList.stream().forEach(animaDB::deleteById)).catchException(e -> log.error("Batch save model error, message: {}", e));
    }

    public static <T extends Model> int deleteById(Class<T> modelClass, Serializable id) {
        return new AnimaDB(modelClass).deleteById(id);
    }

}