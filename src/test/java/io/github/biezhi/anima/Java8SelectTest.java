package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/3/13
 */
public class Java8SelectTest extends BaseTest {

    @Test
    public void testOne() {
        User user = select().from(User.class).where(User::getUserName).eq("jack").one();
        Assert.assertNotNull(user);
    }

    @Test
    public void testAll() {
        List<User> user = select().from(User.class)
                .where(User::getUserName).notNull()
                .and(User::getAge).gt(10)
                .all();

        Assert.assertNotNull(user);
    }

}
