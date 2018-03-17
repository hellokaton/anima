package org.sql2o.converters.java8;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

public class LocalDateConverter implements Converter<LocalDate> {

    @Override
    public LocalDate convert(Object val) throws ConverterException {
        if (val == null) {
            return null;
        }
        try {
            return ((Timestamp) val).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (IllegalArgumentException ex) {
            throw new ConverterException("Don't know how to convert from type '" + val.getClass().getName() + "' to type '" + LocalDate.class.getName() + "'", ex);
        }
    }

    @Override
    public Object toDatabaseParam(LocalDate val) {
        return java.sql.Date.valueOf(val);
    }

}