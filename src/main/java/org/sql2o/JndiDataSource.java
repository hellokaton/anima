package org.sql2o;

import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by lars on 16.09.2014.
 */
@Slf4j
public class JndiDataSource {

    static DataSource getJndiDatasource(String jndiLookup) {
        Context ctx = null;
        DataSource datasource = null;

        try {
            ctx = new InitialContext();
            datasource = (DataSource) ctx.lookup(jndiLookup);
        }
        catch (NamingException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (Throwable e) {
                    log.warn("error closing context", e);
                }
            }
        }

        return datasource;
    }
}
