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

import io.github.biezhi.anima.enhancer.ResultKey;
import io.github.biezhi.anima.exception.AnimaException;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

public abstract class ActiveRecord {

    private static final String DOG_GONE = "这似乎有些不对劲儿，先别慌，冷静分析...";

    protected static JavaRecord javaRecord = null;

    // SELECT COUNT(*) FROM users
    public static long count() {
        return javaRecord.count();
    }

    // SELECT * FROM users WHERE id = ?
    public static <T extends ActiveRecord, V extends Serializable> T findById(V id) {
        return javaRecord.findById(id);
    }

    // SELECT * FROM users WHERE (users.id IN (1,10))
    public static <T extends ActiveRecord, V extends Serializable> List<T> findById(V... ids) {
        return javaRecord.findByIds(ids);
    }

    public static <T extends ActiveRecord, V extends Serializable> void findThen(V id, Consumer<T> consumer) {
        consumer.accept(findById(id));
    }

    public static <T extends ActiveRecord> List<T> all() {
        return javaRecord.all();
    }

    public static <T extends ActiveRecord> void allEach(Consumer<T> consumer) {
        javaRecord.all().stream().map(item -> (T) item).forEach(item -> consumer.accept(item));
    }

    public static <T extends ActiveRecord> List<T> findBySQL(String sql, Object... params) {
        return javaRecord.findBySQL(sql, params);
    }

    public static int execute(String sql, Object... params) {
        return javaRecord.execute(sql, params);
    }

    public static JavaRecord like(String column, Object value) {
        return javaRecord.like(column, value);
    }

    public static JavaRecord where(String statement) {
        return javaRecord.where(statement);
    }

    public static JavaRecord where(String statement, Object value) {
        return javaRecord.where(statement, value);
    }

    public static JavaRecord in(String column, Object... paramValues) {
        return javaRecord.in(column, paramValues);
    }

    public ResultKey save() {
        throw new AnimaException(DOG_GONE);
    }

    public static JavaRecord set(String column, Object value) {
        return javaRecord.set(column, value);
    }

}