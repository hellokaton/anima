package io.github.biezhi.anima;

import io.github.biezhi.anima.core.Anima;
import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author biezhi
 * @date 2018/3/14
 */
public class OtherTest extends BaseTest {

    @Test
    public void testTx1() {
        Anima.atomic(() -> {
            int a = 1 / 0;
            new User("apple", 666).save();
        }).catchException(e -> Assert.assertEquals(ArithmeticException.class, e.getClass()));

        Assert.assertEquals(8, User.count());
    }

    @Test
    public void testTx2() {
        Anima.atomic(() -> new User("google", 666).save());

        Assert.assertEquals(9, User.count());
    }

}
