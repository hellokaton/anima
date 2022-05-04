package com.hellokaton.anima.oracle;

import com.hellokaton.anima.Anima;
import com.hellokaton.anima.model.ZhuanJia;
import com.hellokaton.anima.page.Page;
import com.hellokaton.anima.dialect.OracleDialect;
import org.junit.Assert;

import java.sql.Driver;
import java.sql.DriverManager;

import static com.hellokaton.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/13
 */
public class OracleTest {

//    @Before
    public void before() {
        try {
            Class oracleDriverClass = OracleTest.class.getClassLoader().loadClass("oracle.jdbc.driver.OracleDriver");
            DriverManager.registerDriver((Driver) oracleDriverClass.newInstance());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Anima.open("jdbc:oracle:thin:@localhost:1521:EE", "system", "oracle").dialect(new OracleDialect());

        String sql = "select to_clob('test') val from dual";
        String one = Anima.select().bySQL(String.class, sql).one();
        Assert.assertEquals("test", one);
    }

//    @Test
    public void testPage(){
        Page<ZhuanJia> page = Anima.select().from(ZhuanJia.class).page(1, 10);
        System.out.println(page.getRows());
    }

}
