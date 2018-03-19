package io.github.biezhi.anima;

import io.github.biezhi.anima.enums.OrderBy;
import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.biezhi.anima.Anima.delete;
import static io.github.biezhi.anima.Anima.select;
import static io.github.biezhi.anima.Anima.update;

/**
 * Java 8 lambda Test
 *
 * @author biezhi
 * @date 2018/3/13
 */
public class LambdaTest extends BaseTest {

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
                .order(User::getId, OrderBy.DESC)
                .all();

        Assert.assertNotNull(user);
    }

    @Test
    public void testSelect() {
        List<User> users = select(User::getUserName, User::getAge).from(User.class).limit(3);
        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());
        Assert.assertNull(users.get(0).getId());
    }

    @Test
    public void testOrderBy(){
        select().from(User.class).order(User::getId, OrderBy.DESC).order(User::getAge, OrderBy.ASC).all();
    }

    @Test
    public void testDelete() {
        delete().from(User.class).where(User::getId).deleteById(3);
        delete().from(User.class).where(User::getId).eq(1).execute();
        delete().from(User.class).where(User::getAge).lte(20).execute();
    }

    @Test
    public void testUpdate() {
        update().from(User.class).set(User::getUserName, "base64").updateById(3);
        update().from(User.class).set(User::getUserName, "base64").where(User::getId).eq(2).execute();
    }

}
