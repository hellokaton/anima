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
import io.github.biezhi.anima.annotation.EnumMapping;
import io.github.biezhi.anima.annotation.Ignore;
import io.github.biezhi.anima.core.AnimaCache;
import io.github.biezhi.anima.exception.AnimaException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnimaUtils {

    public static boolean isNotEmpty(String value) {
        return null != value && !value.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return null != collection && !collection.isEmpty();
    }

    public static boolean isEmpty(String value) {
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

    /**
     * eg: user_id -> userId
     */
    public static String toFieldName(String columnName) {
        String[]     partOfNames = columnName.split("_");
        StringBuffer sb          = new StringBuffer(partOfNames[0]);
        for (int i = 1; i < partOfNames.length; i++) {
            sb.append(partOfNames[i].substring(0, 1).toUpperCase());
            sb.append(partOfNames[i].substring(1));
        }
        return sb.toString();
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
                    if (value instanceof Enum) {
                        EnumMapping enumMapping = field.getAnnotation(EnumMapping.class);
                        if (null == enumMapping) {
                            columnValueList.add(value.toString());
                        } else {
                            if (enumMapping.value().equals(EnumMapping.TO_STRING)) {
                                columnValueList.add(value.toString());
                            }
                            if (enumMapping.value().equals(EnumMapping.ORDINAL)) {
                                columnValueList.add(((Enum) value).ordinal());
                            }
                        }
                    } else {
                        columnValueList.add(value);
                    }
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

    public static Object getFieldValue(Field field, Object target) {
        try {
            field.setAccessible(true);
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

    public static <S extends Model> Object getAndRemovePrimaryKey(S model) {
        try {
            String fieldName = AnimaCache.getPKField(model.getClass());
            Field  field     = model.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(model);
            if (null != value) {
                field.set(model, null);
            }
            return value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return null;
    }

}
