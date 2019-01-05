package io.github.biezhi.anima;

import com.zaxxer.hikari.HikariDataSource;
import io.github.biezhi.anima.converter.LevelConverter;
import io.github.biezhi.anima.core.SQLParams;
import io.github.biezhi.anima.dialect.Dialect;
import io.github.biezhi.anima.exception.AnimaException;
import org.junit.Test;
import org.sql2o.Sql2o;
import org.sql2o.quirks.NoQuirks;

import static org.junit.Assert.*;

/**
 * AnimaTest
 *
 * @author biezhi
 * @date 2018-12-15
 */
public class AnimaTest {

//    @Test(expected = AnimaException.class)
//    public void testSql2oIsNull() {
//        Anima.of();
//    }

    @Test
    public void testCreateAnimaByNew() {
        Anima anima = new Anima(new Sql2o("jdbc:h2:file:~/demo;", "sa", ""));
        assertNotNull(anima);
        assertNotNull(Anima.of());

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192");
        ds.setUsername("sa");
        ds.setPassword("");

        anima = new Anima(ds);
        assertNotNull(anima);
        assertNotNull(Anima.of());

        anima = new Anima("jdbc:h2:file:~/demo;", "sa", "");
        assertNotNull(anima);
        assertNotNull(Anima.of());
    }

    @Test
    public void testCreateAnimaByOpen() {
        Anima anima = Anima.open(new Sql2o("jdbc:h2:file:~/demo;", "sa", ""));
        assertNotNull(anima);
        assertNotNull(Anima.of());

        anima = Anima.open("jdbc:sqlite:./test_create.db");
        assertNotNull(anima);
        assertNotNull(Anima.of());

        anima = Anima.open("jdbc:h2:file:~/demo;", "sa", "");
        assertNotNull(anima);
        assertNotNull(Anima.of());

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192");
        ds.setUsername("sa");
        ds.setPassword("");

        anima = Anima.open(ds);
        assertNotNull(anima);
        assertNotNull(Anima.of());
    }

    @Test
    public void testCreateAnimaWithQuirk() {
        Anima anima = Anima.open("jdbc:sqlite:./test_create.db", new NoQuirks());
        assertNotNull(anima);
        assertEquals(NoQuirks.class, anima.getSql2o().getQuirks().getClass());
        assertNotNull(Anima.of());

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192");
        ds.setUsername("sa");
        ds.setPassword("");

        anima = Anima.open(ds, new NoQuirks());
        assertNotNull(anima);
        assertEquals(NoQuirks.class, anima.getSql2o().getQuirks().getClass());
        assertNotNull(Anima.of());

        anima = Anima.open("jdbc:h2:file:~/demo;", "sa", "", new NoQuirks());
        assertNotNull(anima);
        assertEquals(NoQuirks.class, anima.getSql2o().getQuirks().getClass());
        assertNotNull(Anima.of());
    }

    @Test
    public void testChangeRollbackException() {
        Anima anima = createH2Anima().rollbackException(Exception.class);
        assertEquals(Exception.class, anima.rollbackException());
    }

    @Test
    public void testSetTablePrefix() {
        Anima anima = createH2Anima().tablePrefix("t_");
        assertEquals("t_", anima.tablePrefix());
    }

    static class MyDialect implements Dialect {
        @Override
        public String paginate(SQLParams sqlParams) {
            return null;
        }
    }

    @Test
    public void testSetDialect() {
        Anima anima = createH2Anima().dialect(new MyDialect());
        assertEquals(MyDialect.class, anima.dialect().getClass());
    }

    @Test
    public void testEnableSQLStatistic() {
        Anima anima = createH2Anima().enableSQLStatistic(false);
        assertFalse(anima.isEnableSQLStatistic());
    }

    @Test
    public void testUseSQLLimit() {
        Anima anima = createH2Anima().useSQLLimit(false);
        assertFalse(anima.isUseSQLLimit());
    }

    @Test(expected = AnimaException.class)
    public void testAddNullConverter(){
        Anima anima = createH2Anima();
        anima.addConverter(null);
    }

    @Test
    public void testAddConverter(){
        createH2Anima().addConverter(new LevelConverter());
    }

    private Anima createH2Anima(){
        return Anima.open("jdbc:h2:file:~/demo;", "sa", "");
    }

}
