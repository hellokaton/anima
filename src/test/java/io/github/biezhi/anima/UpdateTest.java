package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Test;

import static io.github.biezhi.anima.core.Anima.update;

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
        System.out.println(result);
    }

}
