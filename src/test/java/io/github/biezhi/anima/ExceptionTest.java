package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

/**
 * Exception
 *
 * @author biezhi
 * @date 2018/3/13
 */
@Slf4j
public class ExceptionTest extends BaseTest {


    @BeforeClass
    public static void before() {
        h2();
        initData();
        System.out.println();
        log.info("============== Start Test Code ==============");
        System.out.println();
    }

    protected static void h2() {
        Sql2o sql2o = Anima.open("jdbc:h2:file:~/demo;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192", "sa", "").rollbackException(CustomException.class).getSql2o();

        String sql = "DROP TABLE IF EXISTS `users`;\n" +
                "CREATE TABLE `users` (" +
                "`id` IDENTITY PRIMARY KEY, " +
                "`user_name` varchar(50) NOT NULL, " +
                "`age` int(11)," +
                ");";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        }
    }

    @Test
    public void testCustomRollbackException() {
        boolean isRollback = Anima.atomic(() -> {
            throwCustomException();
            new User("apple", 666).save();
        }).catchException(e -> { }).isRollback();

        Assert.assertEquals(true, isRollback);

        isRollback = Anima.atomic(() -> {
            int a = 1 / 0;
            new User("apple", 666).save();
        }).catchException(e -> {
        }).isRollback();

        Assert.assertEquals(false, isRollback);
    }

    @Test(expected = Sql2oException.class)
    public void test() {
        User user = new User();
        user.save();
    }

    private void throwCustomException() {
        throw new CustomException();
    }

    class CustomException extends RuntimeException {

    }
}
