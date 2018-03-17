package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import static io.github.biezhi.anima.Anima.update;

/**
 * Save
 *
 * @author biezhi
 * @date 2018/3/14
 */
public class UpdateTest extends BaseTest {

    @Test
    public void testUpdate() {
        String newName = "biezhi_" + System.currentTimeMillis();
        int    result  = update().from(User.class).set("user_name", newName).execute();
        Assert.assertEquals(8, result);

        result = update().from(User.class).set("user_name", newName).where("id", 1).execute();
        Assert.assertEquals(1, result);
    }

    @Test
    public void testUpdate2() {
        User user = new User();
        user.setUserName("jack");
        user.update();

        user = new User();
        user.setUserName("jack");
        user.updateById(1);
    }

    @Test
    public void testUpdate3() {
        new User().set("user_name", "jack").where("id", 2).update();
    }

    @Test
    public void testUpdate4() {
        new User().set("user_name", "jack").updateById(3);
    }

}
