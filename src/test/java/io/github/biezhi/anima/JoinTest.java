package io.github.biezhi.anima;

import io.github.biezhi.anima.core.Joins;
import io.github.biezhi.anima.enums.OrderBy;
import io.github.biezhi.anima.model.Address;
import io.github.biezhi.anima.model.OrderInfo;
import io.github.biezhi.anima.model.User;
import io.github.biezhi.anima.model.UserDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.biezhi.anima.Anima.select;

/**
 * Join test
 *
 * @author biezhi
 * @date 2018/4/16
 */
public class JoinTest extends BaseTest {

    @Test
    public void testJoin() {
        // HasOne
        OrderInfo orderInfo = select().from(OrderInfo.class)
                .join(
                        Joins.with(Address.class).as(OrderInfo::getAddress)
                                .on(OrderInfo::getId, Address::getOrderId)
                )
                .byId(3);

        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getAddress());

        orderInfo = select().from(OrderInfo.class)
                .join(
                        Joins.with(Address.class).as(OrderInfo::getAddress)
                                .on(OrderInfo::getId, Address::getOrderId)
                )
                .join(
                        Joins.with(User.class).as(OrderInfo::getUser)
                                .on(OrderInfo::getUid, User::getId)
                )
                .byId(3);

        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getAddress());
        Assert.assertNotNull(orderInfo.getUser());

        // ManyToOne
        orderInfo = select().from(OrderInfo.class)
                .join(
                        Joins.with(User.class).as(OrderInfo::getUser)
                                .on(OrderInfo::getUid, User::getId)
                )
                .byId(3);

        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getUser());
    }

    @Test
    public void testOneToMany(){
        List<UserDto> userDto = select().from(UserDto.class).join(
                Joins.with(OrderInfo.class).as(UserDto::getOrders)
                        .on(UserDto::getId, OrderInfo::getUid)
        ).all();
        Assert.assertNotNull(userDto);
        Assert.assertNotNull(userDto.get(0).getOrders());
    }
    @Test
    public void testOrderBy() {
        // OneToMany
        UserDto userDto = select().from(UserDto.class).join(
                Joins.with(OrderInfo.class).as(UserDto::getOrders)
                        .on(UserDto::getId, OrderInfo::getUid)
                        .order(OrderInfo::getId, OrderBy.DESC)
        ).byId(1);

        userDto = select().from(UserDto.class).join(
                Joins.with(OrderInfo.class).as(UserDto::getOrders)
                        .on(UserDto::getId, OrderInfo::getUid)
                        .order("id asc")
        ).byId(1);

    }
}
