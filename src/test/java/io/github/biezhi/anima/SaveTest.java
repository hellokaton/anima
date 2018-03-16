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
public class SaveTest extends BaseTest {

    @Test
    public void testSave() {
        User user = new User();
        user.setUserName("save1");
        user.setAge(99);
        Integer id = user.save().asInt();
        Assert.assertNotNull(id);
    }

    @Test
    public void testSave2() {
        Integer id = new User("save2", 100).save().asInt();
        Assert.assertNotNull(id);
    }

    @Test
    public void testSave3(){
        Anima.save(new User("save3", 100));
    }

}
