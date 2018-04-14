package io.github.biezhi.anima;

import io.github.biezhi.anima.entity.User;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author: Soft
 * @Date: 2018-04-14 1:11
 * @Desc:
 */
public class ErrorTest {

    @Test
    public void query() {
        User user1 = Anima.select().from(User.class).exclude("tel").byId(5);
        System.out.println(user1);
    }

    @Before
    public void before() {
        Anima.open("jdbc:mysql://localhost/wuliu", "root", "123456");
    }
}
