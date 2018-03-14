package org.sql2o.quirks;

import java.util.Map;

import org.sql2o.converters.Converter;
import org.sql2o.quirks.NoQuirks;

public class SqlDroidQuirks extends NoQuirks {

    public SqlDroidQuirks() {
        super();
    }
    
    public SqlDroidQuirks(Map<Class, Converter> converters) {
        super(converters);
    }
    
    @Override
    public boolean returnGeneratedKeysByDefault() {
        return false;
    }
}