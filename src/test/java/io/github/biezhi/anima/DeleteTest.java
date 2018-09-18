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
        new User(9001, "test", 14).save();
        int result = delete().from(User.class).where("id", 9001).execute();
        Assert.assertEquals(1, result);
    }

    @Test
    public void testDelete2() {
        User user = new User();
        user.setAge(15);
        user.setUserName("jack");
        user.delete();
    }

    @Test
    public void testDelete3() {
        Anima.deleteBatch(User.class, 1, 2, 3);
    }

    @Test
    public void testDelete4(){
        Anima.deleteById(User.class, 5);
    }

    @Test
    public void testDelete5(){
        Anima.execute("delete from users where id = ?", 2);
    }

}
