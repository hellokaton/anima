package io.github.biezhi.anima;

import com.zaxxer.hikari.HikariDataSource;
import io.github.biezhi.anima.converter.LevelConverter;
import io.github.biezhi.anima.enums.Gender;
import io.github.biezhi.anima.enums.VipLevel;
import io.github.biezhi.anima.model.Address;
import io.github.biezhi.anima.model.OrderInfo;
import io.github.biezhi.anima.model.Person;
import io.github.biezhi.anima.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static io.github.biezhi.anima.Anima.delete;

/**
 * @author biezhi
 * @date 2018/3/13
 */
@Slf4j
public class BaseTest {

    @BeforeClass
    public static void before() {
        h2();
        initData();
        System.out.println();
        log.info("============== Start Test Code ==============");
        System.out.println();
    }

    protected static void initData() {
        new User("jack", 25).save();
        new User("rose", 23).save();
        new User("tom", 24).save();
        new User("biezhi", 8).save();
        new User("lilei", 19).save();
        new User("john", 38).save();
        new User("king", 32).save();
        new User("王尼玛", 30).save();

        new OrderInfo(1, "橘子").save();
        new OrderInfo(2, "果汁").save();
        new OrderInfo(1, "芒果").save();
        new OrderInfo(1, "葡萄干").save();

        new Address(3L, "上海", "浦东新区").save();
        new Address(2L, "北京", "朝阳区").save();

        new Person("biezhi", Gender.MALE, VipLevel.VIP2).save();
        new Person("rose", Gender.FEMALE, VipLevel.VIP3).save();
        new Person("tom", Gender.MALE, VipLevel.VIP1).save();
    }

    protected static void h2() {

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192");
        ds.setUsername("sa");
        ds.setPassword("");

        Sql2o sql2o = Anima.open(ds).getSql2o();

//        Sql2o sql2o = Anima.open("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192", "sa", "").getSql2o();

        String sql = "DROP TABLE IF EXISTS `users`;\n" +
                "CREATE TABLE `users` (" +
                "`id` IDENTITY PRIMARY KEY, " +
                "`user_name` varchar(50) NOT NULL, " +
                "`age` int(11)," +
                ");" +
                "DROP TABLE IF EXISTS `order_info`;\n" +
                "CREATE TABLE `order_info` (" +
                "`id` IDENTITY PRIMARY KEY," +
                "`uid` int(11) NOT NULL," +
                "`productname` varchar(100) NOT NULL," +
                "`create_time` datetime NOT NULL" +
                ");" +
                "DROP TABLE IF EXISTS `addresses`;\n" +
                "CREATE TABLE `addresses` (" +
                "`order_id` bigint(20) PRIMARY KEY," +
                "`city` varchar(100) NOT NULL," +
                "`street` varchar(200) NOT NULL" +
                ");" +
                "DROP TABLE IF EXISTS `persons`;\n" +
                "CREATE TABLE `persons` (" +
                "`name` varchar(50) PRIMARY KEY," +
                "`sex` varchar(10) NOT NULL," +
                "`vip_level` int(4) NOT NULL" +
                ");";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        }
    }

    protected static void sqlite() {
        Sql2o sql2o = Anima.open("jdbc:sqlite:./demo.db")
                .addConverter(new LevelConverter())
                .getSql2o();

        sql2o.setIsolationLevel(java.sql.Connection.TRANSACTION_SERIALIZABLE);

        try (Connection con = sql2o.open()) {
            con.createQuery("drop table if exists users").executeUpdate();
            con.createQuery("create table users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "user_name varchar(50) NOT NULL, " +
                    "age INTEGER" +
                    ");").executeUpdate();
        }
    }

    protected static void mysql() {
        Anima.open("jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "123456");
    }

    protected static void mysql8() {
        //CREATE TABLE `demo`.`无标题`  (
        //  `id` int(10) NOT NULL AUTO_INCREMENT,
        //  `user_name` varchar(50) NOT NULL,
        //  `age` tinyint(2) NULL DEFAULT NULL,
        //  PRIMARY KEY (`id`) USING BTREE
        //) ENGINE = InnoDB CHARACTER ROW_FORMAT = Dynamic;

//        Anima.open("jdbc:mysql://127.0.0.1:3317/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "123456");

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3317/demo");
        ds.setUsername("root");
        ds.setPassword("123456");
        Anima.open(ds);
        delete().from(User.class).execute();
    }

}
