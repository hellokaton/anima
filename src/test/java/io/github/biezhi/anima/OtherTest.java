package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/3/14
 */
public class OtherTest extends BaseTest {

    @Test
    public void testTx1() {
        Anima.atomic(() -> {
            int a = 1 / 0;
            System.out.println(a);
            new User("apple", 666).save();
        }).catchException(e -> {
            e.printStackTrace();
            Assert.assertEquals(ArithmeticException.class, e.getClass());
        });

        Assert.assertEquals(8, select().from(User.class).count());
    }

    @Test
    public void testTx2() {
        Anima.atomic(() -> new User("google", 666).save());
        Assert.assertEquals(9, select().from(User.class).count());
    }

    @Test
    public void testTx3() {
        Anima.atomic(() -> {
            select().from(User.class).count();
            int a = 1 / 0;
            System.out.println(a);
            new User("apple2018", 666).save();
        }).catchException(e -> {
            Assert.assertEquals(ArithmeticException.class, e.getClass());
        });

        Assert.assertEquals(0, select().from(User.class).where(User::getUserName, "apple2018").count());
    }

    @Test
    public void testTx4() {
        Integer res = Anima.atomic(() -> {
            int a = 1 / 0;
            System.out.println(a);
            new User("apple2018", 666).save();
        }).catchAndReturn(e -> {
            Assert.assertEquals(ArithmeticException.class, e.getClass());
            return 0;
        });
        Assert.assertEquals(Integer.valueOf(0), res);
    }

    @Test
    public void testExecute(){
        Anima.execute("drop table if exists hello_world");
        Anima.execute("create table hello_world(int integer not null)");
    }

}
