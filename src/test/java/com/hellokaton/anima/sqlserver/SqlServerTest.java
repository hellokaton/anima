package com.hellokaton.anima.sqlserver;

import com.hellokaton.anima.Anima;
import com.hellokaton.anima.model.User;
import com.hellokaton.anima.oracle.OracleTest;
import com.hellokaton.anima.page.Page;
import com.hellokaton.anima.dialect.SqlServer2012Dialect;

import java.sql.Driver;
import java.sql.DriverManager;

import static com.hellokaton.anima.Anima.select;

/**
 * @author darren
 * @date 2018-06-13
 */
public class SqlServerTest {

//    @Before
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

//    @Test
    public void testPage(){
        Page<User> page = Anima.select().from(User.class).where(User::getAge).gte(3).page(2, 10);
        System.out.println(page.getRows());
    }

}
