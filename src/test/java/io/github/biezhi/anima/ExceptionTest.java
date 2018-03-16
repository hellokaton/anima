package io.github.biezhi.anima;

import io.github.biezhi.anima.enums.ErrorCode;
import io.github.biezhi.anima.exception.AnimaException;
import io.github.biezhi.anima.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Sql2oException;

import static io.github.biezhi.anima.Anima.select;

/**
 * Exception
 *
 * @author biezhi
 * @date 2018/3/13
 */
public class ExceptionTest extends BaseTest {

    @Test
    public void testFromIsNull() {
        try {
            select().count();
        } catch (AnimaException e) {
            Assert.assertEquals(ErrorCode.FROM_NOT_NULL.getMsg(), e.getMessage());
        }
        try {
            select().one();
        } catch (AnimaException e) {
            Assert.assertEquals(ErrorCode.FROM_NOT_NULL.getMsg(), e.getMessage());
        }
        try {
            select().all();
        } catch (AnimaException e) {
            Assert.assertEquals(ErrorCode.FROM_NOT_NULL.getMsg(), e.getMessage());
        }
    }

    @Test(expected = Sql2oException.class)
    public void test(){
        User user = new User();
        user.save();
    }

}
