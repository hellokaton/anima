package io.github.biezhi.anima.oracle;

import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.dialect.OracleDialect;
import io.github.biezhi.anima.model.ZhuanJia;
import io.github.biezhi.anima.page.Page;
import org.junit.Assert;

import java.sql.Driver;
import java.sql.DriverManager;

import static io.github.biezhi.anima.Anima.select;

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
        String one = select().bySQL(String.class, sql).one();
        Assert.assertEquals("test", one);
    }

//    @Test
    public void testPage(){
        Page<ZhuanJia> page = select().from(ZhuanJia.class).page(1, 10);
        System.out.println(page.getRows());
    }

}
