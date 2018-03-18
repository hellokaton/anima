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

import java.util.List;

/**
 * ResultList
 * <p>
 * Get a list of collections or single data
 *
 * @author biezhi
 * @date 2018/3/16
 */
public class ResultList<T> {

    private List<T> list;

    public ResultList(List<T> list) {
        this.list = list;
    }

    public T one() {
        if (null == list || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public List<T> all() {
        return list;
    }
}
