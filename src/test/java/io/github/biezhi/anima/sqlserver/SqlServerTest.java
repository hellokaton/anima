package io.github.biezhi.anima.sqlserver;

import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.dialect.SqlServer2012Dialect;
import io.github.biezhi.anima.dialect.SqlServerDialect;
import io.github.biezhi.anima.model.User;
import io.github.biezhi.anima.oracle.OracleTest;
import io.github.biezhi.anima.page.Page;
import org.junit.Before;
import org.junit.Test;

import java.sql.Driver;
import java.sql.DriverManager;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author darren
 * @date 2018-06-13
 */
public class SqlServerTest {

    @Before
    public void before() {
        try {
            Class oracleDriverClass = OracleTest.class.getClassLoader().loadClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            DriverManager.registerDriver((Driver) oracleDriverClass.newInstance());
        } catch (Throwable t) {
            t.printStackTrace();
        }
//        Anima.open("jdbc:sqlserver://192.168.123.114:1433;databaseName=dev", "sa", "Abcd1234").dialect(new SqlServerDialect());
        Anima.open("jdbc:sqlserver://192.168.123.114:1433;databaseName=dev", "sa", "Abcd1234").dialect(new SqlServer2012Dialect());
    }

    @Test
    public void testPage(){
        Page<User> page = select().from(User.class).where(User::getAge).gte(3).page(2, 10);
        System.out.println(page.getRows());
    }

}
