package io.github.biezhi.anima;

import io.github.biezhi.anima.core.Anima;
import io.github.biezhi.anima.model.User;
import org.junit.Before;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

/**
 * @author biezhi
 * @date 2018/3/13
 */
public class BaseTest {

    @Before
    public void before() {
        h2();
        initData();
    }

    public void initData() {
        new User("jack", 25).save();
        new User("rose", 23).save();
        new User("tom", 24).save();
        new User("biezhi", 8).save();
        new User("lilei", 19).save();
        new User("john", 38).save();
        new User("king", 32).save();
        new User("王尼玛", 30).save();
    }

    private void h2() {
        Sql2o sql2o = Anima.open("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192", "sa", "").getCommonSql2o();

        String sql = "DROP TABLE IF EXISTS `users`;\n" +
                "CREATE TABLE `users` (" +
                "`id` IDENTITY PRIMARY KEY, " +
                "`name` varchar(50) NOT NULL, " +
                "`age` int(11)," +
                ");";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        }
    }

    private void mysql() {
        Anima.open("jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "123456");
    }

}
