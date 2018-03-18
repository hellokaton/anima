package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import io.github.biezhi.anima.page.Page;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/3/13
 */
public class SelectTest extends BaseTest {

    @Test
    public void testCount() {
        long count = select().from(User.class).count();
        Assert.assertEquals(8, count);
    }

    @Test
    public void testCountBy1() {
        long count = select().from(User.class).where("age > ?", 15).isNotNull("user_name").count();
        Assert.assertEquals(count, 7L);
    }

    @Test
    public void testFindById() {
        User user = select().from(User.class).byId(2);
        Assert.assertNotNull(user);
        Assert.assertEquals(Integer.valueOf(2), user.getId());

        List<User> users = select().from(User.class).byIds(1, 2, 3);
        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());
    }

    @Test
    public void testFindBySQL() {
        String name = select().bySQL(String.class, "select user_name from users limit 1").one();
        Assert.assertNotNull(name);
    }

    @Test
    public void testFindAllBySQL() {
        List<String> names = select().bySQL(String.class, "select user_name from users limit ?", 3).all();
        Assert.assertNotNull(names);
        Assert.assertEquals(3, names.size());
    }

    @Test
    public void testAll() {
        List<User> users = select().from(User.class).all();
        Assert.assertNotNull(users);
    }

    @Test
    public void testLike() {
        List<User> users = select().from(User.class).like("user_name", "%o%").all();
        Assert.assertNotNull(users);
        Assert.assertEquals(3, users.size());
    }

    @Test
    public void testWhere() {
        List<User> users = select().from(User.class).where("age > ?", 15).all();
        Assert.assertNotNull(users);

        users = select().from(User.class).where("user_name is not null").all();
        Assert.assertNotNull(users);
    }

    @Test
    public void testIn() {
        List<User> users = select().from(User.class).in("id", 1, 2, 3).all();
        Assert.assertNotNull(users);
    }

    @Test
    public void testBetween() {
        long count = select().from(User.class).between("age", 10, 25).count();
        Assert.assertEquals(4L, count);
    }

    @Test
    public void testSelectColumn() {
        User user = select("user_name").from(User.class).order("id desc").one();
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getUserName());
        Assert.assertNull(user.getId());
    }

    @Test
    public void testLimit() {
        List<User> users = select().from(User.class).order("id desc").limit(5);
        Assert.assertNotNull(users);
        Assert.assertEquals(5, users.size());
    }

    @Test
    public void testCondition() {
        select().from(User.class).gt("age", 15).like("user_name", "ja%").count();

        select().from(User.class).where("id > ?", 1).not("age", 10).lte("age", 90).count();
    }

    @Test
    public void testExclude() {
        User user = select().from(User.class).exclude("age").one();
        Assert.assertNotNull(user);
        Assert.assertNull(user.getAge());
    }

    @Test
    public void testPage() {
        Page<User> userPage = select().from(User.class).order("id desc").page(1, 3);

        Assert.assertNotNull(userPage);
        Assert.assertEquals(8, userPage.getTotalRows());
        Assert.assertEquals(3, userPage.getTotalPages());
        Assert.assertEquals(3, userPage.getRows().size());
        Assert.assertEquals(1, userPage.getPageNum());
        Assert.assertEquals(1, userPage.getPrevPage());
        Assert.assertEquals(2, userPage.getNextPage());
        Assert.assertTrue(userPage.isHasNextPage());
        Assert.assertFalse(userPage.isHasPrevPage());
    }

    @Test
    public void testStream() {
        List<String> names = select().from(User.class).parallel()
                .filter(u -> u.getAge() > 10)
                .map(User::getUserName)
                .collect(Collectors.toList());

        Assert.assertNotNull(names);
    }

}
