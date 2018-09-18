package org.sql2o.converters;

import io.github.biezhi.anima.annotation.EnumMapping;
import io.github.biezhi.anima.exception.AnimaException;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link EnumConverterFactory},
 * used by sql2o to convert a value from the database into an {@link Enum}.
 */
public class DefaultEnumConverterFactory implements EnumConverterFactory {

    private static Map<EnumMapping, Field> fieldMap = new HashMap<>();

    private static <E extends Enum> E getEnum(Object target, Class<E> enumType) {
        EnumMapping enumMapping = enumType.getAnnotation(EnumMapping.class);

        if (!fieldMap.containsKey(enumMapping)) {
            try {
                Field field = enumType.getDeclaredField(enumMapping.value());
                fieldMap.put(enumMapping, field);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Field field = fieldMap.get(enumMapping);
        field.setAccessible(true);
        return (E) EnumSet.allOf(enumType).stream()
                .filter(e -> filterEnumValue(target, field, e))
                .findFirst().get();
    }

    private static boolean filterEnumValue(Object target, Field field, Object e) {
        try {
            Object o = field.get(e);
            return target.toString().equals(o.toString());
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public <E extends Enum> Converter<E> newConverter(final Class<E> enumType) {
        return new Converter<E>() {
            @SuppressWarnings("unchecked")
            public E convert(Object val) throws ConverterException {
                if (val == null) {
                    return null;
                }
                try {
                    EnumMapping enumMapping = enumType.getAnnotation(EnumMapping.class);
                    if (null != enumMapping) {
                        return getEnum(val, enumType);
                    }
                    if (val instanceof String) {
                        return (E) Enum.valueOf(enumType, val.toString());
                    } else if (val instanceof Number) {
                        return enumType.getEnumConstants()[((Number) val).intValue()];
                    }
                } catch (Throwable t) {
                    throw new ConverterException("Error converting value '" + val.toString() + "' to " + enumType.getName(), t);
                }
                throw new ConverterException("Cannot convert type '" + val.getClass().getName() + "' to an Enum");
            }

            public Object toDatabaseParam(Enum val) {
                EnumMapping enumMapping = val.getClass().getAnnotation(EnumMapping.class);
                if (null != enumMapping) {
                    try {
                        Field field = val.getClass().getDeclaredField(enumMapping.value());
                        field.setAccessible(true);
                        return field.get(val);
                    } catch (Exception e) {
                        throw new AnimaException(e);
                    }
                }
                return val.name();
            }
        };
    }
}
