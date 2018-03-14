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

import io.github.biezhi.anima.exception.AnimaException;

import java.io.Serializable;
import java.util.List;

public abstract class ActiveRecord {

    private static final String DOG_GONE = "这似乎有些不对劲儿，先别慌，冷静分析...";

    // SELECT COUNT(*) FROM users
    public static long count() {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users WHERE id = ?
    public static <T extends ActiveRecord> T findById(Serializable id){
        throw new AnimaException(DOG_GONE);
    }

    public static <T extends ActiveRecord> List<T> all(){
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users ORDER BY users.id ASC LIMIT 1
    public static <T extends ActiveRecord> T first() {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users ORDER BY users.id ASC LIMIT 3
    public static <T extends ActiveRecord> T first(int limit) {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users ORDER BY users.id DESC LIMIT 1
    public static <T extends ActiveRecord> T last() {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users ORDER BY users.id DESC LIMIT 3
    public static <T extends ActiveRecord> T last(int limit) {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users LIMIT 1
    public static <T extends ActiveRecord> T take() {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users LIMIT 2
    public static <T extends ActiveRecord> T take(int limit) {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users WHERE (users.id = 10) LIMIT 1
    public static <T extends ActiveRecord> T find(Serializable id) {
        throw new AnimaException(DOG_GONE);
    }

    // SELECT * FROM users WHERE (users.id IN (1,10))
    public static <T extends ActiveRecord> T find(Serializable... ids) {
        throw new AnimaException(DOG_GONE);
    }

    public static <T> T takeBySQL(String sql, Object... params) {
        throw new AnimaException(DOG_GONE);
    }

    public static <T> List<T> findBySQL(String sql, Object... params) {
        throw new AnimaException(DOG_GONE);
    }

    public static int execute(String sql){
        throw new AnimaException(DOG_GONE);
    }

    public static JavaRecord where(String statement) {
        throw new AnimaException(DOG_GONE);
    }

    public static JavaRecord where(String statement, Object value) {
        throw new AnimaException(DOG_GONE);
    }

    public static JavaRecord in(String column, Object... paramValues) {
        throw new AnimaException(DOG_GONE);
    }

    public <T extends Serializable> T save() {
        throw new AnimaException(DOG_GONE);
    }

}