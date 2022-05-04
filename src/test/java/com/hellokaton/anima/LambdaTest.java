package com.hellokaton.anima;

import com.hellokaton.anima.enums.OrderBy;
import com.hellokaton.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.hellokaton.anima.Anima.select;

/**
 * Java 8 lambda Test
 *
 * @author biezhi
 * @date 2018/3/13
 */
public class LambdaTest extends BaseTest {

    @Test
    public void testOne() {
        User user = Anima.select().from(User.class).where(User::getUserName).eq("jack").one();
        Assert.assertNotNull(user);
    }

    @Test
    public void testAll() {
        List<User> user = Anima.select().from(User.class)
                .where(User::getUserName).notNull()
                .and(User::getAge).gt(10)
                .order(User::getId, OrderBy.DESC)
                .all();

        Assert.assertNotNull(user);
    }

    @Test
    public void testSelect() {
        List<User> users = Anima.select(User::getUserName, User::getAge).from(User.class).limit(3);
        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());
        Assert.assertNull(users.get(0).getId());
    }

    @Test
    public void testOrderBy() {
        Anima.select().from(User.class).order(User::getId, OrderBy.DESC).order(User::getAge, OrderBy.ASC).all();
    }

    @Test
    public void testUpdate() {
        int result = Anima.update().from(User.class).set(User::getUserName, "base64").updateById(2);
        Assert.assertEquals(1, result);
        result = Anima.update().from(User.class).set(User::getUserName, "base64").where(User::getId).eq(2).execute();
        Assert.assertEquals(1, result);
    }

    @Test
    public void testDelete() {
        Anima.delete().from(User.class).where(User::getId).deleteById(3);
        Anima.delete().from(User.class).where(User::getId).eq(1).execute();
        Anima.delete().from(User.class).where(User::getAge).lte(20).execute();
    }

}
