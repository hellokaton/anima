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

import io.github.biezhi.anima.core.Atomic;
import io.github.biezhi.anima.core.JavaRecord;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.enums.DMLType;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Sql2o;
import org.sql2o.quirks.Quirks;
import org.sql2o.quirks.QuirksDetector;

import javax.sql.DataSource;
import java.util.List;

/**
 * Anima
 *
 * @author biezhi
 * @date 2018/3/13
 */
@Slf4j
public class Anima {

    private Sql2o sql2o;
    private String tablePrefix = "";

    private Anima() {
    }

    public static Anima me() {
        return AnimaHolder.INSTANCE;
    }

    public static JavaRecord select() {
        return new JavaRecord();
    }

    public static JavaRecord select(String coulmns) {
        return new JavaRecord().select(coulmns);
    }

    public static JavaRecord update() {
        return new JavaRecord(DMLType.UPDATE);
    }

    public static JavaRecord delete() {
        return new JavaRecord(DMLType.DELETE);
    }

    public static <T extends Model> ResultKey save(T model) {
        return new JavaRecord(model.getClass()).save(model);
    }

    public static <T extends Model> void saveBatch(List<T> models) {
        atomic(() -> {
            for (T model : models) {
                save(model);
            }
        }).catchException(e -> log.error("Batch save model error, message: {}", e));
    }

    private static final class AnimaHolder {
        private static final Anima INSTANCE = new Anima();
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
        Anima anima = Anima.me();
        anima.sql2o = sql2o;
        return anima;
    }

    public static Atomic atomic(Runnable runnable) {
        try {
            JavaRecord.beginTransaction();
            runnable.run();
            JavaRecord.commit();
            return Atomic.ok();
        } catch (RuntimeException e) {
            JavaRecord.rollback();
            return Atomic.error(e);
        } finally {
            JavaRecord.endTransaction();
        }
    }

    public String tablePrefix() {
        return this.tablePrefix;
    }

    public Anima tablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
    }

    public Sql2o getCommonSql2o() {
        return sql2o;
    }

}