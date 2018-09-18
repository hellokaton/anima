package io.github.biezhi.anima;

import io.github.biezhi.anima.converter.UserStatusConverter;
import io.github.biezhi.anima.converter.UserModel;
import io.github.biezhi.anima.converter.UserStatus;
import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/9/18
 */
public class AnimaTest {

    @Test
    public void testAddConverter(){
        Sql2o sql2o = Anima.open("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1", "sa", "")
                .addConverter(new UserStatusConverter())
                .getSql2o();

        String sql = "DROP TABLE IF EXISTS `user_models`;\n" +
                "CREATE TABLE `user_models` (" +
                "`uid` IDENTITY PRIMARY KEY, " +
                "`status` int(4)" +
                ");";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        }

        UserModel userModel = new UserModel();
        userModel.setUid(23);
        userModel.setStatus(UserStatus.NOMARL);

        userModel.save();

        userModel = new UserModel();
        userModel.setUid(24);
        userModel.setStatus(UserStatus.DISABLE);

        userModel.save();

        Assert.assertEquals(2, select().from(UserModel.class).count());

        List<UserModel> models = select().from(UserModel.class).all();
        System.out.println(models);

    }

}
