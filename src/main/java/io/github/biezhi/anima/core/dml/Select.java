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
package io.github.biezhi.anima.core.dml;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultList;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Select From
 *
 * @author biezhi
 * @date 2018/3/18
 */
@NoArgsConstructor
public class Select {

    private String columns;

    public Select(String columns) {
        this.columns = columns;
    }

    public <T extends Model> AnimaQuery<T> from(Class<T> modelClass) {
        return new AnimaQuery<>(modelClass).select(this.columns);
    }

    public <T> ResultList<T> bySQL(Class<T> type, String sql, Object... params) {
        return new ResultList<>(type, sql, params);
    }

    public <T extends Map<String, Object>> ResultList<T> bySQL(String sql, Object... params) {
        return new ResultList<>(null, sql, params);
    }

}
