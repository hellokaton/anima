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

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.page.Page;
import io.github.biezhi.anima.page.PageRow;

import java.util.List;
import java.util.Map;

/**
 * ResultList
 * <p>
 * Get a list of collections or single data
 *
 * @author biezhi
 * @date 2018/3/16
 */
public class ResultList<T> {


    private final Class<T> type;
    private final String   sql;
    private final Object[] params;

    public ResultList(Class<T> type, String sql, Object[] params) {
        this.type = type;
        this.sql = sql;
        this.params = params;
    }

    public T one() {
        return new AnimaQuery<>().useSQL().queryOne(type, sql, params);
    }

    public List<T> all() {
        return new AnimaQuery<>().useSQL().queryList(type, sql, params);
    }

    public List<Map<String, Object>> maps(){
        return new AnimaQuery<>().useSQL().queryListMap(sql, params);
    }

    public <S extends Model> Page<S> page(PageRow pageRow) {
        Class<S> modelType = (Class<S>) type;
        return new AnimaQuery<>(modelType).useSQL().page(sql, params, pageRow);
    }

    public <S extends Model> Page<S> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

}
