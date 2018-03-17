package org.sql2o.converters.java8;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Used by sql2o to convert a value from the database into a {@link LocalDateTime} instance.
 */
public class LocalDateTimeConverter implements Converter<LocalDateTime> {

    private final LocalDateTime localDateTime;

    // it's possible to create instance for other timezone
    // and re-register converter
    public LocalDateTimeConverter(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDateTimeConverter() {
        this(LocalDateTime.now());
    }

    public LocalDateTime convert(Object val) throws ConverterException {
        if (val == null) {
            return null;
        }
        try {
            Timestamp timestamp = (Timestamp) val;
            return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (IllegalArgumentException ex) {
            throw new ConverterException("Error while converting type " + val.getClass().toString() + " to jodatime", ex);
        }
    }

    public Object toDatabaseParam(LocalDateTime val) {
        return new java.sql.Date(val.atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli());
    }
}