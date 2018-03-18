package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import org.junit.Test;
import org.sql2o.Sql2oException;

/**
 * Exception
 *
 * @author biezhi
 * @date 2018/3/13
 */
public class ExceptionTest extends BaseTest {

    @Test(expected = Sql2oException.class)
    public void test(){
        User user = new User();
        user.save();
    }

}
