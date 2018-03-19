package org.sql2o.issues;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.quirks.NoQuirks;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by lars on 05.10.2014.
 */
public class H2Tests {

    DataSource ds;

    String driverClassName;
    String url;
    String user;
    String pass;

    @Before
    public void setUp() throws Exception {
        driverClassName = "org.h2.Driver";
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        user = "sa";
        pass = "";
        JdbcDataSource datasource = new JdbcDataSource();
        datasource.setURL(url);
        datasource.setUser(user);
        datasource.setPassword(pass);

        ds = datasource;
    }

    @Test
    public void testIssue155() {

        Sql2o sql2o = new Sql2o(ds);

        assertThat(sql2o.getQuirks(), is(instanceOf(NoQuirks.class)));

        try (Connection connection = sql2o.open()) {
            int val = connection.createQuery("select 42").executeScalar(Integer.class);

            assertThat(val, is(equalTo(42)));
        }
    }

}
