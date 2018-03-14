package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author biezhi
 * @date 2018/3/13
 */
public class SelectTest extends BaseTest {

    @Test
    public void testCount() {
        System.out.println(User.count());
    }

    @Test
    public void testCountBy1() {
        long count = User.where("age > ?", 15).isNotNull("name").count();
        System.out.println(count);
    }

    @Test
    public void testFindById(){
        User user = User.findById(9);
        Assert.assertNotNull(user);
        Assert.assertEquals(Integer.valueOf(9), user.getId());
    }

    @Test
    public void testAll(){
        List<User> users = User.all();
        Assert.assertNotNull(users);
    }

}
