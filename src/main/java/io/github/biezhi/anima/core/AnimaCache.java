package io.github.biezhi.anima.core;

import com.blade.reflectasm.MethodAccess;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Ignore;
import io.github.biezhi.anima.annotation.Table;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.utils.AnimaUtils;
import io.github.biezhi.anima.utils.English;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.biezhi.anima.utils.AnimaUtils.methodToFieldName;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Anima Cache
 *
 * @author biezhi
 * @date 2018/3/19
 */
public final class AnimaCache {

    static final Map<Class<?>, String>              CACHE_TABLE_NAME      = new HashMap<>(8);
    static final Map<Class<?>, String>              CACHE_PK_COLUMN_NAME  = new HashMap<>(8);
    static final Map<Class<?>, String>              CACHE_PK_FIELD_NAME   = new HashMap<>(8);
    static final Map<Class<?>, Map<String, String>> MODEL_COLUMN_MAPPINGS = new HashMap<>(8);
    static final Map<SerializedLambda, String>      CACHE_LAMBDA_NAME     = new HashMap<>(8);
    static final Map<SerializedLambda, String>      CACHE_FIELD_NAME      = new HashMap<>(8);

    public static final  Map<Class, MethodAccess> METHOD_ACCESS_MAP  = new HashMap<>();
    private static final Map<String, String>      GETTER_METHOD_NAME = new HashMap<>();
    private static final Map<String, String>      SETTER_METHOD_NAME = new HashMap<>();
    private static final Map<String, String>      FIELD_COLUMN_NAME  = new HashMap<>();

    private static final Map<Class, List<Field>> MODEL_AVAILABLE_FIELDS = new HashMap<>();

    /**
     * Get the column mapping based on the model Class type
     * <p>
     * Generated and stored in the Map when no column mapping exists
     *
     * @param modelType model class type
     * @return model column mapping
     */
    public static Map<String, String> computeModelColumnMappings(Class<?> modelType) {
        return MODEL_COLUMN_MAPPINGS.computeIfAbsent(modelType, model -> {
            List<Field> fields = computeModelFields(model);
            return fields.stream()
                    .collect(toMap(AnimaCache::getColumnName, Field::getName));
        });
    }

    public static List<Field> computeModelFields(Class clazz) {
        return MODEL_AVAILABLE_FIELDS.computeIfAbsent(clazz, model ->
                Stream.of(model.getDeclaredFields())
                        .filter(field -> !isIgnore(field))
                        .collect(toList()));
    }

    /**
     * User -> users
     * User -> t_users
     *
     * @param className
     * @param prefix
     * @return
     */
    public static String getTableName(String className, String prefix) {
        boolean hasPrefix = prefix != null && prefix.trim().length() > 0;
        return hasPrefix ? English.plural(prefix + "_" + AnimaUtils.toUnderline(className), 2) : English.plural(AnimaUtils.toUnderline(className), 2);
    }

    public static String getColumnName(Field field) {
        String fieldName = field.getName();
        String key       = field.getDeclaringClass().getSimpleName() + "_" + fieldName;

        return FIELD_COLUMN_NAME.computeIfAbsent(key, f -> {
            Column column = field.getAnnotation(Column.class);
            if (null != column) {
                return column.name();
            }
            return AnimaUtils.toUnderline(fieldName);
        });
    }

    public static String getGetterName(String fieldName) {
        return GETTER_METHOD_NAME.computeIfAbsent(fieldName, name -> "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    public static String getSetterName(String fieldName) {
        return SETTER_METHOD_NAME.computeIfAbsent(fieldName, name -> "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    public static String getTableName(Class<?> modelClass) {
        return CACHE_TABLE_NAME.computeIfAbsent(modelClass, type -> {
            Table table = type.getAnnotation(Table.class);
            if (null != table && AnimaUtils.isNotEmpty(table.name())) {
                return table.name();
            }
            return getTableName(type.getSimpleName(), Anima.me().getTablePrefix());
        });
    }

    public static String getPKColumn(Class<?> modelClass) {
        String pkColumn = CACHE_PK_COLUMN_NAME.get(modelClass);
        if (null != pkColumn) {
            return pkColumn;
        }
        Table table = modelClass.getAnnotation(Table.class);
        pkColumn = null != table ? table.pk() : "id";
        CACHE_PK_COLUMN_NAME.put(modelClass, pkColumn);
        return pkColumn;
    }

    public static String getPKField(Class<?> modelClass) {
        String pkField = CACHE_PK_FIELD_NAME.get(modelClass);
        if (null != pkField) {
            return pkField;
        }
        String pkColumn = AnimaCache.getPKColumn(modelClass);
        pkField = AnimaUtils.toCamelName(pkColumn);
        CACHE_PK_FIELD_NAME.put(modelClass, pkField);
        return pkField;
    }

    public static String getLambdaColumnName(SerializedLambda serializedLambda) {
        return CACHE_LAMBDA_NAME.computeIfAbsent(serializedLambda, lambda -> {
            String className  = serializedLambda.getImplClass().replace("/", ".");
            String methodName = serializedLambda.getImplMethodName();
            String fieldName  = methodToFieldName(methodName);
            try {
                Field field = Class.forName(className).getDeclaredField(fieldName);
                return getColumnName(field);
            } catch (NoSuchFieldException | ClassNotFoundException e) {
                throw new AnimaException(e);
            }
        });
    }

    public static String getLambdaFieldName(SerializedLambda serializedLambda) {
        String name = CACHE_FIELD_NAME.get(serializedLambda);
        if (null != name) {
            return name;
        }
        String methodName = serializedLambda.getImplMethodName();
        String fieldName  = methodToFieldName(methodName);
        CACHE_FIELD_NAME.put(serializedLambda, fieldName);
        return fieldName;
    }

    public static boolean isIgnore(Field field) {
        if ("serialVersionUID".equals(field.getName())) return true;
        if (null != field.getAnnotation(Ignore.class)) return true;
        return false;
    }

}
