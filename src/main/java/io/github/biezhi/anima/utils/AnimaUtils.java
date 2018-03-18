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
package io.github.biezhi.anima.utils;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Ignore;
import io.github.biezhi.anima.enums.SupportedType;
import io.github.biezhi.anima.exception.AnimaException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for composing SQL statements
 */
public class AnimaUtils {

    private AnimaUtils() {
    }

    public static boolean isNotEmpty(String value) {
        return null != value && !value.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return null != collection && !collection.isEmpty();
    }

    /**
     * User -> user | prefix_user
     */
    public static String toTableName(String className, String prefix) {
        boolean hasPrefix = prefix != null && prefix.trim().length() > 0;
        return hasPrefix ? English.plural(prefix + "_" + toColumnName(className), 2) : English.plural(toColumnName(className), 2);
    }

    /**
     * eg: userId -> user_id
     */
    public static String toColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (null != column) {
            return column.name();
        }
        return toColumnName(field.getName());
    }

    public static String toColumnName(String propertyName) {
        StringBuilder result = new StringBuilder();
        if (propertyName != null && propertyName.length() > 0) {
            result.append(propertyName.substring(0, 1).toLowerCase());
            for (int i = 1; i < propertyName.length(); i++) {
                String s = propertyName.substring(i, i + 1);
                if (s.equals(s.toUpperCase())) {
                    result.append("_");
                    result.append(s.toLowerCase());
                } else {
                    result.append(s);
                }
            }
        }
        return result.toString();
    }

    public static <T extends Model> List<Object> columnValues(T model, boolean allowNull) {
        List<Object> columnValueList = new ArrayList<>();
        for (Field field : model.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(model);
                if (null != value) {
                    columnValueList.add(value);
                } else if (allowNull) {
                    columnValueList.add(value);
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AnimaException("illegal argument or Access:", e);
            }
        }
        return columnValueList;
    }

    public static <T extends Model> String buildColumns(List<String> excludedColumns, Class<T> modelClass) {
        StringBuilder sql            = new StringBuilder();
        Field[]       declaredFields = modelClass.getDeclaredFields();
        for (Field field : declaredFields) {
            String columnName = toColumnName(field.getName());
            if (!excludedColumns.contains(columnName)) {
                sql.append(columnName).append(',');
            }
        }
        if (sql.length() > 0) {
            return sql.substring(0, sql.length() - 1);
        }
        return "*";
    }

    public static boolean isIgnore(Field field) {
        if ("serialVersionUID".equals(field.getName())) {
            return true;
        }
        if (null != field.getAnnotation(Ignore.class)) {
            return true;
        }
        String typeName = field.getType().getSimpleName().toLowerCase();
        if (!SupportedType.contains(typeName)) {
            return true;
        }
        return false;
    }

}
