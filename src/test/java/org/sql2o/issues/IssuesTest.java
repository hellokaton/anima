package org.sql2o.issues;

import io.github.biezhi.anima.Anima;
import org.hsqldb.jdbcDriver;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import org.sql2o.data.Table;
import org.sql2o.issues.pojos.Issue1Pojo;

import lombok.Data;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: lars
 * Date: 10/17/11
 * Time: 9:02 PM
 * This class is to test for reported issues.
 */
@RunWith(Parameterized.class)
public class IssuesTest {

    @Parameterized.Parameters(name = "{index} - {4}")
    public static Collection<Object[]> getData(){
        return Arrays.asList(new Object[][]{
                {null, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1","sa", "", "H2 test" },
                {new jdbcDriver(), "jdbc:hsqldb:mem:testmemdb", "SA", "", "HyperSQL DB test"}
        });
    }

    private Sql2o sql2o;
    private String url;
    private String user;
    private String pass;

    public IssuesTest(Driver driverToRegister, String url, String user, String pass, String testName){
        if (driverToRegister != null) {
            try {
                DriverManager.registerDriver(driverToRegister);
            } catch (SQLException e) {
                throw new RuntimeException("could not register driver '" + driverToRegister.getClass().getName() + "'", e);
            }
        }

        this.sql2o = new Sql2o(url, user, pass);

        Anima.open(sql2o);

        this.url = url;
        this.user = user;
        this.pass = pass;

        if ("HyperSQL DB test".equals( testName )) {
            sql2o.open().createQuery("set database sql syntax MSS true").executeUpdate();
        }
    }

    /**
     * Tests for issue #1 https://github.com/aaberg/sql2o/issues/1
     *
     * Issue:
     * I have a case where I need to override/modify the value loaded from db.
     * I want to do this in a setter but the current version of sql2o modifies the property directly.
     *
     * Comment:
     * The priority was wrong. Sql2o would try to set the field first, and afterwards the setter. The priority should be
     * the setter first and the field after.
     */
    @Test public void testSetterPriority(){
        Sql2o sql2o = new Sql2o(url, user, pass);
        Issue1Pojo pojo = sql2o.open().createQuery("select 1 val from (values(0))").executeAndFetchFirst(Issue1Pojo.class);

        assertEquals(2, pojo.val);

    }

    public static class Issue5POJO{
        public int id;
        public int val;
    }
    
    public static class Issue5POJO2{
        public int id;
        public int val;

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }
    }

    public static enum WhatEverEnum{
        VAL, ANOTHER_VAL;
    }

    /**
     * Test for issue #132 ( https://github.com/aaberg/sql2o/issues/132 )
     * Ref change done in pull request #75
     * Also see comment on google groups
     * https://groups.google.com/forum/#!topic/sql2o/3H4XJIv-i04

     * If a column cannot be mapped to a property, an exception should be thrown. Today it is silently ignored.
     */
    @Test public void testErrorWhenFieldDoesntExist() {

        @Data
        class LocalPojo {
            private long id;
            private String strVal;
        }

        String createQuery = "create table testErrorWhenFieldDoesntExist(id_val integer primary key, str_val varchar(100))";

        try (Connection connection = sql2o.open()) {
            connection.createQuery(createQuery).executeUpdate();

            String insertSql = "insert into testErrorWhenFieldDoesntExist(id_val, str_val) values (?, ?)";
            connection.createQuery(insertSql).withParams(1, "test").executeUpdate();

            Exception ex = null;
            try {
                // This is expected to fail to map columns and throw an exception.
                LocalPojo p = connection.createQuery("select * from testErrorWhenFieldDoesntExist")
                        .executeAndFetchFirst(LocalPojo.class);

                Assert.assertNotNull(p);
            } catch(Exception e) {
                ex = e;
            }
            assertNotNull(ex);

        }
    }

    public static class Issue9Pojo {
        public int id;
        public String theVal;
    }

    /**
     * Test for issue #148 (https://github.com/aaberg/sql2o/issues/148)
     * ## IndexOutOfRange exception
     * When a resultset has multiple columns with the same name, sql2o 1.5.1 will throw an IndexOutOfRange exception when calling executeAndFetchTable() method.
     */
    @Test
    public void testIndexOutOfRangeExceptionWithMultipleColumnsWithSameName() {

        @Data
        class ThePojo {
            public int id;
            public String name;
        }

        String sql = "select 11 id, 'something' name, 'something else' name from (values(0))";

        ThePojo p;
        Table t;
        try (Connection connection = sql2o.open()) {
            p = connection.createQuery(sql).executeAndFetchFirst(ThePojo.class);
            t = connection.createQuery(sql).executeAndFetchTable();
        }

        assertEquals(11, p.id);
        assertEquals("something else", p.name);

        assertEquals(11, (int)t.rows().get(0).getInteger("id"));
        assertEquals("something else", t.rows().get(0).getString("name"));
    }

    /**
     * Reproduce issue #142 (https://github.com/aaberg/sql2o/issues/142)
     */
    @Test
    public void testIgnoreSqlComments() {

        @Data
        class ThePojo {
            public int id;
            public int intval;
            public String strval;
        }

        String createSql = "create table testIgnoreSqlComments(id integer primary key, intval integer, strval varchar(100))";

        String insertQuery =
                "insert into testIgnoreSqlComments (id, intval, strval)\n " +
                "-- It's a comment!\n" +
                "values (?, ?, ?);";

        String fetchQuery =
                "select id, intval, strval\n" +
                "-- a 'comment'\n" +
                "from testIgnoreSqlComments\n" +
                "/* and, it's another type of comment!*/" +
                "where intval = ?";

        try (Connection connection = sql2o.open()) {
            connection.createQuery(createSql).executeUpdate();

            for (int idx = 0; idx < 100; idx++) {
                int intval = idx % 10;
                connection.createQuery(insertQuery)
                        .withParams(idx, intval, "teststring" + idx)
                        .executeUpdate();
            }

            List<ThePojo> resultList = connection.createQuery(fetchQuery)
                    .withParams(5).executeAndFetch(ThePojo.class);

            assertEquals(10, resultList.size());
        }
    }

    /**
     * Testing for github issue #134.
     * Add option to ignore mapping errors
     */
    @Test
    public void testIssue134ThrowOnMappingErrorProperty() {
        String sql = "select 1 id, 'foo' val1, 'bar' val2 from (values(0))";

        class Pojo{
            public int id;
            public String val1;
        }

        try (Connection connection = sql2o.open()) {

            try {
                Pojo pojo = connection.createQuery(sql).executeAndFetchFirst(Pojo.class);
                Assert.assertNotNull(pojo);
                fail("Expeced an exception to be thrown");
            } catch(Sql2oException e) {
                assertEquals("Could not map VAL2 to any property.", e.getMessage());
            }

            Pojo pojo = connection.createQuery(sql).throwOnMappingFailure(false).executeAndFetchFirst(Pojo.class);

            assertEquals(1, pojo.id);
            assertEquals("foo", pojo.val1);
        }
    }

}
