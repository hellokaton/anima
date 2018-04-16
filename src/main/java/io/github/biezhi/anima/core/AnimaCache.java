package io.github.biezhi.anima.core;

import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.annotation.Table;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.utils.AnimaUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static io.github.biezhi.anima.utils.AnimaUtils.methodToFieldName;

/**
 * Anima Cache
 * 
 * @author biezhi
 * @date 2018/3/19
 */
public final class AnimaCache {

    static final Map<Class<?>, Boolean> CACHE_HAS_BELONGS_TO = new HashMap<>(8);
    static final Map<Class<?>, Boolean> CACHE_HAS_ONE = new HashMap<>(8);
    static final Map<Class<?>, Boolean> CACHE_HAS_MANY = new HashMap<>(8);
    static final Map<Class<?>, String> CACHE_TABLE_NAME = new HashMap<>(8);
    static final Map<Class<?>, String> CACHE_PK_NAME = new HashMap<>(8);
    static final Map<SerializedLambda, String> CACHE_LAMBDA_NAME = new HashMap<>(8);
    static final Map<String, Field> CACHE_MODEL_FIELD = new HashMap<>(8);


    public static String getTableName(Class<?> modelClass) {
        String tableName = CACHE_TABLE_NAME.get(modelClass);
        if (null != tableName) {
            return tableName;
        }
        Table table = modelClass.getAnnotation(Table.class);
        if (null != table && AnimaUtils.isNotEmpty(table.name())) {
            tableName = table.name();
            CACHE_TABLE_NAME.put(modelClass, tableName);
            return tableName;
        }
        tableName = AnimaUtils.toTableName(modelClass.getSimpleName(), Anima.me().getTablePrefix());
        CACHE_TABLE_NAME.put(modelClass, tableName);
        return tableName;
    }

    public static String getPKColumn(Class<?> modelClass) {
        String pkColumn = CACHE_PK_NAME.get(modelClass);
        if (null != pkColumn) {
            return pkColumn;
        }
        Table table = modelClass.getAnnotation(Table.class);
        pkColumn = null != table ? table.pk() : "id";
        CACHE_PK_NAME.put(modelClass, pkColumn);
        return pkColumn;
    }

    public static String getLambdaColumnName(SerializedLambda serializedLambda) {
        String name = CACHE_LAMBDA_NAME.get(serializedLambda);
        if (null != name) {
            return name;
        }
        String className = serializedLambda.getImplClass().replace("/", ".");
        String methodName = serializedLambda.getImplMethodName();
        String fieldName = methodToFieldName(methodName);
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            name = AnimaUtils.toColumnName(field);
            CACHE_LAMBDA_NAME.put(serializedLambda, name);
            return name;
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new AnimaException(e);
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + ":" + fieldName;
        Field field = CACHE_MODEL_FIELD.get(key);
        if (null != field) {
            return field;
        }
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            CACHE_MODEL_FIELD.put(key, field);
            return field;
        } catch (Exception e) {
            throw new AnimaException(e);
        }
    }
}
