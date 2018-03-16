package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

import static io.github.biezhi.anima.Anima.delete;

/**
 * Save
 *
 * @author biezhi
 * @date 2018/3/14
 */
public class DeleteTest extends BaseTest {

    @Test
    public void testDelete() {
        int result = delete().from(User.class).where("id", 1).execute();
        Assert.assertEquals(1, result);
    }

    @Test
    public void testDelete2() {
        User user = new User();
        user.setAge(15);
        user.setUserName("jack");
        user.delete();
    }

}
