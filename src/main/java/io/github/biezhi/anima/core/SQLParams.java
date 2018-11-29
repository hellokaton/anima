/**
 * Copyright (c) 2018, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.page.PageRow;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SQLParams
 * <p>
 * This class is used to store the parameters of the generated SQL.
 *
 * @author biezhi
 * @date 2018/3/17
 */
@Data
@Builder
public class SQLParams {

    private Class<? extends Model> modelClass;
    private Object                 model;
    private String                 selectColumns;
    private String                 tableName;
    private String                 pkName;
    private StringBuilder          conditionSQL;
    private List<Object>           columnValues;
    private Map<String, Object>    updateColumns;
    private List<String>           excludedColumns;
    private PageRow                pageRow;
    private String                 orderBy;
    private boolean                isSQLLimit;

    private String customSQL;

}
