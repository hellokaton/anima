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
import io.github.biezhi.anima.annotation.EnumMapping;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.biezhi.anima.core.AnimaCache.METHOD_ACCESS_MAP;
import static io.github.biezhi.anima.core.AnimaCache.isIgnore;

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
        String[]     partOfNames = value.split("_");
        StringBuffer sb          = new StringBuffer(partOfNames[0]);
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
        for (Field field : AnimaCache.computeModelFields(model.getClass())) {
            try {
                Object value = invokeMethod(model, AnimaCache.getGetterName(field.getName()), EMPTY_ARG);
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
            } catch (IllegalArgumentException e) {
                throw new AnimaException("illegal argument or Access:", e);
            }
        }
        return columnValueList;
    }

    public static <T extends Model> String buildColumns(List<String> excludedColumns, Class<T> modelClass) {
        StringBuilder sql = new StringBuilder();
        for (Field field : AnimaCache.computeModelFields(modelClass)) {
            String columnName = AnimaCache.getColumnName(field);
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

    /**
     * Return the primary key of the model and set it to null
     *
     * @param model db model
     * @param <S>   model generic type
     * @return primary key value
     */
    public static <S extends Model> Object getAndRemovePrimaryKey(S model) {
        String fieldName = AnimaCache.getPKField(model.getClass());
        Object value     = invokeMethod(model, AnimaCache.getGetterName(fieldName), EMPTY_ARG);
        if (null != value) {
            invokeMethod(model, AnimaCache.getSetterName(fieldName), NULL_ARG);
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
        T[] toR = (T[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size());
        for (int i = 0; i < list.size(); i++) {
            toR[i] = list.get(i);
        }
        return toR;
    }

}
