package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;

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
        User.set("name", newName).where("id = ?", 1).update();
        User user = (User) User.findById(1);
        Assert.assertEquals(newName, user.getName());
    }

}
