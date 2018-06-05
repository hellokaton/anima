package io.github.biezhi.anima;

import io.github.biezhi.anima.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Sql2oException;

/**
 * Exception
 *
 * @author biezhi
 * @date 2018/3/13
 */
@Slf4j
public class ExceptionTest extends BaseTest {

    @Test
    public void testCustomRollbackException() {
        boolean isRollback = Anima.atomic(() -> {
            throwCustomException();
            new User("apple", 666).save();
        }).catchException(e -> { }).isRollback();

        Assert.assertEquals(true, isRollback);

        isRollback = Anima.atomic(() -> {
            int a = 1 / 0;
            System.out.println(a);
            new User("apple", 666).save();
        }).catchException(e -> {
        }).isRollback();

        Assert.assertEquals(true, isRollback);
    }

    @Test(expected = Sql2oException.class)
    public void test() {
        User user = new User();
        user.save();
    }

    private void throwCustomException() {
        throw new CustomException();
    }

    static class CustomException extends RuntimeException {

        private static final long serialVersionUID = 6329605066783987521L;

    }
}
