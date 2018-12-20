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

import com.blade.reflectasm.MethodAccess;
import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.core.AnimaCache;
import io.github.biezhi.anima.exception.AnimaException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sql2o.converters.Converter;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static io.github.biezhi.anima.core.AnimaCache.*;

/**
 * Utility class for composing SQL statements
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnimaUtils {

    public static final Object[] EMPTY_ARG = new Object[]{};
    public static final Object[] NULL_ARG  = new Object[]{null};

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
     * eg: user_id -> userId
     *
     * @param value
     * @return
     */
    public static String toCamelName(String value) {
        String[] partOfNames = value.split("_");

        StringBuilder sb = new StringBuilder(partOfNames[0]);
        for (int i = 1; i < partOfNames.length; i++) {
            sb.append(partOfNames[i].substring(0, 1).toUpperCase());
            sb.append(partOfNames[i].substring(1));
        }
        return sb.toString();
    }

    /**
     * eg: userId -> user_id
     */
    public static String toUnderline(String value) {
        StringBuilder result = new StringBuilder();
        if (value != null && value.length() > 0) {
            result.append(value.substring(0, 1).toLowerCase());
            for (int i = 1; i < value.length(); i++) {
                String s = value.substring(i, i + 1);
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
        for (Field field : computeModelFields(model.getClass())) {
            try {
                Object value = invokeMethod(model, getGetterName(field.getName()), EMPTY_ARG);
                if (null == value) {
                    if (allowNull) {
                        columnValueList.add(null);
                    }
                    continue;
                }
                columnValueList.add(value);
            } catch (IllegalArgumentException e) {
                throw new AnimaException("illegal argument or Access:", e);
            }
        }
        return columnValueList;
    }

    public static <T extends Model> String buildColumns(List<String> excludedColumns, Class<T> modelClass) {
        StringBuilder sql = new StringBuilder();
        for (Field field : computeModelFields(modelClass)) {
            String columnName = getColumnName(field);
            if (!isIgnore(field) && !excludedColumns.contains(columnName)) {
                sql.append(columnName).append(',');
            }
        }
        if (sql.length() > 0) {
            return sql.substring(0, sql.length() - 1);
        }
        return "*";
    }

    public static Object invokeMethod(Object target, String methodName, Object[] args) {
        MethodAccess methodAccess = METHOD_ACCESS_MAP.computeIfAbsent(target.getClass(), type -> {
            List<Method> methods = Arrays.asList(type.getDeclaredMethods());
            return MethodAccess.get(type, methods);
        });
        return methodAccess.invokeWithCache(target, methodName, args);
    }

    public static String getLambdaColumnName(Serializable lambda) {
        SerializedLambda serializedLambda = computeSerializedLambda(lambda);
        return AnimaCache.getLambdaColumnName(serializedLambda);
    }

    public static String getLambdaFieldName(Serializable lambda) {
        SerializedLambda serializedLambda = computeSerializedLambda(lambda);
        return AnimaCache.getLambdaFieldName(serializedLambda);
    }

    private static SerializedLambda computeSerializedLambda(Serializable lambda) {
        for (Class<?> cl = lambda.getClass(); cl != null; cl = cl.getSuperclass()) {
            try {
                Method m = cl.getDeclaredMethod("writeReplace");
                m.setAccessible(true);
                Object replacement = m.invoke(lambda);
                if (!(replacement instanceof SerializedLambda)) {
                    break; // custom interface implementation
                }
                return (SerializedLambda) replacement;
            } catch (Exception e) {
                throw new AnimaException("get lambda column name fail", e);
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

    /**
     * Return the primary key of the model and set it to null
     *
     * @param model db model
     * @param <S>   model generic type
     * @return primary key value
     */
    public static <S extends Model> Object getAndRemovePrimaryKey(S model) {
        String fieldName = getPKField(model.getClass());
        Object value     = invokeMethod(model, getGetterName(fieldName), EMPTY_ARG);
        if (null != value) {
            invokeMethod(model, getSetterName(fieldName), NULL_ARG);
        }
        return value;
    }

    /**
     * Convert a List to a generic array
     *
     * @param list list collection
     * @param <T>  generic
     * @return array
     */
    public static <T> T[] toArray(List<T> list) {
        T[] toR = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
        for (int i = 0; i < list.size(); i++) {
            toR[i] = list.get(i);
        }
        return toR;
    }

    public static boolean isBasicType(Class<?> type) {
        return type.equals(char.class) ||
                type.equals(Character.class) ||
                type.equals(boolean.class) ||
                type.equals(Boolean.class) ||
                type.equals(byte.class) ||
                type.equals(Byte.class) ||
                type.equals(short.class) ||
                type.equals(Short.class) ||
                type.equals(int.class) ||
                type.equals(Integer.class) ||
                type.equals(long.class) ||
                type.equals(Long.class) ||
                type.equals(BigDecimal.class) ||
                type.equals(BigInteger.class) ||
                type.equals(Date.class) ||
                type.equals(String.class) ||
                type.equals(double.class) ||
                type.equals(Double.class) ||
                type.equals(float.class) ||
                type.equals(Float.class);
    }

    public static Class getConverterType(Converter<?> converter) {
        Type[] types  = converter.getClass().getGenericInterfaces();
        Type[] params = ((ParameterizedType) types[0]).getActualTypeArguments();
        return (Class) params[0];
    }

}
