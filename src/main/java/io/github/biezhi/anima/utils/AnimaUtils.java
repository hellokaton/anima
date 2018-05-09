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
import io.github.biezhi.anima.annotation.*;
import io.github.biezhi.anima.core.AnimaCache;
import io.github.biezhi.anima.exception.AnimaException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public static boolean isNotEmpty(Collection<?> collection) {
        return null != collection && !collection.isEmpty();
    }

    public static boolean isEmpty(String value){
        return null == value || value.isEmpty();
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

    public static <T extends Model> List<Object> toColumnValues(T model, boolean allowNull) {
        List<Object> columnValueList = new ArrayList<>();
        for (Field field : model.getClass().getDeclaredFields()) {
            if (isIgnore(field)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(model);
                if (null != value) {
                    columnValueList.add(value);
                } else if (allowNull) {
                    columnValueList.add(null);
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
            if (!isIgnore(field) && !excludedColumns.contains(columnName)) {
                sql.append(columnName).append(',');
            }
        }
        if (sql.length() > 0) {
            return sql.substring(0, sql.length() - 1);
        }
        return "*";
    }

    public static boolean isIgnore(Field field) {
        if ("serialVersionUID".equals(field.getName())) return true;
        if (null != field.getAnnotation(Ignore.class)) return true;
        return false;
    }

    public static Object getPKFieldValue(Object target) {
        try {
            String  pkName = AnimaCache.getPKColumn(target.getClass());
            Field[] fields = target.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(pkName)) {
                    field.setAccessible(true);
                    return field.get(target);
                }
                Column column = field.getAnnotation(Column.class);
                if (null != column && pkName.equals(column.name())) {
                    field.setAccessible(true);
                    return field.get(target);
                }
                if (pkName.equals(toColumnName(field.getName()))) {
                    field.setAccessible(true);
                    return field.get(target);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldValue(String fieldName, Object target) {
        try {
            Field field = AnimaCache.getField(target.getClass(), fieldName);
            return field.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldValue(String fieldName, Object target, Object value) {
        try {
            Field field = AnimaCache.getField(target.getClass(), fieldName);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String getLambdaColumnName(Serializable lambda) {
        for (Class<?> cl = lambda.getClass(); cl != null; cl = cl.getSuperclass()) {
            try {
                Method m = cl.getDeclaredMethod("writeReplace");
                m.setAccessible(true);
                Object replacement = m.invoke(lambda);
                if (!(replacement instanceof SerializedLambda)) {
                    break; // custom interface implementation
                }
                SerializedLambda serializedLambda = (SerializedLambda) replacement;
                return AnimaCache.getLambdaColumnName(serializedLambda);
            } catch (NoSuchMethodException e) {
                // do nothing
            } catch (IllegalAccessException | InvocationTargetException e) {
                break;
            }
        }
        return null;
    }

    public static String getLambdaFieldName(Serializable lambda) {
        for (Class<?> cl = lambda.getClass(); cl != null; cl = cl.getSuperclass()) {
            try {
                Method m = cl.getDeclaredMethod("writeReplace");
                m.setAccessible(true);
                Object replacement = m.invoke(lambda);
                if (!(replacement instanceof SerializedLambda)) {
                    break; // custom interface implementation
                }
                SerializedLambda serializedLambda = (SerializedLambda) replacement;
                return AnimaCache.getLambdaFieldName(serializedLambda);
            } catch (NoSuchMethodException e) {
                // do nothing
            } catch (IllegalAccessException | InvocationTargetException e) {
                break;
            }
        }
        return null;
    }

    public static String methodToFieldName(String methodName) {
        return capitalize(methodName.replace("get", ""));
    }

    public static String capitalize(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1, input.length());
    }

}
